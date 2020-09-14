package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Delayer {

    public static void delay(long minMs, long maxMs) {
        if (maxMs <= minMs) {
            log.warn("wrong delay parameters, min:{}ms, max:{}ms", minMs, maxMs);
            return;
        }
        long delay = (long) ((Math.random() * (maxMs - minMs)) + minMs);
        try {
            Thread.sleep(delay);
        } catch (InterruptedException ex) {
            log.warn("thread interrupted, ", ex);
            Thread.currentThread().interrupt();
        }
    }
}
