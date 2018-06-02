package se.tink.backend.core.oauth2;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Test;
import org.junit.runner.RunWith;
import se.tink.backend.auth.OAuth2ClientRequest;
import se.tink.backend.core.Client;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static se.tink.backend.core.oauth2.OAuth2Utils.getOAuth2Client;
import static se.tink.backend.core.oauth2.OAuth2Utils.getPayloadValue;

@RunWith(JUnitParamsRunner.class)
public class OAuth2UtilsTest {

    private Object[] parametersForGetClientByRequest() {
        return new Object[] {
                new Object[] {
                        null,
                        false
                },
                new Object[] {
                        new OAuth2ClientRequest.Builder()
                                .setLinkClient(new Client())
                                .build(),
                        false
                },
                new Object[] {
                        new OAuth2ClientRequest.Builder()
                                .setLinkClient(new Client())
                                .build(),
                        false
                },
                new Object[] {
                        new OAuth2ClientRequest.Builder()
                                .setOAuth2Client(new OAuth2Client())
                                .build(),
                        true
                },
                new Object[] {
                        new OAuth2ClientRequest.Builder()
                                .setLinkClient(new Client())
                                .setOAuth2Client(new OAuth2Client())
                                .build(),
                        true
                },
        };
    }

    @Test
    @Parameters(method = "parametersForGetClientByRequest")
    public void getClientByRequest(OAuth2ClientRequest oauth2ClientRequest, boolean isPresent) {
        Optional<OAuth2Client> client = getOAuth2Client(oauth2ClientRequest);
        assertEquals(isPresent, client.isPresent());
    }

    @Test
    public void getAbsentClientByAbsentOptionalRequest() {
        Optional<OAuth2Client> client = getOAuth2Client(Optional.empty());
        assertFalse(client.isPresent());
    }

    private Object[] parametersForGetPayloadByClient() throws JsonProcessingException {
        return new Object[] {
                new Object[] {
                        Optional.empty(),
                        false
                },
                new Object[] {
                        Optional.ofNullable(null),
                        false
                },
                new Object[] {
                        Optional.of(new OAuth2Client() {{
                            setPayloadSerialized("");
                        }}),
                        false
                },
                new Object[] {
                        Optional.of(new OAuth2Client() {{
                            setPayloadSerialized(toJson("key1", "value1"));
                        }}),
                        false
                },
                new Object[] {
                        Optional.of(new OAuth2Client() {{
                            setPayloadSerialized(toJson("key", "value"));
                        }}),
                        true
                },
        };
    }

    @Test
    @Parameters(method = "parametersForGetPayloadByClient")
    public void getPayloadByClient(Optional<OAuth2Client> client, boolean isPresent) {
        Optional<String> payload = getPayloadValue(client, "key");
        assertEquals(isPresent, payload.isPresent());
    }

    private String toJson(String key, String value) throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(ImmutableMap.of(key, value));
    }
}
