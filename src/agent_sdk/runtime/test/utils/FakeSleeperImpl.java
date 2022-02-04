package se.tink.agent.runtime.test.utils;

import java.time.Duration;
import se.tink.agent.sdk.utils.Sleeper;

public class FakeSleeperImpl implements Sleeper {

    @Override
    public void sleep(Duration duration) {
        // NO OP
    }
}
