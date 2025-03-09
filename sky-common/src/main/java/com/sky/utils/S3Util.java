package com.sky.utils;

import com.sky.properties.S3Properties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.nio.file.Paths;

@Component
@RequiredArgsConstructor
public class S3Util {

    private final S3Client s3Client;
    private final S3Properties s3Properties;

    /**
     * 上传文件到 S3
     * @param localFilePath 本地文件路径
     * @param s3Key S3 存储的文件名称
     */
    public String uploadFile(String localFilePath, String s3Key) {
        String bucketName = s3Properties.getBucketName();
        String region = s3Properties.getRegion();

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(s3Key)
                .build();

        s3Client.putObject(putObjectRequest, Paths.get(localFilePath));
        String fileUrl = String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, region, s3Key);
        System.out.println("success upload to：" + fileUrl);
        return fileUrl;
    }
}

