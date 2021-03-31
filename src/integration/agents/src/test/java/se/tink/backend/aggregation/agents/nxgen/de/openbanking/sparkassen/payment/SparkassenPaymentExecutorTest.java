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
import static se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.payment.PaymentTestUtil.PAYMENT_AUTHORIZATION_RESPONSE;
import static se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.payment.PaymentTestUtil.PAYMENT_SCA_AUTHENTICATION_STATUS_RESPONSE;
import static se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.payment.PaymentTestUtil.PAYMENT_SCA_METHOD_SELECTION_RESPONSE;
import static se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.payment.PaymentTestUtil.PAYMENT_STATUS_CANCELED_RESPONSE;
import static se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.payment.PaymentTestUtil.PAYMENT_STATUS_REJECTED_RESPONSE;
import static se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.payment.PaymentTestUtil.PAYMENT_STATUS_SIGNED_RESPONSE;

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
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.i18n.LocalizableKey;

public class SparkassenPaymentExecutorTest {

    private SparkassenApiClient apiClient;
    private PaymentAuthenticator paymentAuthenticator;
    BasePaymentExecutor paymentExecutor;
    private Credentials credentials;

    private SupplementalInformationHelper supplementalInformationHelper;
    private SparkassenStorage persistentStorage;
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

        paymentAuthenticator =
                new SparkassenPaymentAuthenticator(
                        catalog,
                        supplementalInformationHelper,
                        apiClient,
                        persistentStorage,
                        credentials);
        when(catalog.getString(any(LocalizableKey.class))).thenReturn("");
        paymentTestUtil = new PaymentTestUtil(supplementalInformationHelper, apiClient);

        paymentExecutor =
                new BasePaymentExecutor(
                        apiClient,
                        paymentAuthenticator,
                        credentials,
                        PaymentAuthenticationMode.EMBEDDED);
    }

    @Test
    public void testPaymentCreate() throws PaymentException {
        // given
        paymentTestUtil.whenCreatePaymentAuthorizationReturn(PAYMENT_AUTHORIZATION_RESPONSE);
        paymentTestUtil.whenSelectPaymentAuthorizationMethodReturn(
                PAYMENT_SCA_METHOD_SELECTION_RESPONSE);
        paymentTestUtil.whenCreatePaymentFinalizeAuthorizationReturn(
                PAYMENT_SCA_AUTHENTICATION_STATUS_RESPONSE);
        paymentTestUtil.whenSupplementalInformationHelperReturn(SELECT_AUTH_METHOD_OK);

        PaymentRequest paymentRequest = paymentTestUtil.createPaymentRequest();
        paymentTestUtil.whenCreatePaymentReturn(paymentRequest);

        // when
        paymentExecutor.create(paymentRequest);

        // then
        paymentTestUtil.verifyInitializePaymentAuthorizationCalled();
        paymentTestUtil.verifySelectPaymentAuthorizationMethodCalled();
        paymentTestUtil.verifyFinalizePaymentAuthorizationCalled();
        paymentTestUtil.verifyAskSupplementalInformationCalled(2);
        paymentTestUtil.verifyCreatePaymentCalled();
        verifyNoMoreInteractions(apiClient);
        verifyNoMoreInteractions(supplementalInformationHelper);
    }

    @Test
    public void testPaymentSignSuccess() throws PaymentException {
        // given
        PaymentRequest paymentRequest = paymentTestUtil.createPaymentRequest();
        paymentTestUtil.whenFetchPaymentStatusReturn(
                paymentRequest, PAYMENT_STATUS_SIGNED_RESPONSE);

        // when
        paymentExecutor.sign(PaymentMultiStepRequest.of(paymentTestUtil.createPaymentResponse()));

        // then

        paymentTestUtil.verifyFetchPaymentStatusCalled();
        verifyNoMoreInteractions(apiClient);
        verifyNoMoreInteractions(supplementalInformationHelper);
    }

    @Test
    public void testPaymentSignRejected() throws PaymentException {
        // given
        PaymentRequest paymentRequest = paymentTestUtil.createPaymentRequest();
        paymentTestUtil.whenFetchPaymentStatusReturn(
                paymentRequest, PAYMENT_STATUS_REJECTED_RESPONSE);

        // when
        Throwable throwable =
                catchThrowable(
                        () ->
                                paymentExecutor.sign(
                                        PaymentMultiStepRequest.of(
                                                paymentTestUtil.createPaymentResponse())));

        // then

        assertThat(throwable).isInstanceOf(PaymentRejectedException.class);
        paymentTestUtil.verifyFetchPaymentStatusCalled();
        verifyNoMoreInteractions(apiClient);
        verifyNoMoreInteractions(supplementalInformationHelper);
    }

    @Test
    public void testPaymentSignCancelled() throws PaymentException {
        // given
        PaymentRequest paymentRequest = paymentTestUtil.createPaymentRequest();
        paymentTestUtil.whenFetchPaymentStatusReturn(
                paymentRequest, PAYMENT_STATUS_CANCELED_RESPONSE);

        // when
        Throwable throwable =
                catchThrowable(
                        () ->
                                paymentExecutor.sign(
                                        PaymentMultiStepRequest.of(
                                                paymentTestUtil.createPaymentResponse())));

        // then

        assertThat(throwable).isInstanceOf(PaymentCancelledException.class);
        paymentTestUtil.verifyFetchPaymentStatusCalled();
        verifyNoMoreInteractions(apiClient);
        verifyNoMoreInteractions(supplementalInformationHelper);
    }
}
