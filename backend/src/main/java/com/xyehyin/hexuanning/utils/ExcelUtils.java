package com.xyehyin.hexuanning.utils;

import com.xyehyin.hexuanning.dto.knowledgepoint.KnowledgePointExportDTO;
import com.xyehyin.hexuanning.dto.knowledgepoint.KnowledgePointImportDTO;
import com.xyehyin.hexuanning.entity.KnowledgePoint;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Slf4j
public class ExcelUtils {

    private static final String[] EXPORT_HEADERS = {
            "知识点ID",
            "课程ID",
            "所属课程",
            "知识点名称*",
            "知识点描述",
            "难度(EASY/MEDIUM/HARD 或 简单/中等/困难)",
            "排序号",
            "知识点内容",
            "关键词",
            "关联题目数",
            "状态(启用/禁用)",
            "创建时间",
            "更新时间"
    };

    private static final String[] TEMPLATE_HEADERS = {
            "知识点名称*",
            "知识点描述",
            "难度(EASY/MEDIUM/HARD 或 简单/中等/困难)",
            "排序号",
            "知识点内容",
            "关键词",
            "状态(启用/禁用)"
    };

    private enum ImportField {
        NAME,
        DESCRIPTION,
        DIFFICULTY,
        ORDER_NUM,
        CONTENT,
        KEYWORDS,
        ENABLED
    }

    /**
     * 导出知识点。导出的 Excel 也可以直接作为导入文件使用。
     */
    public static void exportKnowledgePoints(List<KnowledgePointExportDTO> data,
                                             HttpServletResponse response) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("知识点数据");

        Row headerRow = sheet.createRow(0);
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        headerStyle.setWrapText(true);

        CellStyle wrapStyle = workbook.createCellStyle();
        wrapStyle.setWrapText(true);

        for (int i = 0; i < EXPORT_HEADERS.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(EXPORT_HEADERS[i]);
            cell.setCellStyle(headerStyle);
        }

        for (int i = 0; i < data.size(); i++) {
            Row row = sheet.createRow(i + 1);
            KnowledgePointExportDTO dto = data.get(i);

            row.createCell(0).setCellValue(dto.getId() != null ? dto.getId() : 0);
            row.createCell(1).setCellValue(dto.getCourseId() != null ? dto.getCourseId() : 0);
            row.createCell(2).setCellValue(nullToEmpty(dto.getCourseName()));
            row.createCell(3).setCellValue(nullToEmpty(dto.getName()));
            row.createCell(4).setCellValue(nullToEmpty(dto.getDescription()));
            row.createCell(5).setCellValue(nullToEmpty(dto.getDifficulty()));
            row.createCell(6).setCellValue(dto.getOrderNum() != null ? dto.getOrderNum() : 0);
            row.createCell(7).setCellValue(nullToEmpty(dto.getContent()));
            row.createCell(8).setCellValue(nullToEmpty(dto.getKeywords()));
            row.createCell(9).setCellValue(dto.getQuestionCount() != null ? dto.getQuestionCount() : 0);
            row.createCell(10).setCellValue(nullToEmpty(dto.getEnabled()));
            row.createCell(11).setCellValue(nullToEmpty(dto.getCreateTime()));
            row.createCell(12).setCellValue(nullToEmpty(dto.getUpdateTime()));

            row.getCell(4).setCellStyle(wrapStyle);
            row.getCell(7).setCellStyle(wrapStyle);
        }

        sheet.createFreezePane(0, 1);
        sheet.setAutoFilter(new CellRangeAddress(0, Math.max(data.size(), 1), 0, EXPORT_HEADERS.length - 1));

        for (int i = 0; i < EXPORT_HEADERS.length; i++) {
            sheet.autoSizeColumn(i);
            int width = sheet.getColumnWidth(i);
            int maxWidth = switch (i) {
                case 4, 7 -> 12000;
                case 8 -> 6000;
                default -> 5000;
            };
            sheet.setColumnWidth(i, Math.min(Math.max(width, 3000), maxWidth));
        }

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=knowledge_points.xlsx");

