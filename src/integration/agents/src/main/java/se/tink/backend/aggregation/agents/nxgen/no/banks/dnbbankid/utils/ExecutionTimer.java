package se.tink.backend.aggregation.agents.nxgen.no.banks.dnbbankid.utils;

import java.util.concurrent.Callable;
import lombok.SneakyThrows;

public class ExecutionTimer {

    private final Timer timer = new Timer();

    @SneakyThrows
    public <T> T execute(Callable<T> callable) {
        try {
            timer.start();
            return callable.call();
        } finally {
            timer.stop();
        }
    }

    public Double getDurationInSeconds() {
        return timer.getDurationInSeconds();
    }

    private static class Timer {

        private Long startTime;
        private Long stopTime;

        public void start() {
            startTime = System.currentTimeMillis();
            stopTime = null;
        }

        public void stop() {
            stopTime = System.currentTimeMillis();
        }

        public Double getDurationInSeconds() {
            if (startTime == null || stopTime == null) {
                return null;
            }
            long millis = stopTime - startTime;
            return millis / 1000.;
        }
    }
}
