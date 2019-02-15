package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.IngApiClient;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.IngHelper;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.entities.CryptoInitValues;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.entities.MobileHelloResponseEntity;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.rpc.InitEnrollResponse;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.serialization.utils.SerializationUtils;

import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class IngCardReaderAuthenticatorTest {

    private static final String YOU_DO_NOT_HAVE_ACCESS_TO_ONLINE_SERVICES =
            "{\n"
                    + "\"mobileResponse\": {\n"
                    + "\"returnCode\": \"NOK\",\n"
                    + "\"errors\": [\n"
                    + "{\n"
                    + "\"code\": \"E50/01/G350-220\",\n"
                    + "\"text\": \"You do not have access to this ING online banking service."
                    + " Check if you have selected the right service "
                    + "(Home'Bank or Business'Bank) on the log-in page. (220)\"\n"
                    + "}\n"
                    + "]\n"
                    + "}\n"
                    + "}\n";

    @Mock private IngApiClient apiClient;
    @Mock private PersistentStorage persistentStorage;
    @Mock private IngHelper ingHelper;
    @Mock private CryptoInitValues cryptoInitValues;

    @InjectMocks IngCardReaderAuthenticator ingCardReaderAuthenticator;

    @Rule public ExpectedException expectedEx = ExpectedException.none();

    @Test
    public void throwRegisterDeviceErrorWhenAccessNotAllowedToBankingService()
            throws AuthenticationException, AuthorizationException {
        when(apiClient.mobileHello()).thenReturn(new MobileHelloResponseEntity());
        doNothing()
                .when(apiClient)
                .trustBuilderEnroll(
                        anyString(), anyString(), anyString(), anyString(), anyString());
        HttpResponse initEnrollResponse = mock(HttpResponse.class);

        when(apiClient.initEnroll(any(), anyString(), anyString(), anyString()))
                .thenReturn(initEnrollResponse);
        when(initEnrollResponse.getStatus()).thenReturn(200);
        when(initEnrollResponse.getBody(InitEnrollResponse.class))
                .thenReturn(
                        SerializationUtils.deserializeFromString(
                                YOU_DO_NOT_HAVE_ACCESS_TO_ONLINE_SERVICES,
                                InitEnrollResponse.class));

        try {
            ingCardReaderAuthenticator.initEnroll("ingId", "cardNumber", "otp");
            fail();
        } catch (LoginException e) {
            Assert.assertEquals("Cause: LoginError.REGISTER_DEVICE_ERROR", e.getMessage());
        }
    }
}
