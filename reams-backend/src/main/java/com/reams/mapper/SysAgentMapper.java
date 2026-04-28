package com.reams.mapper;

import com.reams.entity.SysAgent;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SysAgentMapper {

    SysAgent selectByPhone(@Param("phone") String phone);

    List<SysAgent> selectAll();

    List<SysAgent> selectPage(@Param("offset") Integer offset, @Param("limit") Integer limit);

    List<SysAgent> selectPageWithSearch(
            @Param("offset") Integer offset,
            @Param("limit") Integer limit,
            @Param("keyword") String keyword,
            @Param("status") Integer status
    );

    SysAgent selectById(@Param("id") Long id);

    int insert(SysAgent agent);

    int update(SysAgent agent);

    int deleteById(@Param("id") Long id);

    long count();

    long countWithSearch(
            @Param("keyword") String keyword,
            @Param("status") Integer status
    );

    long countPendingAgents();
}
