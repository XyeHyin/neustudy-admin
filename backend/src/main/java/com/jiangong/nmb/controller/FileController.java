package com.jiangong.nmb.controller;

import cn.hutool.core.exceptions.StatefulException;
import cn.hutool.http.HttpStatus;
import com.jiangong.nmb.common.ApiResponse;
import com.jiangong.nmb.constant.PermissionConstants;
import com.jiangong.nmb.service.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Slf4j
@Tag(name = "文件管理", description = "文件相关接口")
@RestController
@CrossOrigin
@RequestMapping("/files")
@RequiredArgsConstructor
public class FileController {
    private final FileService fileService;

    @Operation(summary = "文件上传", description = "上传文件到腾讯云COS")
    @PreAuthorize("hasAuthority('" + PermissionConstants.FILE_UPLOAD + "')")
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<String> upload(@RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new StatefulException(HttpStatus.HTTP_BAD_REQUEST, "文件不能为空");
        }
        long maxSize = 5 * 1024 * 1024;
        if (file.getSize() > maxSize) {
            throw new StatefulException(HttpStatus.HTTP_ENTITY_TOO_LARGE, "文件大小不能超过5MB");
        }
        String contentType = file.getContentType();
        if (contentType == null ||
                !(contentType.equals("image/png") || contentType.equals("image/jpeg") || contentType.equals("image/jpg"))) {
            throw new StatefulException(HttpStatus.HTTP_UNSUPPORTED_TYPE, "只允许上传PNG/JPG图片");
        }
        String url = fileService.uploadToCos(file);
        return ApiResponse.success(url);
    }
}
