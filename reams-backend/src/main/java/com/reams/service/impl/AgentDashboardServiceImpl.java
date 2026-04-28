package com.reams.service.impl;

import com.reams.common.result.Result;
import com.reams.entity.Review;
import com.reams.entity.Transaction;
import com.reams.entity.Viewing;
import com.reams.mapper.HouseMapper;
import com.reams.mapper.ReviewMapper;
import com.reams.mapper.TransactionMapper;
import com.reams.mapper.ViewingMapper;
import com.reams.service.AgentDashboardService;
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
public class AgentDashboardServiceImpl implements AgentDashboardService {

    private static final int MONTH_WINDOW = 6;
    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");

    @Autowired
    private HouseMapper houseMapper;

    @Autowired
    private ViewingMapper viewingMapper;

    @Autowired
    private TransactionMapper transactionMapper;

    @Autowired
    private ReviewMapper reviewMapper;

    @Override
    public Result<?> getDashboardData(Long agentId, Integer salesWindow) {
        if (agentId == null) {
            return Result.error("中介 ID 不能为空");
        }
        int normalizedWindow = normalizeSalesWindow(salesWindow);
        int offsetMonths = Math.max(normalizedWindow - 1, 0);

        Map<String, Object> overview = new LinkedHashMap<>();
        overview.put("myHouseCount", countHouses(agentId, null, null));
        overview.put("publishedHouseCount", countHouses(agentId, 1, 2));
        overview.put("pendingAuditCount", countHouses(agentId, null, 0) + countHouses(agentId, null, 1));
        overview.put("pendingViewingCount", countViewings(agentId, 0));
        overview.put("confirmedViewingCount", countViewings(agentId, 1));
        overview.put("completedViewingCount", countViewings(agentId, 2));
        overview.put("cancelledViewingCount", countViewings(agentId, 3));
        overview.put("totalTransactionCount", countTransactions(agentId, null));
        overview.put("pendingTransactionCount", countTransactions(agentId, 0));
        overview.put("negotiatingTransactionCount", countTransactions(agentId, 1));
        overview.put("signedTransactionCount", countTransactions(agentId, 2));
        overview.put("completedTransactionCount", countTransactions(agentId, 3));
        overview.put("cancelledTransactionCount", countTransactions(agentId, 4));
        overview.put("reviewCount", countAgentReviews(agentId));
        overview.put("avgRating", toDecimal(reviewMapper.selectAvgRatingByAgentId(agentId)));
        overview.put("totalSalesAmount", getTotalSales(agentId));

        List<Map<String, Object>> monthlySales = fillMonthlySales(transactionMapper.selectMonthlySalesByAgentId(agentId));
        BigDecimal currentMonthSales = monthlySales.isEmpty()
                ? zeroMoney()
                : toMoney(monthlySales.get(monthlySales.size() - 1).get("salesAmount"));
        BigDecimal previousMonthSales = monthlySales.size() < 2
                ? zeroMoney()
                : toMoney(monthlySales.get(monthlySales.size() - 2).get("salesAmount"));
        overview.put("currentMonthSalesAmount", currentMonthSales);
        overview.put("salesGrowthRate", calculateGrowthRate(currentMonthSales, previousMonthSales));

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("overview", overview);
        data.put("monthlySales", monthlySales);
        data.put("districtSales", sanitizeRanking(
                DashboardLocationNormalizer.normalizeRows(
                        transactionMapper.selectDistrictSalesRankingByAgentId(agentId, 30, offsetMonths)
                ),
                "salesAmount"
        ));
        data.put("inventoryDistribution", sanitizeRanking(
                DashboardLocationNormalizer.normalizeRows(houseMapper.selectAgentDistrictInventory(agentId, 50)),
                "houseCount"
        ));
        data.put("viewingStatusDistribution", buildViewingStatusDistribution(agentId));
        data.put("pendingViewings", getPendingViewings(agentId));
        data.put("pendingTransactions", getPendingTransactions(agentId));
        data.put("recentDeals", getRecentDeals(agentId));
        data.put("activeAgents", sanitizeRanking(transactionMapper.selectTopAgentSales(5, 0), "salesAmount"));
        data.put("recentReviews", getRecentReviews(agentId));
        data.put("salesWindow", normalizedWindow);
        return Result.success(data);
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

    private long countHouses(Long agentId, Integer houseStatus, Integer auditStatus) {
        Map<String, Object> params = new HashMap<>();
        params.put("agentId", agentId);
        params.put("houseStatus", houseStatus);
        params.put("auditStatus", auditStatus);
        return houseMapper.count(params);
    }

    private long countViewings(Long agentId, Integer status) {
        Map<String, Object> params = new HashMap<>();
        params.put("agentId", agentId);
        params.put("status", status);
        return viewingMapper.count(params);
    }

    private long countTransactions(Long agentId, Integer status) {
        Map<String, Object> params = new HashMap<>();
        params.put("agentId", agentId);
        params.put("status", status);
        return transactionMapper.count(params);
    }

    private long countAgentReviews(Long agentId) {
        Map<String, Object> params = new HashMap<>();
        params.put("agentId", agentId);
        params.put("targetType", 2);
        params.put("isShow", 1);
        return reviewMapper.count(params);
    }

    private BigDecimal getTotalSales(Long agentId) {
        Map<String, Object> params = new HashMap<>();
        params.put("agentId", agentId);
        params.put("status", 3);
        return toMoney(transactionMapper.sumFinalPrice(params));
    }

    private List<Map<String, Object>> fillMonthlySales(List<Map<String, Object>> rawRows) {
        Map<String, Map<String, Object>> lookup = new HashMap<>();
        for (Map<String, Object> row : rawRows) {
            lookup.put(String.valueOf(row.get("month")), row);
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

    private List<Map<String, Object>> buildViewingStatusDistribution(Long agentId) {
        List<Map<String, Object>> result = new ArrayList<>();
        int[] statuses = {0, 1, 2, 3};
        String[] labels = {"待确认", "已确认", "已完成", "已取消"};
        long maxCount = 0L;
        long[] counts = new long[statuses.length];
        for (int i = 0; i < statuses.length; i++) {
            counts[i] = countViewings(agentId, statuses[i]);
            if (counts[i] > maxCount) {
                maxCount = counts[i];
            }
        }

        for (int i = 0; i < statuses.length; i++) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("status", statuses[i]);
            item.put("label", labels[i]);
            item.put("count", counts[i]);
            item.put("shareRate", maxCount == 0 ? zeroMoney() : calculateRate(counts[i], maxCount));
            result.add(item);
        }
        return result;
    }

    private List<Map<String, Object>> getRecentReviews(Long agentId) {
        Map<String, Object> params = new HashMap<>();
        params.put("agentId", agentId);
        params.put("targetType", 2);
        params.put("isShow", 1);
        params.put("offset", 0);
        params.put("limit", 4);

        List<Review> reviews = reviewMapper.selectPage(params);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Review review : reviews) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", review.getId());
            item.put("customerName", review.getCustomerName());
            item.put("customerAvatar", review.getCustomerAvatar());
            item.put("houseTitle", review.getHouseTitle());
            item.put("rating", review.getRating());
            item.put("content", review.getContent());
            item.put("createTime", review.getCreateTime());
            result.add(item);
        }
        return result;
    }

