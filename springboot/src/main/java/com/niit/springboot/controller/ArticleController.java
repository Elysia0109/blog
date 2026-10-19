package com.niit.springboot.controller;
import com.niit.springboot.dto.response.ApiResponse;
import com.niit.springboot.dto.response.ArticleDetailResponse;
import com.niit.springboot.dto.response.ArticleListResponse;
import com.niit.springboot.dto.request.ArticleRequest;
import com.niit.springboot.dto.request.CollectRequest;  // 新增
import com.niit.springboot.dto.response.ApiResponse;
import com.niit.springboot.dto.response.ArticleDetailResponse;
import com.niit.springboot.dto.response.ArticleListResponse;
import com.niit.springboot.service.ArticleService;
import com.niit.springboot.service.FavoriteService;
import com.niit.springboot.service.LikeService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/articles")
@RequiredArgsConstructor
public class ArticleController {

    private final ArticleService articleService;
    private final FavoriteService favoriteService;
    private final LikeService likeService;
    private static final Logger logger = LoggerFactory.getLogger(ArticleController.class);

    // 创建文章
    @PostMapping
    public ApiResponse<Map<String, Object>> createArticle(@RequestBody ArticleRequest request,
                                                          HttpServletRequest httpRequest) {
        logger.debug("创建文章: {}", request.getTitle());
        Integer authorId = getUserIdFromRequest(httpRequest);
        logger.debug("获取到作者ID: {}", authorId);
        if (authorId == null) {
            logger.debug("未找到作者ID，返回401");
            return ApiResponse.error(401, "用户未登录或登录已过期");
        }
        Integer articleId = articleService.createArticle(request, authorId);

        Map<String, Object> data = new HashMap<>();
        data.put("id", articleId);
        data.put("title", request.getTitle());
        data.put("message", "文章创建成功");
        return ApiResponse.success(data);
    }

    // 获取文章列表（支持多种查询：全部、分类、搜索、收藏、浏览记录）
    @GetMapping
    public ApiResponse<Map<String, Object>> getArticles(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean favorites,
            @RequestParam(required = false) Boolean record,
            HttpServletRequest request) {

        Integer userId = getUserIdFromRequest(request);
        Map<String, Object> data = new HashMap<>();
        List<ArticleListResponse> articles;
        Integer total;

        logger.debug("获取文章列表，参数: page={}, size={}, category={}, keyword={}, favorites={}, record={}, userId={}",
                page, size, category, keyword, favorites, record, userId);

        if (favorites != null && favorites && userId != null) {
            // 收藏文章
            articles = articleService.getFavoriteArticles(userId, page, size);
            total = favoriteService.getUserFavoriteCount(userId);
            logger.debug("获取收藏文章: {}篇", articles.size());
        } else if (record != null && record && userId != null) {
            // 浏览记录
            articles = articleService.getBrowseRecords(userId, page, size);
            total = articleService.countBrowseRecords(userId);
            logger.debug("获取浏览记录: {}篇", articles.size());
        } else if (category != null && !category.isEmpty()) {
            // 分类文章
            articles = articleService.getArticlesByCategory(category, page, size);
            total = articleService.countArticlesByCategory(category);
            logger.debug("获取分类[{}]文章: {}篇", category, articles.size());
        } else if (keyword != null && !keyword.isEmpty()) {
            // 搜索文章
            articles = articleService.searchArticles(keyword, page, size);
            total = articleService.countSearchArticles(keyword);
            logger.debug("搜索[{}]文章: {}篇", keyword, articles.size());
        } else {
            // 全部文章
            articles = articleService.getArticleList(page, size);
            total = articleService.countArticles();
            logger.debug("获取全部文章: {}篇", articles.size());
        }

        // 只要用户已登录，就返回收藏状态
        if (userId != null) {
            List<Integer> favoriteIds = favoriteService.getUserFavorites(userId);
            data.put("collectedIds", favoriteIds);
        }

        data.put("list", articles);
        data.put("total", total);
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

        logger.debug("获取我的文章，page: {}, size: {}", page, size);
        Integer userId = getUserIdFromRequest(request);
        logger.debug("获取到用户ID: {}", userId);
        if (userId == null) {
            logger.debug("未找到用户ID，返回401");
            return ApiResponse.error(401, "用户未登录或登录已过期");
        }

        Map<String, Object> data = new HashMap<>();
        data.put("list", articleService.getUserArticles(userId, page, size));
        data.put("total", articleService.countArticlesByAuthor(userId));
        data.put("page", page);
        data.put("size", size);

        logger.debug("我的文章获取成功，共{}篇", data.get("total"));
        return ApiResponse.success(data);
    }

