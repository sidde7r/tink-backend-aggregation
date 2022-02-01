package src.agent_sdk.runtime.test.user_interaction;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.Assert;
import org.junit.Test;
import src.agent_sdk.runtime.src.user_interaction.UserResponseDataImpl;

public class UserResponseDataImplTest {
    @Test
    public void testTryGet() {
        Map<String, String> rawData = new HashMap<>();
        rawData.put("foo", "bar");
        rawData.put("fooEmpty", "");
        rawData.put("fooNull", null);

        UserResponseDataImpl userResponseData = new UserResponseDataImpl(rawData);

        Assert.assertEquals(Optional.of("bar"), userResponseData.tryGet("foo"));
        Assert.assertEquals(Optional.empty(), userResponseData.tryGet("fooEmpty"));
        Assert.assertEquals(Optional.empty(), userResponseData.tryGet("fooNull"));
        Assert.assertEquals(Optional.empty(), userResponseData.tryGet("doesNotExist"));
    }

    @Test
    public void testNullRawData() {
        UserResponseDataImpl userResponseData = new UserResponseDataImpl(null);
        Assert.assertEquals(Optional.empty(), userResponseData.tryGet("doesNotExist"));
    }
}
