package se.tink.backend.aggregation.nxgen.controllers.authentication.password.dk.nemid.v2.metrics;

import java.util.ArrayList;
import java.util.List;

public class NemIdTimer {

    private final List<Long> savedIntervals = new ArrayList<>();

    private Long startTime;

    public void start() {
        startTime = System.currentTimeMillis();
    }

    public void stop() {
        savedIntervals.add(System.currentTimeMillis() - startTime);
        startTime = null;
    }

    public Double getTotalTimeInSeconds() {
        Long millis = savedIntervals.stream().reduce(0L, Long::sum);
        return millis / 1_000.;
    }
}
