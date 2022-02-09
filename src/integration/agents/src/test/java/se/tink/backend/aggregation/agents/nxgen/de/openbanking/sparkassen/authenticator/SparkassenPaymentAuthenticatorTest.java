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
import static se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.payment.PaymentTestHelper.PAYMENT_SCA_METHOD_CHIP_TAN_SELECTION_RESPONSE;
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
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.detail.ScaMethodFilter;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.detail.SparkassenDecoupledFieldBuilder;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.detail.SparkassenEmbeddedFieldBuilderPayments;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.detail.SparkassenIconUrlMapper;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.payment.PaymentTestHelper;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.i18n_aggregation.Catalog;
import se.tink.libraries.i18n_aggregation.LocalizableKey;

public class SparkassenPaymentAuthenticatorTest {
    private SupplementalInformationController supplementalInformationController;
    private SparkassenApiClient apiClient;
    private SparkassenStorage storage;
    private SparkassenPaymentAuthenticator paymentAuthenticator;
    private Credentials credentials;
    private PaymentTestHelper paymentTestHelper;

    @Before
    public void initSetup() {
        Catalog catalog = mock(Catalog.class);
        supplementalInformationController = mock(SupplementalInformationController.class);
        apiClient = mock(SparkassenApiClient.class);
        storage = new SparkassenStorage(new PersistentStorage());

        credentials = new Credentials();
        credentials.setType(CredentialsTypes.PASSWORD);
        credentials.setField(Field.Key.USERNAME, USERNAME);
        credentials.setField(Field.Key.PASSWORD, PASSWORD);
        when(catalog.getString(any(LocalizableKey.class))).thenReturn("");
        paymentAuthenticator =
                new SparkassenPaymentAuthenticator(
                        apiClient,
                        supplementalInformationController,
                        storage,
                        credentials,
                        new SparkassenEmbeddedFieldBuilderPayments(
                                catalog, new SparkassenIconUrlMapper()),
                        new SparkassenDecoupledFieldBuilder(catalog),
                        new ScaMethodFilter());
        paymentTestHelper = new PaymentTestHelper(supplementalInformationController, apiClient);
    }

    @Test
    public void should_complete_payment_authentication_with_selecting_PushTan_SCAMethod() {
        // given
        paymentTestHelper.whenCreatePaymentAuthorizationReturn(
                PAYMENT_AUTHORIZATION_RESPONSE_WITH_MULTIPLE_SCA_METHOD);
        paymentTestHelper.whenSelectPaymentAuthorizationMethodReturn(
                PAYMENT_SCA_METHOD_SELECTION_RESPONSE);
        paymentTestHelper.whenCreatePaymentFinalizeAuthorizationReturn(
                PAYMENT_SCA_AUTHENTICATION_STATUS_RESPONSE);
        paymentTestHelper.whenSupplementalInformationControllerReturn(SELECT_AUTH_METHOD_OK, 1);

        // when
        paymentAuthenticator.authenticatePayment(SCA_LINKS);

        // then
        paymentTestHelper.verifyInitializePaymentAuthorizationCalled();
        paymentTestHelper.verifySelectPaymentAuthorizationMethodCalled();
        paymentTestHelper.verifyFinalizePaymentAuthorizationCalled();
        paymentTestHelper.verifyAskSupplementalInformationCalled(2);
        verifyNoMoreInteractions(apiClient);
        verifyNoMoreInteractions(supplementalInformationController);
    }

    @Test
    public void should_complete_payment_authentication_with_selecting_ChipTan_SCAMethod() {
        // given
        paymentTestHelper.whenCreatePaymentAuthorizationReturn(
                PAYMENT_AUTHORIZATION_RESPONSE_WITH_MULTIPLE_SCA_METHOD);
        paymentTestHelper.whenSelect2ndOptionPaymentAuthorizationMethodReturn(
                PAYMENT_SCA_METHOD_CHIP_TAN_SELECTION_RESPONSE);
        paymentTestHelper.whenCreatePaymentFinalizeAuthorizationReturn(
                PAYMENT_SCA_AUTHENTICATION_STATUS_RESPONSE);
        paymentTestHelper.whenSupplementalInformationControllerReturn(
                PAYMENT_SCA_METHOD_CHIP_TAN_SELECTION_RESPONSE, 2);

        // when
        Throwable throwable =
                catchThrowable(() -> paymentAuthenticator.authenticatePayment(SCA_LINKS));

        // then
        assertThat(throwable).isNull();
    }

    @Test
    public void should_throw_payment_AuthenticationError_when_Sca_Authentication_Failed() {
        // given
        paymentTestHelper.whenCreatePaymentAuthorizationReturn(
                PAYMENT_AUTHORIZATION_RESPONSE_WITH_MULTIPLE_SCA_METHOD);
        paymentTestHelper.whenSelectPaymentAuthorizationMethodReturn(
                PAYMENT_SCA_METHOD_SELECTION_RESPONSE);
        paymentTestHelper.whenCreatePaymentFinalizeAuthorizationReturn(
                PAYMENT_SCA_AUTHENTICATION_FAILED_STATUS_RESPONSE);
        paymentTestHelper.whenSupplementalInformationControllerReturn(SELECT_AUTH_METHOD_OK, 1);

        // when
        Throwable throwable =
                catchThrowable(() -> paymentAuthenticator.authenticatePayment(SCA_LINKS));

        // then
        assertThat(throwable).isInstanceOf(LoginException.class);
    }
}
