package com.offershow.test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT,
        properties = {
                "spring.datasource.url=jdbc:mysql://localhost:3306/test_offer_show?useSSL=false&serverTimezone=UTC",
                "spring.datasource.driverClassName=com.mysql.cj.jdbc.Driver",
                "spring.datasource.username=root",  // 这里替换为你的数据库用户名
                "spring.datasource.password=qa091923",  // 这里替换为你的数据库密码
                "spring.jpa.database-platform=org.hibernate.dialect.MySQL8Dialect",
                "spring.jpa.hibernate.ddl-auto=create-drop"  // 根据需求，你可以设置为 update 或 create-drop
        }
)

@ActiveProfiles("test")
public class OfferShowAPITest {

    private static final String BASE_URL = "http://localhost:8080/api";
    private final TestRestTemplate restTemplate = new TestRestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private Long offerId;
    private List<Long> batchIds = new ArrayList<>();
    private Long exportTaskId;

    private int totalTests = 0;
    private int passedTests = 0;

    @BeforeEach
    void setUp() {
        System.out.println("========================================");
        System.out.println("    Offer Show API 接口测试 开始        ");
        System.out.println("========================================");
        System.out.println();
    }

    @AfterEach
    void tearDown() {
        System.out.println("========================================");
        System.out.println("测试结果统计:");
        System.out.println("总测试数: " + totalTests);
        System.out.println("通过测试: " + passedTests);
        System.out.println("失败测试: " + (totalTests - passedTests));
        // 防止除以零错误
        int passRate = totalTests > 0 ? (passedTests * 100 / totalTests) : 0;
        System.out.println("通过率: " + passRate + "%");
        System.out.println("========================================");
    }

    private void printResult(boolean isPassed, String description, String response) {
        totalTests++;
        if (isPassed) {
            passedTests++;
            System.out.println("[PASS] " + description);
            // 添加显示成功的API返回内容
            System.out.println("Response: " + formatJson(response));
        } else {
            System.out.println("[FAIL] " + description);
            System.out.println("Response: " + response);
        }
        System.out.println();
    }

