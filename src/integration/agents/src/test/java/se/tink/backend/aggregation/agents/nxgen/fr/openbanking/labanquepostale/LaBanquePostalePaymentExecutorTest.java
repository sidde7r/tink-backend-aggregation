package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale;

import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import lombok.SneakyThrows;
import org.assertj.core.api.Assertions;
import org.iban4j.CountryCode;
import org.iban4j.Iban;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentRejectedException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentValidationException;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.LaBanquePostaleConstants.CreditorAgentConstants;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.LaBanquePostaleConstants.PaymentTypeInformation;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.authenticator.rpc.ConfirmPaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.authenticator.rpc.CreatePaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.entities.BeneficiaryEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.entities.CreditTransferTransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.entities.CreditorAgentEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.entities.PaymentEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.entities.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.entities.ConsentApprovalEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.entities.LinksEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.rpc.CreatePaymentResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepConstants;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.payment.enums.PaymentStatus;
import se.tink.libraries.payment.enums.PaymentType;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.transfer.enums.RemittanceInformationType;
import se.tink.libraries.transfer.rpc.RemittanceInformation;

public class LaBanquePostalePaymentExecutorTest {

    private static final String AUTHORIZATION_URL = "http://something.com/redirect?id=123";
    public static final String SOME_URL = "someUrl";

    private LaBanquePostalePaymentExecutor paymentExecutor;
    private LaBanquePostalePaymentApiClient apiClient;
    private SessionStorage sessionStorage;
    private SupplementalInformationHelper supplementalInformationHelper;
    private StrongAuthenticationState strongAuthenticationState;

    @Before
    public void setup() {
        apiClient = mock(LaBanquePostalePaymentApiClient.class);
        sessionStorage = mock(SessionStorage.class);
        strongAuthenticationState = mock(StrongAuthenticationState.class);
        supplementalInformationHelper = mock(SupplementalInformationHelper.class);

        paymentExecutor =
                new LaBanquePostalePaymentExecutor(
                        apiClient,
                        SOME_URL,
                        strongAuthenticationState,
                        supplementalInformationHelper);
    }

    @Test
    public void getCreatePaymentRequestShoulReturnCorrectPaymentRequest() {
        // given
        Iban sourceIban = new Iban.Builder().countryCode(CountryCode.FR).buildRandom();
        Iban destIban = new Iban.Builder().countryCode(CountryCode.FR).buildRandom();
        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setType(RemittanceInformationType.UNSTRUCTURED);
        remittanceInformation.setValue("ReferenceToCreditor");
        PaymentRequest paymentRequest =
                new PaymentRequest(
                        new Payment.Builder()
                                .withCreditor(
                                        new Creditor(
                                                new IbanIdentifier(sourceIban.toString()),
                                                "Payment Creditor"))
                                .withDebtor(new Debtor(new IbanIdentifier(destIban.toString())))
                                .withExactCurrencyAmount(ExactCurrencyAmount.inEUR(1))
                                .withCurrency("EUR")
                                .withRemittanceInformation(remittanceInformation)
                                .withUniqueId(UUID.randomUUID().toString())
                                .build());

        CreditorAgentEntity creditorAgent =
                new CreditorAgentEntity(CreditorAgentConstants.BICFI, CreditorAgentConstants.NAME);
        AccountEntity creditor = AccountEntity.creditorOf(paymentRequest);

        // when
        CreatePaymentRequest result = paymentExecutor.getCreatePaymentRequest(paymentRequest);

        // then
        Assertions.assertThat(result.getNumberOfTransactions()).isEqualTo(1);
        Assertions.assertThat(result.getBeneficiary().getCreditorAgent()).isEqualTo(creditorAgent);
        Assertions.assertThat(result.getBeneficiary().getCreditor().getName())
                .isEqualTo("Payment Creditor");
        Assertions.assertThat(result.getBeneficiary().getCreditorAccount()).isEqualTo(creditor);

        Assertions.assertThat(result.getCreditTransferTransaction().get(0).getAmount().getAmount())
                .isEqualTo("1.0");
        Assertions.assertThat(
                        result.getCreditTransferTransaction().get(0).getAmount().getCurrency())
                .isEqualTo("EUR");

        Assertions.assertThat(
                        result.getCreditTransferTransaction()
                                .get(0)
                                .getBeneficiary()
                                .getCreditorAccount())
                .isEqualTo(creditor);
        Assertions.assertThat(
                        result.getCreditTransferTransaction()
                                .get(0)
                                .getBeneficiary()
                                .getCreditor()
                                .getName())
                .isEqualTo("Payment Creditor");
        Assertions.assertThat(
                        result.getCreditTransferTransaction()
                                .get(0)
                                .getBeneficiary()
                                .getCreditorAgent())
                .isEqualTo(creditorAgent);

        Assertions.assertThat(
                        result.getCreditTransferTransaction()
                                .get(0)
                                .getRemittanceInformation()
                                .getStructured())
                .isEqualTo(null);
        Assertions.assertThat(
                        result.getCreditTransferTransaction()
                                .get(0)
                                .getRemittanceInformation()
                                .getUnstructured()
                                .get(0))
                .isEqualTo("ReferenceToCreditor");

        Assertions.assertThat(result.getChargeBearer())
                .isEqualTo(LaBanquePostaleConstants.CHANGE_BEARER);
        Assertions.assertThat(result.getSupplementaryData().getSuccessfulReportUrl())
                .isEqualTo(SOME_URL);
        Assertions.assertThat(result.getSupplementaryData().getUnsuccessfulReportUrl())
                .isEqualTo(SOME_URL + "&error=authentication_error");
        Assertions.assertThat(
                        result.getSupplementaryData().getAcceptedAuthenticationApproach().get(0))
                .isEqualTo("REDIRECT");

        Assertions.assertThat(result.getPaymentTypeInformation().getCategoryPurpose())
                .isEqualTo(PaymentTypeInformation.CATEGORY_PURPOSE);
        Assertions.assertThat(result.getPaymentTypeInformation().getLocalInstrument())
                .isEqualTo(PaymentTypeInformation.SEPA_STANDARD_CREDIT_TRANSFER);
        Assertions.assertThat(result.getPaymentTypeInformation().getServiceLevel())
                .isEqualTo(PaymentType.SEPA.toString().toUpperCase());
    }

