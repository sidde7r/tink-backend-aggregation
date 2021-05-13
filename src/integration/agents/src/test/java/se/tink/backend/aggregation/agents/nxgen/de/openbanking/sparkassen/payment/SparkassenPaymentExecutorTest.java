package se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.payment;

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
import static se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.payment.PaymentTestHelper.PAYMENT_AUTHORIZATION_RESPONSE_WITH_SINGLE_SCA_METHOD;
import static se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.payment.PaymentTestHelper.PAYMENT_SCA_AUTHENTICATION_STATUS_RESPONSE;
import static se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.payment.PaymentTestHelper.PAYMENT_SCA_EXEMPTION_RESPONSE;
import static se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.payment.PaymentTestHelper.PAYMENT_SCA_METHOD_SELECTION_RESPONSE;
import static se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.payment.PaymentTestHelper.PAYMENT_STATUS_CANCELED_RESPONSE;
import static se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.payment.PaymentTestHelper.PAYMENT_STATUS_REJECTED_RESPONSE;
import static se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.payment.PaymentTestHelper.PAYMENT_STATUS_SIGNED_RESPONSE;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentCancelledException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentRejectedException;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.SparkassenApiClient;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.SparkassenStorage;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.SparkassenPaymentAuthenticator;
import se.tink.backend.aggregation.agents.utils.berlingroup.payment.BasePaymentExecutor;
import se.tink.backend.aggregation.agents.utils.berlingroup.payment.PaymentAuthenticator;
import se.tink.backend.aggregation.agents.utils.berlingroup.payment.enums.PaymentAuthenticationMode;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepConstants;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.i18n.LocalizableKey;

public class SparkassenPaymentExecutorTest {

    private SparkassenApiClient apiClient;
    private PaymentAuthenticator paymentAuthenticator;
    BasePaymentExecutor paymentExecutor;
    private Credentials credentials;

    private SupplementalInformationController supplementalInformationController;
    private SparkassenStorage storage;
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

        paymentAuthenticator =
                new SparkassenPaymentAuthenticator(
                        apiClient,
                        supplementalInformationController,
                        storage,
                        credentials,
                        catalog);
        when(catalog.getString(any(LocalizableKey.class))).thenReturn("");
        paymentTestHelper = new PaymentTestHelper(supplementalInformationController, apiClient);

