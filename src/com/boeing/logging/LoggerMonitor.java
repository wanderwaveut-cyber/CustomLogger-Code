package com.boeing.logging;



import com.google.gson.Gson;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class LoggerMonitor {
    private static final Map<String, com.boeing.logging.LoggerMonitor.LoggerMetrics> LOGGER_METRICS = new ConcurrentHashMap();
    private static final Gson gson = new Gson();

    public LoggerMonitor() {
    }

    public static void registerLogger(String appName) {
        LOGGER_METRICS.putIfAbsent(appName, new com.boeing.logging.LoggerMonitor.LoggerMetrics(appName));
    }

    public static String getAllLoggerMetricsAsJson() {
        Map<String, Map<String, Object>> metricsMap = (Map)LOGGER_METRICS.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, (entry) -> {
            com.boeing.logging.LoggerMonitor.LoggerMetrics metrics = (com.boeing.logging.LoggerMonitor.LoggerMetrics)entry.getValue();
            Map<String, Object> metricDetails = new HashMap();
            metricDetails.put("creationTimestamp", metrics.getCreationTimestamp());
            metricDetails.put("totalLogEvents", metrics.getTotalLogEvents());
            metricDetails.put("totalLogBytes", metrics.getTotalLogBytes());
            return metricDetails;
        }));
        return gson.toJson(metricsMap);
    }

    public static String getLoggerMetricsAsJson(String appName) {
        com.boeing.logging.LoggerMonitor.LoggerMetrics metrics = (com.boeing.logging.LoggerMonitor.LoggerMetrics)LOGGER_METRICS.get(appName);
        if (metrics == null) {
            return null;
        } else {
            Map<String, Object> metricDetails = new HashMap();
            metricDetails.put("appName", metrics.getAppName());
            metricDetails.put("creationTimestamp", metrics.getCreationTimestamp());
            metricDetails.put("totalLogEvents", metrics.getTotalLogEvents());
            metricDetails.put("totalLogBytes", metrics.getTotalLogBytes());
            return gson.toJson(metricDetails);
        }
    }

    public static String getTotalLoggerCountAsJson() {
        Map<String, Integer> countMap = new HashMap();
        countMap.put("totalLoggerCount", LOGGER_METRICS.size());
        return gson.toJson(countMap);
    }

    public static String getTotalLogEventCountAsJson() {
        long totalEvents = LOGGER_METRICS.values().stream().mapToLong(com.boeing.logging.LoggerMonitor.LoggerMetrics::getTotalLogEvents).sum();
        Map<String, Long> eventMap = new HashMap();
        eventMap.put("totalLogEventCount", totalEvents);
        return gson.toJson(eventMap);
    }

    public static String getTotalLogBytesAsJson() {
        long totalBytes = LOGGER_METRICS.values().stream().mapToLong(com.boeing.logging.LoggerMonitor.LoggerMetrics::getTotalLogBytes).sum();
        Map<String, Long> bytesMap = new HashMap();
        bytesMap.put("totalLogBytes", totalBytes);
        return gson.toJson(bytesMap);
    }

    public static void trackLogEvent(String appName, int logSize) {
        ((com.boeing.logging.LoggerMonitor.LoggerMetrics)LOGGER_METRICS.computeIfAbsent(appName, com.boeing.logging.LoggerMonitor.LoggerMetrics::new)).incrementLogEvent(logSize);
    }

    public static class LoggerMetrics {
        private final String appName;
        private final long creationTimestamp;
        private final AtomicLong totalLogEvents = new AtomicLong(0L);
        private final AtomicLong totalLogBytes = new AtomicLong(0L);

        public LoggerMetrics(String appName) {
            this.appName = appName;
            this.creationTimestamp = System.currentTimeMillis();
        }

        public void incrementLogEvent(int logSize) {
            this.totalLogEvents.incrementAndGet();
            this.totalLogBytes.addAndGet((long)logSize);
        }

        public String getAppName() {
            return this.appName;
        }

        public long getCreationTimestamp() {
            return this.creationTimestamp;
        }

        public long getTotalLogEvents() {
            return this.totalLogEvents.get();
        }

        public long getTotalLogBytes() {
            return this.totalLogBytes.get();
        }
    }
}
