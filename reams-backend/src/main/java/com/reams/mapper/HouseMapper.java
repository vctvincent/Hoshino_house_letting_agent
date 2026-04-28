package com.reams.mapper;

import com.reams.entity.House;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface HouseMapper {

    List<House> selectPage(Map<String, Object> params);

    List<House> selectPublished();

    House selectById(@Param("id") Long id);

    List<House> selectByAgentId(@Param("agentId") Long agentId);

    int insert(House house);

    int update(House house);

    int deleteById(@Param("id") Long id);

    long count(Map<String, Object> params);

    List<House> selectPendingAudit();

    long countPendingHouses();

    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    int updateAuditStatus(@Param("id") Long id,
                          @Param("auditStatus") Integer auditStatus,
                          @Param("rejectReason") String rejectReason);

    int incrementViewCount(@Param("id") Long id);

    int incrementFavoriteCount(@Param("id") Long id);

    int decrementFavoriteCount(@Param("id") Long id);

    List<House> selectHot(@Param("limit") Integer limit, @Param("city") String city);

    List<String> selectCityList();

    List<String> selectDistrictList(@Param("city") String city);

    List<String> selectDistrictListByCities(@Param("cities") List<String> cities);

    List<Map<String, Object>> selectDistrictInventory(@Param("limit") Integer limit);

    List<Map<String, Object>> selectAgentDistrictInventory(@Param("agentId") Long agentId,
                                                           @Param("limit") Integer limit);
}
