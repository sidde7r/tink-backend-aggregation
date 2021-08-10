package se.tink.backend.aggregation.nxgen.http.filter.filters.executiontime;

import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

public class TimeMeasuredRequestExecutor {
    private static final int DEFAULT_THRESHOLD_IN_MS = 5000; // 5sec
    private final HttpRequest request;
    private final long thresholdInMs;

    private TimeMeasuredRequestExecutor(HttpRequest request, long thresholdInMs) {
        this.request = request;
        this.thresholdInMs = thresholdInMs;
    }

    public static TimeMeasuredRequestExecutor withRequest(HttpRequest request) {
        return new TimeMeasuredRequestExecutor(request, DEFAULT_THRESHOLD_IN_MS);
    }

    public TimeMeasuredRequestExecutor withThreshold(long thresholdInMs) {
        return new TimeMeasuredRequestExecutor(request, thresholdInMs);
    }

    public ExecutionDetails execute(Filter filter) {
        long start = System.currentTimeMillis();
        HttpResponse response = filter.getNext().handle(request);
        long end = System.currentTimeMillis();
        long executionTime = end - start;
        return new ExecutionDetails(
                response, executionTime, executionTime >= thresholdInMs, thresholdInMs);
    }

    static class ExecutionDetails {
        private final HttpResponse response;

        private final long executionTime;

        private final boolean shouldBeLogged;

        private final long threshold;

        public ExecutionDetails(
                HttpResponse response, long executionTime, boolean shouldBeLogged, long threshold) {
            this.response = response;
            this.executionTime = executionTime;
            this.shouldBeLogged = shouldBeLogged;
            this.threshold = threshold;
        }

        public HttpResponse getResponse() {
            return response;
        }

        public long getExecutionTime() {
            return executionTime;
        }

        public boolean shouldBeLogged() {
            return shouldBeLogged;
        }

        public long getThreshold() {
            return threshold;
        }
    }
}
