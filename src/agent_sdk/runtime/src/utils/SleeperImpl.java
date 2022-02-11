package se.tink.agent.runtime.utils;

import com.google.common.util.concurrent.Uninterruptibles;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import se.tink.agent.sdk.utils.Sleeper;

public class SleeperImpl implements Sleeper {

    @Override
    public void sleep(Duration duration) {
        Uninterruptibles.sleepUninterruptibly(duration.toNanos(), TimeUnit.NANOSECONDS);
    }
}