    @SneakyThrows
    @Test
    public void createShouldCallApiClientAndReturnPaymentResponse() {
        // given
        IbanIdentifier cred = new IbanIdentifier("FR383390733324Z58PF2RSRNB11");
        IbanIdentifier deb = new IbanIdentifier("FR385390733324Z58PF2RSRNB11");
        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setValue("ReferenceToCreditor");
        remittanceInformation.setType(RemittanceInformationType.UNSTRUCTURED);
        PaymentRequest paymentRequest =
                new PaymentRequest(
                        new Payment.Builder()
                                .withCreditor(new Creditor(cred))
                                .withDebtor(new Debtor(deb))
                                .withExactCurrencyAmount(ExactCurrencyAmount.inEUR(2))
                                .withCurrency("EUR")
                                .withRemittanceInformation(remittanceInformation)
                                .withUniqueId(UUID.randomUUID().toString())
                                .build());

        String paymentId = "paymentId";
        when(apiClient.createPayment(any()))
                .thenReturn(
                        new CreatePaymentResponse(
                                new LinksEntity(new ConsentApprovalEntity(AUTHORIZATION_URL))));

        when(apiClient.findPaymentId(AUTHORIZATION_URL)).thenReturn(paymentId);

        // when
        PaymentResponse paymentResponse = paymentExecutor.create(paymentRequest);

        // then
        Assertions.assertThat(paymentResponse.getPayment().getStatus())
                .isEqualTo(PaymentStatus.CREATED);
        Assertions.assertThat(paymentResponse.getPayment().getUniqueId()).isEqualTo(paymentId);
        Assertions.assertThat(paymentResponse.getPayment().getType()).isEqualTo(PaymentType.SEPA);
        Assertions.assertThat(paymentResponse.getPayment().getCurrency()).isEqualTo("EUR");
        Assertions.assertThat(
                        paymentResponse.getPayment().getExactCurrencyAmount().getDoubleValue())
                .isEqualTo(2.0);
        Assertions.assertThat(paymentResponse.getPayment().getCreditor().getAccountNumber())
                .isEqualTo("FR383390733324Z58PF2RSRNB11");
        Assertions.assertThat(
                        paymentResponse
                                .getPayment()
                                .getCreditor()
                                .getAccountIdentifier()
                                .getIdentifier())
                .isEqualTo("FR383390733324Z58PF2RSRNB11");
        Assertions.assertThat(
                        paymentResponse.getPayment().getCreditor().getAccountIdentifier().getType())
                .isEqualTo(AccountIdentifierType.IBAN);
        Assertions.assertThat(paymentResponse.getPayment().getDebtor()).isEqualTo(null);

        verify(apiClient).createPayment(any());
    }

