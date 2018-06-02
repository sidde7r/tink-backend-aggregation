package se.tink.backend.common.statistics;

import java.util.concurrent.ExecutionException;

public class StatisticException extends RuntimeException {
    StatisticException(ExecutionException e) {
        super(e);
    }

    StatisticException(String message) {
        super(message);
    }
}
