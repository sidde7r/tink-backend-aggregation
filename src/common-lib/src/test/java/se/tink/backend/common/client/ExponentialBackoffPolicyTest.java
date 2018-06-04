package se.tink.backend.common.client;

import org.junit.Assert;
import org.junit.Test;

public class ExponentialBackoffPolicyTest {

    @Test
    public void testBasic() {
        ExponentialBackoffPolicy backoff = new ExponentialBackoffPolicy(1000);
        Assert.assertEquals(0, backoff.getSleepDuration());
        Assert.assertEquals(1000, backoff.getSleepDuration());
        Assert.assertEquals(2000, backoff.getSleepDuration());
        Assert.assertEquals(4000, backoff.getSleepDuration());
        Assert.assertEquals(8000, backoff.getSleepDuration());
    }

}
