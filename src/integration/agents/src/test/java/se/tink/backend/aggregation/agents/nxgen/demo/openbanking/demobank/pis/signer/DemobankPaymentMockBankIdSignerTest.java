package se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.pis.signer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.BankIdException;
import se.tink.backend.aggregation.agents.exceptions.errors.BankIdError;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentAuthorizationException;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.DemobankConstants.ScaApproach;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.DemobankConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.pis.apiclient.DemobankPaymentApiClient;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.pis.storage.DemobankStorage;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.BankIdAuthenticationController;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class DemobankPaymentMockBankIdSignerTest {
    @Rule public ExpectedException exceptionRule = ExpectedException.none();

    private DemobankPaymentMockBankIdSigner signer;

    private DemobankPaymentApiClient apiClient;
    private DemobankStorage apiStorage;

    private BankIdAuthenticationController<String> authenticationController;
    private Credentials credentials;
    private PersistentStorage authStorage;

    private final OAuth2Token mockToken =
            OAuth2Token.createBearer("ACCESS_TOKEN", "REFRESH_TOKEN", 3600);

    @Before
    public void setup() {
        apiClient = mock(DemobankPaymentApiClient.class);
        apiStorage = mock(DemobankStorage.class);
        authenticationController = mock(BankIdAuthenticationController.class);
        credentials = mock(Credentials.class);
        authStorage = mock(PersistentStorage.class);

        signer =
                new DemobankPaymentMockBankIdSigner(
                        apiClient, apiStorage, authenticationController, credentials, authStorage);
    }

    @Test
    public void tokenNotFound() throws PaymentAuthorizationException {
        // when
        when(authStorage.get(StorageKeys.OAUTH2_TOKEN, OAuth2Token.class))
                .thenReturn(Optional.empty());

        // then
        exceptionRule.expect(PaymentAuthorizationException.class);
        exceptionRule.expectMessage(PaymentAuthorizationException.DEFAULT_MESSAGE);

        signer.sign();
    }

    @Test
    public void authenticatorThrows() throws PaymentAuthorizationException {
        BankIdException bankIdException = BankIdError.UNKNOWN.exception();
        String expectedErrorMessage = bankIdException.getError().userMessage().get();

        // when
        doThrow(bankIdException).when(authenticationController).authenticate(any());

        // then
        exceptionRule.expect(PaymentAuthorizationException.class);
        exceptionRule.expectMessage(expectedErrorMessage);

        signer.sign();
    }

    @Test
    public void apiClientThrowsWhenStartAuthorisation() throws PaymentAuthorizationException {
        // when
        when(authStorage.get(StorageKeys.OAUTH2_TOKEN, OAuth2Token.class))
                .thenReturn(Optional.of(mockToken));
        doThrow(HttpResponseException.class)
                .when(apiClient)
                .startPaymentAuthorisation(ScaApproach.BANK_ID);

        // then
        exceptionRule.expect(PaymentAuthorizationException.class);
        exceptionRule.expectMessage(PaymentAuthorizationException.DEFAULT_MESSAGE);

        signer.sign();
    }

    @Test
    public void apiClientThrowsWhenSigning() throws PaymentAuthorizationException {
        // when
        when(authStorage.get(StorageKeys.OAUTH2_TOKEN, OAuth2Token.class))
                .thenReturn(Optional.of(mockToken));
        doThrow(HttpResponseException.class).when(apiClient).signPayment();

        // then
        exceptionRule.expect(PaymentAuthorizationException.class);
        exceptionRule.expectMessage(PaymentAuthorizationException.DEFAULT_MESSAGE);

        signer.sign();
    }
}
