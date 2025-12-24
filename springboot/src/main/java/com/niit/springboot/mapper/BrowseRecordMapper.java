package com.niit.springboot.mapper;

import com.niit.springboot.dto.response.ArticleListResponse;
import com.niit.springboot.entity.BrowseRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface BrowseRecordMapper {
    // 插入浏览记录
    int insert(BrowseRecord record);

    // 获取用户最近浏览的文章
    List<ArticleListResponse> selectRecentBrowseArticles(@Param("userId") Integer userId,
                                                         @Param("offset") Integer offset,
                                                         @Param("limit") Integer limit);

    // 统计用户浏览记录数量
    int countByUser(Integer userId);
}