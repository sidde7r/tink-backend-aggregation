package se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.AuthenticatorTestData.PASSWORD;
import static se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.AuthenticatorTestData.SELECT_AUTH_METHOD_OK;
import static se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.AuthenticatorTestData.USERNAME;
import static se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.payment.PaymentTestUtil.PAYMENT_AUTHORIZATION_RESPONSE;
import static se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.payment.PaymentTestUtil.PAYMENT_CREATE_RESPONSE;
import static se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.payment.PaymentTestUtil.PAYMENT_SCA_AUTHENTICATION_FAILED_STATUS_RESPONSE;
import static se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.payment.PaymentTestUtil.PAYMENT_SCA_AUTHENTICATION_STATUS_RESPONSE;
import static se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.payment.PaymentTestUtil.PAYMENT_SCA_METHOD_SELECTION_RESPONSE;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.SparkassenApiClient;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.SparkassenStorage;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.payment.PaymentTestUtil;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.i18n.LocalizableKey;

public class PaymentAuthenticatorTest {

    private SupplementalInformationHelper supplementalInformationHelper;
    private SparkassenApiClient apiClient;
    private SparkassenStorage persistentStorage;

    private SparkassenPaymentAuthenticator authenticator;
    private Credentials credentials;
    PaymentTestUtil paymentTestUtil;

    @Before
    public void setup() {
        Catalog catalog = mock(Catalog.class);
        supplementalInformationHelper = mock(SupplementalInformationHelper.class);
        apiClient = mock(SparkassenApiClient.class);
        persistentStorage = new SparkassenStorage(new PersistentStorage());

        credentials = new Credentials();
        credentials.setType(CredentialsTypes.PASSWORD);
        credentials.setField(Field.Key.USERNAME, USERNAME);
        credentials.setField(Field.Key.PASSWORD, PASSWORD);
        when(catalog.getString(any(LocalizableKey.class))).thenReturn("");
        authenticator =
                new SparkassenPaymentAuthenticator(
                        catalog,
                        supplementalInformationHelper,
                        apiClient,
                        persistentStorage,
                        credentials);
        paymentTestUtil = new PaymentTestUtil(supplementalInformationHelper, apiClient);
    }

    @Test
    public void shouldCompletePaymentAuthenticationWithSelectingSCAMethod() {
        // given
        paymentTestUtil.whenCreatePaymentAuthorizationReturn(PAYMENT_AUTHORIZATION_RESPONSE);
        paymentTestUtil.whenSelectPaymentAuthorizationMethodReturn(
                PAYMENT_SCA_METHOD_SELECTION_RESPONSE);
        paymentTestUtil.whenCreatePaymentFinalizeAuthorizationReturn(
                PAYMENT_SCA_AUTHENTICATION_STATUS_RESPONSE);
        paymentTestUtil.whenSupplementalInformationHelperReturn(SELECT_AUTH_METHOD_OK);

        // when
        authenticator.authenticatePayment(credentials, PAYMENT_CREATE_RESPONSE);

        // then
        paymentTestUtil.verifyInitializePaymentAuthorizationCalled();
        paymentTestUtil.verifySelectPaymentAuthorizationMethodCalled();
        paymentTestUtil.verifyFinalizePaymentAuthorizationCalled();
        paymentTestUtil.verifyAskSupplementalInformationCalled(2);
        verifyNoMoreInteractions(apiClient);
        verifyNoMoreInteractions(supplementalInformationHelper);
    }

    @Test
    public void shouldThrowPaymentAuthenticationErrorWhenScaAuthenticationFailed() {
        // given
        paymentTestUtil.whenCreatePaymentAuthorizationReturn(PAYMENT_AUTHORIZATION_RESPONSE);
        paymentTestUtil.whenSelectPaymentAuthorizationMethodReturn(
                PAYMENT_SCA_METHOD_SELECTION_RESPONSE);
        paymentTestUtil.whenCreatePaymentFinalizeAuthorizationReturn(
                PAYMENT_SCA_AUTHENTICATION_FAILED_STATUS_RESPONSE);
        paymentTestUtil.whenSupplementalInformationHelperReturn(SELECT_AUTH_METHOD_OK);

        // when
        Throwable throwable =
                catchThrowable(
                        () ->
                                authenticator.authenticatePayment(
                                        credentials, PAYMENT_CREATE_RESPONSE));

        // then
        assertThat(throwable).isInstanceOf(LoginException.class);
    }
}