    @Test
    public void paymentValidationShouldThrowException() {
        // given
        IbanIdentifier cred = new IbanIdentifier("FR383390733324Z58PF2RSRNB11");
        IbanIdentifier deb = new IbanIdentifier("FR385390733324Z58PF2RSRNB11");
        PaymentRequest paymentRequest =
                new PaymentRequest(
                        new Payment.Builder()
                                .withCreditor(new Creditor(cred))
                                .withDebtor(new Debtor(deb))
                                .withExactCurrencyAmount(ExactCurrencyAmount.inEUR(1))
                                .withCurrency("EUR")
                                .build());

        // when
        Throwable thrown = catchThrowable(() -> paymentExecutor.create(paymentRequest));

        // then
        Assertions.assertThat(thrown)
                .isInstanceOf(PaymentValidationException.class)
                .hasMessage("Transfer amount can't be less than 1.5 EUR.");
    }

    @Test
    public void signShouldOpenThirdPartyAppOnInitWithCallbackParams() throws PaymentException {
        // given
        PaymentMultiStepRequest paymentRequest =
                new PaymentMultiStepRequest(
                        mock(Payment.class),
                        sessionStorage,
                        AuthenticationStepConstants.STEP_INIT,
                        Collections.emptyList());

        Map<String, String> queryMap = new HashMap<>();
        String psuAuthenticationFactor = "psuAuthenticationFactor";
        queryMap.put(
                LaBanquePostalePaymentExecutor.PSU_AUTHORIZATION_FACTOR_KEY,
                psuAuthenticationFactor);
        Optional<Map<String, String>> paramOptional = Optional.of(queryMap);

        String supplementKey = "supplementKey";
        when(strongAuthenticationState.getSupplementalKey()).thenReturn(supplementKey);

        when(supplementalInformationHelper.waitForSupplementalInformation(
                        eq(supplementKey), eq(9L), eq(TimeUnit.MINUTES)))
                .thenReturn(paramOptional);
        paymentExecutor
                .getLaBanquePostalePaymentSigner()
                .setPaymentAuthorizationUrl(AUTHORIZATION_URL);
        // when
        PaymentMultiStepResponse response = paymentExecutor.sign(paymentRequest);

        // then
        Assertions.assertThat(response.getStep())
                .isEqualTo(LaBanquePostalePaymentExecutor.CONFIRM_PAYMENT);
        verify(supplementalInformationHelper).openThirdPartyApp(any());
    }

    @Test
    public void signShouldOpenThirdPartyAppOnInitWithLowercaseCallbackparams()
            throws PaymentException {
        // given
        PaymentMultiStepRequest paymentRequest =
                new PaymentMultiStepRequest(
                        mock(Payment.class),
                        sessionStorage,
                        AuthenticationStepConstants.STEP_INIT,
                        Collections.emptyList());

        Map<String, String> queryMap = new HashMap<>();
        String psuAuthenticationFactor = "psuAuthenticationFactor";
        queryMap.put(
                LaBanquePostalePaymentExecutor.PSU_AUTHORIZATION_FACTOR_KEY.toLowerCase(),
                psuAuthenticationFactor);
        Optional<Map<String, String>> paramOptional = Optional.of(queryMap);

        String supplementKey = "supplementKey";
        when(strongAuthenticationState.getSupplementalKey()).thenReturn(supplementKey);

        when(supplementalInformationHelper.waitForSupplementalInformation(
                        eq(supplementKey), eq(9L), eq(TimeUnit.MINUTES)))
                .thenReturn(paramOptional);
        paymentExecutor
                .getLaBanquePostalePaymentSigner()
                .setPaymentAuthorizationUrl(AUTHORIZATION_URL);
        // when
        PaymentMultiStepResponse response = paymentExecutor.sign(paymentRequest);

        // then
        Assertions.assertThat(response.getStep())
                .isEqualTo(LaBanquePostalePaymentExecutor.CONFIRM_PAYMENT);
        verify(supplementalInformationHelper).openThirdPartyApp(any());
    }