        paymentExecutor =
                new BasePaymentExecutor(
                        apiClient,
                        paymentAuthenticator,
                        credentials,
                        PaymentAuthenticationMode.EMBEDDED);
    }

    @Test
    public void shouldCreatePaymentWithMultipleScaMethods() throws PaymentException {
        // given
        paymentTestHelper.whenCreatePaymentAuthorizationReturn(
                PAYMENT_AUTHORIZATION_RESPONSE_WITH_MULTIPLE_SCA_METHOD);
        paymentTestHelper.whenSelectPaymentAuthorizationMethodReturn(
                PAYMENT_SCA_METHOD_SELECTION_RESPONSE);
        paymentTestHelper.whenCreatePaymentFinalizeAuthorizationReturn(
                PAYMENT_SCA_AUTHENTICATION_STATUS_RESPONSE);
        paymentTestHelper.whenSupplementalInformationControllerReturn(SELECT_AUTH_METHOD_OK);

        PaymentRequest paymentRequest = paymentTestHelper.createPaymentRequest();
        paymentTestHelper.whenCreatePaymentReturn(paymentRequest);

        // when
        paymentExecutor.create(paymentRequest);

        // then
        paymentTestHelper.verifyInitializePaymentAuthorizationCalled();
        paymentTestHelper.verifySelectPaymentAuthorizationMethodCalled();
        paymentTestHelper.verifyFinalizePaymentAuthorizationCalled();
        paymentTestHelper.verifyAskSupplementalInformationCalled(2);
        paymentTestHelper.verifyCreatePaymentCalled();
        verifyNoMoreInteractions(apiClient);
        verifyNoMoreInteractions(supplementalInformationController);
    }

    @Test
    public void shouldCreatePaymentWithSingleScaMethod() throws PaymentException {
        // given
        paymentTestHelper.whenCreatePaymentAuthorizationReturn(
                PAYMENT_AUTHORIZATION_RESPONSE_WITH_SINGLE_SCA_METHOD);
        paymentTestHelper.whenSelectPaymentAuthorizationMethodReturn(
                PAYMENT_SCA_METHOD_SELECTION_RESPONSE);
        paymentTestHelper.whenCreatePaymentFinalizeAuthorizationReturn(
                PAYMENT_SCA_AUTHENTICATION_STATUS_RESPONSE);
        paymentTestHelper.whenSupplementalInformationControllerReturn(SELECT_AUTH_METHOD_OK);

        PaymentRequest paymentRequest = paymentTestHelper.createPaymentRequest();
        paymentTestHelper.whenCreatePaymentReturn(paymentRequest);

        // when
        paymentExecutor.create(paymentRequest);

        // then
        paymentTestHelper.verifyInitializePaymentAuthorizationCalled();
        paymentTestHelper.verifyFinalizePaymentAuthorizationCalled();
        paymentTestHelper.verifyAskSupplementalInformationCalled(1);
        paymentTestHelper.verifyCreatePaymentCalled();
        verifyNoMoreInteractions(apiClient);
        verifyNoMoreInteractions(supplementalInformationController);
    }

    @Test
    public void shouldCreatePaymentWithSCAExemption() throws PaymentException {
        // given
        paymentTestHelper.whenCreatePaymentAuthorizationReturn(PAYMENT_SCA_EXEMPTION_RESPONSE);
        paymentTestHelper.whenSupplementalInformationControllerReturn(SELECT_AUTH_METHOD_OK);
        PaymentRequest paymentRequest = paymentTestHelper.createPaymentRequest();
        paymentTestHelper.whenCreatePaymentReturn(paymentRequest);

        // when
        paymentExecutor.create(paymentRequest);

        // then
        paymentTestHelper.verifyInitializePaymentAuthorizationCalled();
        paymentTestHelper.verifyAskSupplementalInformationCalled(0);
        paymentTestHelper.verifyCreatePaymentCalled();
        verifyNoMoreInteractions(apiClient);
        verifyNoMoreInteractions(supplementalInformationController);
    }

    @Test
    public void shouldSignAndPaymentIsSuccessful() throws PaymentException {
        // given
        PaymentRequest paymentRequest = paymentTestHelper.createPaymentRequest();
        paymentTestHelper.whenFetchPaymentStatusReturn(
                paymentRequest, PAYMENT_STATUS_SIGNED_RESPONSE);

        // when
        PaymentMultiStepResponse paymentMultiStepResponse =
                paymentExecutor.sign(
                        PaymentMultiStepRequest.of(paymentTestHelper.createPaymentResponse()));
        Assert.assertEquals(
                AuthenticationStepConstants.STEP_FINALIZE, paymentMultiStepResponse.getStep());
        // then

        paymentTestHelper.verifyFetchPaymentStatusCalled();
        verifyNoMoreInteractions(apiClient);
        verifyNoMoreInteractions(supplementalInformationController);
    }

    @Test
    public void shouldSignAndPaymentIsRejected() {
        // given
        PaymentRequest paymentRequest = paymentTestHelper.createPaymentRequest();
        paymentTestHelper.whenFetchPaymentStatusReturn(
                paymentRequest, PAYMENT_STATUS_REJECTED_RESPONSE);

        // when
        Throwable throwable =
                catchThrowable(
                        () ->
                                paymentExecutor.sign(
                                        PaymentMultiStepRequest.of(
                                                paymentTestHelper.createPaymentResponse())));

        // then

        assertThat(throwable).isInstanceOf(PaymentRejectedException.class);
        paymentTestHelper.verifyFetchPaymentStatusCalled();
        verifyNoMoreInteractions(apiClient);
        verifyNoMoreInteractions(supplementalInformationController);
    }

    @Test
    public void shouldSignAndPaymentIsCancelled() {
        // given
        PaymentRequest paymentRequest = paymentTestHelper.createPaymentRequest();
        paymentTestHelper.whenFetchPaymentStatusReturn(
                paymentRequest, PAYMENT_STATUS_CANCELED_RESPONSE);

        // when
        Throwable throwable =
                catchThrowable(
                        () ->
                                paymentExecutor.sign(
                                        PaymentMultiStepRequest.of(
                                                paymentTestHelper.createPaymentResponse())));

        // then

        assertThat(throwable).isInstanceOf(PaymentCancelledException.class);
        paymentTestHelper.verifyFetchPaymentStatusCalled();
        verifyNoMoreInteractions(apiClient);
        verifyNoMoreInteractions(supplementalInformationController);
    }
}
