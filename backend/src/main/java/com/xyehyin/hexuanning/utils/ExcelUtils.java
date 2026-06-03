package com.xyehyin.hexuanning.utils;

import com.xyehyin.hexuanning.dto.knowledgepoint.KnowledgePointExportDTO;
import com.xyehyin.hexuanning.dto.knowledgepoint.KnowledgePointImportDTO;
import com.xyehyin.hexuanning.entity.KnowledgePoint;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class ExcelUtils {

    /**
     * 导出知识点到Excel
     */
    public static void exportKnowledgePoints(List<KnowledgePointExportDTO> data,
                                             HttpServletResponse response) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("知识点数据");

        // 创建标题行
        Row headerRow = sheet.createRow(0);
        String[] headers = {"知识点名称", "知识点描述", "所属课程", "难度", "排序号", "知识点内容", "关键词", "状态", "创建时间"};

        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);

        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // 填充数据
        for (int i = 0; i < data.size(); i++) {
            Row row = sheet.createRow(i + 1);
            KnowledgePointExportDTO dto = data.get(i);

            row.createCell(0).setCellValue(dto.getName());
            row.createCell(1).setCellValue(dto.getDescription());
            row.createCell(2).setCellValue(dto.getCourseName());
            row.createCell(3).setCellValue(dto.getDifficulty());
            row.createCell(4).setCellValue(dto.getOrderNum() != null ? dto.getOrderNum() : 0);
            row.createCell(5).setCellValue(dto.getContent());
            row.createCell(6).setCellValue(dto.getKeywords());
            row.createCell(7).setCellValue(dto.getEnabled());
            row.createCell(8).setCellValue(dto.getCreateTime());
        }

        // 自动调整列宽
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        // 设置响应头
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=knowledge_points.xlsx");

        // 写入响应
        try (OutputStream outputStream = response.getOutputStream()) {
            workbook.write(outputStream);
        } finally {
            workbook.close();
        }
    }

    /**
     * Excel导入结果
     */
    public static class ImportResult {
        private List<KnowledgePointImportDTO> data;
        private List<String> warnings;
        private int totalRows;
        private int validRows;

        public ImportResult(List<KnowledgePointImportDTO> data, List<String> warnings, int totalRows, int validRows) {
            this.data = data;
            this.warnings = warnings;
            this.totalRows = totalRows;
            this.validRows = validRows;
        }

        public List<KnowledgePointImportDTO> getData() {
            return data;
        }

        public List<String> getWarnings() {
            return warnings;
        }

        public int getTotalRows() {
            return totalRows;
        }

        public int getValidRows() {
            return validRows;
        }
    }

    /**
     * 从Excel导入知识点（统一使用改进版）
     */
    public static ImportResult importKnowledgePointsFromExcel(MultipartFile file) throws IOException {
        List<KnowledgePointImportDTO> result = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        int totalRows = 0;
        int validRows = 0;

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            totalRows = sheet.getLastRowNum();

            // 跳过标题行
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                KnowledgePointImportDTO dto = new KnowledgePointImportDTO();

                // 解析每一列
                dto.setName(getCellValue(row.getCell(0)));
                dto.setDescription(getCellValue(row.getCell(1)));

                // 处理难度值
                String difficultyStr = getCellValue(row.getCell(2));
                dto.setOriginalDifficultyValue(difficultyStr);

                KnowledgePoint.Difficulty difficulty = parseDifficultyWithWarning(difficultyStr, i + 1, warnings);
                dto.setDifficulty(difficulty);

                String orderNumStr = getCellValue(row.getCell(3));
                if (orderNumStr != null && !orderNumStr.isEmpty()) {
                    try {
                        dto.setOrderNum(Integer.parseInt(orderNumStr));
                    } catch (NumberFormatException e) {
                        warnings.add(String.format("第%d行：排序号格式错误，将使用默认值", i + 1));
                    }
                }

                dto.setContent(getCellValue(row.getCell(4)));
                dto.setKeywords(getCellValue(row.getCell(5)));

                String enabledStr = getCellValue(row.getCell(6));
                dto.setEnabled(!"禁用".equals(enabledStr));

                result.add(dto);
                validRows++;
            }
        }

        return new ImportResult(result, warnings, totalRows, validRows);
    }

    /**
     * 解析难度值，支持多种格式并提供警告信息
     */
    private static KnowledgePoint.Difficulty parseDifficultyWithWarning(String difficultyStr, int rowNum, List<String> warnings) {
        if (difficultyStr == null || difficultyStr.trim().isEmpty()) {
            return KnowledgePoint.Difficulty.MEDIUM;
        }

        String normalized = difficultyStr.trim().toUpperCase();

        // 支持英文枚举值
        try {
            return KnowledgePoint.Difficulty.valueOf(normalized);
        } catch (IllegalArgumentException e) {
            // 继续尝试中文映射
        }

        // 支持中文描述
        return switch (normalized) {
            case "简单", "容易", "低", "1" -> KnowledgePoint.Difficulty.EASY;
            case "中等", "中", "普通", "2" -> KnowledgePoint.Difficulty.MEDIUM;
            case "困难", "难", "高", "3" -> KnowledgePoint.Difficulty.HARD;
            default -> {
                warnings.add(String.format("第%d行：无法识别的难度值 '%s'，已设置为默认值 '中等'", rowNum, difficultyStr));
                yield KnowledgePoint.Difficulty.MEDIUM;
            }
        };
    }

    /**
     * 生成导入模板
     */
    public static void generateTemplate(HttpServletResponse response) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("知识点导入模板");

        // 创建标题行
        Row headerRow = sheet.createRow(0);
        String[] headers = {
                "知识点名称*",
                "知识点描述",
                "难度(EASY/MEDIUM/HARD 或 简单/中等/困难)",
                "排序号",
                "知识点内容",
                "关键词",
                "状态(启用/禁用)"
        };

        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);

        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // 添加示例数据
        Row exampleRow = sheet.createRow(1);
        exampleRow.createCell(0).setCellValue("示例知识点");
        exampleRow.createCell(1).setCellValue("这是一个示例知识点的描述");
        exampleRow.createCell(2).setCellValue("MEDIUM");
        exampleRow.createCell(3).setCellValue("1");
        exampleRow.createCell(4).setCellValue("详细的知识点内容...");
        exampleRow.createCell(5).setCellValue("关键词1,关键词2");
        exampleRow.createCell(6).setCellValue("启用");

        // 添加说明行
        Row instructionRow = sheet.createRow(2);
        instructionRow.createCell(0).setCellValue("说明：");
        instructionRow.createCell(1).setCellValue("必填字段标有*号");
        instructionRow.createCell(2).setCellValue("支持：EASY/简单、MEDIUM/中等、HARD/困难");

        // 自动调整列宽
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        // 设置响应头
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=knowledge_points_template.xlsx");

        // 写入响应
        try (OutputStream outputStream = response.getOutputStream()) {
            workbook.write(outputStream);
        } finally {
            workbook.close();
        }
    }

    private static String getCellValue(Cell cell) {
        if (cell == null) return null;

        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> String.valueOf((int) cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> cell.getCellFormula();
            default -> null;
        };
    }
}