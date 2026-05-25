package com.basisi.backend.domain.review;

import java.util.List;

/** JPQL coalesce(avg), count 집계 결과를 안전하게 파싱합니다. */
public final class ReviewSummaryRow {

    private ReviewSummaryRow() {
    }

    public static double averageRating(List<Object[]> rows) {
        Object[] cells = firstRow(rows);
        if (cells == null || cells.length < 1 || !(cells[0] instanceof Number)) {
            return 0.0;
        }
        return ((Number) cells[0]).doubleValue();
    }

    public static long reviewCount(List<Object[]> rows) {
        Object[] cells = firstRow(rows);
        if (cells == null || cells.length < 2 || !(cells[1] instanceof Number)) {
            return 0L;
        }
        return ((Number) cells[1]).longValue();
    }

    private static Object[] firstRow(List<Object[]> rows) {
        if (rows == null || rows.isEmpty()) {
            return null;
        }
        Object[] row = rows.get(0);
        if (row == null) {
            return null;
        }
        // 일부 드라이버/버전에서 단일 Object[]로 한 번 더 감싸는 경우
        if (row.length == 1 && row[0] instanceof Object[] nested) {
            return nested;
        }
        return row;
    }
}
