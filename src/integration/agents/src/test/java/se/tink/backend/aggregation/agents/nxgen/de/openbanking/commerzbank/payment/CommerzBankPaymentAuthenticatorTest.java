package se.tink.backend.aggregation.agents.nxgen.de.openbanking.commerzbank.payment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field.Key;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersConstants.StorageKeys;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticationController;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.payment.rpc.Payment;

public class CommerzBankPaymentAuthenticatorTest {

    private static final String TEST_PAYMENT_ID = "test_payment_id";
    private Credentials credentials;
    private PersistentStorage persistentStorage;
    private SessionStorage sessionStorage;

    private ThirdPartyAppAuthenticationController mockRedirectAuthenticator;
    private CommerzBankDecoupledPaymentAuthenticator mockDecoupledAuthenticator;

    private Payment mockPayment;

    private CommerzBankPaymentAuthenticator paymentAuthenticator;

    @Before
    public void setup() {
        credentials = new Credentials();
        persistentStorage = new PersistentStorage();
        sessionStorage = new SessionStorage();

        mockRedirectAuthenticator = mock(ThirdPartyAppAuthenticationController.class);
        mockDecoupledAuthenticator = mock(CommerzBankDecoupledPaymentAuthenticator.class);

        mockPayment = mock(Payment.class);
        when(mockPayment.getUniqueId()).thenReturn(TEST_PAYMENT_ID);

        paymentAuthenticator =
                new CommerzBankPaymentAuthenticator(
                        credentials,
                        persistentStorage,
                        sessionStorage,
                        mockRedirectAuthenticator,
                        mockDecoupledAuthenticator);
    }

    @Test
    public void shouldGoWithDecoupledWhenProperStateInSessionStorage() {
        // given
        credentials.setField(Key.USERNAME, "TEST_USERNAME");
        sessionStorage.put(StorageKeys.SCA_APPROACH, "DECOUPLED");

        // when
        paymentAuthenticator.authorizePayment(mockPayment);

        // then
        verify(mockDecoupledAuthenticator).authenticate();
    }

    @Test
    public void shouldFallBackToRedirectIfNoUsernameInCredentials() {
        // given
        sessionStorage.put(StorageKeys.SCA_APPROACH, "DECOUPLED");

        // when
        paymentAuthenticator.authorizePayment(mockPayment);

        // then
        verify(mockRedirectAuthenticator).authenticate(credentials);
        assertThat(persistentStorage.get(StorageKeys.PAYMENT_ID)).isEqualTo(TEST_PAYMENT_ID);
    }

    @Test
    public void shouldFallBackToRedirectIfChosenApproachIsNotDecoupled() {
        // given
        credentials.setField(Key.USERNAME, "TEST_USERNAME");
        sessionStorage.put(StorageKeys.SCA_APPROACH, "NOT_DECOUPLED");

        // when
        paymentAuthenticator.authorizePayment(mockPayment);

        // then
        verify(mockRedirectAuthenticator).authenticate(credentials);
        assertThat(persistentStorage.get(StorageKeys.PAYMENT_ID)).isEqualTo(TEST_PAYMENT_ID);
    }
}
