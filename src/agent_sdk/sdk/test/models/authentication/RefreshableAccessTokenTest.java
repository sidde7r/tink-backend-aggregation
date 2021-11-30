package se.tink.agent.sdk.test.models.authentication;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import se.tink.agent.sdk.models.authentication.RefreshableAccessToken;
import se.tink.libraries.serialization.utils.SerializationUtils;

@Ignore
public class RefreshableAccessTokenTest {

    @Test
    public void testSerialization() {
        RefreshableAccessToken refreshableAccessToken =
                RefreshableAccessToken.builder()
                        .tokenType("bearer")
                        .accessToken("someAccessToken")
                        .refreshToken("someRefreshToken")
                        .expiresInSeconds(123)
                        .build();

        String serialized = SerializationUtils.serializeToString(refreshableAccessToken);
        Assert.fail(serialized);
    }

    @Test
    public void testDeserialization() {}
}
