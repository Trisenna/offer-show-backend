package com.offershow.task;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.offershow.model.entity.Offer;
import com.offershow.model.entity.Statistics;
import com.offershow.repository.OfferRepository;
import com.offershow.repository.StatisticsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 统计任务调度器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StatisticsTaskScheduler {

    private final OfferRepository offerRepository;
    private final StatisticsRepository statisticsRepository;
    private final ObjectMapper objectMapper;

    /**
     * 每日薪资统计任务
     * 每天凌晨1:00执行
     */
    @Scheduled(cron = "${app.task.statistics.cron.daily}")
    @Transactional
    public void dailySalaryStatistics() {
        log.info("Starting daily salary statistics task...");

        LocalDate yesterday = LocalDate.now().minusDays(1);
        String dateStr = yesterday.format(DateTimeFormatter.ISO_DATE);

        // 检查是否已经生成过统计数据
        int count = statisticsRepository.checkStatisticsExists("SALARY", "COMPANY", dateStr);
        if (count > 0) {
            log.info("Daily salary statistics for {} already exists, skipping...", dateStr);
            return;
        }

        // 查询昨天的所有Offer数据
        String startDate = dateStr + " 00:00:00";
        String endDate = dateStr + " 23:59:59";
        List<Offer> offers = offerRepository.findByDateRange(startDate, endDate);

        if (offers.isEmpty()) {
            log.info("No offers found for {}, skipping statistics generation", dateStr);
            return;
        }

        // 按公司分组统计
        generateCompanySalaryStatistics(offers, yesterday);

        // 按职位分组统计
        generatePositionSalaryStatistics(offers, yesterday);

        // 按城市分组统计
        generateCitySalaryStatistics(offers, yesterday);

        log.info("Daily salary statistics task completed for {}", dateStr);
    }

    /**
     * 每周趋势统计任务
     * 每周一凌晨2:00执行
     */
    @Scheduled(cron = "${app.task.statistics.cron.weekly}")
    @Transactional
    public void weeklyTrendStatistics() {
        log.info("Starting weekly trend statistics task...");

        LocalDate lastWeekStart = LocalDate.now().minusDays(7);
        LocalDate lastWeekEnd = LocalDate.now().minusDays(1);

        String startDate = lastWeekStart.format(DateTimeFormatter.ISO_DATE) + " 00:00:00";
        String endDate = lastWeekEnd.format(DateTimeFormatter.ISO_DATE) + " 23:59:59";

        // 检查是否已经生成过统计数据
        int count = statisticsRepository.checkStatisticsExists("TREND", "WEEKLY", lastWeekEnd.format(DateTimeFormatter.ISO_DATE));
        if (count > 0) {
            log.info("Weekly trend statistics for week ending {} already exists, skipping...", lastWeekEnd);
            return;
        }

        // 查询上周的所有Offer数据
        List<Offer> offers = offerRepository.findByDateRange(startDate, endDate);

        if (offers.isEmpty()) {
            log.info("No offers found for week ending {}, skipping statistics generation", lastWeekEnd);
            return;
        }

        // 生成周度趋势统计
        generateWeeklyTrendStatistics(offers, lastWeekEnd);

        log.info("Weekly trend statistics task completed for week ending {}", lastWeekEnd);
    }

    /**
     * 每月报表生成任务
     * 每月1日凌晨3:00执行
     */
    @Scheduled(cron = "${app.task.statistics.cron.monthly}")
    @Transactional
    public void monthlyReportStatistics() {
        log.info("Starting monthly report statistics task...");

        // 获取上个月的日期范围
        LocalDate now = LocalDate.now();
        LocalDate lastMonthEnd = now.minusDays(now.getDayOfMonth());
        LocalDate lastMonthStart = lastMonthEnd.withDayOfMonth(1);

        String startDate = lastMonthStart.format(DateTimeFormatter.ISO_DATE) + " 00:00:00";
        String endDate = lastMonthEnd.format(DateTimeFormatter.ISO_DATE) + " 23:59:59";

        // 检查是否已经生成过统计数据
        int count = statisticsRepository.checkStatisticsExists("TREND", "MONTHLY", lastMonthEnd.format(DateTimeFormatter.ISO_DATE));
        if (count > 0) {
            log.info("Monthly report statistics for month ending {} already exists, skipping...", lastMonthEnd);
            return;
        }

        // 查询上月的所有Offer数据
        List<Offer> offers = offerRepository.findByDateRange(startDate, endDate);

        if (offers.isEmpty()) {
            log.info("No offers found for month ending {}, skipping statistics generation", lastMonthEnd);
            return;
        }

        // 生成月度趋势统计
        generateMonthlyTrendStatistics(offers, lastMonthEnd);

        // 生成公司维度的月度统计
        generateCompanyMonthlyStatistics(offers, lastMonthEnd);

        log.info("Monthly report statistics task completed for month ending {}", lastMonthEnd);
    }

    /**
     * 清理过期统计数据
     * 每月15日凌晨4:00执行
     */
    @Scheduled(cron = "0 0 4 15 * ?")
    @Transactional
    public void cleanupOldStatistics() {
        log.info("Starting statistics cleanup task...");

        // 删除一年前的统计数据
        LocalDate oneYearAgo = LocalDate.now().minusYears(1);
        String dateStr = oneYearAgo.format(DateTimeFormatter.ISO_DATE);

        int deletedCount = statisticsRepository.deleteStatisticsBefore(dateStr);
        log.info("Deleted {} old statistics records before {}", deletedCount, dateStr);
    }

    /**
     * 生成公司维度的薪资统计
     *
     * @param offers       Offer列表
     * @param statisticDate 统计日期
     */
    private void generateCompanySalaryStatistics(List<Offer> offers, LocalDate statisticDate) {
        // 按公司分组
        Map<String, List<Offer>> offersByCompany = offers.stream()
                .collect(Collectors.groupingBy(Offer::getCompanyName));

        List<Statistics> statisticsList = new ArrayList<>();

        for (Map.Entry<String, List<Offer>> entry : offersByCompany.entrySet()) {
            String company = entry.getKey();
            List<Offer> companyOffers = entry.getValue();

            // 计算平均薪资
            BigDecimal avgSalary = calculateAverageSalary(companyOffers);

            // 创建统计记录
            Statistics statistics = Statistics.builder()
                    .statisticType("SALARY")
                    .dimension("COMPANY")
                    .dimensionValue(company)
                    .statisticValue(avgSalary)
                    .count(companyOffers.size())
                    .statisticDate(statisticDate)
                    .createdAt(LocalDateTime.now())
                    .build();

            statisticsList.add(statistics);
        }

        if (!statisticsList.isEmpty()) {
            statisticsRepository.batchInsert(statisticsList);
        }
    }

    /**
     * 生成职位维度的薪资统计
     *
     * @param offers       Offer列表
     * @param statisticDate 统计日期
     */
    private void generatePositionSalaryStatistics(List<Offer> offers, LocalDate statisticDate) {
        // 按职位分组
        Map<String, List<Offer>> offersByPosition = offers.stream()
                .collect(Collectors.groupingBy(Offer::getPosition));

        List<Statistics> statisticsList = new ArrayList<>();

        for (Map.Entry<String, List<Offer>> entry : offersByPosition.entrySet()) {
            String position = entry.getKey();
            List<Offer> positionOffers = entry.getValue();

            // 计算平均薪资
            BigDecimal avgSalary = calculateAverageSalary(positionOffers);

            // 创建统计记录
            Statistics statistics = Statistics.builder()
                    .statisticType("SALARY")
                    .dimension("POSITION")
                    .dimensionValue(position)
                    .statisticValue(avgSalary)
                    .count(positionOffers.size())
                    .statisticDate(statisticDate)
                    .createdAt(LocalDateTime.now())
                    .build();

            statisticsList.add(statistics);
        }

        if (!statisticsList.isEmpty()) {
            statisticsRepository.batchInsert(statisticsList);
        }
    }

    /**
     * 生成城市维度的薪资统计
     *
     * @param offers       Offer列表
     * @param statisticDate 统计日期
     */
    private void generateCitySalaryStatistics(List<Offer> offers, LocalDate statisticDate) {
        // 按城市分组
        Map<String, List<Offer>> offersByCity = offers.stream()
                .collect(Collectors.groupingBy(Offer::getCity));

        List<Statistics> statisticsList = new ArrayList<>();

        for (Map.Entry<String, List<Offer>> entry : offersByCity.entrySet()) {
            String city = entry.getKey();
            List<Offer> cityOffers = entry.getValue();

            // 计算平均薪资
            BigDecimal avgSalary = calculateAverageSalary(cityOffers);

            // 创建统计记录
            Statistics statistics = Statistics.builder()
                    .statisticType("SALARY")
                    .dimension("CITY")
                    .dimensionValue(city)
                    .statisticValue(avgSalary)
                    .count(cityOffers.size())
                    .statisticDate(statisticDate)
                    .createdAt(LocalDateTime.now())
                    .build();

            statisticsList.add(statistics);
        }

        if (!statisticsList.isEmpty()) {
            statisticsRepository.batchInsert(statisticsList);
        }
    }

    /**
     * 生成周度趋势统计
     *
     * @param offers       Offer列表
     * @param statisticDate 统计日期
     */
    private void generateWeeklyTrendStatistics(List<Offer> offers, LocalDate statisticDate) {
        // 计算总平均薪资
        BigDecimal avgSalary = calculateAverageSalary(offers);

        // 创建统计记录
        Statistics statistics = Statistics.builder()
                .statisticType("TREND")
                .dimension("WEEKLY")
                .dimensionValue("ALL")
                .statisticValue(avgSalary)
                .count(offers.size())
                .statisticDate(statisticDate)
                .createdAt(LocalDateTime.now())
                .build();

        statisticsRepository.insert(statistics);

        // 按公司分组生成周度趋势
        Map<String, List<Offer>> offersByCompany = offers.stream()
                .collect(Collectors.groupingBy(Offer::getCompanyName));

        List<Statistics> companyStatistics = new ArrayList<>();

        for (Map.Entry<String, List<Offer>> entry : offersByCompany.entrySet()) {
            String company = entry.getKey();
            List<Offer> companyOffers = entry.getValue();

            if (companyOffers.size() >= 3) { // 只统计样本数大于等于3的公司
                BigDecimal companyAvgSalary = calculateAverageSalary(companyOffers);

                Statistics companyStats = Statistics.builder()
                        .statisticType("TREND")
                        .dimension("WEEKLY_COMPANY")
                        .dimensionValue(company)
                        .statisticValue(companyAvgSalary)
                        .count(companyOffers.size())
                        .statisticDate(statisticDate)
                        .createdAt(LocalDateTime.now())
                        .build();

                companyStatistics.add(companyStats);
            }
        }

        if (!companyStatistics.isEmpty()) {
            statisticsRepository.batchInsert(companyStatistics);
        }
    }

    /**
     * 生成月度趋势统计
     *
     * @param offers       Offer列表
     * @param statisticDate 统计日期
     */
    private void generateMonthlyTrendStatistics(List<Offer> offers, LocalDate statisticDate) {
        // 计算总平均薪资
        BigDecimal avgSalary = calculateAverageSalary(offers);

        // 创建统计记录
        Statistics statistics = Statistics.builder()
                .statisticType("TREND")
                .dimension("MONTHLY")
                .dimensionValue("ALL")
                .statisticValue(avgSalary)
                .count(offers.size())
                .statisticDate(statisticDate)
                .createdAt(LocalDateTime.now())
                .build();

        statisticsRepository.insert(statistics);
    }

    /**
     * 生成公司维度的月度统计
     *
     * @param offers       Offer列表
     * @param statisticDate 统计日期
     */
    private void generateCompanyMonthlyStatistics(List<Offer> offers, LocalDate statisticDate) {
        // 按公司分组
        Map<String, List<Offer>> offersByCompany = offers.stream()
                .collect(Collectors.groupingBy(Offer::getCompanyName));

        List<Statistics> statisticsList = new ArrayList<>();

        for (Map.Entry<String, List<Offer>> entry : offersByCompany.entrySet()) {
            String company = entry.getKey();
            List<Offer> companyOffers = entry.getValue();

            if (companyOffers.size() >= 5) { // 只统计样本数大于等于5的公司
                BigDecimal avgSalary = calculateAverageSalary(companyOffers);

                Statistics statistics = Statistics.builder()
                        .statisticType("TREND")
                        .dimension("MONTHLY_COMPANY")
                        .dimensionValue(company)
                        .statisticValue(avgSalary)
                        .count(companyOffers.size())
                        .statisticDate(statisticDate)
                        .createdAt(LocalDateTime.now())
                        .build();

                statisticsList.add(statistics);
            }
        }

        if (!statisticsList.isEmpty()) {
            statisticsRepository.batchInsert(statisticsList);
        }
    }

    /**
     * 计算平均薪资
     *
     * @param offers Offer列表
     * @return 平均薪资
     */
    private BigDecimal calculateAverageSalary(List<Offer> offers) {
        if (offers == null || offers.isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal totalSalary = BigDecimal.ZERO;
        int validCount = 0;

        for (Offer offer : offers) {
            try {
                // 解析薪资结构JSON
                Map<String, Object> salaryMap = objectMapper.readValue(offer.getSalaryStructure(), Map.class);

                // 提取基本工资、奖金、股票等信息
                BigDecimal baseSalary = getBigDecimalValue(salaryMap.get("base"));
                BigDecimal bonus = getBigDecimalValue(salaryMap.get("bonus"));
                BigDecimal stock = getBigDecimalValue(salaryMap.get("stock"));

                // 计算总薪资
                BigDecimal totalOfferSalary = baseSalary.add(bonus).add(stock);
                if (totalOfferSalary.compareTo(BigDecimal.ZERO) > 0) {
                    totalSalary = totalSalary.add(totalOfferSalary);
                    validCount++;
                }
            } catch (JsonProcessingException e) {
                log.error("Failed to parse salary structure: {}", offer.getSalaryStructure(), e);
            }
        }

        if (validCount > 0) {
            return totalSalary.divide(BigDecimal.valueOf(validCount), 2, RoundingMode.HALF_UP);
        } else {
            return BigDecimal.ZERO;
        }
    }

    /**
     * 从对象中获取BigDecimal值
     *
     * @param value 对象值
     * @return BigDecimal值
     */
    private BigDecimal getBigDecimalValue(Object value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }

        if (value instanceof Number) {
            return new BigDecimal(value.toString());
        } else if (value instanceof String) {
            try {
                return new BigDecimal((String) value);
            } catch (NumberFormatException e) {
                return BigDecimal.ZERO;
            }
        }

        return BigDecimal.ZERO;
    }
}