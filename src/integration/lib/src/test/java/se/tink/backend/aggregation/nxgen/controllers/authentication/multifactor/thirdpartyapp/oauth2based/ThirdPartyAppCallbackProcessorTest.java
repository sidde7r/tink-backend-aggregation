package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2based;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;

public class ThirdPartyAppCallbackProcessorTest {

    private static final String CALLBACK_DATA_AUTH_CODE_KEY = "code";

    private ThirdPartyAppCallbackProcessor thirdPartyAppCallbackProcessor;

    @Before
    public void setUp() {
        final OAuth2ThirdPartyAppRequestParamsProvider
                oAuth2ThirdPartyAppRequestParamsProviderMock =
                        mock(OAuth2ThirdPartyAppRequestParamsProvider.class);
        when(oAuth2ThirdPartyAppRequestParamsProviderMock.getCallbackDataAuthCodeKey())
                .thenReturn(CALLBACK_DATA_AUTH_CODE_KEY);

        thirdPartyAppCallbackProcessor =
                new ThirdPartyAppCallbackProcessor(oAuth2ThirdPartyAppRequestParamsProviderMock);
    }

    @Test
    public void shouldGetAccessCodeFromCallbackData() {
        // given
        final String codeValue = "abc1234";
        final Map<String, String> callbackData = new HashMap<>();
        callbackData.put("key1", "value1");
        callbackData.put(CALLBACK_DATA_AUTH_CODE_KEY, codeValue);

        // when
        final String returnedCode =
                thirdPartyAppCallbackProcessor.getAccessCodeFromCallbackData(callbackData);

        // then
        assertThat(returnedCode).isEqualTo(codeValue);
    }

    @Test
    public void shouldThrowExceptionIfAccessCodeIsNotPresent() {
        // given
        final Map<String, String> callbackData = new HashMap<>();
        callbackData.put("key1", "value1");

        // when
        final Throwable thrown =
                catchThrowable(
                        () ->
                                thirdPartyAppCallbackProcessor.getAccessCodeFromCallbackData(
                                        callbackData));

        // then
        assertThat(thrown)
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessage("callbackData did not contain 'code'.");
    }

    @Test
    public void shouldValidateThatThirdPartyLoginWasSuccessful() {
        // given
        final Map<String, String> callbackData = new HashMap<>();
        callbackData.put(CALLBACK_DATA_AUTH_CODE_KEY, "value1");

        // when
        final boolean returnedStatus =
                thirdPartyAppCallbackProcessor.isThirdPartyAppLoginSuccessful(callbackData);

        // then
        assertThat(returnedStatus).isTrue();
    }

    @Test
    public void shouldValidateThatThirdPartyLoginWasNotSuccessful() {
        // given
        final Map<String, String> callbackData = new HashMap<>();
        callbackData.put("error", "access_denied");

        // when
        final boolean returnedStatus =
                thirdPartyAppCallbackProcessor.isThirdPartyAppLoginSuccessful(callbackData);

        // then
        assertThat(returnedStatus).isFalse();
    }

    @Test
    public void shouldThrowExceptionIfCallbackDataIsNotPresent() {
        // when
        final Throwable thrown =
                catchThrowable(
                        () -> thirdPartyAppCallbackProcessor.isThirdPartyAppLoginSuccessful(null));

        // then
        assertThat(thrown)
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessage("callbackData did is not present.");
    }

    @Test
    public void shouldThrowExceptionIfCallbackDataIsEmpty() {
        // when
        final Throwable thrown =
                catchThrowable(
                        () ->
                                thirdPartyAppCallbackProcessor.isThirdPartyAppLoginSuccessful(
                                        Collections.emptyMap()));

        // then
        assertThat(thrown)
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessage("callbackData did is not present.");
    }

    @Test
    public void shouldThrowExceptionForUnknownErrorType() {
        // given
        final String errorDescription = "other error";
        final Map<String, String> callbackData = new HashMap<>();
        callbackData.put("error", errorDescription);

        // when
        final Throwable thrown =
                catchThrowable(
                        () ->
                                thirdPartyAppCallbackProcessor.isThirdPartyAppLoginSuccessful(
                                        callbackData));

        // then
        assertThat(thrown)
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessage("Unknown error: UNKNOWN:.");
    }
}
