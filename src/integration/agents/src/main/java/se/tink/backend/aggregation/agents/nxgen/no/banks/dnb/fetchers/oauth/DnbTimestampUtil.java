package se.tink.backend.aggregation.agents.nxgen.no.banks.dnb.fetchers.oauth;

import java.util.Random;

public class DnbTimestampUtil {
    private DnbTimestampUtil.Timer timer = new DnbTimestampUtil.Timer();

    public DnbTimestampUtil() {}

    private Long getTimestamp() {
        return this.timer.getMilis() / 1000L;
    }

    public String getNonce() {
        return String.valueOf(this.getTimestamp() + (long) this.timer.getRandomInteger());
    }

    public String getTimestampInSeconds() {
        return String.valueOf(this.getTimestamp());
    }

    void setTimer(DnbTimestampUtil.Timer var1) {
        this.timer = var1;
    }

    static class Timer {
        private final Random rand = new Random();

        Timer() {}

        Long getMilis() {
            return System.currentTimeMillis();
        }

        Integer getRandomInteger() {
            return this.rand.nextInt();
        }
    }
}
