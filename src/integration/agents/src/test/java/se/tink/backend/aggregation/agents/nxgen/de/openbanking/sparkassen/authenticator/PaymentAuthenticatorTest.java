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
import static se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.payment.PaymentTestHelper.PAYMENT_AUTHORIZATION_RESPONSE_WITH_MULTIPLE_SCA_METHOD;
import static se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.payment.PaymentTestHelper.PAYMENT_SCA_AUTHENTICATION_FAILED_STATUS_RESPONSE;
import static se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.payment.PaymentTestHelper.PAYMENT_SCA_AUTHENTICATION_STATUS_RESPONSE;
import static se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.payment.PaymentTestHelper.PAYMENT_SCA_METHOD_SELECTION_RESPONSE;
import static se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.payment.PaymentTestHelper.SCA_LINKS;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.SparkassenApiClient;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.SparkassenStorage;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.payment.PaymentTestHelper;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.i18n.LocalizableKey;

public class PaymentAuthenticatorTest {

    private SupplementalInformationController supplementalInformationController;
    private SparkassenApiClient apiClient;
    private SparkassenStorage storage;

    private SparkassenPaymentAuthenticator authenticator;
    private Credentials credentials;
    PaymentTestHelper paymentTestHelper;

    @Before
    public void setup() {
        Catalog catalog = mock(Catalog.class);
        supplementalInformationController = mock(SupplementalInformationController.class);
        apiClient = mock(SparkassenApiClient.class);
        storage = new SparkassenStorage(new PersistentStorage());

        credentials = new Credentials();
        credentials.setType(CredentialsTypes.PASSWORD);
        credentials.setField(Field.Key.USERNAME, USERNAME);
        credentials.setField(Field.Key.PASSWORD, PASSWORD);
        when(catalog.getString(any(LocalizableKey.class))).thenReturn("");
        authenticator =
                new SparkassenPaymentAuthenticator(
                        apiClient,
                        supplementalInformationController,
                        storage,
                        credentials,
                        catalog);
        paymentTestHelper = new PaymentTestHelper(supplementalInformationController, apiClient);
    }

    @Test
    public void shouldCompletePaymentAuthenticationWithSelectingSCAMethod() {
        // given
        paymentTestHelper.whenCreatePaymentAuthorizationReturn(
                PAYMENT_AUTHORIZATION_RESPONSE_WITH_MULTIPLE_SCA_METHOD);
        paymentTestHelper.whenSelectPaymentAuthorizationMethodReturn(
                PAYMENT_SCA_METHOD_SELECTION_RESPONSE);
        paymentTestHelper.whenCreatePaymentFinalizeAuthorizationReturn(
                PAYMENT_SCA_AUTHENTICATION_STATUS_RESPONSE);
        paymentTestHelper.whenSupplementalInformationControllerReturn(SELECT_AUTH_METHOD_OK);

        // when
        authenticator.authenticatePayment(SCA_LINKS);

        // then
        paymentTestHelper.verifyInitializePaymentAuthorizationCalled();
        paymentTestHelper.verifySelectPaymentAuthorizationMethodCalled();
        paymentTestHelper.verifyFinalizePaymentAuthorizationCalled();
        paymentTestHelper.verifyAskSupplementalInformationCalled(2);
        verifyNoMoreInteractions(apiClient);
        verifyNoMoreInteractions(supplementalInformationController);
    }

    @Test
    public void shouldThrowPaymentAuthenticationErrorWhenScaAuthenticationFailed() {
        // given
        paymentTestHelper.whenCreatePaymentAuthorizationReturn(
                PAYMENT_AUTHORIZATION_RESPONSE_WITH_MULTIPLE_SCA_METHOD);
        paymentTestHelper.whenSelectPaymentAuthorizationMethodReturn(
                PAYMENT_SCA_METHOD_SELECTION_RESPONSE);
        paymentTestHelper.whenCreatePaymentFinalizeAuthorizationReturn(
                PAYMENT_SCA_AUTHENTICATION_FAILED_STATUS_RESPONSE);
        paymentTestHelper.whenSupplementalInformationControllerReturn(SELECT_AUTH_METHOD_OK);

        // when
        Throwable throwable = catchThrowable(() -> authenticator.authenticatePayment(SCA_LINKS));

        // then
        assertThat(throwable).isInstanceOf(LoginException.class);
    }
}
