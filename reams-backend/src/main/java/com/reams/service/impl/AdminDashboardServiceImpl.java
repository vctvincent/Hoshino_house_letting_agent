package com.reams.service.impl;

import com.reams.common.result.Result;
import com.reams.entity.House;
import com.reams.entity.SysAgent;
import com.reams.entity.Transaction;
import com.reams.mapper.HouseMapper;
import com.reams.mapper.SysAgentMapper;
import com.reams.mapper.SysCustomerMapper;
import com.reams.mapper.TransactionMapper;
import com.reams.mapper.ViewingMapper;
import com.reams.service.AdminDashboardService;
import com.reams.util.DashboardLocationNormalizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdminDashboardServiceImpl implements AdminDashboardService {

    private static final int MONTH_WINDOW = 6;
    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");

    @Autowired
    private TransactionMapper transactionMapper;

    @Autowired
    private HouseMapper houseMapper;

    @Autowired
    private SysAgentMapper agentMapper;

    @Autowired
    private SysCustomerMapper customerMapper;

    @Autowired
    private ViewingMapper viewingMapper;

    @Override
    public Result<?> getDashboardData(Integer salesWindow) {
        int normalizedWindow = normalizeSalesWindow(salesWindow);
        int offsetMonths = Math.max(normalizedWindow - 1, 0);
        Map<String, Object> transactionOverview = transactionMapper.selectTransactionOverview();

        Map<String, Object> publishedHouseParams = new HashMap<>();
        publishedHouseParams.put("houseStatus", 1);
        publishedHouseParams.put("auditStatus", 2);

        Map<String, Object> completedViewingParams = new HashMap<>();
        completedViewingParams.put("status", 2);

        Map<String, Object> overview = new LinkedHashMap<>();
        overview.put("agentCount", agentMapper.count());
        overview.put("customerCount", customerMapper.count());
        overview.put("totalHouseCount", houseMapper.count(new HashMap<>()));
        overview.put("publishedHouseCount", houseMapper.count(publishedHouseParams));
        overview.put("pendingAuditCount", houseMapper.countPendingHouses());
        overview.put("completedViewingCount", viewingMapper.count(completedViewingParams));
        overview.put("totalTransactionCount", toLong(transactionOverview.get("totalTransactionCount")));
        overview.put("completedTransactionCount", toLong(transactionOverview.get("completedTransactionCount")));
        overview.put("pendingTransactionCount", toLong(transactionOverview.get("pendingTransactionCount")));
        overview.put("negotiatingTransactionCount", toLong(transactionOverview.get("negotiatingTransactionCount")));
        overview.put("signedTransactionCount", toLong(transactionOverview.get("signedTransactionCount")));
        overview.put("cancelledTransactionCount", toLong(transactionOverview.get("cancelledTransactionCount")));
        overview.put("completedSalesAmount", toMoney(transactionOverview.get("completedSalesAmount")));
        overview.put("averageDealPrice", toMoney(transactionOverview.get("averageDealPrice")));
        overview.put("currentMonthSalesAmount", toMoney(transactionOverview.get("currentMonthSalesAmount")));
        overview.put("lastMonthSalesAmount", toMoney(transactionOverview.get("lastMonthSalesAmount")));
        overview.put("currentMonthCompletedCount", toLong(transactionOverview.get("currentMonthCompletedCount")));
        overview.put("lastMonthCompletedCount", toLong(transactionOverview.get("lastMonthCompletedCount")));
        overview.put("salesGrowthRate", calculateGrowthRate(
                toMoney(transactionOverview.get("currentMonthSalesAmount")),
                toMoney(transactionOverview.get("lastMonthSalesAmount"))
        ));
        overview.put("completionRate", calculateRate(
                toLong(transactionOverview.get("completedTransactionCount")),
                toLong(transactionOverview.get("totalTransactionCount"))
        ));

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("overview", overview);
        data.put("monthlySales", fillMonthlySales(transactionMapper.selectMonthlySales()));
        data.put("districtSales", sanitizeRanking(
                DashboardLocationNormalizer.normalizeRows(transactionMapper.selectDistrictSalesRanking(30, offsetMonths)),
                "salesAmount"
        ));
        data.put("topAgents", sanitizeRanking(
                transactionMapper.selectTopAgentSales(6, offsetMonths),
                "salesAmount"
        ));
        data.put("companySalesRanking", sanitizeRanking(
                transactionMapper.selectCompanySalesRanking(10, offsetMonths),
                "salesAmount"
        ));
        data.put("houseDistribution", sanitizeRanking(
                DashboardLocationNormalizer.normalizeRows(houseMapper.selectDistrictInventory(50)),
                "houseCount"
        ));
        data.put("statusDistribution", normalizeStatusDistribution(transactionMapper.selectStatusDistribution()));
        data.put("pendingHouseReviews", getPendingHouseReviews());
        data.put("pendingAgentReviews", getPendingAgentReviews());
        data.put("recentDeals", getRecentDeals());
        data.put("salesWindow", normalizedWindow);

        return Result.success(data);
    }

    private List<Map<String, Object>> getPendingHouseReviews() {
        List<House> source = houseMapper.selectPendingAudit();
        List<Map<String, Object>> result = new ArrayList<>();
        for (House item : source) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", item.getId());
            row.put("houseTitle", item.getTitle());
            row.put("province", item.getProvince());
            row.put("city", item.getCity());
            row.put("district", item.getDistrict());
            row.put("agentName", item.getAgentName());
            row.put("createTime", item.getCreateTime());
            row.put("priority", Integer.valueOf(1).equals(item.getAuditStatus()) ? "urgent" : "normal");
            result.add(row);
            if (result.size() >= 5) {
                break;
            }
        }
        return result;
    }

    private List<Map<String, Object>> getPendingAgentReviews() {
        List<SysAgent> source = agentMapper.selectPageWithSearch(0, 5, null, 2);
        List<Map<String, Object>> result = new ArrayList<>();
        for (SysAgent item : source) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", item.getId());
            row.put("name", item.getName());
            row.put("company", item.getCompany());
            row.put("phone", item.getPhone());
            row.put("createTime", item.getCreateTime());
            result.add(row);
        }
        return result;
    }

    private List<Map<String, Object>> getRecentDeals() {
        Map<String, Object> params = new HashMap<>();
        params.put("status", 3);
        params.put("offset", 0);
        params.put("limit", 5);

        List<Transaction> source = transactionMapper.selectPage(params);
        source.sort(Comparator.comparing(
                item -> item.getDealDate() == null ? item.getUpdateTime() : item.getDealDate(),
                Comparator.nullsLast(Comparator.reverseOrder())
        ));

        List<Map<String, Object>> result = new ArrayList<>();
        for (Transaction item : source) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", item.getId());
            row.put("houseTitle", item.getHouseTitle());
            row.put("agentName", item.getAgentName());
            row.put("customerName", item.getCustomerName());
            row.put("finalPrice", item.getFinalPrice());
            row.put("dealTime", item.getDealDate() == null ? item.getUpdateTime() : item.getDealDate());
            result.add(row);
        }
        return result;
    }

    private int normalizeSalesWindow(Integer salesWindow) {
        if (salesWindow == null) {
            return 6;
        }
        if (salesWindow <= 1) {
            return 1;
        }
        if (salesWindow <= 3) {
            return 3;
        }
        return 6;
    }

    private List<Map<String, Object>> fillMonthlySales(List<Map<String, Object>> rawRows) {
        Map<String, Map<String, Object>> lookup = new HashMap<>();
        for (Map<String, Object> rawRow : rawRows) {
            lookup.put(String.valueOf(rawRow.get("month")), rawRow);
        }

        List<Map<String, Object>> result = new ArrayList<>();
        YearMonth current = YearMonth.now();
        for (int i = MONTH_WINDOW - 1; i >= 0; i--) {
            YearMonth month = current.minusMonths(i);
            String monthKey = month.format(MONTH_FORMATTER);
            Map<String, Object> source = lookup.get(monthKey);

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("month", monthKey);
            item.put("transactionCount", source == null ? 0L : toLong(source.get("transactionCount")));
            item.put("salesAmount", source == null ? zeroMoney() : toMoney(source.get("salesAmount")));
            item.put("averagePrice", source == null ? zeroMoney() : toMoney(source.get("averagePrice")));
            result.add(item);
        }

        return result;
    }

    private List<Map<String, Object>> sanitizeRanking(List<Map<String, Object>> rows, String primaryKey) {
        List<Map<String, Object>> result = new ArrayList<>();
        BigDecimal maxValue = BigDecimal.ZERO;
        for (Map<String, Object> row : rows) {
            BigDecimal value = toMoney(row.get(primaryKey));
            if (value.compareTo(maxValue) > 0) {
                maxValue = value;
            }
        }

        for (Map<String, Object> row : rows) {
            Map<String, Object> item = new LinkedHashMap<>(row);
            item.put(primaryKey, toMoney(row.get(primaryKey)));
            if (item.containsKey("averagePrice")) {
                item.put("averagePrice", toMoney(item.get("averagePrice")));
            }
            if (item.containsKey("averageUnitPrice")) {
                item.put("averageUnitPrice", toMoney(item.get("averageUnitPrice")));
            }
            if (item.containsKey("houseCount")) {
                item.put("houseCount", toLong(item.get("houseCount")));
            }
            if (item.containsKey("publishedCount")) {
                item.put("publishedCount", toLong(item.get("publishedCount")));
            }
            if (item.containsKey("transactionCount")) {
                item.put("transactionCount", toLong(item.get("transactionCount")));
            }
            item.put("shareRate", calculateRate(toMoney(item.get(primaryKey)), maxValue));
            result.add(item);
        }

        return result;
    }

    private List<Map<String, Object>> normalizeStatusDistribution(List<Map<String, Object>> rows) {
        Map<Integer, Long> counts = new HashMap<>();
        for (Map<String, Object> row : rows) {
            counts.put(toInt(row.get("status")), toLong(row.get("count")));
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (int status = 0; status <= 4; status++) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("status", status);
            item.put("label", getStatusLabel(status));
            item.put("count", counts.getOrDefault(status, 0L));
            result.add(item);
        }
        return result;
    }

    private String getStatusLabel(int status) {
        switch (status) {
            case 0:
                return "待确认";
            case 1:
                return "谈判中";
            case 2:
                return "已签约";
            case 3:
                return "已完成";
            case 4:
                return "已取消";
            default:
                return "未知状态";
        }
    }

    private BigDecimal calculateGrowthRate(BigDecimal current, BigDecimal previous) {
        if (previous.compareTo(BigDecimal.ZERO) <= 0) {
            return current.compareTo(BigDecimal.ZERO) > 0 ? new BigDecimal("100.00") : zeroMoney();
        }
        return current.subtract(previous)
                .multiply(new BigDecimal("100"))
                .divide(previous, 2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateRate(long numerator, long denominator) {
        if (denominator <= 0) {
            return zeroMoney();
        }
        return new BigDecimal(numerator)
                .multiply(new BigDecimal("100"))
                .divide(new BigDecimal(denominator), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateRate(BigDecimal numerator, BigDecimal denominator) {
        if (denominator.compareTo(BigDecimal.ZERO) <= 0) {
            return zeroMoney();
        }
        return numerator
                .multiply(new BigDecimal("100"))
                .divide(denominator, 2, RoundingMode.HALF_UP);
    }

    private BigDecimal toMoney(Object value) {
        if (value == null) {
            return zeroMoney();
        }
        if (value instanceof BigDecimal) {
            return ((BigDecimal) value).setScale(2, RoundingMode.HALF_UP);
        }
        return new BigDecimal(String.valueOf(value)).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal zeroMoney() {
        return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    }

    private long toLong(Object value) {
        if (value == null) {
            return 0L;
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return Long.parseLong(String.valueOf(value));
    }

    private int toInt(Object value) {
        if (value == null) {
            return 0;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return Integer.parseInt(String.valueOf(value));
    }
}