    @Test
    public void signShouldVerifyPaymentStatusOnPostSign() throws PaymentException {
        // given
        PaymentMultiStepRequest paymentRequest =
                new PaymentMultiStepRequest(
                        mock(Payment.class),
                        sessionStorage,
                        LaBanquePostalePaymentExecutor.CONFIRM_PAYMENT,
                        Collections.emptyList());

        String psuAuthorizationFactor = "psuAuthorizationFactor";
        Map<String, String> callback = new HashMap<>();
        callback.put("psuAuthenticationFactor", psuAuthorizationFactor);
        when(apiClient.confirmPayment(null, psuAuthorizationFactor))
                .thenReturn(new ConfirmPaymentResponse(getPaymentEntity("ACSC")));
        paymentExecutor
                .getLaBanquePostalePaymentSigner()
                .setPaymentAuthorizationUrl(AUTHORIZATION_URL);
        paymentExecutor
                .getLaBanquePostalePaymentSigner()
                .setPsuAuthenticationFactorOrThrow(callback);

        // when
        PaymentMultiStepResponse response = paymentExecutor.sign(paymentRequest);

        // then
        Assertions.assertThat(response.getStep())
                .isEqualTo(AuthenticationStepConstants.STEP_FINALIZE);
        verify(apiClient).confirmPayment(null, psuAuthorizationFactor);
    }

    @Test
    public void signShouldThrowExceptionIfPaymentIsPending() throws PaymentRejectedException {
        // given
        PaymentMultiStepRequest paymentRequest =
                new PaymentMultiStepRequest(
                        mock(Payment.class),
                        sessionStorage,
                        LaBanquePostalePaymentExecutor.CONFIRM_PAYMENT,
                        Collections.emptyList());

        String psuAuthorizationFactor = "psuAuthorizationFactor";
        Map<String, String> callback = new HashMap<>();
        callback.put("psuAuthenticationFactor", psuAuthorizationFactor);

        when(apiClient.confirmPayment(null, psuAuthorizationFactor))
                .thenReturn(new ConfirmPaymentResponse(getPaymentEntity("RCVD")));
        paymentExecutor
                .getLaBanquePostalePaymentSigner()
                .setPaymentAuthorizationUrl(AUTHORIZATION_URL);
        paymentExecutor
                .getLaBanquePostalePaymentSigner()
                .setPsuAuthenticationFactorOrThrow(callback);

        // when
        Throwable thrown = catchThrowable(() -> paymentExecutor.sign(paymentRequest));

        // then
        Assertions.assertThat(thrown).isInstanceOf(PaymentRejectedException.class);
        verify(apiClient).confirmPayment(null, psuAuthorizationFactor);
    }

    @Test
    public void signShouldThrowExceptionIfPaymentIsRejected() throws PaymentRejectedException {
        // given
        PaymentMultiStepRequest paymentRequest =
                new PaymentMultiStepRequest(
                        mock(Payment.class),
                        sessionStorage,
                        LaBanquePostalePaymentExecutor.CONFIRM_PAYMENT,
                        Collections.emptyList());

        String psuAuthorizationFactor = "psuAuthorizationFactor";

        when(apiClient.confirmPayment(null, psuAuthorizationFactor))
                .thenReturn(new ConfirmPaymentResponse(getPaymentEntity("RJCT")));
        Map<String, String> callback = new HashMap<>();
        callback.put("psuAuthenticationFactor", psuAuthorizationFactor);
        paymentExecutor
                .getLaBanquePostalePaymentSigner()
                .setPaymentAuthorizationUrl(AUTHORIZATION_URL);
        paymentExecutor
                .getLaBanquePostalePaymentSigner()
                .setPsuAuthenticationFactorOrThrow(callback);

        // when
        Throwable thrown = catchThrowable(() -> paymentExecutor.sign(paymentRequest));

        // then
        Assertions.assertThat(thrown).isInstanceOf(PaymentRejectedException.class);
        verify(apiClient).confirmPayment(null, psuAuthorizationFactor);
    }

    private PaymentEntity getPaymentEntity(String rjct) {
        return PaymentEntity.builder()
                .paymentInformationStatus(rjct)
                .beneficiary(
                        BeneficiaryEntity.builder()
                                .creditorAccount(new AccountEntity("123456789"))
                                .build())
                .creditTransferTransaction(
                        Collections.singletonList(
                                CreditTransferTransactionEntity.builder()
                                        .amount(new AmountEntity("1.0", "EUR"))
                                        .build()))
                .debtorAccount(new AccountEntity("123456789"))
                .build();
    }
}
