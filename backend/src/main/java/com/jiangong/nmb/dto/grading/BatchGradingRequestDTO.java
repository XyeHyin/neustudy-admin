package com.jiangong.nmb.dto.grading;

import lombok.Data;
import jakarta.validation.constraints.NotEmpty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/**
 * 批量判分请求 DTO
 */
@Data
public class BatchGradingRequestDTO {

    @Schema(description = "判分请求列表", required = true)
    @NotEmpty(message = "判分请求列表不能为空")
    private List<GradingRequestDTO> requests;
} 