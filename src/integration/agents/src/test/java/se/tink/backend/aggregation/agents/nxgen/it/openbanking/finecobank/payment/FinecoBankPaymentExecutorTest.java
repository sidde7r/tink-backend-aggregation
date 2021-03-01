package se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.payment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import junitparams.custom.combined.CombinedParameters;
import org.iban4j.CountryCode;
import org.iban4j.Iban;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentAuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentCancelledException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.FinecoStorage;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.client.FinecoBankApiClient;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.payment.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.payment.entities.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.payment.entities.LinksEntity;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.payment.enums.FinecoBankPaymentProduct;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.payment.rpc.CreatePaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.payment.rpc.CreatePaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.payment.rpc.GetPaymentAuthStatusResponse;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.payment.rpc.GetPaymentAuthsResponse;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.payment.rpc.GetPaymentStatusResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepConstants;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.Storage;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.payment.enums.PaymentStatus;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.transfer.enums.RemittanceInformationType;
import se.tink.libraries.transfer.rpc.RemittanceInformation;

@RunWith(JUnitParamsRunner.class)
public class FinecoBankPaymentExecutorTest {

    @Rule public MockitoRule initRule = MockitoJUnit.rule();

    private static final String TEST_STATE = "test_state";
    private static final String TEST_REMITTANCE_INFO = "test_remittance_info";
    private static final String TEST_SCA_REDIRECT = "test_sca_redirect";
    private static final String TEST_PAYMENT_ID = "test_payment_id";
    private static final String TEST_TRANSACTION_STATUS = "RCVD";
    private static final String TEST_AUTH_ID = "test_auth_id";

    private static final ExactCurrencyAmount TEST_AMOUNT = ExactCurrencyAmount.inEUR(1);
    private static final String TEST_CREDITOR_NAME = "test_creditor_name";
    private static final String TEST_CREDITOR_IBAN =
            new Iban.Builder().countryCode(CountryCode.IT).buildRandom().toString();
    private static final Creditor TEST_CREDITOR =
            new Creditor(new IbanIdentifier(TEST_CREDITOR_IBAN), TEST_CREDITOR_NAME);

    @Mock private FinecoBankApiClient mockApiClient;
    @Mock private FinecoStorage mockStorage;
    @Mock private StrongAuthenticationState mockStrongAuthenticationState;
    @Mock private SupplementalInformationController mockSupplementalInformationController;

    private FinecoBankPaymentExecutor paymentExecutor;

    @Before
    public void setup() {
        paymentExecutor =
                new FinecoBankPaymentExecutor(
                        mockApiClient,
                        mockStorage,
                        mockStrongAuthenticationState,
                        mockSupplementalInformationController);

        when(mockStrongAuthenticationState.getState()).thenReturn(TEST_STATE);
    }

    @Test
    public void shouldCreatePaymentSuccessfully() {
        // given
        PaymentRequest paymentRequest = new PaymentRequest(buildTestPayment());
        LinksEntity linksEntity = new LinksEntity(TEST_SCA_REDIRECT);
        mockCreatePaymentCall(
                new CreatePaymentResponse(TEST_PAYMENT_ID, TEST_TRANSACTION_STATUS, linksEntity));
        mockGetPaymentAuthsCall(
                new GetPaymentAuthsResponse(Collections.singletonList(TEST_AUTH_ID)));

        // when
        PaymentResponse paymentResponse = paymentExecutor.create(paymentRequest);

        // then
        Payment payment = paymentResponse.getPayment();
        assertThat(payment).isNotNull();

        assertThat(payment.getExactCurrencyAmountFromField()).isEqualTo(TEST_AMOUNT);
        assertThat(payment.getCreditor()).isEqualTo(TEST_CREDITOR);
        assertThat(payment.getUniqueId()).isEqualTo(TEST_PAYMENT_ID);
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.CREATED);
        assertThat(payment.getRemittanceInformation().getValue()).isEqualTo(TEST_REMITTANCE_INFO);
        assertThat(payment.getRemittanceInformation().getType())
                .isEqualTo(RemittanceInformationType.UNSTRUCTURED);

