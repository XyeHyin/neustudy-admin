package com.xyehyin.hexuanning.utils;

import com.xyehyin.hexuanning.dto.knowledgepoint.KnowledgePointExportDTO;
import com.xyehyin.hexuanning.dto.knowledgepoint.KnowledgePointImportDTO;
import com.xyehyin.hexuanning.entity.KnowledgePoint;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ExcelUtilsTest {

    @Test
    void exportedKnowledgePointsCanBeImportedDirectly() throws Exception {
        KnowledgePointExportDTO exportDTO = new KnowledgePointExportDTO();
        exportDTO.setId(10L);
        exportDTO.setCourseId(20L);
        exportDTO.setCourseName("数据结构与算法");
        exportDTO.setName("二叉树遍历");
        exportDTO.setDescription("掌握前序、中序、后序和层序遍历");
        exportDTO.setDifficulty("HARD");
        exportDTO.setOrderNum(7);
        exportDTO.setContent("递归遍历、迭代遍历、队列层序遍历");
        exportDTO.setKeywords("二叉树,遍历,递归,队列");
        exportDTO.setQuestionCount(3L);
        exportDTO.setEnabled("禁用");
        exportDTO.setCreateTime("2026-06-07 10:00:00");
        exportDTO.setUpdateTime("2026-06-07 11:00:00");

        MockHttpServletResponse response = new MockHttpServletResponse();
        ExcelUtils.exportKnowledgePoints(List.of(exportDTO), response);

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "knowledge_points.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                response.getContentAsByteArray());
        ExcelUtils.ImportResult result = ExcelUtils.importKnowledgePointsFromExcel(file);

        assertThat(result.getValidRows()).isEqualTo(1);
        assertThat(result.getWarnings()).isEmpty();

        KnowledgePointImportDTO imported = result.getData().get(0);
        assertThat(imported.getRowNumber()).isEqualTo(2);
        assertThat(imported.getName()).isEqualTo("二叉树遍历");
        assertThat(imported.getDescription()).isEqualTo("掌握前序、中序、后序和层序遍历");
        assertThat(imported.getDifficulty()).isEqualTo(KnowledgePoint.Difficulty.HARD);
        assertThat(imported.getOrderNum()).isEqualTo(7);
        assertThat(imported.getContent()).isEqualTo("递归遍历、迭代遍历、队列层序遍历");
        assertThat(imported.getKeywords()).isEqualTo("二叉树,遍历,递归,队列");
        assertThat(imported.getEnabled()).isFalse();
    }

    @Test
    void importTemplateSkipsExampleAndInstructionRows() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        ExcelUtils.generateTemplate(response);

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "knowledge_points_template.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                response.getContentAsByteArray());
        ExcelUtils.ImportResult result = ExcelUtils.importKnowledgePointsFromExcel(file);

        assertThat(result.getValidRows()).isZero();
        assertThat(result.getData()).isEmpty();
    }
}
