package com.reams.mapper;

import com.reams.entity.HouseAudit;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface HouseAuditMapper {

    HouseAudit selectById(@Param("id") Long id);

    HouseAudit selectByHouseId(@Param("houseId") Long houseId);

    List<HouseAudit> selectAll();

    int insert(HouseAudit audit);

    int update(HouseAudit audit);

    int deleteById(@Param("id") Long id);
}
