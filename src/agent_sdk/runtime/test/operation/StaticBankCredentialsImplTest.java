package se.tink.agent.runtime.test.operation;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.Assert;
import org.junit.Test;
import se.tink.agent.runtime.operation.StaticBankCredentialsImpl;

public class StaticBankCredentialsImplTest {
    @Test
    public void testTryGet() {
        Map<String, String> rawData = new HashMap<>();
        rawData.put("foo", "bar");
        rawData.put("fooEmpty", "");
        rawData.put("fooNull", null);

        StaticBankCredentialsImpl staticBankCredentials = new StaticBankCredentialsImpl(rawData);

        Assert.assertEquals(Optional.of("bar"), staticBankCredentials.tryGet("foo"));
        Assert.assertEquals(Optional.empty(), staticBankCredentials.tryGet("fooEmpty"));
        Assert.assertEquals(Optional.empty(), staticBankCredentials.tryGet("fooNull"));
        Assert.assertEquals(Optional.empty(), staticBankCredentials.tryGet("doesNotExist"));
    }

    @Test
    public void testNullRawData() {
        StaticBankCredentialsImpl staticBankCredentials = new StaticBankCredentialsImpl(null);
        Assert.assertEquals(Optional.empty(), staticBankCredentials.tryGet("doesNotExist"));
    }
}
