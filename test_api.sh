#!/bin/bash

# Offer Show API 接口测试脚本
# 用法：./test_api.sh

# 基础 URL
BASE_URL="http://localhost:8080/api"

# 设置文本颜色
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[0;33m'
NC='\033[0m' # No Color

# 测试结果统计
total_tests=0
passed_tests=0

# 辅助函数：打印测试结果
print_result() {
    total_tests=$((total_tests+1))
    if [ $1 -eq 0 ]; then
        passed_tests=$((passed_tests+1))
        echo -e "${GREEN}[PASS]${NC} $2"
    else
        echo -e "${RED}[FAIL]${NC} $2"
        echo -e "${YELLOW}Response:${NC} $3"
    fi
    echo
}

# 辅助函数：检查 HTTP 状态码
check_status() {
    status=$1
    expected=$2
    description=$3
    response=$4

    if [ "$status" -eq "$expected" ]; then
        print_result 0 "$description"
    else
        print_result 1 "$description" "$response"
    fi
}

echo "========================================"
echo "    Offer Show API 接口测试 开始        "
echo "========================================"
echo

# 1. 测试创建 Offer
echo "测试 1: 创建 Offer"
response=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/v1/offers" \
    -H "Content-Type: application/json" \
    -d '{
        "companyName": "字节跳动",
        "position": "后端工程师",
        "city": "北京",
        "salaryStructure": {
            "base": 30000,
            "bonus": 150000,
            "stock": 100000
        },
        "workYears": 3,
        "jobDescription": "负责服务端架构设计和开发",
        "interviewProcess": "三轮技术面 + HR面",
        "interviewDifficulty": 4,
        "isAccepted": true
    }')

status=$(echo "$response" | tail -n1)
body=$(echo "$response" | sed '$d')
offer_id=$(echo "$body" | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)

check_status "$status" 200 "创建 Offer" "$body"

# 2. 测试获取 Offer 详情
echo "测试 2: 获取 Offer 详情"
if [ -n "$offer_id" ]; then
    response=$(curl -s -w "\n%{http_code}" -X GET "$BASE_URL/v1/offers/$offer_id")
    status=$(echo "$response" | tail -n1)
    body=$(echo "$response" | sed '$d')

    check_status "$status" 200 "获取 Offer 详情" "$body"
else
    echo -e "${YELLOW}[SKIP]${NC} 跳过获取 Offer 详情测试，因为创建 Offer 失败"
fi

# 3. 测试更新 Offer
echo "测试 3: 更新 Offer"
if [ -n "$offer_id" ]; then
    response=$(curl -s -w "\n%{http_code}" -X PUT "$BASE_URL/v1/offers/$offer_id" \
        -H "Content-Type: application/json" \
        -d '{
            "companyName": "字节跳动",
            "position": "高级后端工程师",
            "city": "北京",
            "salaryStructure": {
                "base": 35000,
                "bonus": 200000,
                "stock": 150000
            },
            "workYears": 5,
            "jobDescription": "负责核心服务端架构设计和开发",
            "interviewProcess": "四轮技术面 + HR面",
            "interviewDifficulty": 5,
            "isAccepted": true
        }')
    status=$(echo "$response" | tail -n1)
    body=$(echo "$response" | sed '$d')

    check_status "$status" 200 "更新 Offer" "$body"
else
    echo -e "${YELLOW}[SKIP]${NC} 跳过更新 Offer 测试，因为创建 Offer 失败"
fi

# 4. 测试部分更新 Offer
echo "测试 4: 部分更新 Offer"
if [ -n "$offer_id" ]; then
    response=$(curl -s -w "\n%{http_code}" -X PATCH "$BASE_URL/v1/offers/$offer_id" \
        -H "Content-Type: application/json" \
        -d '{
            "isAccepted": false,
            "rejectReason": "薪资低于期望"
        }')
    status=$(echo "$response" | tail -n1)
    body=$(echo "$response" | sed '$d')

    check_status "$status" 200 "部分更新 Offer" "$body"
else
    echo -e "${YELLOW}[SKIP]${NC} 跳过部分更新 Offer 测试，因为创建 Offer 失败"
fi

# 5. 测试搜索 Offer
echo "测试 5: 搜索 Offer"
response=$(curl -s -w "\n%{http_code}" -X GET "$BASE_URL/v1/offers/search?keyword=字节跳动&page=1&size=10")
status=$(echo "$response" | tail -n1)
body=$(echo "$response" | sed '$d')

check_status "$status" 200 "搜索 Offer" "$body"

