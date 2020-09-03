package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.ws.rs.core.MultivaluedMap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.IngApiClient;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.IngConstants;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.IngHelper;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.entities.LoginResponseEntity;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.entities.MobileHelloResponseEntity;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.rpc.LoginResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.serialization.utils.SerializationUtils;

@RunWith(MockitoJUnitRunner.class)
public class IngAutoAuthenticatorTest {

    private static final String LOGIN_TEMPORARY_ERROR =
            "{"
                    + "    \"mobileResponse\": {"
                    + "        \"returnCode\": \"NOK\","
                    + "        \"errors\": ["
                    + "            {"
                    + "                \"code\": \"EWS/01/G560-000\","
                    + "                \"text\": \"For technical reasons this service is temporarily unavailable.\""
                    + "            }"
                    + "        ]"
                    + "    }"
                    + "}";

    private static final String LOGIN_UNKNOWN_ERROR =
            "{"
                    + "    \"mobileResponse\": {"
                    + "        \"returnCode\": \"NOK\","
                    + "        \"errors\": ["
                    + "            {"
                    + "                \"code\": \"EWS/01/XYZ-000\","
                    + "                \"text\": \"Some unknown error.\""
                    + "            }"
                    + "        ]"
                    + "    }"
                    + "}";

    private static final String LOGIN_OK =
            "{"
                    + "    \"mobileResponse\": {"
                    + "        \"returnCode\": \"OK\","
                    + "        \"customer\" : {}"
                    + "    }"
                    + "}";

    @Mock private IngApiClient ingApiClient;

    @Mock private IngHelper ingHelper;

    @Mock private PersistentStorage persistentStorage;

    @InjectMocks private IngAutoAuthenticator autoAuthenticator;

    @Test
    public void shouldPersistOnSuccess() {
        mockDependencies();

        LoginResponseEntity okResponse =
                SerializationUtils.deserializeFromString(LOGIN_OK, LoginResponse.class)
                        .getMobileResponse();

        when(ingApiClient.login("http://test/login", "ingId", "vcnmbr", "deviceId"))
                .thenReturn(okResponse);

        autoAuthenticator.autoAuthenticate();

        verify(ingHelper).persist(okResponse);
    }

    @Test
    public void shouldThrowOnTemporaryError() {
        mockDependencies();

        when(ingApiClient.login("http://test/login", "ingId", "vcnmbr", "deviceId"))
                .thenReturn(
                        SerializationUtils.deserializeFromString(
                                        LOGIN_TEMPORARY_ERROR, LoginResponse.class)
                                .getMobileResponse());

        Throwable thrown = catchThrowable(autoAuthenticator::autoAuthenticate);

        assertThat(thrown)
                .isExactlyInstanceOf(BankServiceException.class)
                .hasMessage("error code: EWS/01/G560-000");
    }

    @Test
    public void shouldThrowOnUnknownError() {
        mockDependencies();

        when(ingApiClient.login("http://test/login", "ingId", "vcnmbr", "deviceId"))
                .thenReturn(
                        SerializationUtils.deserializeFromString(
                                        LOGIN_UNKNOWN_ERROR, LoginResponse.class)
                                .getMobileResponse());

        Throwable thrown = catchThrowable(autoAuthenticator::autoAuthenticate);

        assertThat(thrown)
                .isExactlyInstanceOf(LoginException.class)
                .hasMessage(
                        "AutoAuth not successful! Code: EWS/01/XYZ-000 Message: Optional[Some unknown error.]");
    }

    private void mockDependencies() {

        when(ingApiClient.mobileHello()).thenReturn(mock(MobileHelloResponseEntity.class));

        when(persistentStorage.get(IngConstants.Storage.OTP_COUNTER)).thenReturn("1");
        when(persistentStorage.get(IngConstants.Storage.OTP_KEY_HEX))
                .thenReturn("e36bcfc576c3ba6fd4b6c34e12924b41");
        when(persistentStorage.get(IngConstants.Storage.DEVICE_ID)).thenReturn("deviceId");
        when(persistentStorage.get(IngConstants.Storage.VIRTUAL_CARDNUMBER)).thenReturn("vcnmbr");
        when(persistentStorage.get(IngConstants.Storage.ING_ID)).thenReturn("ingId");
        when(persistentStorage.get(IngConstants.Storage.PSN)).thenReturn("psn");

        when(ingHelper.getUrl(IngConstants.RequestNames.AUTHENTICATE))
                .thenReturn("http://test/auth");

        HttpResponse trustBuilderLoginResponse = mockResponseWithHeaders();
        when(ingApiClient.trustBuilderLogin(
                        "http://test/auth", "ingId", "vcnmbr", 85636, "deviceId", "psn"))
                .thenReturn(trustBuilderLoginResponse);

        when(ingHelper.getUrl(IngConstants.RequestNames.LOGON)).thenReturn("http://test/login");
    }

    @SuppressWarnings("unchecked")
    private HttpResponse mockResponseWithHeaders() {
        HttpResponse mockResponse = mock(HttpResponse.class);
        MultivaluedMap<String, String> mockedHeaders = mock(MultivaluedMap.class);
        when(mockResponse.getHeaders()).thenReturn(mockedHeaders);
        return mockResponse;
    }
}