        try (OutputStream outputStream = response.getOutputStream()) {
            workbook.write(outputStream);
        } finally {
            workbook.close();
        }
    }

    /**
     * Excel 导入结果。
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
     * 从 Excel 导入知识点。优先按表头识别列，兼容导入模板和导出宽表。
     */
    public static ImportResult importKnowledgePointsFromExcel(MultipartFile file) throws IOException {
        List<KnowledgePointImportDTO> result = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        int totalRows = 0;

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            totalRows = sheet.getLastRowNum();

            Row headerRow = sheet.getRow(0);
            Map<ImportField, Integer> columns = resolveImportColumns(headerRow);

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null || isBlankRow(row)) {
                    continue;
                }

                int rowNumber = i + 1;
                String name = read(row, columns, ImportField.NAME);
                if (isTemplateExampleOrInstructionRow(name)) {
                    continue;
                }

                KnowledgePointImportDTO dto = new KnowledgePointImportDTO();
                dto.setRowNumber(rowNumber);
                dto.setName(name);
                dto.setDescription(read(row, columns, ImportField.DESCRIPTION));

                String difficultyStr = read(row, columns, ImportField.DIFFICULTY);
                dto.setOriginalDifficultyValue(difficultyStr);
                dto.setDifficulty(parseDifficultyWithWarning(difficultyStr, rowNumber, warnings));

                String orderNumStr = read(row, columns, ImportField.ORDER_NUM);
                if (orderNumStr != null && !orderNumStr.isEmpty()) {
                    try {
                        dto.setOrderNum(Integer.parseInt(orderNumStr));
                    } catch (NumberFormatException e) {
                        warnings.add(String.format("第%d行：排序号格式错误，将使用默认值", rowNumber));
                    }
                }

                dto.setContent(read(row, columns, ImportField.CONTENT));
                dto.setKeywords(read(row, columns, ImportField.KEYWORDS));
                dto.setEnabled(parseEnabled(read(row, columns, ImportField.ENABLED)));

                result.add(dto);
            }
        }

        return new ImportResult(result, warnings, totalRows, result.size());
    }

    private static Map<ImportField, Integer> resolveImportColumns(Row headerRow) {
        Map<ImportField, Integer> columns = new EnumMap<>(ImportField.class);
        Map<String, Integer> normalizedHeaders = new HashMap<>();

        if (headerRow != null) {
            short lastCellNum = headerRow.getLastCellNum();
            for (int i = 0; i < lastCellNum; i++) {
                String normalized = normalizeHeader(getCellValue(headerRow.getCell(i)));
                if (!normalized.isEmpty()) {
                    normalizedHeaders.put(normalized, i);
                }
            }
        }

        putColumn(columns, normalizedHeaders, ImportField.NAME, "知识点名称", "名称");
        putColumn(columns, normalizedHeaders, ImportField.DESCRIPTION, "知识点描述", "描述");
        putColumn(columns, normalizedHeaders, ImportField.DIFFICULTY, "难度", "难度等级");
        putColumn(columns, normalizedHeaders, ImportField.ORDER_NUM, "排序号", "排序", "序号");
        putColumn(columns, normalizedHeaders, ImportField.CONTENT, "知识点内容", "内容");
        putColumn(columns, normalizedHeaders, ImportField.KEYWORDS, "关键词", "关键字");
        putColumn(columns, normalizedHeaders, ImportField.ENABLED, "状态", "是否启用", "启用状态");

        if (!columns.containsKey(ImportField.NAME)) {
            columns.put(ImportField.NAME, 0);
            columns.put(ImportField.DESCRIPTION, 1);
            columns.put(ImportField.DIFFICULTY, 2);
            columns.put(ImportField.ORDER_NUM, 3);
            columns.put(ImportField.CONTENT, 4);
            columns.put(ImportField.KEYWORDS, 5);
            columns.put(ImportField.ENABLED, 6);
        }

        return columns;
    }

    private static void putColumn(Map<ImportField, Integer> columns,
                                  Map<String, Integer> normalizedHeaders,
                                  ImportField field,
                                  String... aliases) {
        for (String alias : aliases) {
            Integer index = normalizedHeaders.get(normalizeHeader(alias));
            if (index != null) {
                columns.put(field, index);
                return;
            }
        }
    }

    private static String normalizeHeader(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\uFEFF", "")
                .replaceAll("[（(].*?[）)]", "")
                .replaceAll("[\\s*＊:：]", "")
                .trim();
    }

    private static String read(Row row, Map<ImportField, Integer> columns, ImportField field) {
        Integer index = columns.get(field);
        return index == null ? null : getCellValue(row.getCell(index));
    }

    private static KnowledgePoint.Difficulty parseDifficultyWithWarning(String difficultyStr, int rowNum, List<String> warnings) {
        if (difficultyStr == null || difficultyStr.trim().isEmpty()) {
            return KnowledgePoint.Difficulty.MEDIUM;
        }

        String normalized = difficultyStr.trim().toUpperCase(Locale.ROOT);

        try {
            return KnowledgePoint.Difficulty.valueOf(normalized);
        } catch (IllegalArgumentException e) {
            // Continue with localized aliases.
        }

        return switch (normalized) {
            case "简单", "容易", "低", "1" -> KnowledgePoint.Difficulty.EASY;
            case "中等", "中", "普通", "2" -> KnowledgePoint.Difficulty.MEDIUM;
            case "困难", "难", "高", "3" -> KnowledgePoint.Difficulty.HARD;
            default -> {
                warnings.add(String.format("第%d行：无法识别的难度值'%s'，已设置为默认值'中等'", rowNum, difficultyStr));
                yield KnowledgePoint.Difficulty.MEDIUM;
            }
        };
    }

    private static Boolean parseEnabled(String enabledStr) {
        if (enabledStr == null || enabledStr.trim().isEmpty()) {
            return true;
        }

        String normalized = enabledStr.trim().toLowerCase(Locale.ROOT);
        if (normalized.equals("禁用")
                || normalized.equals("停用")
                || normalized.equals("否")
                || normalized.equals("false")
                || normalized.equals("0")) {
            return false;
        }

        return true;
    }

    /**
     * 生成知识点导入模板。
     */
    public static void generateTemplate(HttpServletResponse response) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("知识点导入模板");

        Row headerRow = sheet.createRow(0);
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);

        for (int i = 0; i < TEMPLATE_HEADERS.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(TEMPLATE_HEADERS[i]);
            cell.setCellStyle(headerStyle);
        }

        Row exampleRow = sheet.createRow(1);
        exampleRow.createCell(0).setCellValue("示例知识点");
        exampleRow.createCell(1).setCellValue("这是一个示例知识点的描述");
        exampleRow.createCell(2).setCellValue("MEDIUM");
        exampleRow.createCell(3).setCellValue("1");
        exampleRow.createCell(4).setCellValue("详细的知识点内容...");
        exampleRow.createCell(5).setCellValue("关键词,关键词");
        exampleRow.createCell(6).setCellValue("启用");

        Row instructionRow = sheet.createRow(2);
        instructionRow.createCell(0).setCellValue("说明：");
        instructionRow.createCell(1).setCellValue("必填字段标有*号");
        instructionRow.createCell(2).setCellValue("支持：EASY/简单、MEDIUM/中等、HARD/困难");
        instructionRow.createCell(3).setCellValue("导入时系统会自动跳过示例行和说明行");

        for (int i = 0; i < TEMPLATE_HEADERS.length; i++) {
            sheet.autoSizeColumn(i);
        }

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=knowledge_points_template.xlsx");

        try (OutputStream outputStream = response.getOutputStream()) {
            workbook.write(outputStream);
        } finally {
            workbook.close();
        }
    }

    private static String getCellValue(Cell cell) {
        if (cell == null) {
            return null;
        }
        String value = new DataFormatter().formatCellValue(cell);
        return value == null ? null : value.trim();
    }

    private static boolean isBlankRow(Row row) {
        short lastCellNum = row.getLastCellNum();
        if (lastCellNum < 0) {
            return true;
        }
        for (int i = 0; i < lastCellNum; i++) {
            String value = getCellValue(row.getCell(i));
            if (value != null && !value.trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private static boolean isTemplateExampleOrInstructionRow(String name) {
        if (name == null) {
            return false;
        }
        String normalized = name.trim();
        return normalized.startsWith("示例")
                || normalized.equals("说明")
                || normalized.equals("说明：")
                || normalized.startsWith("说明:");
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