    // 添加JSON格式化方法
    private String formatJson(String jsonString) {
        try {
            if (jsonString == null || jsonString.isEmpty()) {
                return "Empty response";
            }

            JsonNode jsonNode = objectMapper.readTree(jsonString);
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonNode);
        } catch (Exception e) {
            return "Invalid JSON: " + jsonString;
        }
    }

    private void checkStatus(int status, int expected, String description, String response) {
        boolean isPassed = status == expected;
        printResult(isPassed, description, response);

        // 添加详细的状态码信息
        if (!isPassed) {
            System.out.println("Expected status: " + expected + ", Actual status: " + status);

            // 尝试解析错误原因
            if (response != null && response.contains("Exception")) {
                try {
                    JsonNode errorNode = objectMapper.readTree(response);
                    if (errorNode.has("message")) {
                        System.out.println("错误详情: " + errorNode.get("message").asText());
                    }
                } catch (Exception e) {
                    System.out.println("无法解析错误详情: " + e.getMessage());
                }
            }
        }
    }

    @Test
    @DisplayName("1. 测试创建 Offer")
    void testCreateOffer() throws Exception {
        System.out.println("测试 1: 创建 Offer");
        System.out.println("请求URL: " + BASE_URL + "/v1/offers");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("companyName", "字节跳动");
        requestBody.put("position", "后端工程师");
        requestBody.put("city", "北京");

        ObjectNode salaryStructure = requestBody.putObject("salaryStructure");
        salaryStructure.put("base", 30000);
        salaryStructure.put("bonus", 150000);
        salaryStructure.put("stock", 100000);

        requestBody.put("workYears", 3);
        requestBody.put("jobDescription", "负责服务端架构设计和开发");
        requestBody.put("interviewProcess", "三轮技术面 + HR面");
        requestBody.put("interviewDifficulty", 4);
        requestBody.put("isAccepted", true);

        // 打印请求体
        System.out.println("请求参数: " + formatJson(requestBody.toString()));

        HttpEntity<String> entity = new HttpEntity<>(requestBody.toString(), headers);
        ResponseEntity<String> response = restTemplate.exchange(
                BASE_URL + "/v1/offers",
                HttpMethod.POST,
                entity,
                String.class);

        String responseBody = response.getBody();
        int status = response.getStatusCodeValue();

        // 打印响应头信息
        System.out.println("响应状态: " + status);
        System.out.println("响应头: " + response.getHeaders());

        if (status == 200 && responseBody != null) {
            JsonNode jsonNode = objectMapper.readTree(responseBody);
            if (jsonNode.has("id")) {
                offerId = jsonNode.get("id").asLong();
                System.out.println("成功创建Offer，ID: " + offerId);
            }
        }

        checkStatus(status, 200, "创建 Offer", responseBody);
    }

    @Test
    @DisplayName("2. 测试获取 Offer 详情")
    void testGetOfferDetail() throws Exception {
        System.out.println("测试 2: 获取 Offer 详情");

        // 首先创建offer以确保有数据
        testCreateOffer();

        if (offerId != null) {
            String url = BASE_URL + "/v1/offers/" + offerId;
            System.out.println("请求URL: " + url);

            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

            String responseBody = response.getBody();
            int status = response.getStatusCodeValue();

            System.out.println("响应状态: " + status);
            System.out.println("响应头: " + response.getHeaders());

            checkStatus(status, 200, "获取 Offer 详情", responseBody);
        } else {
            System.out.println("[SKIP] 跳过获取 Offer 详情测试，因为创建 Offer 失败");
        }
    }

    @Test
    @DisplayName("3. 测试更新 Offer")
    void testUpdateOffer() throws Exception {
        System.out.println("测试 3: 更新 Offer");

        // 首先创建offer以确保有数据
        testCreateOffer();

        if (offerId != null) {
            String url = BASE_URL + "/v1/offers/" + offerId;
            System.out.println("请求URL: " + url);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.put("companyName", "字节跳动");
            requestBody.put("position", "高级后端工程师");
            requestBody.put("city", "北京");

            ObjectNode salaryStructure = requestBody.putObject("salaryStructure");
            salaryStructure.put("base", 35000);
            salaryStructure.put("bonus", 200000);
            salaryStructure.put("stock", 150000);

            requestBody.put("workYears", 5);
            requestBody.put("jobDescription", "负责核心服务端架构设计和开发");
            requestBody.put("interviewProcess", "四轮技术面 + HR面");
            requestBody.put("interviewDifficulty", 5);
            requestBody.put("isAccepted", true);

            // 打印请求体
            System.out.println("请求参数: " + formatJson(requestBody.toString()));

            HttpEntity<String> entity = new HttpEntity<>(requestBody.toString(), headers);
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.PUT,
                    entity,
                    String.class);

            String responseBody = response.getBody();
            int status = response.getStatusCodeValue();

            System.out.println("响应状态: " + status);
            System.out.println("响应头: " + response.getHeaders());

            checkStatus(status, 200, "更新 Offer", responseBody);
        } else {
            System.out.println("[SKIP] 跳过更新 Offer 测试，因为创建 Offer 失败");
        }
    }

    @Test
    @DisplayName("4. 测试部分更新 Offer")
    void testPartialUpdateOffer() throws Exception {
        System.out.println("测试 4: 部分更新 Offer");

        // 首先创建offer以确保有数据
        testCreateOffer();

        if (offerId != null) {
            String url = BASE_URL + "/v1/offers/" + offerId;
            System.out.println("请求URL: " + url);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.put("isAccepted", false);
            requestBody.put("rejectReason", "薪资低于期望");

            // 打印请求体
            System.out.println("请求参数: " + formatJson(requestBody.toString()));

            HttpEntity<String> entity = new HttpEntity<>(requestBody.toString(), headers);
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.PATCH,
                    entity,
                    String.class);

            String responseBody = response.getBody();
            int status = response.getStatusCodeValue();

            System.out.println("响应状态: " + status);
            System.out.println("响应头: " + response.getHeaders());

            checkStatus(status, 200, "部分更新 Offer", responseBody);
        } else {
            System.out.println("[SKIP] 跳过部分更新 Offer 测试，因为创建 Offer 失败");
        }
    }

    @Test
    @DisplayName("5. 测试搜索 Offer")
    void testSearchOffer() {
        System.out.println("测试 5: 搜索 Offer");

        String url = BASE_URL + "/v1/offers/search?keyword=字节跳动&page=1&size=10";
        System.out.println("请求URL: " + url);

        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        String responseBody = response.getBody();
        int status = response.getStatusCodeValue();

        System.out.println("响应状态: " + status);
        System.out.println("响应头: " + response.getHeaders());

        checkStatus(status, 200, "搜索 Offer", responseBody);
    }

    @Test
    @DisplayName("6. 测试批量创建 Offer")
    void testBatchCreateOffer() throws Exception {
        System.out.println("测试 6: 批量创建 Offer");

        String url = BASE_URL + "/v1/offers/batch";
        System.out.println("请求URL: " + url);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ObjectNode requestBody = objectMapper.createObjectNode();
        ArrayNode offersArray = requestBody.putArray("offers");

        // 第一个offer
        ObjectNode offer1 = offersArray.addObject();
        offer1.put("companyName", "阿里巴巴");
        offer1.put("position", "算法工程师");
        offer1.put("city", "杭州");

        ObjectNode salary1 = offer1.putObject("salaryStructure");
        salary1.put("base", 30000);
        salary1.put("bonus", 100000);
        salary1.put("stock", 200000);

        offer1.put("workYears", 3);
        offer1.put("jobDescription", "负责推荐算法研发");
        offer1.put("interviewProcess", "四轮算法面 + HR面");
        offer1.put("interviewDifficulty", 5);
        offer1.put("isAccepted", true);

        // 第二个offer
        ObjectNode offer2 = offersArray.addObject();
        offer2.put("companyName", "腾讯");
        offer2.put("position", "前端工程师");
        offer2.put("city", "深圳");

        ObjectNode salary2 = offer2.putObject("salaryStructure");
        salary2.put("base", 25000);
        salary2.put("bonus", 120000);
        salary2.put("stock", 150000);

        offer2.put("workYears", 2);
        offer2.put("jobDescription", "负责用户界面开发");
        offer2.put("interviewProcess", "三轮技术面 + HR面");
        offer2.put("interviewDifficulty", 4);
        offer2.put("isAccepted", false);
        offer2.put("rejectReason", "团队文化不合适");

        // 打印请求体
        System.out.println("请求参数: " + formatJson(requestBody.toString()));

        HttpEntity<String> entity = new HttpEntity<>(requestBody.toString(), headers);
        ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                String.class);

        String responseBody = response.getBody();
        int status = response.getStatusCodeValue();

        System.out.println("响应状态: " + status);
        System.out.println("响应头: " + response.getHeaders());

        if (status == 200 && responseBody != null) {
            JsonNode jsonNode = objectMapper.readTree(responseBody);
            if (jsonNode.has("ids") && jsonNode.get("ids").isArray()) {
                ArrayNode idsArray = (ArrayNode) jsonNode.get("ids");
                for (JsonNode idNode : idsArray) {
                    batchIds.add(idNode.asLong());
                }
                System.out.println("成功批量创建Offer，ID列表: " + batchIds);
            }
        }

        checkStatus(status, 200, "批量创建 Offer", responseBody);
    }

    @Test
    @DisplayName("7. 测试批量删除 Offer")
    void testBatchDeleteOffer() throws Exception {
        System.out.println("测试 7: 批量删除 Offer");

        // 首先批量创建offer以确保有数据
        testBatchCreateOffer();

        if (!batchIds.isEmpty()) {
            String url = BASE_URL + "/v1/offers/batch";
            System.out.println("请求URL: " + url);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            ObjectNode requestBody = objectMapper.createObjectNode();
            ArrayNode idsArray = requestBody.putArray("ids");
            for (Long id : batchIds) {
                idsArray.add(id);
            }

            // 打印请求体
            System.out.println("请求参数: " + formatJson(requestBody.toString()));

            HttpEntity<String> entity = new HttpEntity<>(requestBody.toString(), headers);
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.DELETE,
                    entity,
                    String.class);

            String responseBody = response.getBody();
            int status = response.getStatusCodeValue();

            System.out.println("响应状态: " + status);
            System.out.println("响应头: " + response.getHeaders());

            checkStatus(status, 200, "批量删除 Offer", responseBody);
        } else {
            System.out.println("[SKIP] 跳过批量删除 Offer 测试，因为批量创建 Offer 失败");
        }
    }

    @Test
    @DisplayName("8. 测试创建导出任务")
    void testCreateExportTask() throws Exception {
        System.out.println("测试 8: 创建导出任务");

        String url = BASE_URL + "/v1/offers/export";
        System.out.println("请求URL: " + url);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("format", "csv");

        ObjectNode filters = requestBody.putObject("filters");
        ArrayNode companies = filters.putArray("companies");
        companies.add("字节跳动");
        companies.add("阿里巴巴");
        filters.put("startDate", "2025-01-01");
        filters.put("endDate", "2025-12-31");

        // 打印请求体
        System.out.println("请求参数: " + formatJson(requestBody.toString()));

        HttpEntity<String> entity = new HttpEntity<>(requestBody.toString(), headers);
        ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                String.class);

        String responseBody = response.getBody();
        int status = response.getStatusCodeValue();

        System.out.println("响应状态: " + status);
        System.out.println("响应头: " + response.getHeaders());

        if (status == 200 && responseBody != null) {
            JsonNode jsonNode = objectMapper.readTree(responseBody);
            if (jsonNode.has("taskId")) {
                exportTaskId = jsonNode.get("taskId").asLong();
                System.out.println("成功创建导出任务，任务ID: " + exportTaskId);
            }
        }

        checkStatus(status, 200, "创建导出任务", responseBody);
    }

    @Test
    @DisplayName("9. 测试获取导出任务状态")
    void testGetExportTaskStatus() throws Exception {
        System.out.println("测试 9: 获取导出任务状态");

        // 首先创建导出任务以确保有数据
        testCreateExportTask();

        if (exportTaskId != null) {
            String url = BASE_URL + "/v1/export-tasks/" + exportTaskId;
            System.out.println("请求URL: " + url);

            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

            String responseBody = response.getBody();
            int status = response.getStatusCodeValue();

            System.out.println("响应状态: " + status);
            System.out.println("响应头: " + response.getHeaders());

            checkStatus(status, 200, "获取导出任务状态", responseBody);
        } else {
            System.out.println("[SKIP] 跳过获取导出任务状态测试，因为创建导出任务失败");
        }
    }

    @Test
    @DisplayName("10. 测试获取薪资统计数据")
    void testGetSalaryStatistics() {
        System.out.println("测试 10: 获取薪资统计数据");

        String url = BASE_URL + "/v1/statistics/salary?dimension=company";
        System.out.println("请求URL: " + url);

        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        String responseBody = response.getBody();
        int status = response.getStatusCodeValue();

        System.out.println("响应状态: " + status);
        System.out.println("响应头: " + response.getHeaders());

        checkStatus(status, 200, "获取薪资统计数据", responseBody);

        // 如果失败，尝试分析错误原因
        if (status != 200) {
            System.out.println("获取薪资统计数据失败，可能的原因：");
            System.out.println("1. 数据库连接问题 - 确认test_offer_show数据库已创建");
            System.out.println("2. 统计查询SQL错误 - 检查StatisticsMapper.xml文件");
            System.out.println("3. 服务未正确启动或配置 - 检查应用日志");
            System.out.println("4. 数据库用户权限不足 - 确认数据库用户有权限访问test_offer_show");

            if (responseBody != null && responseBody.contains("your_test_db")) {
                System.out.println("错误提示存在'your_test_db'，可能在某处仍使用了错误的数据库名");
                System.out.println("建议检查：");
                System.out.println("- application.properties/application.yml文件");
                System.out.println("- application-test.properties/application-test.yml文件");
                System.out.println("- 其他可能覆盖数据库配置的地方");
            }
        }
    }

    @Test
    @DisplayName("11. 测试获取趋势统计数据")
    void testGetTrendStatistics() {
        System.out.println("测试 11: 获取趋势统计数据");

        String url = BASE_URL + "/v1/statistics/trend?type=monthly&dimension=all";
        System.out.println("请求URL: " + url);

        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        String responseBody = response.getBody();
        int status = response.getStatusCodeValue();

        System.out.println("响应状态: " + status);
        System.out.println("响应头: " + response.getHeaders());

        checkStatus(status, 200, "获取趋势统计数据", responseBody);

        // 如果失败，尝试分析错误原因
        if (status != 200) {
            System.out.println("获取趋势统计数据失败，可能的原因与薪资统计相同");
            System.out.println("建议先修复薪资统计接口的问题，再进行该测试");
        }
    }

    @Test
    @DisplayName("12. 测试删除 Offer")
    void testDeleteOffer() throws Exception {
        System.out.println("测试 12: 删除 Offer");

        // 首先创建offer以确保有数据
        testCreateOffer();

        if (offerId != null) {
            String url = BASE_URL + "/v1/offers/" + offerId;
            System.out.println("请求URL: " + url);

            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.DELETE,
                    null,
                    String.class);

            String responseBody = response.getBody();
            int status = response.getStatusCodeValue();

            System.out.println("响应状态: " + status);
            System.out.println("响应头: " + response.getHeaders());

            checkStatus(status, 200, "删除 Offer", responseBody);
        } else {
            System.out.println("[SKIP] 跳过删除 Offer 测试，因为创建 Offer 失败");
        }
    }
}