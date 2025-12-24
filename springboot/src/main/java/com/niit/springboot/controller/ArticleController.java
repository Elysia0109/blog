package com.niit.springboot.controller;

import com.niit.springboot.dto.request.ArticleRequest;
import com.niit.springboot.dto.response.ApiResponse;
import com.niit.springboot.service.ArticleService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/articles")
@RequiredArgsConstructor
public class ArticleController {

    private final ArticleService articleService;
    private static final Logger logger = LoggerFactory.getLogger(ArticleController.class);

    // 创建文章
    @PostMapping
    public ApiResponse<Map<String, Object>> createArticle(@RequestBody ArticleRequest request, HttpServletRequest httpRequest) {
        logger.debug("Creating article: {}", request.getTitle());
        Integer authorId = getUserIdFromRequest(httpRequest);
        logger.debug("Extracted authorId: {}", authorId);
        if (authorId == null) {
            logger.debug("No authorId found, returning 401");
            return ApiResponse.error(401, "用户未登录或登录已过期");
        }
        Integer articleId = articleService.createArticle(request, authorId);
        
        Map<String, Object> data = new HashMap<>();
        data.put("id", articleId);
        data.put("title", request.getTitle());
        data.put("message", "文章创建成功");
        return ApiResponse.success(data);
    }

    // 获取文章列表
    @GetMapping
    public ApiResponse<Map<String, Object>> getArticles(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {

        Map<String, Object> data = new HashMap<>();
        data.put("list", articleService.getArticleList(page, size));
        data.put("total", articleService.countArticles());
        data.put("page", page);
        data.put("size", size);

        return ApiResponse.success(data);
    }

    // 获取我的文章
    @GetMapping("/my-articles")
    public ApiResponse<Map<String, Object>> getMyArticles(
            HttpServletRequest request,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {

        logger.debug("Getting my articles, page: {}, size: {}", page, size);
        Integer userId = getUserIdFromRequest(request);
        logger.debug("Extracted userId: {}", userId);
        if (userId == null) {
            logger.debug("No userId found, returning 401");
            return ApiResponse.error(401, "用户未登录或登录已过期");
        }
        Map<String, Object> data = new HashMap<>();
        data.put("list", articleService.getUserArticles(userId, page, size));
        data.put("total", articleService.countArticlesByAuthor(userId));
        data.put("page", page);
        data.put("size", size);

        return ApiResponse.success(data);
    }

    // 获取文章详情
//    @GetMapping("/{id}")
//    public ApiResponse<Map<String, Object>> getArticleById(@PathVariable Integer id) {
//    }

    // 更新文章
    @PutMapping("/{id}")
    public ApiResponse<Map<String, Object>> updateArticle(
            @PathVariable Integer id,
            @RequestBody ArticleRequest request) {

        Map<String, Object> data = new HashMap<>();
        data.put("id", id);
        data.put("title", request.getTitle());
        data.put("message", "文章更新成功");

        return ApiResponse.success(data);
    }

    // 删除文章
    @DeleteMapping("/{id}")
    public ApiResponse<String> deleteArticle(@PathVariable Integer id) {
        // 这里应该添加权限检查
        articleService.deleteArticle(id, 1); // 暂时硬编码userId为1
        return ApiResponse.success("文章删除成功");
    }
    
    // 从请求属性中获取用户ID
    private Integer getUserIdFromRequest(HttpServletRequest request) {
        // 从请求属性获取用户ID（由JwtInterceptor设置）
        Object userId = request.getAttribute("userId");
        return userId != null ? (Integer) userId : null;
    }
}