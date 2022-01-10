package src.agent_sdk.linter.test;

import com.google.common.util.concurrent.Uninterruptibles;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("java:S2925")
public class SleepTestCases {
    public void preventThreadSleep() throws InterruptedException {
        // BUG: Diagnostic contains: Disallowed usage of method or class.
        Thread.sleep(10);
    }

    public void preventGuavaSleep() {
        // BUG: Diagnostic contains: Disallowed usage of method or class.
        Uninterruptibles.sleepUninterruptibly(10, TimeUnit.MILLISECONDS);
    }
}
