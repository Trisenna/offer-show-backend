package com.offershow.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.offershow.exception.BusinessException;
import com.offershow.exception.ResourceNotFoundException;
import com.offershow.model.dto.ExportRequestDTO;
import com.offershow.model.dto.OfferDTO;
import com.offershow.model.entity.ExportTask;
import com.offershow.model.entity.Offer;
import com.offershow.model.vo.ExportTaskVO;
import com.offershow.repository.ExportTaskRepository;
import com.offershow.repository.OfferRepository;
import com.offershow.util.ExcelUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.offershow.model.vo.OfferVO;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;


/**
 * 导出任务服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExportTaskServiceImpl implements ExportTaskService {

    private final ExportTaskRepository exportTaskRepository;
    private final OfferRepository offerRepository;
    private final OfferService offerService;
    private final ObjectMapper objectMapper;

    @Value("${app.export.path}")
    private String exportPath;

    @Override
    @Transactional
    public ExportTaskVO createExportTask(ExportRequestDTO exportRequestDTO) {
        try {
            // 创建导出任务记录
            ExportTask exportTask = new ExportTask();
            exportTask.setTaskName("Offer数据导出-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
            exportTask.setTaskParams(objectMapper.writeValueAsString(exportRequestDTO));
            exportTask.setStatus("PENDING");

            // 设置时间
            LocalDateTime now = LocalDateTime.now();
            exportTask.setCreatedAt(now);
            exportTask.setUpdatedAt(now);

            // 保存任务
            exportTaskRepository.insert(exportTask);

            return convertToVO(exportTask);
        } catch (JsonProcessingException e) {
            log.error("Failed to convert export request to JSON", e);
            throw new BusinessException("导出请求参数格式不正确");
        }
    }

    @Override
    public ExportTaskVO getExportTaskStatus(Long taskId) {
        ExportTask exportTask = exportTaskRepository.findById(taskId);
        if (exportTask == null) {
            throw new ResourceNotFoundException("导出任务不存在: " + taskId);
        }

        return convertToVO(exportTask);
    }

    @Override
    @Async
    public void processPendingExportTasks() {
        List<ExportTask> pendingTasks = exportTaskRepository.findPendingTasks();
        log.info("Found {} pending export tasks to process", pendingTasks.size());

        for (ExportTask task : pendingTasks) {
            try {
                // 更新任务状态为处理中
                exportTaskRepository.updateStatus(task.getId(), "PROCESSING");

                // 解析任务参数
                ExportRequestDTO requestDTO = objectMapper.readValue(task.getTaskParams(), ExportRequestDTO.class);

                // 导出数据
                String fileUrl = exportData(task.getId(), requestDTO);

                // 更新任务状态和文件URL
                exportTaskRepository.updateFileUrl(task.getId(), fileUrl);

                log.info("Export task {} completed successfully", task.getId());
            } catch (Exception e) {
                log.error("Failed to process export task: {}", task.getId(), e);
                exportTaskRepository.updateStatus(task.getId(), "FAILED");
            }
        }
    }

    @Override
    @Transactional
    public Map<String, Object> importOffers(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("导入文件不能为空");
        }

        Workbook workbook;
        try {
            workbook = WorkbookFactory.create(file.getInputStream());
        } catch (Exception e) {
            throw new BusinessException("文件格式不支持，请上传Excel文件");
        }

        Sheet sheet = workbook.getSheetAt(0);

        // 校验表头
        Row headerRow = sheet.getRow(0);
        validateHeaders(headerRow);

        List<OfferDTO> offerDTOs = new ArrayList<>();
        List<Map<String, Object>> errors = new ArrayList<>();

        int totalRows = sheet.getPhysicalNumberOfRows();
        for (int i = 1; i < totalRows; i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;

            try {
                OfferDTO offerDTO = parseOfferFromRow(row);
                offerDTOs.add(offerDTO);
            } catch (Exception e) {
                Map<String, Object> error = new HashMap<>();
                error.put("rowNumber", i + 1);
                error.put("message", e.getMessage());
                errors.add(error);
            }
        }

        // 批量创建Offer
        List<OfferDTO> validOffers = offerDTOs.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        List<OfferVO> createdOffers = offerService.batchCreateOffers(validOffers);

        // 构建返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("totalRecords", totalRows - 1);
        result.put("successCount", createdOffers.size());
        result.put("failedCount", errors.size());
        result.put("errors", errors);

        return result;
    }

    /**
     * 导出数据
     *
     * @param taskId     任务ID
     * @param requestDTO 导出请求
     * @return 文件URL
     */
    private String exportData(Long taskId, ExportRequestDTO requestDTO) throws IOException {
        // 确保导出目录存在
        Path exportDir = Paths.get(exportPath);
        if (!Files.exists(exportDir)) {
            Files.createDirectories(exportDir);
        }

        // 准备文件名
        String fileName = StringUtils.isNotBlank(requestDTO.getFileName())
                ? requestDTO.getFileName()
                : "offer_export_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "_" + taskId;

        // 查询数据
        List<Offer> offers = queryOffersForExport(requestDTO);

        // 导出路径
        String format = StringUtils.defaultIfBlank(requestDTO.getFormat(), "csv").toLowerCase();
        String filePath = exportPath + File.separator + fileName + "." + format;

        // 根据格式导出
        if ("csv".equals(format)) {
            exportToCsv(offers, filePath);
        } else if ("excel".equals(format) || "xlsx".equals(format)) {
            exportToExcel(offers, filePath);
        } else {
            throw new BusinessException("不支持的导出格式: " + format);
        }

        // 返回相对路径
        return "/exports/" + fileName + "." + format;
    }

    /**
     * 查询要导出的Offer数据
     *
     * @param requestDTO 导出请求
     * @return Offer列表
     */
    private List<Offer> queryOffersForExport(ExportRequestDTO requestDTO) {
        ExportRequestDTO.ExportFilters filters = requestDTO.getFilters();
        if (filters == null) {
            return offerRepository.findByDateRange(null, null);
        }

        // TODO: 实现更复杂的过滤条件查询
        return offerRepository.findByDateRange(filters.getStartDate(), filters.getEndDate());
    }

    /**
     * 导出为CSV
     *
     * @param offers   Offer列表
     * @param filePath 文件路径
     */
    private void exportToCsv(List<Offer> offers, String filePath) throws IOException {
        // 构建CSV头
        StringBuilder csv = new StringBuilder();
        csv.append("ID,公司名称,职位名称,工作城市,工作年限,薪资结构,面试难度,是否接受,创建时间\n");

        // 添加数据行
        for (Offer offer : offers) {
            csv.append(offer.getId()).append(",");
            csv.append(escapeCSV(offer.getCompanyName())).append(",");
            csv.append(escapeCSV(offer.getPosition())).append(",");
            csv.append(escapeCSV(offer.getCity())).append(",");
            csv.append(offer.getWorkYears()).append(",");
            csv.append(escapeCSV(offer.getSalaryStructure())).append(",");
            csv.append(offer.getInterviewDifficulty() == null ? "" : offer.getInterviewDifficulty()).append(",");
            csv.append(offer.getIsAccepted() == null ? "" : offer.getIsAccepted()).append(",");
            csv.append(offer.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\n");
        }

        // 写入文件
        Files.write(Paths.get(filePath), csv.toString().getBytes());
    }

    /**
     * 导出为Excel
     *
     * @param offers   Offer列表
     * @param filePath 文件路径
     */
    private void exportToExcel(List<Offer> offers, String filePath) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Offer数据");

            // 创建头部样式
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            // 创建头部行
            Row headerRow = sheet.createRow(0);
            String[] headers = {"ID", "公司名称", "职位名称", "工作城市", "工作年限", "薪资结构", "面试难度", "是否接受", "创建时间"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
                sheet.setColumnWidth(i, 256 * 20); // 设置列宽
            }

            // 填充数据
            int rowNum = 1;
            for (Offer offer : offers) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(offer.getId());
                row.createCell(1).setCellValue(offer.getCompanyName());
                row.createCell(2).setCellValue(offer.getPosition());
                row.createCell(3).setCellValue(offer.getCity());
                row.createCell(4).setCellValue(offer.getWorkYears());
                row.createCell(5).setCellValue(offer.getSalaryStructure());

                if (offer.getInterviewDifficulty() != null) {
                    row.createCell(6).setCellValue(offer.getInterviewDifficulty());
                } else {
                    row.createCell(6).setCellValue("");
                }

                if (offer.getIsAccepted() != null) {
                    row.createCell(7).setCellValue(offer.getIsAccepted() ? "是" : "否");
                } else {
                    row.createCell(7).setCellValue("");
                }

                row.createCell(8).setCellValue(offer.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            }

            // 写入文件
            try (FileOutputStream outputStream = new FileOutputStream(filePath)) {
                workbook.write(outputStream);
            }
        }
    }

    /**
     * 校验导入文件表头
     *
     * @param headerRow 表头行
     */
    private void validateHeaders(Row headerRow) {
        if (headerRow == null) {
            throw new BusinessException("导入文件格式不正确：缺少表头");
        }

        List<String> requiredHeaders = Arrays.asList("公司名称", "职位名称", "工作城市", "工作年限", "薪资结构");
        List<String> actualHeaders = new ArrayList<>();

        for (int i = 0; i < headerRow.getPhysicalNumberOfCells(); i++) {
            Cell cell = headerRow.getCell(i);
            if (cell != null) {
                actualHeaders.add(cell.getStringCellValue());
            }
        }

        for (String required : requiredHeaders) {
            if (!actualHeaders.contains(required)) {
                throw new BusinessException("导入文件格式不正确：缺少必要字段 " + required);
            }
        }
    }

    /**
     * 从Excel行解析Offer数据
     *
     * @param row Excel行
     * @return OfferDTO
     */
    private OfferDTO parseOfferFromRow(Row row) {
        if (row == null) {
            throw new BusinessException("空行");
        }

        OfferDTO offerDTO = new OfferDTO();

        // 公司名称（必填）
        Cell companyCell = row.getCell(0);
        if (companyCell == null || StringUtils.isBlank(ExcelUtils.getCellValueAsString(companyCell))) {
            throw new BusinessException("公司名称不能为空");
        }
        offerDTO.setCompanyName(ExcelUtils.getCellValueAsString(companyCell));

        // 职位名称（必填）
        Cell positionCell = row.getCell(1);
        if (positionCell == null || StringUtils.isBlank(ExcelUtils.getCellValueAsString(positionCell))) {
            throw new BusinessException("职位名称不能为空");
        }
        offerDTO.setPosition(ExcelUtils.getCellValueAsString(positionCell));

        // 工作城市（必填）
        Cell cityCell = row.getCell(2);
        if (cityCell == null || StringUtils.isBlank(ExcelUtils.getCellValueAsString(cityCell))) {
            throw new BusinessException("工作城市不能为空");
        }
        offerDTO.setCity(ExcelUtils.getCellValueAsString(cityCell));

        // 工作年限（必填）
        Cell workYearsCell = row.getCell(3);
        if (workYearsCell == null) {
            throw new BusinessException("工作年限不能为空");
        }
        try {
            offerDTO.setWorkYears((int) workYearsCell.getNumericCellValue());
        } catch (Exception e) {
            throw new BusinessException("工作年限必须是数字");
        }

        // 薪资结构（必填）
        Cell salaryCell = row.getCell(4);
        if (salaryCell == null || StringUtils.isBlank(ExcelUtils.getCellValueAsString(salaryCell))) {
            throw new BusinessException("薪资结构不能为空");
        }
        try {
            String salaryStr = ExcelUtils.getCellValueAsString(salaryCell);
            Map<String, Object> salaryMap = objectMapper.readValue(salaryStr, Map.class);
            offerDTO.setSalaryStructure(salaryMap);
        } catch (Exception e) {
            throw new BusinessException("薪资结构格式不正确，应为JSON格式");
        }

        // 工作描述（选填）
        Cell descCell = row.getCell(5);
        if (descCell != null) {
            offerDTO.setJobDescription(ExcelUtils.getCellValueAsString(descCell));
        }

        // 面试流程（选填）
        Cell processCell = row.getCell(6);
        if (processCell != null) {
            offerDTO.setInterviewProcess(ExcelUtils.getCellValueAsString(processCell));
        }

        // 面试难度（选填）
        Cell difficultyCell = row.getCell(7);
        if (difficultyCell != null) {
            try {
                offerDTO.setInterviewDifficulty((int) difficultyCell.getNumericCellValue());
            } catch (Exception e) {
                // 忽略无效值
            }
        }

        // 是否接受（选填）
        Cell acceptedCell = row.getCell(8);
        if (acceptedCell != null) {
            String value = ExcelUtils.getCellValueAsString(acceptedCell).toLowerCase();
            offerDTO.setIsAccepted("是".equals(value) || "true".equals(value) || "1".equals(value));
        }

        // 拒绝原因（选填）
        Cell rejectCell = row.getCell(9);
        if (rejectCell != null) {
            offerDTO.setRejectReason(ExcelUtils.getCellValueAsString(rejectCell));
        }

        return offerDTO;
    }

    /**
     * 转义CSV字段
     *
     * @param value 字段值
     * @return 转义后的值
     */
    private String escapeCSV(String value) {
        if (value == null) {
            return "";
        }

        // 如果包含逗号、双引号或换行符，则需要用双引号包裹
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            // 将字段中的双引号替换为两个双引号
            value = value.replace("\"", "\"\"");
            // 用双引号包裹
            return "\"" + value + "\"";
        }

        return value;
    }

    /**
     * 将实体对象转换为视图对象
     *
     * @param exportTask 导出任务实体
     * @return 导出任务视图对象
     */
    private ExportTaskVO convertToVO(ExportTask exportTask) {
        return ExportTaskVO.builder()
                .taskId(exportTask.getId())
                .status(exportTask.getStatus())
                .fileUrl(exportTask.getFileUrl())
                .createdAt(exportTask.getCreatedAt())
                .completedAt(exportTask.getCompletedAt())
                .build();
    }
}