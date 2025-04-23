package com.offershow.util;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;

import java.time.LocalDateTime;

/**
 * Excel工具类
 */
public class ExcelUtils {

    /**
     * 获取单元格的字符串值
     *
     * @param cell Excel单元格
     * @return 字符串值
     */
    public static String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }

        CellType cellType = cell.getCellType();

        switch (cellType) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getLocalDateTimeCellValue().toString();
                } else {
                    // 避免数字显示科学计数法
                    double value = cell.getNumericCellValue();
                    long longValue = (long) value;
                    if (value == longValue) {
                        return String.valueOf(longValue);
                    } else {
                        return String.valueOf(value);
                    }
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    return String.valueOf(cell.getNumericCellValue());
                } catch (Exception e) {
                    try {
                        return cell.getStringCellValue();
                    } catch (Exception e2) {
                        return cell.getCellFormula();
                    }
                }
            case BLANK:
                return "";
            default:
                return "";
        }
    }

    /**
     * 获取单元格的日期值
     *
     * @param cell Excel单元格
     * @return 日期值
     */
    public static LocalDateTime getCellValueAsDate(Cell cell) {
        if (cell == null) {
            return null;
        }

        CellType cellType = cell.getCellType();

        if (cellType == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
            return cell.getLocalDateTimeCellValue();
        } else if (cellType == CellType.STRING) {
            String dateStr = cell.getStringCellValue();
            // 这里可以添加字符串转日期的逻辑
            return null;
        }

        return null;
    }

    /**
     * 获取单元格的数值
     *
     * @param cell Excel单元格
     * @return 数值
     */
    public static Double getCellValueAsNumeric(Cell cell) {
        if (cell == null) {
            return null;
        }

        CellType cellType = cell.getCellType();

        switch (cellType) {
            case NUMERIC:
                return cell.getNumericCellValue();
            case STRING:
                try {
                    return Double.parseDouble(cell.getStringCellValue());
                } catch (NumberFormatException e) {
                    return null;
                }
            case BOOLEAN:
                return cell.getBooleanCellValue() ? 1.0 : 0.0;
            default:
                return null;
        }
    }

    /**
     * 获取单元格的布尔值
     *
     * @param cell Excel单元格
     * @return 布尔值
     */
    public static Boolean getCellValueAsBoolean(Cell cell) {
        if (cell == null) {
            return null;
        }

        CellType cellType = cell.getCellType();

        switch (cellType) {
            case BOOLEAN:
                return cell.getBooleanCellValue();
            case NUMERIC:
                return cell.getNumericCellValue() != 0;
            case STRING:
                String value = cell.getStringCellValue().toLowerCase();
                return "true".equals(value) || "yes".equals(value) || "1".equals(value) || "是".equals(value);
            default:
                return null;
        }
    }
}