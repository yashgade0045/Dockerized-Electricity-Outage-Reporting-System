package com.example.outage.dto;

/**
 * Data Transfer Object for dashboard summary statistics.
 */
public class DashboardStatsDto {

    private long totalReports;
    private long reportedCount;
    private long inProgressCount;
    private long resolvedCount;

    public DashboardStatsDto() {}

    public DashboardStatsDto(long totalReports, long reportedCount,
                             long inProgressCount, long resolvedCount) {
        this.totalReports = totalReports;
        this.reportedCount = reportedCount;
        this.inProgressCount = inProgressCount;
        this.resolvedCount = resolvedCount;
    }

    public long getTotalReports() { return totalReports; }
    public void setTotalReports(long totalReports) { this.totalReports = totalReports; }
    public long getReportedCount() { return reportedCount; }
    public void setReportedCount(long reportedCount) { this.reportedCount = reportedCount; }
    public long getInProgressCount() { return inProgressCount; }
    public void setInProgressCount(long inProgressCount) { this.inProgressCount = inProgressCount; }
    public long getResolvedCount() { return resolvedCount; }
    public void setResolvedCount(long resolvedCount) { this.resolvedCount = resolvedCount; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private long totalReports, reportedCount, inProgressCount, resolvedCount;
        public Builder totalReports(long v) { this.totalReports = v; return this; }
        public Builder reportedCount(long v) { this.reportedCount = v; return this; }
        public Builder inProgressCount(long v) { this.inProgressCount = v; return this; }
        public Builder resolvedCount(long v) { this.resolvedCount = v; return this; }
        public DashboardStatsDto build() {
            return new DashboardStatsDto(totalReports, reportedCount, inProgressCount, resolvedCount);
        }
    }
}
