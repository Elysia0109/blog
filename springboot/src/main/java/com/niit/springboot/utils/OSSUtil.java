// OSSUtil.java
package com.niit.springboot.utils;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.CannedAccessControlList;
import com.aliyun.oss.model.PutObjectRequest;
import com.niit.springboot.config.OSSConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class OSSUtil {

    private final OSSConfig ossConfig;

    /**
     * 上传文件到阿里云OSS
     * @param file 文件
     * @param folder 文件夹路径，如 "avatars/"
     * @return 文件访问URL
     */
    public String uploadFile(MultipartFile file, String folder) {
        // 创建OSSClient实例
        OSS ossClient = null;
        try {
            ossClient = new OSSClientBuilder().build(
                    ossConfig.getEndpoint(),
                    ossConfig.getAccessKeyId(),
                    ossConfig.getAccessKeySecret()
            );

            // 生成文件名
            String originalFilename = file.getOriginalFilename();
            String fileExtension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String fileName = folder + UUID.randomUUID().toString().replace("-", "") + fileExtension;

            // 上传文件
            InputStream inputStream = file.getInputStream();
            PutObjectRequest putObjectRequest = new PutObjectRequest(
                    ossConfig.getBucketName(),
                    fileName,
                    inputStream
            );
            
            ossClient.putObject(putObjectRequest);

            // 设置文件访问权限为公共读取
            ossClient.setObjectAcl(ossConfig.getBucketName(), fileName, CannedAccessControlList.PublicRead);

            // 返回文件访问URL
            String fileUrl = ossConfig.getFileUrl(fileName);
            log.info("文件上传成功: {}", fileUrl);
            return fileUrl;

        } catch (Exception e) {
            log.error("文件上传失败: {}", e.getMessage(), e);
            throw new RuntimeException("文件上传失败: " + e.getMessage());
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }

    /**
     * 上传字节数组到OSS
     */
    public String uploadBytes(byte[] bytes, String fileName, String folder) {
        OSS ossClient = null;
        try {
            ossClient = new OSSClientBuilder().build(
                    ossConfig.getEndpoint(),
                    ossConfig.getAccessKeyId(),
                    ossConfig.getAccessKeySecret()
            );

            String fullFileName = folder + fileName;
            ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);

            PutObjectRequest putObjectRequest = new PutObjectRequest(
                    ossConfig.getBucketName(),
                    fullFileName,
                    inputStream
            );
            
            ossClient.putObject(putObjectRequest);

            // 设置文件访问权限为公共读取
            ossClient.setObjectAcl(ossConfig.getBucketName(), fullFileName, CannedAccessControlList.PublicRead);

            return ossConfig.getFileUrl(fullFileName);

        } catch (Exception e) {
            log.error("字节数组上传失败: {}", e.getMessage(), e);
            throw new RuntimeException("字节数组上传失败: " + e.getMessage());
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }

    /**
     * 删除OSS文件
     */
    public void deleteFile(String fileUrl) {
        if (fileUrl == null || !fileUrl.contains(ossConfig.getHost())) {
            return;
        }

        // 从完整URL中提取文件名
        String fileName = fileUrl.replace(ossConfig.getHost() + "/", "");

        OSS ossClient = null;
        try {
            ossClient = new OSSClientBuilder().build(
                    ossConfig.getEndpoint(),
                    ossConfig.getAccessKeyId(),
                    ossConfig.getAccessKeySecret()
            );

            ossClient.deleteObject(ossConfig.getBucketName(), fileName);
            log.info("文件删除成功: {}", fileUrl);

        } catch (Exception e) {
            log.error("文件删除失败: {}", e.getMessage(), e);
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }
}