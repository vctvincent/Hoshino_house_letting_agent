package com.reams.mapper;

import com.reams.entity.SysAdmin;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 管理员Mapper接口
 */
@Mapper
public interface SysAdminMapper {

    /**
     * 根据用户名查询管理员
     */
    SysAdmin selectByName(@Param("name") String name);

    /**
     * 查询所有管理员
     */
    List<SysAdmin> selectAll();

    /**
     * 根据 ID 查询管理员
     */
    SysAdmin selectById(@Param("id") Long id);
    
    /**
     * 根据手机号查询管理员
     */
    SysAdmin selectByPhone(@Param("phone") String phone);

    /**
     * 新增管理员
     */
    int insert(SysAdmin admin);

    /**
     * 更新管理员
     */
    int update(SysAdmin admin);

    /**
     * 删除管理员
     */
    int deleteById(@Param("id") Long id);
}