        verify(mockStorage).storePaymentAuthorizationUrl(TEST_PAYMENT_ID, TEST_SCA_REDIRECT);
        verify(mockStorage).storePaymentAuthId(TEST_PAYMENT_ID, TEST_AUTH_ID);
    }

    private Object[] linksEntitiesWithoutScaRedirect() {
        return new Object[] {null, new LinksEntity(), new LinksEntity("")};
    }

    @Test
    @Parameters(method = "linksEntitiesWithoutScaRedirect")
    public void shouldThrowIfNoScaRedirectLink(LinksEntity linksEntity) {
        // given
        PaymentRequest paymentRequest = new PaymentRequest(buildTestPayment());
        mockCreatePaymentCall(
                new CreatePaymentResponse(TEST_PAYMENT_ID, TEST_TRANSACTION_STATUS, linksEntity));
        mockGetPaymentAuthsCall(
                new GetPaymentAuthsResponse(Collections.singletonList(TEST_AUTH_ID)));

        // when
        Throwable throwable = catchThrowable(() -> paymentExecutor.create(paymentRequest));

        // then
        assertThat(throwable)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage(
                        "CreatePaymentResponse received from bank is incorrect, missing SCA link!");
    }

    private Object[] incorrectGetPaymentAuthsResponses() {
        return new Object[] {
            new GetPaymentAuthsResponse(),
            new GetPaymentAuthsResponse(Collections.emptyList()),
            new GetPaymentAuthsResponse(Arrays.asList("1", "2"))
        };
    }

    @Test
    @Parameters(method = "incorrectGetPaymentAuthsResponses")
    public void shouldThrowIfNoAuthIdFound(GetPaymentAuthsResponse getPaymentAuthsResponse) {
        // given
        PaymentRequest paymentRequest = new PaymentRequest(buildTestPayment());
        LinksEntity linksEntity = new LinksEntity(TEST_SCA_REDIRECT);
        mockCreatePaymentCall(
                new CreatePaymentResponse(TEST_PAYMENT_ID, TEST_TRANSACTION_STATUS, linksEntity));
        mockGetPaymentAuthsCall(getPaymentAuthsResponse);

        // when
        Throwable throwable = catchThrowable(() -> paymentExecutor.create(paymentRequest));

        // then
        assertThat(throwable)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage(
                        "CreatePaymentResponse received from bank is incorrect, could not find just one authId!");
    }

    @Test
    public void shouldOpenThirdPartyAppDuringFirstSignStep() throws PaymentException {
        // given
        PaymentMultiStepRequest paymentMultiStepRequest =
                new PaymentMultiStepRequest(
                        buildTestPayment(),
                        new Storage(),
                        AuthenticationStepConstants.STEP_INIT,
                        null,
                        null);

        when(mockStorage.getPaymentAuthorizationUrl(TEST_PAYMENT_ID)).thenReturn(TEST_SCA_REDIRECT);

        // when
        PaymentMultiStepResponse paymentMultiStepResponse =
                paymentExecutor.sign(paymentMultiStepRequest);

        // then
        verify(mockSupplementalInformationController)
                .openThirdPartyAppSync(
                        ThirdPartyAppAuthenticationPayload.of(new URL(TEST_SCA_REDIRECT)));

        assertThat(paymentMultiStepResponse.getStep()).isEqualTo("payment_post_sign_state");
    }

    @Test
    @CombinedParameters({"exempted,finalised", "ACCC,ACCP,ACSC,ACSP,ACTC,ACWC,ACWP,PDNG,ACFC,PATC"})
    public void shouldCompletePostSignStepSuccessfully(String okAuthStatus, String okPaymentStatus)
            throws PaymentException {
        // given
        PaymentMultiStepRequest paymentMultiStepRequest =
                new PaymentMultiStepRequest(
                        buildTestPayment(), new Storage(), "payment_post_sign_state", null, null);

        when(mockApiClient.getPaymentAuthStatus(
                        FinecoBankPaymentProduct.SEPA_CREDIT_TRANSFER,
                        TEST_PAYMENT_ID,
                        TEST_AUTH_ID))
                .thenReturn(new GetPaymentAuthStatusResponse(okAuthStatus));

        when(mockStorage.getPaymentAuthId(TEST_PAYMENT_ID)).thenReturn(TEST_AUTH_ID);

        when(mockApiClient.getPaymentStatus(
                        FinecoBankPaymentProduct.SEPA_CREDIT_TRANSFER, TEST_PAYMENT_ID))
                .thenReturn(new GetPaymentStatusResponse(okPaymentStatus));
        // when

        PaymentMultiStepResponse paymentMultiStepResponse =
                paymentExecutor.sign(paymentMultiStepRequest);

        // then
        assertThat(paymentMultiStepResponse.getStep())
                .isEqualTo(AuthenticationStepConstants.STEP_FINALIZE);
    }

    private Object[] postSignWrongOutcomes() {
        return new Object[] {
            new Object[] {"notFinalStatus", "ACCC", PaymentAuthorizationException.class},
            new Object[] {"finalised", "RCVD", PaymentAuthorizationException.class},
            new Object[] {"finalised", "RJCT", PaymentAuthorizationException.class},
            new Object[] {"finalised", "CANC", PaymentCancelledException.class}
        };
    }

    @Test
    @Parameters(method = "postSignWrongOutcomes")
    public void shouldThrowOnPostSignStep(
            String authStatus, String paymentStatus, Class expectedExceptionClass) {
        // given
        PaymentMultiStepRequest paymentMultiStepRequest =
                new PaymentMultiStepRequest(
                        buildTestPayment(), new Storage(), "payment_post_sign_state", null, null);

        when(mockApiClient.getPaymentAuthStatus(
                        FinecoBankPaymentProduct.SEPA_CREDIT_TRANSFER,
                        TEST_PAYMENT_ID,
                        TEST_AUTH_ID))
                .thenReturn(new GetPaymentAuthStatusResponse(authStatus));

        when(mockStorage.getPaymentAuthId(TEST_PAYMENT_ID)).thenReturn(TEST_AUTH_ID);

        when(mockApiClient.getPaymentStatus(
                        FinecoBankPaymentProduct.SEPA_CREDIT_TRANSFER, TEST_PAYMENT_ID))
                .thenReturn(new GetPaymentStatusResponse(paymentStatus));
        // when

        Throwable throwable = catchThrowable(() -> paymentExecutor.sign(paymentMultiStepRequest));

        // then
        assertThat(throwable).isInstanceOf(expectedExceptionClass);
    }

    private Payment buildTestPayment() {

        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setValue(TEST_REMITTANCE_INFO);
        remittanceInformation.setType(RemittanceInformationType.UNSTRUCTURED);

        return new Payment.Builder()
                .withCreditor(TEST_CREDITOR)
                .withExactCurrencyAmount(TEST_AMOUNT)
                .withCurrency("EUR")
                .withRemittanceInformation(remittanceInformation)
                .withUniqueId(TEST_PAYMENT_ID)
                .build();
    }

    private void mockCreatePaymentCall(CreatePaymentResponse createPaymentResponse) {
        when(mockApiClient.createPayment(
                        CreatePaymentRequest.builder()
                                .creditorAccount(new AccountEntity(TEST_CREDITOR_IBAN))
                                .creditorName(TEST_CREDITOR_NAME)
                                .instructedAmount(new AmountEntity(TEST_AMOUNT))
                                .remittanceInformationUnstructured(TEST_REMITTANCE_INFO)
                                .build(),
                        FinecoBankPaymentProduct.SEPA_CREDIT_TRANSFER,
                        TEST_STATE))
                .thenReturn(createPaymentResponse);
    }

    private void mockGetPaymentAuthsCall(GetPaymentAuthsResponse getPaymentAuthsResponse) {
        when(mockApiClient.getPaymentAuths(
                        FinecoBankPaymentProduct.SEPA_CREDIT_TRANSFER, TEST_PAYMENT_ID))
                .thenReturn(getPaymentAuthsResponse);
    }
}