    // 获取文章详情
    @GetMapping("/{id}")
    public ApiResponse<ArticleDetailResponse> getArticleById(@PathVariable Integer id,
                                                             HttpServletRequest httpRequest) {
        logger.debug("获取文章详情，ID: {}", id);

        Integer userId = getUserIdFromRequest(httpRequest);
        ArticleDetailResponse article = articleService.getArticleDetail(id);

        // 添加浏览记录
        if (userId != null) {
            try {
                articleService.addBrowseRecord(userId, id);
                logger.debug("为用户{}添加文章{}的浏览记录", userId, id);
            } catch (Exception e) {
                logger.warn("添加浏览记录失败: {}", e.getMessage());
            }
        }

        // 设置收藏和点赞状态
        if (userId != null) {
            boolean isFavorite = favoriteService.isFavorite(userId, id);
            boolean isLiked = likeService.isLiked(id, userId);
            article.setIsFavorite(isFavorite);
            article.setIsLiked(isLiked);
            logger.debug("用户{}对文章{}的收藏状态: {}, 点赞状态: {}", userId, id, isFavorite, isLiked);
        } else {
            article.setIsFavorite(false);
            article.setIsLiked(false);
        }

        logger.debug("文章详情获取成功: {}", article.getTitle());
        return ApiResponse.success(article);
    }

    // 更新文章
    @PutMapping("/{id}")
    public ApiResponse<Map<String, Object>> updateArticle(
            @PathVariable Integer id,
            @RequestBody ArticleRequest request,
            HttpServletRequest httpRequest) {

        logger.debug("更新文章，ID: {}, 标题: {}", id, request.getTitle());
        Integer authorId = getUserIdFromRequest(httpRequest);
        if (authorId == null) {
            return ApiResponse.error(401, "用户未登录或登录已过期");
        }

        articleService.updateArticle(id, request, authorId);

        Map<String, Object> data = new HashMap<>();
        data.put("id", id);
        data.put("title", request.getTitle());
        data.put("message", "文章更新成功");

        logger.debug("文章更新成功，ID: {}", id);
        return ApiResponse.success(data);
    }

    // 删除文章
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteArticle(@PathVariable Integer id,
                                           HttpServletRequest httpRequest) {
        logger.debug("删除文章，ID: {}", id);
        Integer authorId = getUserIdFromRequest(httpRequest);
        if (authorId == null) {
            return ApiResponse.error(401, "用户未登录或登录已过期");
        }

        articleService.deleteArticle(id, authorId);

        logger.debug("文章删除成功，ID: {}", id);
        return ApiResponse.success();
    }

    // 增加阅读量
    @PostMapping("/{id}/view")
    public ApiResponse<Void> incrementViewCount(@PathVariable Integer id) {
        logger.debug("增加文章阅读量，ID: {}", id);
        articleService.incrementViewCount(id);
        return ApiResponse.success();
    }

    // 点赞文章
    @PostMapping("/{id}/like")
    public ApiResponse<Void> likeArticle(@PathVariable Integer id,
                                         HttpServletRequest request) {
        logger.debug("点赞文章，ID: {}", id);
        Integer userId = getUserIdFromRequest(request);
        if (userId == null) {
            return ApiResponse.error(401, "请先登录");
        }

        articleService.likeArticle(id, userId);

        logger.debug("用户{}点赞文章{}", userId, id);
        return ApiResponse.success();
    }

