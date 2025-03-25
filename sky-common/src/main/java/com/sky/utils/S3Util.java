package com.sky.utils;

import software.amazon.awssdk.auth.credentials.InstanceProfileCredentialsProvider;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.core.async.AsyncResponseTransformer;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class S3Util {

    private final S3Client s3Client;
    private final S3AsyncClient s3AsyncClient;
    private final S3Presigner s3Presigner;
    private final String bucketName;

    /**
     * 创建S3Util实例
     *
     * @param region AWS区域
     * @param bucketName S3存储桶名称
     */
    public S3Util(Region region, String bucketName) {
        this.s3Client = S3Client.builder()
                .region(region)
                .credentialsProvider(InstanceProfileCredentialsProvider.create())
                .build();

        this.s3AsyncClient = S3AsyncClient.builder()
                .region(region)
                .credentialsProvider(InstanceProfileCredentialsProvider.create())
                .build();

        this.s3Presigner = S3Presigner.builder()
                .region(region)
                .credentialsProvider(InstanceProfileCredentialsProvider.create())
                .build();

        this.bucketName = bucketName;
    }
    /**
     * 同步上传文件
     *
     * @param key 对象键（S3中的文件路径）
     * @param file 要上传的文件
     * @return 上传的对象URL
     */
    public String uploadFile(String key, File file) {
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        s3Client.putObject(request, RequestBody.fromFile(file));
        return getObjectUrl(key);
    }

//    /**
//     * 同步上传输入流
//     *
//     * @param key 对象键（S3中的文件路径）
//     * @param inputStream 输入流
//     * @param contentLength 内容长度
//     * @param contentType 内容类型
//     * @return 上传的对象URL
//     */
//    public String uploadInputStream(String key, InputStream inputStream, long contentLength, String contentType) {
//        PutObjectRequest request = PutObjectRequest.builder()
//                .bucket(bucketName)
//                .key(key)
//                .contentType(contentType)
//                .contentLength(contentLength)
//                .build();
//
//        s3Client.putObject(request, RequestBody.fromInputStream(inputStream, contentLength));
//        return getObjectUrl(key);
//    }
//
//    /**
//     * 同步上传字节数组
//     *
//     * @param key 对象键（S3中的文件路径）
//     * @param bytes 字节数组
//     * @param contentType 内容类型
//     * @return 上传的对象URL
//     */
//    public String uploadBytes(String key, byte[] bytes, String contentType) {
//        PutObjectRequest request = PutObjectRequest.builder()
//                .bucket(bucketName)
//                .key(key)
//                .contentType(contentType)
//                .build();
//
//        s3Client.putObject(request, RequestBody.fromBytes(bytes));
//        return getObjectUrl(key);
//    }
//
//    /**
//     * 异步上传文件
//     *
//     * @param key 对象键（S3中的文件路径）
//     * @param file 要上传的文件
//     * @return 包含上传URL的CompletableFuture
//     */
//    public CompletableFuture<String> uploadFileAsync(String key, File file) {
//        PutObjectRequest request = PutObjectRequest.builder()
//                .bucket(bucketName)
//                .key(key)
//                .build();
//
//        return s3AsyncClient.putObject(request, AsyncRequestBody.fromFile(file))
//                .thenApply(response -> getObjectUrl(key));
//    }
//
//    /**
//     * 异步上传字节数组
//     *
//     * @param key 对象键（S3中的文件路径）
//     * @param bytes 字节数组
//     * @param contentType 内容类型
//     * @return 包含上传URL的CompletableFuture
//     */
//    public CompletableFuture<String> uploadBytesAsync(String key, byte[] bytes, String contentType) {
//        PutObjectRequest request = PutObjectRequest.builder()
//                .bucket(bucketName)
//                .key(key)
//                .contentType(contentType)
//                .build();
//
//        return s3AsyncClient.putObject(request, AsyncRequestBody.fromBytes(bytes))
//                .thenApply(response -> getObjectUrl(key));
//    }
//
//    /**
//     * 同步下载对象到文件
//     *
//     * @param key 对象键
//     * @param file 目标文件
//     */
//    public void downloadFile(String key, File file) {
//        GetObjectRequest request = GetObjectRequest.builder()
//                .bucket(bucketName)
//                .key(key)
//                .build();
//
//        s3Client.getObject(request, Path.of(file.toURI()));
//    }
//
//    /**
//     * 异步下载对象
//     *
//     * @param key 对象键
//     * @return 包含对象字节数组的CompletableFuture
//     */
//    public CompletableFuture<byte[]> downloadBytesAsync(String key) {
//        GetObjectRequest request = GetObjectRequest.builder()
//                .bucket(bucketName)
//                .key(key)
//                .build();
//
//        return s3AsyncClient.getObject(request, AsyncResponseTransformer.toBytes());
//    }
//
//    /**
//     * 异步下载对象到文件
//     *
//     * @param key 对象键
//     * @param file 目标文件
//     * @return 包含结果的CompletableFuture
//     */
//    public CompletableFuture<Void> downloadFileAsync(String key, File file) {
//        GetObjectRequest request = GetObjectRequest.builder()
//                .bucket(bucketName)
//                .key(key)
//                .build();
//
//        return s3AsyncClient.getObject(request, AsyncResponseTransformer.toFile(file.toPath()));
//    }
//
//    /**
//     * 同步删除对象
//     *
//     * @param key 对象键
//     */
//    public void deleteObject(String key) {
//        DeleteObjectRequest request = DeleteObjectRequest.builder()
//                .bucket(bucketName)
//                .key(key)
//                .build();
//
//        s3Client.deleteObject(request);
//    }
//
//    /**
//     * 异步删除对象
//     *
//     * @param key 对象键
//     * @return 包含结果的CompletableFuture
//     */
//    public CompletableFuture<DeleteObjectResponse> deleteObjectAsync(String key) {
//        DeleteObjectRequest request = DeleteObjectRequest.builder()
//                .bucket(bucketName)
//                .key(key)
//                .build();
//
//        return s3AsyncClient.deleteObject(request);
//    }
//
//    /**
//     * 同步批量删除对象
//     *
//     * @param keys 对象键列表
//     * @return 删除结果
//     */
//    public DeleteObjectsResponse deleteObjects(List<String> keys) {
//        List<ObjectIdentifier> objectIds = keys.stream()
//                .map(key -> ObjectIdentifier.builder().key(key).build())
//                .collect(Collectors.toList());
//
//        DeleteObjectsRequest request = DeleteObjectsRequest.builder()
//                .bucket(bucketName)
//                .delete(Delete.builder().objects(objectIds).build())
//                .build();
//
//        return s3Client.deleteObjects(request);
//    }
//
//    /**
//     * 异步批量删除对象
//     *
//     * @param keys 对象键列表
//     * @return 包含删除结果的CompletableFuture
//     */
//    public CompletableFuture<DeleteObjectsResponse> deleteObjectsAsync(List<String> keys) {
//        List<ObjectIdentifier> objectIds = keys.stream()
//                .map(key -> ObjectIdentifier.builder().key(key).build())
//                .collect(Collectors.toList());
//
//        DeleteObjectsRequest request = DeleteObjectsRequest.builder()
//                .bucket(bucketName)
//                .delete(Delete.builder().objects(objectIds).build())
//                .build();
//
//        return s3AsyncClient.deleteObjects(request);
//    }
//
//    /**
//     * 判断对象是否存在
//     *
//     * @param key 对象键
//     * @return 如果对象存在返回true，否则返回false
//     */
//    public boolean doesObjectExist(String key) {
//        try {
//            HeadObjectRequest request = HeadObjectRequest.builder()
//                    .bucket(bucketName)
//                    .key(key)
//                    .build();
//
//            s3Client.headObject(request);
//            return true;
//        } catch (NoSuchKeyException e) {
//            return false;
//        }
//    }
//
//    /**
//     * 生成下载预签名URL
//     *
//     * @param key 对象键
//     * @param expirationInSeconds URL有效期（秒）
//     * @return 预签名URL
//     */
//    public String generatePresignedDownloadUrl(String key, long expirationInSeconds) {
//        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
//                .bucket(bucketName)
//                .key(key)
//                .build();
//
//        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
//                .signatureDuration(Duration.ofSeconds(expirationInSeconds))
//                .getObjectRequest(getObjectRequest)
//                .build();
//
//        PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(presignRequest);
//        return presignedRequest.url().toString();
//    }
//
//    /**
//     * 生成上传预签名URL
//     *
//     * @param key 对象键
//     * @param expirationInSeconds URL有效期（秒）
//     * @return 预签名URL
//     */
//    public String generatePresignedUploadUrl(String key, long expirationInSeconds) {
//        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
//                .bucket(bucketName)
//                .key(key)
//                .build();
//
//        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
//                .signatureDuration(Duration.ofSeconds(expirationInSeconds))
//                .putObjectRequest(putObjectRequest)
//                .build();
//
//        PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(presignRequest);
//        return presignedRequest.url().toString();
//    }
//
//    /**
//     * 为对象设置元数据
//     *
//     * @param key 对象键
//     * @param metadata 元数据Map
//     */
//    public void setObjectMetadata(String key, Map<String, String> metadata) {
//        CopyObjectRequest request = CopyObjectRequest.builder()
//                .sourceBucket(bucketName)
//                .sourceKey(key)
//                .destinationBucket(bucketName)
//                .destinationKey(key)
//                .metadata(metadata)
//                .metadataDirective(MetadataDirective.REPLACE)
//                .build();
//
//        s3Client.copyObject(request);
//    }
//
//    /**
//     * 获取对象元数据
//     *
//     * @param key 对象键
//     * @return 对象元数据
//     */
//    public HeadObjectResponse getObjectMetadata(String key) {
//        HeadObjectRequest request = HeadObjectRequest.builder()
//                .bucket(bucketName)
//                .key(key)
//                .build();
//
//        return s3Client.headObject(request);
//    }
//
    /**
     * 获取对象URL
     *
     * @param key 对象键
     * @return 对象URL
     */
    public String getObjectUrl(String key) {
        return String.format("https://%s.s3.amazonaws.com/%s", bucketName, key);
    }
//
//    /**
//     * 复制对象
//     *
//     * @param sourceKey 源对象键
//     * @param destinationKey 目标对象键
//     * @return 复制的对象URL
//     */
//    public String copyObject(String sourceKey, String destinationKey) {
//        CopyObjectRequest request = CopyObjectRequest.builder()
//                .sourceBucket(bucketName)
//                .sourceKey(sourceKey)
//                .destinationBucket(bucketName)
//                .destinationKey(destinationKey)
//                .build();
//
//        s3Client.copyObject(request);
//        return getObjectUrl(destinationKey);
//    }
//
    /**
     * 关闭资源
     */
    public void close() {
        if (s3Client != null) {
            s3Client.close();
        }
        if (s3AsyncClient != null) {
            s3AsyncClient.close();
        }
        if (s3Presigner != null) {
            s3Presigner.close();
        }
    }
}