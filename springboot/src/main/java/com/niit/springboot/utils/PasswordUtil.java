package com.niit.springboot.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class PasswordUtil {

    /**
     * 使用 SHA-256 算法生成密码的哈希值
     * @param password 原始密码
     * @return 十六进制字符串形式的哈希值
     */
    public static String encode(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedHash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(encodedHash);
        } catch (NoSuchAlgorithmException e) {
            // 理论上SHA-256是Java标准算法，不会走到这里
            throw new RuntimeException("加密算法不可用", e);
        }
    }

    /**
     * 验证密码：比较原始密码的哈希值是否与存储的哈希值匹配
     * @param rawPassword 用户输入的原始密码
     * @param encodedPassword 数据库存储的已加密密码
     * @return 匹配返回 true，否则 false
     */
    public static boolean matches(String rawPassword, String encodedPassword) {
        String hashedInput = encode(rawPassword);
        return hashedInput.equals(encodedPassword);
    }

    /**
     * 将字节数组转换为十六进制字符串
     */
    private static String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}