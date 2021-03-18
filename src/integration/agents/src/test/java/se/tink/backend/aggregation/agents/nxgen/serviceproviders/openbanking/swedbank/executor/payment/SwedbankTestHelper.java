package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.executor.payment;

import static java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.executor.payment.rpc.PaymentAuthorisationResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.executor.payment.rpc.PaymentStatusResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepRequest;
import se.tink.backend.aggregation.nxgen.controllers.signing.SigningStepConstants;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.pair.Pair;
import se.tink.libraries.payment.enums.PaymentStatus;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.transfer.enums.RemittanceInformationType;
import se.tink.libraries.transfer.rpc.RemittanceInformation;

public final class SwedbankTestHelper {
    private static final String NAME = "DUMMY_NAME";
    private static final String ACCOUNT_NUMBER = "12345678901234";
    private static final String AMOUNT = "10.23";
    private static final String CURRENCY = "SEK";
    private static final String EXECUTION_DATE_TIME = "2020-12-20T12:23:45Z";
    private static final LocalDate EXECUTION_DATE =
            LocalDate.parse(EXECUTION_DATE_TIME, ISO_OFFSET_DATE_TIME);
    public static final String INSTRUCTION_ID = "DUMMY_INSTRUCTION_ID";
    public static final String STRONG_AUTH_STATE = "MY_STATE";
    public static final URL REDIRECT_URL = new URL("REDIRECT_URL");
    private static final String REMITTANCE_INFORMATION = "DUMMY_REMITTANCE_INFORMATION";

    static StrongAuthenticationState createStrongAuthenticationStateMock() {
        final StrongAuthenticationState strongAuthenticationState =
                mock(StrongAuthenticationState.class);
        when(strongAuthenticationState.getState()).thenReturn(STRONG_AUTH_STATE);
        return strongAuthenticationState;
    }

    static PaymentMultiStepRequest createPaymentRequest() {
        return createPaymentMultiStepRequest(SigningStepConstants.STEP_INIT);
    }

    static PaymentStatusResponse createPaymentStatusResponseWith(
            boolean readyToSign, String status) {
        final PaymentStatusResponse paymentStatusResponse = mock(PaymentStatusResponse.class);
        when(paymentStatusResponse.isReadyForSigning()).thenReturn(readyToSign);
        when(paymentStatusResponse.getTransactionStatus()).thenReturn(status);
        return paymentStatusResponse;
    }

    static PaymentAuthorisationResponse createPaymentAuthorisationResponse() {
        final PaymentAuthorisationResponse response = mock(PaymentAuthorisationResponse.class);
        when(response.getSelectAuthenticationMethod()).thenReturn("");
        when(response.getScaRedirectUrl()).thenReturn(REDIRECT_URL);
        return response;
    }

    static PaymentMultiStepRequest createPaymentMultiStepRequest(String step) {
        final PaymentMultiStepRequest paymentMultiStepRequestMock =
                mock(PaymentMultiStepRequest.class);
        final Payment paymentMock = createPayment();

        when(paymentMultiStepRequestMock.getStep()).thenReturn(step);
        when(paymentMultiStepRequestMock.getPayment()).thenReturn(paymentMock);

        return paymentMultiStepRequestMock;
    }

    public static Payment createPayment() {
        return createPaymentWithStatus(PaymentStatus.PENDING);
    }

    private static Payment createPaymentWithStatus(PaymentStatus paymentStatus) {
        final Payment paymentMock = createPayment(EXECUTION_DATE);
        final Debtor debtorMock = createDebtor();
        final Creditor creditorMock = createCreditor();
        final se.tink.libraries.transfer.rpc.RemittanceInformation
                unstructuredRemittanceInformationMock = createUnstructuredRemittanceInformation();
        final ExactCurrencyAmount exactCurrencyAmountMock = createExactCurrencyAmount();

        when(paymentMock.getDebtor()).thenReturn(debtorMock);
        when(paymentMock.getCreditor()).thenReturn(creditorMock);
        when(paymentMock.getRemittanceInformation())
                .thenReturn(unstructuredRemittanceInformationMock);
        when(paymentMock.getExactCurrencyAmountFromField()).thenReturn(exactCurrencyAmountMock);
        when(paymentMock.getCurrency()).thenReturn(CURRENCY);
        when(paymentMock.getUniqueId()).thenReturn(INSTRUCTION_ID);
        when(paymentMock.getUniqueIdForUKOPenBanking()).thenReturn(INSTRUCTION_ID);
        when(paymentMock.getExactCurrencyAmount()).thenReturn(exactCurrencyAmountMock);
        when(paymentMock.getStatus()).thenReturn(paymentStatus);

        return paymentMock;
    }

    private static Payment createPayment(LocalDate executionDate) {
        final Payment paymentMock = mock(Payment.class);

        when(paymentMock.getCreditorAndDebtorAccountType())
                .thenReturn(new Pair<>(AccountIdentifierType.SE, AccountIdentifierType.SE));

        when(paymentMock.getExecutionDate()).thenReturn(executionDate);

        return paymentMock;
    }

    private static Debtor createDebtor() {
        final AccountIdentifier accountIdentifierMock = createAccountIdentifier();

        return new Debtor(accountIdentifierMock);
    }

    private static Creditor createCreditor() {
        final Creditor creditorMock = mock(Creditor.class);
        final AccountIdentifier accountIdentifierMock = createAccountIdentifier();

        when(creditorMock.getAccountIdentifier()).thenReturn(accountIdentifierMock);
        when(creditorMock.getAccountIdentifierType()).thenReturn(AccountIdentifierType.SE);
        when(creditorMock.getName()).thenReturn(NAME);
        when(creditorMock.getAccountNumber()).thenReturn(ACCOUNT_NUMBER);

        return creditorMock;
    }

    private static AccountIdentifier createAccountIdentifier() {
        final AccountIdentifier accountIdentifierMock = mock(AccountIdentifier.class);

        when(accountIdentifierMock.getType()).thenReturn(AccountIdentifierType.SE);
        when(accountIdentifierMock.getIdentifier()).thenReturn(ACCOUNT_NUMBER);

        return accountIdentifierMock;
    }

    private static RemittanceInformation createUnstructuredRemittanceInformation() {
        final RemittanceInformation remittanceInformationMock = mock(RemittanceInformation.class);

        when(remittanceInformationMock.getValue()).thenReturn(REMITTANCE_INFORMATION);
        when(remittanceInformationMock.getType())
                .thenReturn(RemittanceInformationType.UNSTRUCTURED);

        return remittanceInformationMock;
    }

    public static ExactCurrencyAmount createExactCurrencyAmount() {
        return new ExactCurrencyAmount(new BigDecimal(AMOUNT), CURRENCY);
    }
}
