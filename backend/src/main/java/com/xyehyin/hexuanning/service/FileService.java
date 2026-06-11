package com.xyehyin.hexuanning.service;

import com.xyehyin.hexuanning.config.properties.AppProperties;
import com.xyehyin.hexuanning.config.properties.CosProperties;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.region.Region;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileService {
    private final CosProperties cosProperties;
    private final AppProperties appProperties;

    public String upload(MultipartFile file) throws IOException {
        if (isCosConfigured()) {
            return uploadToCos(file);
        }
        return uploadToLocal(file);
    }

    private String uploadToCos(MultipartFile file) throws IOException {
        COSCredentials cred = new BasicCOSCredentials(cosProperties.getSecretId(), cosProperties.getSecretKey());
        ClientConfig clientConfig = new ClientConfig(new Region(cosProperties.getRegion()));
        COSClient cosClient = new COSClient(cred, clientConfig);

        String ext = org.apache.commons.io.FilenameUtils.getExtension(file.getOriginalFilename());
        String key = (cosProperties.getPrefix() == null ? "" : cosProperties.getPrefix())
                + UUID.randomUUID() + "." + ext;

        // 临时文件
        File tempFile = File.createTempFile("cos-", "." + ext);
        file.transferTo(tempFile);

        try {
            PutObjectRequest putObjectRequest = new PutObjectRequest(cosProperties.getBucket(), key, tempFile);
            cosClient.putObject(putObjectRequest);
        } finally {
            tempFile.delete();
            cosClient.shutdown();
        }
        return cosProperties.getUrlPrefix() + "/" + key;
    }

    private String uploadToLocal(MultipartFile file) throws IOException {
        String ext = org.apache.commons.io.FilenameUtils.getExtension(file.getOriginalFilename());
        String filename = UUID.randomUUID() + (StringUtils.hasText(ext) ? "." + ext : "");
        Path uploadDir = Paths.get(appProperties.getUpload().getDir()).toAbsolutePath().normalize();
        Files.createDirectories(uploadDir);

        Path target = uploadDir.resolve(filename).normalize();
        if (!target.startsWith(uploadDir)) {
            throw new IOException("非法文件路径");
        }

        file.transferTo(target);
        log.info("COS配置不完整，文件已保存到本地: {}", target);
        return trimTrailingSlash(appProperties.getUpload().getUrlPrefix()) + "/" + filename;
    }

    private boolean isCosConfigured() {
        return StringUtils.hasText(cosProperties.getSecretId())
                && StringUtils.hasText(cosProperties.getSecretKey())
                && StringUtils.hasText(cosProperties.getBucket())
                && StringUtils.hasText(cosProperties.getUrlPrefix());
    }

    private String trimTrailingSlash(String value) {
        if (!StringUtils.hasText(value)) {
            return "/api/uploads";
        }
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }
}
