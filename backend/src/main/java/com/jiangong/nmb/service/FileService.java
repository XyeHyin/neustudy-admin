package com.jiangong.nmb.service;

import com.jiangong.nmb.config.CosProperties;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.region.Region;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileService {
    private final CosProperties cosProperties;

    public String uploadToCos(MultipartFile file) throws IOException {
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
}