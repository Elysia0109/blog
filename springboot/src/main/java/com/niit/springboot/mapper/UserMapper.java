package com.niit.springboot.mapper;

import com.niit.springboot.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper {
    // 插入用户
    int insert(User user);

    // 根据ID查询
    User selectById(Integer id);

    // 根据账号查询
    User selectByAccount(String account);

    // 更新用户信息
    int update(User user);

    // 逻辑删除
    int deleteById(Integer id);

    // 检查账号是否存在
    int countByAccount(String account);

    // 修改密码
    int updatePassword(@Param("id") Integer id, @Param("password") String password);
}