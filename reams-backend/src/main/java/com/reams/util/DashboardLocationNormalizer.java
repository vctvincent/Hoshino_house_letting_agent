package com.reams.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class DashboardLocationNormalizer {

    private static final String UNKNOWN = "\u672a\u6807\u6ce8";
    private static final Map<String, String> CITY_TO_PROVINCE = new HashMap<>();
    private static final Map<String, String> PROVINCE_ALIASES = new HashMap<>();

    static {
        addProvinceAliases("\u5317\u4eac\u5e02", "\u5317\u4eac", "\u5317\u4eac\u5e02");
        addProvinceAliases("\u4e0a\u6d77\u5e02", "\u4e0a\u6d77", "\u4e0a\u6d77\u5e02");
        addProvinceAliases("\u5929\u6d25\u5e02", "\u5929\u6d25", "\u5929\u6d25\u5e02");
        addProvinceAliases("\u91cd\u5e86\u5e02", "\u91cd\u5e86", "\u91cd\u5e86\u5e02");
        addProvinceAliases("\u6cb3\u5317\u7701", "\u6cb3\u5317", "\u6cb3\u5317\u7701");
        addProvinceAliases("\u5c71\u897f\u7701", "\u5c71\u897f", "\u5c71\u897f\u7701");
        addProvinceAliases("\u8fbd\u5b81\u7701", "\u8fbd\u5b81", "\u8fbd\u5b81\u7701");
        addProvinceAliases("\u5409\u6797\u7701", "\u5409\u6797", "\u5409\u6797\u7701");
        addProvinceAliases("\u9ed1\u9f99\u6c5f\u7701", "\u9ed1\u9f99\u6c5f", "\u9ed1\u9f99\u6c5f\u7701");
        addProvinceAliases("\u6c5f\u82cf\u7701", "\u6c5f\u82cf", "\u6c5f\u82cf\u7701");
        addProvinceAliases("\u6d59\u6c5f\u7701", "\u6d59\u6c5f", "\u6d59\u6c5f\u7701");
        addProvinceAliases("\u5b89\u5fbd\u7701", "\u5b89\u5fbd", "\u5b89\u5fbd\u7701");
        addProvinceAliases("\u798f\u5efa\u7701", "\u798f\u5efa", "\u798f\u5efa\u7701");
        addProvinceAliases("\u6c5f\u897f\u7701", "\u6c5f\u897f", "\u6c5f\u897f\u7701");
        addProvinceAliases("\u5c71\u4e1c\u7701", "\u5c71\u4e1c", "\u5c71\u4e1c\u7701");
        addProvinceAliases("\u6cb3\u5357\u7701", "\u6cb3\u5357", "\u6cb3\u5357\u7701");
        addProvinceAliases("\u6e56\u5317\u7701", "\u6e56\u5317", "\u6e56\u5317\u7701");
        addProvinceAliases("\u6e56\u5357\u7701", "\u6e56\u5357", "\u6e56\u5357\u7701");
        addProvinceAliases("\u5e7f\u4e1c\u7701", "\u5e7f\u4e1c", "\u5e7f\u4e1c\u7701");
        addProvinceAliases("\u6d77\u5357\u7701", "\u6d77\u5357", "\u6d77\u5357\u7701");
        addProvinceAliases("\u56db\u5ddd\u7701", "\u56db\u5ddd", "\u56db\u5ddd\u7701");
        addProvinceAliases("\u8d35\u5dde\u7701", "\u8d35\u5dde", "\u8d35\u5dde\u7701");
        addProvinceAliases("\u4e91\u5357\u7701", "\u4e91\u5357", "\u4e91\u5357\u7701");
        addProvinceAliases("\u9655\u897f\u7701", "\u9655\u897f", "\u9655\u897f\u7701");
        addProvinceAliases("\u7518\u8083\u7701", "\u7518\u8083", "\u7518\u8083\u7701");
        addProvinceAliases("\u9752\u6d77\u7701", "\u9752\u6d77", "\u9752\u6d77\u7701");
        addProvinceAliases("\u53f0\u6e7e\u7701", "\u53f0\u6e7e", "\u53f0\u6e7e\u7701");
        addProvinceAliases("\u5185\u8499\u53e4\u81ea\u6cbb\u533a", "\u5185\u8499\u53e4", "\u5185\u8499\u53e4\u81ea\u6cbb\u533a");
        addProvinceAliases("\u5e7f\u897f\u58ee\u65cf\u81ea\u6cbb\u533a", "\u5e7f\u897f", "\u5e7f\u897f\u58ee\u65cf\u81ea\u6cbb\u533a");
        addProvinceAliases("\u897f\u85cf\u81ea\u6cbb\u533a", "\u897f\u85cf", "\u897f\u85cf\u81ea\u6cbb\u533a");
        addProvinceAliases("\u5b81\u590f\u56de\u65cf\u81ea\u6cbb\u533a", "\u5b81\u590f", "\u5b81\u590f\u56de\u65cf\u81ea\u6cbb\u533a");
        addProvinceAliases("\u65b0\u7586\u7ef4\u543e\u5c14\u81ea\u6cbb\u533a", "\u65b0\u7586", "\u65b0\u7586\u7ef4\u543e\u5c14\u81ea\u6cbb\u533a");
        addProvinceAliases("\u9999\u6e2f\u7279\u522b\u884c\u653f\u533a", "\u9999\u6e2f", "\u9999\u6e2f\u7279\u522b\u884c\u653f\u533a");
        addProvinceAliases("\u6fb3\u95e8\u7279\u522b\u884c\u653f\u533a", "\u6fb3\u95e8", "\u6fb3\u95e8\u7279\u522b\u884c\u653f\u533a");

        addCityProvince("\u5317\u4eac\u5e02", "\u5317\u4eac", "\u5317\u4eac\u5e02");
        addCityProvince("\u4e0a\u6d77\u5e02", "\u4e0a\u6d77", "\u4e0a\u6d77\u5e02");
        addCityProvince("\u5929\u6d25\u5e02", "\u5929\u6d25", "\u5929\u6d25\u5e02");
        addCityProvince("\u91cd\u5e86\u5e02", "\u91cd\u5e86", "\u91cd\u5e86\u5e02");
        addCityProvince("\u5e7f\u4e1c\u7701", "\u5e7f\u5dde", "\u5e7f\u5dde\u5e02", "\u6df1\u5733", "\u6df1\u5733\u5e02");
        addCityProvince("\u56db\u5ddd\u7701", "\u6210\u90fd", "\u6210\u90fd\u5e02");
        addCityProvince("\u6d59\u6c5f\u7701", "\u676d\u5dde", "\u676d\u5dde\u5e02");
        addCityProvince("\u6c5f\u82cf\u7701", "\u5357\u4eac", "\u5357\u4eac\u5e02", "\u82cf\u5dde", "\u82cf\u5dde\u5e02");
        addCityProvince("\u6e56\u5317\u7701", "\u6b66\u6c49", "\u6b66\u6c49\u5e02");
        addCityProvince("\u9655\u897f\u7701", "\u897f\u5b89", "\u897f\u5b89\u5e02");
        addCityProvince("\u6e56\u5357\u7701", "\u957f\u6c99", "\u957f\u6c99\u5e02");
        addCityProvince("\u5c71\u4e1c\u7701", "\u9752\u5c9b", "\u9752\u5c9b\u5e02");
    }

    private DashboardLocationNormalizer() {
    }

    public static List<Map<String, Object>> normalizeRows(List<Map<String, Object>> rows) {
        List<Map<String, Object>> normalized = new ArrayList<>();
        if (rows == null) {
            return normalized;
        }

        for (Map<String, Object> row : rows) {
            Map<String, Object> item = new LinkedHashMap<>(row);
            String province = asText(row.get("province"));
            String city = asText(row.get("city"));

            item.put("province", normalizeProvince(province, city));
            item.put("city", normalizeCity(city));
            normalized.add(item);
        }
        return normalized;
    }

    static String normalizeProvince(String province, String city) {
        String cleanProvince = cleanText(province);
        if (isValidProvince(cleanProvince)) {
            return normalizeProvinceAlias(cleanProvince);
        }

        String inferredProvince = CITY_TO_PROVINCE.get(cleanText(city));
        if (inferredProvince != null && !inferredProvince.isEmpty()) {
            return normalizeProvinceAlias(inferredProvince);
        }

        if (!cleanProvince.isEmpty()) {
            return normalizeProvinceAlias(cleanProvince);
        }
        return UNKNOWN;
    }

    static String normalizeCity(String city) {
        String cleanCity = cleanText(city);
        if (cleanCity.isEmpty()) {
            return UNKNOWN;
        }

        if (cleanCity.endsWith("\u5e02") && cleanCity.length() > 1) {
            return cleanCity.substring(0, cleanCity.length() - 1);
        }

        return cleanCity;
    }

    private static boolean isValidProvince(String province) {
        return !province.isEmpty() && !province.matches("^\\d{4,}$");
    }

    private static String cleanText(String value) {
        return value == null ? "" : value.trim();
    }

    private static String normalizeProvinceAlias(String province) {
        return PROVINCE_ALIASES.getOrDefault(province, province);
    }

    private static String asText(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private static void addProvinceAliases(String canonical, String... aliases) {
        for (String alias : aliases) {
            PROVINCE_ALIASES.put(alias, canonical);
        }
    }

    private static void addCityProvince(String canonicalProvince, String... cities) {
        for (String city : cities) {
            CITY_TO_PROVINCE.put(city, canonicalProvince);
        }
    }
}
