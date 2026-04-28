package com.reams.mapper;

import com.reams.entity.Viewing;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface ViewingMapper {

    List<Viewing> selectPage(Map<String, Object> params);

    Viewing selectById(@Param("id") Long id);

    List<Viewing> selectByCustomerId(@Param("customerId") Long customerId);

    List<Viewing> selectByAgentId(@Param("agentId") Long agentId);

    List<Map<String, Object>> selectDistinctCustomersByAgentId(@Param("agentId") Long agentId);

    List<Viewing> selectByHouseId(@Param("houseId") Long houseId);

    int insert(Viewing viewing);

    int update(Viewing viewing);

    int deleteById(@Param("id") Long id);

    long count(Map<String, Object> params);
}