    // 取消点赞
    @DeleteMapping("/{id}/like")
    public ApiResponse<Void> cancelLike(@PathVariable Integer id,
                                        HttpServletRequest request) {
        logger.debug("取消点赞文章，ID: {}", id);
        Integer userId = getUserIdFromRequest(request);
        if (userId == null) {
            return ApiResponse.error(401, "请先登录");
        }

        articleService.cancelLike(id, userId);

        logger.debug("用户{}取消点赞文章{}", userId, id);
        return ApiResponse.success();
    }

    // 收藏文章
    @PostMapping("/{id}/collect")
    public ApiResponse<Void> collectArticle(@PathVariable Integer id,
                                            HttpServletRequest request) {
        logger.debug("收藏文章，文章ID: {}", id);
        Integer userId = getUserIdFromRequest(request);
        if (userId == null) {
            return ApiResponse.error(401, "请先登录");
        }

        favoriteService.addFavorite(userId, id);

        logger.debug("用户{}收藏文章{}", userId, id);
        return ApiResponse.success();
    }

    // 取消收藏
    @DeleteMapping("/{id}/collect")
    public ApiResponse<Void> cancelCollect(@PathVariable Integer id,
                                           HttpServletRequest request) {
        logger.debug("取消收藏文章，文章ID: {}", id);
        Integer userId = getUserIdFromRequest(request);
        if (userId == null) {
            return ApiResponse.error(401, "请先登录");
        }

        favoriteService.removeFavorite(userId, id);

        logger.debug("用户{}取消收藏文章{}", userId, id);
        return ApiResponse.success();
    }

    // 置顶/取消置顶文章
    @PostMapping("/{id}/toggle-top")
    public ApiResponse<Void> toggleTop(@PathVariable Integer id,
                                       HttpServletRequest request) {
        logger.debug("置顶/取消置顶文章，ID: {}", id);
        Integer userId = getUserIdFromRequest(request);
        if (userId == null) {
            return ApiResponse.error(401, "请先登录");
        }

        articleService.toggleTop(id, userId);

        logger.debug("用户{}操作文章{}置顶状态", userId, id);
        return ApiResponse.success();
    }

    // 获取文章统计数据
    @GetMapping("/{id}/stats")
    public ApiResponse<Map<String, Object>> getArticleStats(@PathVariable Integer id,
                                                            HttpServletRequest request) {
        logger.debug("获取文章统计数据，ID: {}", id);

        Integer userId = getUserIdFromRequest(request);
        Map<String, Object> stats = new HashMap<>();

        // 获取点赞数
        int likeCount = likeService.getArticleLikeCount(id);
        stats.put("likeCount", likeCount);

        // 获取收藏数
        int favoriteCount = favoriteService.getUserFavoriteCount(id);
        stats.put("favoriteCount", favoriteCount);

        // 获取是否已点赞和收藏
        if (userId != null) {
            boolean isLiked = likeService.isLiked(id, userId);
            boolean isFavorite = favoriteService.isFavorite(userId, id);
            stats.put("isLiked", isLiked);
            stats.put("isFavorite", isFavorite);
        }

        logger.debug("文章{}统计数据: {}", id, stats);
        return ApiResponse.success(stats);
    }

    // 搜索建议
    @GetMapping("/search/suggest")
    public ApiResponse<List<String>> searchSuggest(@RequestParam String keyword) {
        logger.debug("搜索建议，关键词: {}", keyword);
        // 这里可以返回热门搜索词或历史搜索
        return ApiResponse.success(List.of());
    }

    // 热门文章
    @GetMapping("/hot")
    public ApiResponse<List<ArticleListResponse>> getHotArticles(
            @RequestParam(defaultValue = "10") Integer limit) {
        logger.debug("获取热门文章，限制: {}篇", limit);
        // 这里可以根据阅读量、点赞数等获取热门文章
        List<ArticleListResponse> hotArticles = articleService.getArticleList(1, limit);
        return ApiResponse.success(hotArticles);
    }

    // 从请求属性中获取用户ID（由JwtInterceptor设置）
    private Integer getUserIdFromRequest(HttpServletRequest request) {
        Object userId = request.getAttribute("userId");
        return userId != null ? (Integer) userId : null;
    }

    // 内部类：收藏请求DTO
    @Data
    static class CollectRequest {
        private Integer articleId;
    }
}