# 6. 测试批量创建 Offer
echo "测试 6: 批量创建 Offer"
response=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/v1/offers/batch" \
    -H "Content-Type: application/json" \
    -d '{
        "offers": [
            {
                "companyName": "阿里巴巴",
                "position": "算法工程师",
                "city": "杭州",
                "salaryStructure": {
                    "base": 30000,
                    "bonus": 100000,
                    "stock": 200000
                },
                "workYears": 3,
                "jobDescription": "负责推荐算法研发",
                "interviewProcess": "四轮算法面 + HR面",
                "interviewDifficulty": 5,
                "isAccepted": true
            },
            {
                "companyName": "腾讯",
                "position": "前端工程师",
                "city": "深圳",
                "salaryStructure": {
                    "base": 25000,
                    "bonus": 120000,
                    "stock": 150000
                },
                "workYears": 2,
                "jobDescription": "负责用户界面开发",
                "interviewProcess": "三轮技术面 + HR面",
                "interviewDifficulty": 4,
                "isAccepted": false,
                "rejectReason": "团队文化不合适"
            }
        ]
    }')
status=$(echo "$response" | tail -n1)
body=$(echo "$response" | sed '$d')

check_status "$status" 200 "批量创建 Offer" "$body"

# 提取批量创建返回的ID
batch_ids=$(echo "$body" | grep -o '"id":[0-9]*' | cut -d':' -f2 | tr '\n' ',' | sed 's/,$//')

# 7. 测试批量删除 Offer
echo "测试 7: 批量删除 Offer"
if [ -n "$batch_ids" ]; then
    response=$(curl -s -w "\n%{http_code}" -X DELETE "$BASE_URL/v1/offers/batch" \
        -H "Content-Type: application/json" \
        -d "{
            \"ids\": [$batch_ids]
        }")
    status=$(echo "$response" | tail -n1)
    body=$(echo "$response" | sed '$d')

    check_status "$status" 200 "批量删除 Offer" "$body"
else
    echo -e "${YELLOW}[SKIP]${NC} 跳过批量删除 Offer 测试，因为批量创建 Offer 失败"
fi

# 8. 测试创建导出任务
echo "测试 8: 创建导出任务"
response=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/v1/offers/export" \
    -H "Content-Type: application/json" \
    -d '{
        "format": "csv",
        "filters": {
            "companies": ["字节跳动", "阿里巴巴"],
            "startDate": "2025-01-01",
            "endDate": "2025-12-31"
        }
    }')
status=$(echo "$response" | tail -n1)
body=$(echo "$response" | sed '$d')
task_id=$(echo "$body" | grep -o '"taskId":[0-9]*' | cut -d':' -f2)

check_status "$status" 200 "创建导出任务" "$body"

# 9. 测试获取导出任务状态
echo "测试 9: 获取导出任务状态"
if [ -n "$task_id" ]; then
    response=$(curl -s -w "\n%{http_code}" -X GET "$BASE_URL/v1/export-tasks/$task_id")
    status=$(echo "$response" | tail -n1)
    body=$(echo "$response" | sed '$d')

    check_status "$status" 200 "获取导出任务状态" "$body"
else
    echo -e "${YELLOW}[SKIP]${NC} 跳过获取导出任务状态测试，因为创建导出任务失败"
fi

# 10. 测试获取薪资统计数据
echo "测试 10: 获取薪资统计数据"
response=$(curl -s -w "\n%{http_code}" -X GET "$BASE_URL/v1/statistics/salary?dimension=company")
status=$(echo "$response" | tail -n1)
body=$(echo "$response" | sed '$d')

check_status "$status" 200 "获取薪资统计数据" "$body"

# 11. 测试获取趋势统计数据
echo "测试 11: 获取趋势统计数据"
response=$(curl -s -w "\n%{http_code}" -X GET "$BASE_URL/v1/statistics/trend?type=monthly&dimension=all")
status=$(echo "$response" | tail -n1)
body=$(echo "$response" | sed '$d')

check_status "$status" 200 "获取趋势统计数据" "$body"

# 12. 测试删除 Offer
echo "测试 12: 删除 Offer"
if [ -n "$offer_id" ]; then
    response=$(curl -s -w "\n%{http_code}" -X DELETE "$BASE_URL/v1/offers/$offer_id")
    status=$(echo "$response" | tail -n1)
    body=$(echo "$response" | sed '$d')

    check_status "$status" 200 "删除 Offer" "$body"
else
    echo -e "${YELLOW}[SKIP]${NC} 跳过删除 Offer 测试，因为创建 Offer 失败"
fi

# 输出测试结果统计
echo "========================================"
echo "测试结果统计:"
echo "总测试数: $total_tests"
echo "通过测试: $passed_tests"
echo "失败测试: $((total_tests-passed_tests))"
echo "通过率: $((passed_tests*100/total_tests))%"
echo "========================================"