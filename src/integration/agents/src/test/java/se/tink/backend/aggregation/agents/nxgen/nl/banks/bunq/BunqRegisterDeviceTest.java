package se.tink.backend.aggregation.agents.nxgen.nl.banks.bunq;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import agents_platform_agents_framework.org.springframework.test.util.ReflectionTestUtils;
import junitparams.JUnitParamsRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bunq.BunqBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bunq.entities.ErrorResponse;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.libraries.serialization.utils.SerializationUtils;

@RunWith(JUnitParamsRunner.class)
public class BunqRegisterDeviceTest {

    private BunqBaseApiClient apiClient;
    private TinkHttpClient httpClient;
    private HttpResponseException exception;
    private HttpRequest request;
    private HttpResponse repsonse;
    private ErrorResponse errorResponse;

    @Before
    public void setUp() {
        apiClient = mock(BunqBaseApiClient.class);
        httpClient = mock(TinkHttpClient.class);
        repsonse = mock(HttpResponse.class);
        exception = mock(HttpResponseException.class);
    }

    @Test
    public void shouldThrowIncorrectUserCredentials() {
        when(exception.getResponse()).thenReturn(repsonse);
        when(repsonse.getBody(ErrorResponse.class)).thenReturn(getIncorrectUserCredentialsError());

        assertThatThrownBy(
                        () ->
                                ReflectionTestUtils.invokeMethod(
                                        apiClient, "handleException", exception))
                .isInstanceOf(LoginError.INCORRECT_CREDENTIALS.exception().getClass());
    }

    @Test
    public void shouldThrowCredentialsVerficationError() {
        when(exception.getResponse()).thenReturn(repsonse);
        when(repsonse.getBody(ErrorResponse.class)).thenReturn(getCredentialsVerifcationError());

        assertThatThrownBy(
                        () ->
                                ReflectionTestUtils.invokeMethod(
                                        apiClient, "handleException", exception))
                .isInstanceOf(LoginError.INCORRECT_CREDENTIALS.exception().getClass());
    }

    private static ErrorResponse getIncorrectUserCredentialsError() {
        return SerializationUtils.deserializeFromString(
                "{\n"
                        + "    \"Error\":\n"
                        + "    [\n"
                        + "        {\n"
                        + "            \"error_description\": \"User credentials are incorrect. Incorrect API key or IP address.\",\n"
                        + "            \"error_description_translated\": \"User credentials are incorrect. Incorrect API key or IP address.\"\n"
                        + "        }\n"
                        + "    ]\n"
                        + "}",
                ErrorResponse.class);
    }

    private static ErrorResponse getCredentialsVerifcationError() {
        return SerializationUtils.deserializeFromString(
                "{\n"
                        + "    \"Error\":\n"
                        + "    [\n"
                        + "        {\n"
                        + "            \"error_description\": \"The operation could not be completed. Please try again.\",\n"
                        + "            \"error_description_translated\": \"The operation could not be completed. Please try again.\"\n"
                        + "        }\n"
                        + "    ]\n"
                        + "}",
                ErrorResponse.class);
    }
}
