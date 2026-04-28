package com.reams.mapper;

import com.reams.entity.SysCustomer;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SysCustomerMapper {

    SysCustomer selectByPhone(@Param("phone") String phone);

    List<SysCustomer> selectAll();

    List<SysCustomer> selectPage(@Param("offset") Integer offset, @Param("limit") Integer limit);

    List<SysCustomer> selectPageWithSearch(
            @Param("offset") Integer offset,
            @Param("limit") Integer limit,
            @Param("keyword") String keyword,
            @Param("status") Integer status
    );

    SysCustomer selectById(@Param("id") Long id);

    int insert(SysCustomer customer);

    int update(SysCustomer customer);

    int deleteById(@Param("id") Long id);

    long count();

    long countWithSearch(
            @Param("keyword") String keyword,
            @Param("status") Integer status
    );
}
