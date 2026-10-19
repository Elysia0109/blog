// OSSConfig.java
package com.niit.springboot.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "aliyun.oss")
public class OSSConfig {
    private String endpoint;        // 如：oss-cn-guangzhou.aliyuncs.com
    private String accessKeyId;     // AccessKey ID
    private String accessKeySecret; // AccessKey Secret
    private String bucketName;      // 存储桶名称，如：niit-pro
    private String host;            // 访问域名，如：https://niit-pro.oss-cn-guangzhou.aliyuncs.com

    // 获取完整的文件访问URL
    public String getFileUrl(String fileName) {
        return host + "/" + fileName;
    }
}