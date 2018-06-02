package se.tink.backend.common.health;

import com.codahale.metrics.health.HealthCheck;
import com.codahale.metrics.health.HealthCheck.Result;
import com.google.api.client.util.Lists;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Stopwatch;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import se.tink.backend.utils.LogUtils;

public class HealthCheckManager {

    private final ImmutableMap<String, HealthCheck> healthChecks;
    private final LogUtils log;
    
    public HealthCheckManager(LogUtils log, Map<String, HealthCheck> healthChecks) {
        if (log == null) {
            this.log = new LogUtils(HealthCheckManager.class);
        } else {
            this.log = log;
        }
        
        this.healthChecks = ImmutableMap.copyOf(healthChecks);
    }
    
    private static class ResultSuccessPredicate implements Predicate<Outcome> {

        @Override
        public boolean apply(Outcome input) {
            return input.result.isHealthy();
        }

    }

    public boolean check() {
        return Iterables.all(checkVerbose(), new ResultSuccessPredicate());
    }
    
    public static class Outcome {
        public final String identifier;
        public final Result result;
        private final long executionTimeNs;
        
        public Outcome(String identifier, Result result, long executionTimeNs) {
            this.identifier = Preconditions.checkNotNull(identifier);
            this.result = Preconditions.checkNotNull(result);
            this.executionTimeNs = executionTimeNs;
        }
        
        public String constructLogMessage() {
            Result r = result;

            final boolean healthy = r.isHealthy();
            final String message = r.getMessage();

            long milliseconds = TimeUnit.MILLISECONDS.convert(executionTimeNs, TimeUnit.NANOSECONDS);

            final String logMessage;
            if (Strings.isNullOrEmpty(message)) {
                logMessage = String.format("%s `%s`. Took %d ms.", healthy ? "Healthy" : "Unhealthy", identifier,
                        milliseconds);
            } else {
                logMessage = String.format("%s `%s`: %s. Took %d ms.", healthy ? "Healthy" : "Unhealthy", identifier,
                        message, milliseconds);
            }

            return logMessage;
        }

        /**
         * Log patterns:
         * 
         * > All health checks `healthy`
         * 
         * > Failed health checks `^error.*unhealthy` or `unhealthy`
         * 
         * > Successful health checks `^debug.*healthy`
         * 
         * > Health checks the past day `YYYY-MM-DD.*healthy`
         * 
         * > Health checks for a specific service the past day `YYYY-MM-DD.*XXXXXXXXXServiceResource.*healthy`
         * 
         * > Failed health checks for a specific service the past day
         * `^error.*YYYY-MM-DD.*XXXXXXXXXServiceResource.*unhealthy` or
         * `YYYY-MM-DD.*XXXXXXXXXServiceResource.*unhealthy`
         * 
         * > Successful health checks for a specific service the past day
         * `^debug.*YYYY-MM-DD.*XXXXXXXXXServiceResource.*healthy`
         */
        public void logTo(LogUtils log) {
            final String logMessage = constructLogMessage();

            if (result.isHealthy()) {
                log.debug(logMessage);
            } else {
                if (result.getError() != null) {
                    log.error(logMessage, result.getError());
                } else {
                    log.error(logMessage);
                }
            }
        }
    }

    private List<Outcome> checkVerbose() {
        
        List<Outcome> results = Lists.newArrayListWithCapacity(healthChecks.size());
        
        for (Entry<String, HealthCheck> check : healthChecks.entrySet()) {
            final String identifier = check.getKey();
            final HealthCheck checker = check.getValue();

            Stopwatch watch = Stopwatch.createStarted();
            final Result r = checker.execute();
            watch.stop();

            final Outcome loggable = new Outcome(identifier, r, watch.elapsed(TimeUnit.NANOSECONDS));
            loggable.logTo(log);

            results.add(loggable);
        }
        
        return results;
    }
}