    private List<Map<String, Object>> getPendingViewings(Long agentId) {
        List<Viewing> source = viewingMapper.selectByAgentId(agentId);
        List<Viewing> filtered = new ArrayList<>();
        for (Viewing item : source) {
            Integer status = item.getStatus();
            if (status != null && (status == 0 || status == 1)) {
                filtered.add(item);
            }
        }

        filtered.sort(Comparator.comparing(
                item -> item.getAppointTime() == null ? item.getUpdateTime() : item.getAppointTime(),
                Comparator.nullsLast(Comparator.naturalOrder())
        ));

        List<Map<String, Object>> result = new ArrayList<>();
        for (Viewing item : filtered) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", item.getId());
            row.put("houseTitle", item.getHouseTitle());
            row.put("customerName", item.getCustomerName());
            row.put("appointTime", item.getAppointTime());
            row.put("updateTime", item.getUpdateTime());
            row.put("status", item.getStatus());
            result.add(row);
            if (result.size() >= 3) {
                break;
            }
        }
        return result;
    }

    private List<Map<String, Object>> getPendingTransactions(Long agentId) {
        List<Transaction> source = transactionMapper.selectByAgentId(agentId);
        List<Transaction> filtered = new ArrayList<>();
        for (Transaction item : source) {
            Integer status = item.getStatus();
            if (status != null && status >= 0 && status <= 2) {
                filtered.add(item);
            }
        }

        filtered.sort(Comparator.comparing(Transaction::getUpdateTime, Comparator.nullsLast(Comparator.reverseOrder())));

        List<Map<String, Object>> result = new ArrayList<>();
        for (Transaction item : filtered) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", item.getId());
            row.put("houseTitle", item.getHouseTitle());
            row.put("customerName", item.getCustomerName());
            row.put("updateTime", item.getUpdateTime());
            row.put("status", item.getStatus());
            result.add(row);
            if (result.size() >= 3) {
                break;
            }
        }
        return result;
    }

    private List<Map<String, Object>> getRecentDeals(Long agentId) {
        List<Transaction> source = transactionMapper.selectByAgentId(agentId);
        List<Transaction> filtered = new ArrayList<>();
        for (Transaction item : source) {
            Integer status = item.getStatus();
            if (status != null && status == 3) {
                filtered.add(item);
            }
        }

        filtered.sort(Comparator.comparing(
                item -> item.getDealDate() == null ? item.getUpdateTime() : item.getDealDate(),
                Comparator.nullsLast(Comparator.reverseOrder())
        ));

        List<Map<String, Object>> result = new ArrayList<>();
        for (Transaction item : filtered) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", item.getId());
            row.put("houseTitle", item.getHouseTitle());
            row.put("customerName", item.getCustomerName());
            row.put("finalPrice", item.getFinalPrice());
            row.put("dealTime", item.getDealDate() == null ? item.getUpdateTime() : item.getDealDate());
            result.add(row);
            if (result.size() >= 3) {
                break;
            }
        }
        return result;
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

    private BigDecimal toDecimal(Object value) {
        if (value == null) {
            return zeroMoney();
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
}
