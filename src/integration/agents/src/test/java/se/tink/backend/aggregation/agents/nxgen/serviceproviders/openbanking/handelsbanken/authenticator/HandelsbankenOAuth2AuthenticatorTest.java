package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.authenticator;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import agents_platform_agents_framework.org.springframework.test.util.ReflectionTestUtils;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.authenticator.entities.ScaMethodsItemEntity;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class HandelsbankenOAuth2AuthenticatorTest {
    private HandelsbankenOAuth2Authenticator authenticator;

    @Before
    public void setup() {
        this.authenticator = mock(HandelsbankenOAuth2Authenticator.class);
    }

    @Test
    public void shouldThrowExceptionWhenNoAuthorizationLinksProvidedByBank() {
        // given
        ScaMethodsItemEntity redirectMethodItemEntityWithoutLinks =
                getRedirectMethodItemEntityWithoutLinks();

        ScaMethodsItemEntity redirectMethodItemEntityWithoutAuthorizationLinks =
                getRedirectMethodItemEntityWithoutAuthorizationLinks();

        // then
        assertThatThrownBy(
                        () ->
                                ReflectionTestUtils.invokeMethod(
                                        authenticator,
                                        "getBaseAuthUrl",
                                        redirectMethodItemEntityWithoutLinks))
                .isInstanceOf(IllegalStateException.class);

        assertThatThrownBy(
                        () ->
                                ReflectionTestUtils.invokeMethod(
                                        authenticator,
                                        "getBaseAuthUrl",
                                        redirectMethodItemEntityWithoutAuthorizationLinks))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    public void shouldThrowExceptionWhenAuthorizationUrlIsNull() {
        // given
        ScaMethodsItemEntity redirectMethodItemEntityWithoutAuthorizationHref =
                getRedirectMethodItemEntityWithoutAuthorizationHref();

        // then
        assertThatThrownBy(
                        () ->
                                ReflectionTestUtils.invokeMethod(
                                        authenticator,
                                        "getBaseAuthUrl",
                                        redirectMethodItemEntityWithoutAuthorizationHref))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    public void shouldReturnAuthorizationUrlWhenProvidedByBank() {
        // given
        ScaMethodsItemEntity redirectMethodItemEntity = getValidRedirectMethodItemEntity();

        // then
        assertEquals(
                "https://secure.handelsbanken.com/bb/gls5/oauth2/authorize/1.0",
                ReflectionTestUtils.invokeMethod(
                        authenticator, "getBaseAuthUrl", redirectMethodItemEntity));
    }

    private ScaMethodsItemEntity getValidRedirectMethodItemEntity() {
        return SerializationUtils.deserializeFromString(
                "{\n"
                        + "  \"_links\": {\n"
                        + "    \"authorization\": [\n"
                        + "      {\n"
                        + "        \"href\": \"https://secure.handelsbanken.com/bb/gls5/oauth2/authorize/1.0\",\n"
                        + "        \"name\": \"authorize_1.0\",\n"
                        + "        \"type\": \"application/x-www-form-urlencoded\"\n"
                        + "      }\n"
                        + "    ]\n"
                        + "  },\n"
                        + "  \"scaMethodType\": \"REDIRECT\"\n"
                        + "}",
                ScaMethodsItemEntity.class);
    }

    private ScaMethodsItemEntity getRedirectMethodItemEntityWithoutLinks() {
        return SerializationUtils.deserializeFromString(
                "{\n" + "  \"scaMethodType\": \"REDIRECT\"\n" + "}", ScaMethodsItemEntity.class);
    }

    private ScaMethodsItemEntity getRedirectMethodItemEntityWithoutAuthorizationLinks() {
        return SerializationUtils.deserializeFromString(
                "{\n"
                        + "  \"_links\": {\n"
                        + "    \"authorization\": []\n"
                        + "  },\n"
                        + "  \"scaMethodType\": \"REDIRECT\"\n"
                        + "}",
                ScaMethodsItemEntity.class);
    }

    private ScaMethodsItemEntity getRedirectMethodItemEntityWithoutAuthorizationHref() {
        return SerializationUtils.deserializeFromString(
                "{\n"
                        + "  \"_links\": {\n"
                        + "    \"authorization\": [\n"
                        + "      {\n"
                        + "        \"name\": \"authorize_1.0\",\n"
                        + "        \"type\": \"application/x-www-form-urlencoded\"\n"
                        + "      }\n"
                        + "    ]\n"
                        + "  },\n"
                        + "  \"scaMethodType\": \"REDIRECT\"\n"
                        + "}",
                ScaMethodsItemEntity.class);
    }
}
