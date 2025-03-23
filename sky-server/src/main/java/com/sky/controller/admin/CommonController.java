package com.sky.controller.admin;

import com.sky.constant.MessageConstant;
import com.sky.result.Result;
import com.sky.utils.S3Util;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/admin/common")
@Api(tags = "common api")
@Slf4j
public class CommonController {

    private final S3Util s3Util;

    @Autowired
    public CommonController(S3Util s3Util) {
        this.s3Util = s3Util;
    }

    @PostMapping("/upload")
    @ApiOperation("upload file")
    public Result<String> upload(MultipartFile file){


        try{
            // 创建临时文件
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String fileName = UUID.randomUUID().toString() + extension;
            String key = "uploads/" + fileName;

            File tempFile = File.createTempFile("temp", extension);
            file.transferTo(tempFile);

            // 上传到S3
            String fileUrl = s3Util.uploadFile(key, tempFile);

            // 删除临时文件
            tempFile.delete();
            return Result.success(fileUrl);
        } catch (IOException e) {
            log.error("upload file failed: {}", e.getMessage());
        }
        return Result.error(MessageConstant.UPLOAD_FAILED);
    }

//    @PostMapping("/upload-async")
//    public CompletableFuture<ResponseEntity<String>> uploadFileAsync(
//            @RequestParam("file") MultipartFile file) {
//        try {
//            // 创建临时文件
//            String originalFilename = file.getOriginalFilename();
//            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
//            String fileName = UUID.randomUUID().toString() + extension;
//            String key = "uploads/" + fileName;
//
//            byte[] bytes = file.getBytes();
//
//            // 异步上传到S3
//            return s3Util.uploadBytesAsync(key, bytes, file.getContentType())
//                    .thenApply(fileUrl -> ResponseEntity.ok(fileUrl))
//                    .exceptionally(e -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                            .body("上传失败: " + e.getMessage()));
//        } catch (Exception e) {
//            CompletableFuture<ResponseEntity<String>> future = new CompletableFuture<>();
//            future.complete(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body("上传失败: " + e.getMessage()));
//            return future;
//        }
//    }
//
//    @GetMapping("/{fileName}")
//    public ResponseEntity<String> getFileUrl(@PathVariable String fileName) {
//        try {
//            String key = "uploads/" + fileName;
//            if (!s3Util.doesObjectExist(key)) {
//                return ResponseEntity.notFound().build();
//            }
//
//            String presignedUrl = s3Util.generatePresignedDownloadUrl(key, 3600);
//            return ResponseEntity.ok(presignedUrl);
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body("获取文件URL失败: " + e.getMessage());
//        }
//    }
//
//    @DeleteMapping("/{fileName}")
//    public ResponseEntity<String> deleteFile(@PathVariable String fileName) {
//        try {
//            String key = "uploads/" + fileName;
//            s3Util.deleteObject(key);
//            return ResponseEntity.ok("文件删除成功");
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body("删除失败: " + e.getMessage());
//        }
//    }
//
//    @GetMapping("/metadata/{fileName}")
//    public ResponseEntity<Map<String, String>> getFileMetadata(@PathVariable String fileName) {
//        try {
//            String key = "uploads/" + fileName;
//            var metadata = s3Util.getObjectMetadata(key);
//            return ResponseEntity.ok(metadata.metadata());
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
//        }
//    }
}
