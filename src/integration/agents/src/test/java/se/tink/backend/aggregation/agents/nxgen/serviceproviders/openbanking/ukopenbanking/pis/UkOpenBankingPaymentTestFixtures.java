package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis;

import static java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.dto.common.CreditorAccount;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.dto.common.DebtorAccount;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.dto.common.FundsConfirmationResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.dto.common.InstructedAmount;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.dto.common.RemittanceInformation;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.dto.domestic.DomesticPaymentConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.dto.domestic.DomesticPaymentConsentResponseData;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.dto.domestic.DomesticPaymentInitiation;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.dto.domestic.DomesticPaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.dto.domestic.DomesticPaymentResponseData;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.dto.domesticscheduled.DomesticScheduledPaymentConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.dto.domesticscheduled.DomesticScheduledPaymentConsentResponseData;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.dto.domesticscheduled.DomesticScheduledPaymentInitiation;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.dto.domesticscheduled.DomesticScheduledPaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.dto.domesticscheduled.DomesticScheduledPaymentResponseData;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.backend.aggregation.nxgen.storage.Storage;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.pair.Pair;
import se.tink.libraries.payment.enums.PaymentStatus;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.transfer.enums.RemittanceInformationType;

public class UkOpenBankingPaymentTestFixtures {

    public static final String PAYMENT_ID = "DUMMY_PAYMENT_ID";
    public static final String CONSENT_ID = "DUMMY_CONSENT_ID";
    public static final String END_TO_END_ID = "DUMMY_END_TO_END_ID";
    public static final String INSTRUCTION_ID = "DUMMY_INSTRUCTION_ID";
    public static final String NAME = "DUMMY_NAME";
    public static final String AMOUNT = "10.23";
    public static final String CURRENCY = "GBP";

    private static final String ACCOUNT_NUMBER = "12345678901234";
    private static final String REMITTANCE_INFORMATION = "DUMMY_REMITTANCE_INFORMATION";
    private static final String RESPONSE_STATUS = "Authorised";
    private static final String RESPONSE_STATUS_FOR_SCHEDULED_PAYMENTS = "InitiationPending";
    private static final String EXECUTION_DATE_TIME = "2020-12-20T12:23:45Z";
    private static final LocalDate EXECUTION_DATE =
            LocalDate.parse(EXECUTION_DATE_TIME, ISO_OFFSET_DATE_TIME);
    private static final String SCHEME_NAME = "UK.OBIE.SortCodeAccountNumber";

    public static Payment createPayment() {
        final Payment paymentMock = createTodayPayment(EXECUTION_DATE);
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
        when(paymentMock.getStatus()).thenReturn(PaymentStatus.PENDING);

        return paymentMock;
    }

    public static DebtorAccount createDebtorAccount() {
        return DebtorAccount.builder()
                .schemeName(SCHEME_NAME)
                .identification(ACCOUNT_NUMBER)
                .build();
    }

    public static CreditorAccount createCreditorAccount() {
        return CreditorAccount.builder()
                .schemeName(SCHEME_NAME)
                .identification(ACCOUNT_NUMBER)
                .name(NAME)
                .build();
    }

    public static RemittanceInformation createRemittanceInformation() {
        return RemittanceInformation.ofUnstructuredAndReference(REMITTANCE_INFORMATION);
    }

    public static InstructedAmount createInstructedAmount() {
        return new InstructedAmount(createExactCurrencyAmount());
    }

    public static ExactCurrencyAmount createExactCurrencyAmount() {
        return new ExactCurrencyAmount(new BigDecimal(AMOUNT), CURRENCY);
    }

    public static Debtor createDebtor() {
        final AccountIdentifier accountIdentifierMock = createAccountIdentifier();

        return new Debtor(accountIdentifierMock);
    }

    public static PaymentRequest createDomesticPaymentRequestForNotExecutedPayment(
            Clock clockMock) {
        final PaymentRequest paymentRequestMock = mock(PaymentRequest.class);
        final Payment paymentMock = createTodayPayment(clockMock);

        when(paymentRequestMock.getPayment()).thenReturn(paymentMock);
        setConsentId(paymentRequestMock);

        return paymentRequestMock;
    }

    public static PaymentRequest createDomesticPaymentRequestForAlreadyExecutedPayment(
            Clock clockMock) {
        final PaymentRequest paymentRequestMock =
                createDomesticPaymentRequestForNotExecutedPayment(clockMock);

        setPaymentIdAndConsentId(paymentRequestMock);

        return paymentRequestMock;
    }

    public static PaymentRequest createDomesticScheduledPaymentRequestForNotExecutedPayment(
            Clock clockMock) {
        final PaymentRequest paymentRequestMock = mock(PaymentRequest.class);
        final Payment paymentMock = createFutureDatePayment(clockMock);

        when(paymentRequestMock.getPayment()).thenReturn(paymentMock);
        setConsentId(paymentRequestMock);

        return paymentRequestMock;
    }

    public static PaymentRequest createDomesticScheduledPaymentRequestForAlreadyExecutedPayment(
            Clock clockMock) {
        final PaymentRequest paymentRequestMock =
                createDomesticScheduledPaymentRequestForNotExecutedPayment(clockMock);

        setPaymentIdAndConsentId(paymentRequestMock);

        return paymentRequestMock;
    }

    public static FundsConfirmationResponse createFundsConfirmationResponse() {
        return mock(FundsConfirmationResponse.class);
    }

    public static PaymentResponse createPaymentResponseForConsent() {
        final PaymentResponse paymentResponseMock = mock(PaymentResponse.class);
        final Storage storage = createStorageWithConsentId();
        final Payment paymentMock = createPayment();

        when(paymentResponseMock.getStorage()).thenReturn(storage);
        when(paymentResponseMock.getPayment()).thenReturn(paymentMock);

        return paymentResponseMock;
    }

    public static PaymentResponse createPaymentResponse() {
        final PaymentResponse paymentResponseMock = mock(PaymentResponse.class);
        final Storage storage = createStorageWithConsentIdAndPaymentId();
        final Payment paymentMock = createPayment();

        when(paymentResponseMock.getStorage()).thenReturn(storage);
        when(paymentResponseMock.getPayment()).thenReturn(paymentMock);

        return paymentResponseMock;
    }

    public static DomesticPaymentResponse createDomesticPaymentResponse() {
        final DomesticPaymentResponse domesticPaymentResponseMock =
                mock(DomesticPaymentResponse.class);
        final DomesticPaymentResponseData responseDataMock = createDomesticPaymentResponseData();

        when(domesticPaymentResponseMock.getData()).thenReturn(responseDataMock);

        return domesticPaymentResponseMock;
    }

    public static DomesticPaymentConsentResponse createDomesticPaymentConsentResponse() {
        final DomesticPaymentConsentResponse domesticPaymentConsentResponseMock =
                mock(DomesticPaymentConsentResponse.class);
        final DomesticPaymentConsentResponseData responseDataMock =
                createDomesticPaymentConsentResponseData();

        when(domesticPaymentConsentResponseMock.getData()).thenReturn(responseDataMock);

        return domesticPaymentConsentResponseMock;
    }

    public static DomesticScheduledPaymentResponse createDomesticScheduledPaymentResponse() {
        final DomesticScheduledPaymentResponse domesticScheduledPaymentResponseMock =
                mock(DomesticScheduledPaymentResponse.class);
        final DomesticScheduledPaymentResponseData responseDataMock =
                createDomesticScheduledPaymentResponseData();

        when(domesticScheduledPaymentResponseMock.getData()).thenReturn(responseDataMock);

        return domesticScheduledPaymentResponseMock;
    }

    public static DomesticScheduledPaymentConsentResponse
            createDomesticScheduledPaymentConsentResponse() {
        final DomesticScheduledPaymentConsentResponse domesticScheduledPaymentConsentResponseMock =
                mock(DomesticScheduledPaymentConsentResponse.class);
        final DomesticScheduledPaymentConsentResponseData responseDataMock =
                createDomesticScheduledPaymentConsentResponseData();

        when(domesticScheduledPaymentConsentResponseMock.getData()).thenReturn(responseDataMock);

        return domesticScheduledPaymentConsentResponseMock;
    }

    private static Payment createTodayPayment(Clock clockMock) {
        final LocalDate executionDate = LocalDate.now(clockMock);

        return createTodayPayment(executionDate);
    }

    private static Payment createTodayPayment(LocalDate executionDate) {
        final Payment paymentMock = mock(Payment.class);

        when(paymentMock.getCreditorAndDebtorAccountType())
                .thenReturn(
                        new Pair<>(
                                AccountIdentifier.Type.SORT_CODE,
                                AccountIdentifier.Type.SORT_CODE));

        when(paymentMock.getExecutionDate()).thenReturn(executionDate);

        return paymentMock;
    }

    private static Payment createFutureDatePayment(Clock clockMock) {
        final Payment paymentMock = mock(Payment.class);

        when(paymentMock.getCreditorAndDebtorAccountType())
                .thenReturn(
                        new Pair<>(
                                AccountIdentifier.Type.SORT_CODE,
                                AccountIdentifier.Type.SORT_CODE));

        final LocalDate executionDate = LocalDate.now(clockMock).plusDays(1L);
        when(paymentMock.getExecutionDate()).thenReturn(executionDate);

        return paymentMock;
    }

    private static void setPaymentIdAndConsentId(PaymentRequest paymentRequestMock) {
        final Storage storageMock = mock(Storage.class);

        when(storageMock.get(UkOpenBankingV31PaymentConstants.Storage.PAYMENT_ID))
                .thenReturn(PAYMENT_ID);
        when(storageMock.get(UkOpenBankingV31PaymentConstants.Storage.CONSENT_ID))
                .thenReturn(CONSENT_ID);
        when(paymentRequestMock.getStorage()).thenReturn(storageMock);
    }

    private static void setConsentId(PaymentRequest paymentRequestMock) {
        final Storage storageMock = mock(Storage.class);

        when(storageMock.get(UkOpenBankingV31PaymentConstants.Storage.CONSENT_ID))
                .thenReturn(CONSENT_ID);
        when(paymentRequestMock.getStorage()).thenReturn(storageMock);
    }

    private static Creditor createCreditor() {
        final Creditor creditorMock = mock(Creditor.class);
        final AccountIdentifier accountIdentifierMock = createAccountIdentifier();

        when(creditorMock.getAccountIdentifier()).thenReturn(accountIdentifierMock);
        when(creditorMock.getAccountIdentifierType()).thenReturn(AccountIdentifier.Type.SORT_CODE);
        when(creditorMock.getName()).thenReturn(NAME);
        when(creditorMock.getAccountNumber()).thenReturn(ACCOUNT_NUMBER);

        return creditorMock;
    }

    private static se.tink.libraries.transfer.rpc.RemittanceInformation
            createUnstructuredRemittanceInformation() {
        final se.tink.libraries.transfer.rpc.RemittanceInformation remittanceInformationMock =
                mock(se.tink.libraries.transfer.rpc.RemittanceInformation.class);

        when(remittanceInformationMock.getValue()).thenReturn(REMITTANCE_INFORMATION);
        when(remittanceInformationMock.getType())
                .thenReturn(RemittanceInformationType.UNSTRUCTURED);

        return remittanceInformationMock;
    }

    private static AccountIdentifier createAccountIdentifier() {
        final AccountIdentifier accountIdentifierMock = mock(AccountIdentifier.class);

        when(accountIdentifierMock.getType()).thenReturn(AccountIdentifier.Type.SORT_CODE);
        when(accountIdentifierMock.getIdentifier()).thenReturn(ACCOUNT_NUMBER);

        return accountIdentifierMock;
    }

    private static DomesticScheduledPaymentResponseData
            createDomesticScheduledPaymentResponseData() {
        final DomesticScheduledPaymentResponseData responseDataMock =
                mock(DomesticScheduledPaymentResponseData.class);
        final DomesticScheduledPaymentInitiation initiationMock =
                createDomesticScheduledPaymentInitiation();

        when(responseDataMock.getInitiation()).thenReturn(initiationMock);
        when(responseDataMock.getStatus()).thenReturn(RESPONSE_STATUS_FOR_SCHEDULED_PAYMENTS);
        when(responseDataMock.getConsentId()).thenReturn(CONSENT_ID);
        when(responseDataMock.getDomesticScheduledPaymentId()).thenReturn(PAYMENT_ID);

        return responseDataMock;
    }

    private static DomesticScheduledPaymentConsentResponseData
            createDomesticScheduledPaymentConsentResponseData() {
        final DomesticScheduledPaymentConsentResponseData responseDataMock =
                mock(DomesticScheduledPaymentConsentResponseData.class);
        final DomesticScheduledPaymentInitiation initiationMock =
                createDomesticScheduledPaymentInitiation();

        when(responseDataMock.getInitiation()).thenReturn(initiationMock);
        when(responseDataMock.getStatus()).thenReturn(RESPONSE_STATUS);
        when(responseDataMock.getConsentId()).thenReturn(CONSENT_ID);

        return responseDataMock;
    }

    private static DomesticPaymentResponseData createDomesticPaymentResponseData() {
        final DomesticPaymentResponseData responseDataMock =
                mock(DomesticPaymentResponseData.class);
        final DomesticPaymentInitiation initiationMock = createDomesticPaymentInitiation();

        when(responseDataMock.getInitiation()).thenReturn(initiationMock);
        when(responseDataMock.getStatus()).thenReturn(RESPONSE_STATUS);
        when(responseDataMock.getConsentId()).thenReturn(CONSENT_ID);
        when(responseDataMock.getDomesticPaymentId()).thenReturn(PAYMENT_ID);

        return responseDataMock;
    }

    private static DomesticPaymentConsentResponseData createDomesticPaymentConsentResponseData() {
        final DomesticPaymentConsentResponseData responseDataMock =
                mock(DomesticPaymentConsentResponseData.class);
        final DomesticPaymentInitiation initiationMock = createDomesticPaymentInitiation();

        when(responseDataMock.getInitiation()).thenReturn(initiationMock);
        when(responseDataMock.getStatus()).thenReturn(RESPONSE_STATUS);
        when(responseDataMock.getConsentId()).thenReturn(CONSENT_ID);

        return responseDataMock;
    }

    private static DomesticPaymentInitiation createDomesticPaymentInitiation() {
        final DomesticPaymentInitiation initiationMock = mock(DomesticPaymentInitiation.class);
        final DebtorAccount debtorAccountMock = createDebtorAccount();
        final CreditorAccount creditorAccountMock = createCreditorAccount();
        final RemittanceInformation remittanceInformationMock = createRemittanceInformation();
        final InstructedAmount instructedAmountMock = createInstructedAmount();

        when(initiationMock.getDebtorAccount()).thenReturn(debtorAccountMock);
        when(initiationMock.getCreditorAccount()).thenReturn(creditorAccountMock);
        when(initiationMock.getRemittanceInformation()).thenReturn(remittanceInformationMock);
        when(initiationMock.getInstructedAmount()).thenReturn(instructedAmountMock);
        when(initiationMock.getInstructionIdentification()).thenReturn(INSTRUCTION_ID);

        return initiationMock;
    }

    private static DomesticScheduledPaymentInitiation createDomesticScheduledPaymentInitiation() {
        final DomesticScheduledPaymentInitiation initiationMock =
                mock(DomesticScheduledPaymentInitiation.class);
        final DebtorAccount debtorAccountMock = createDebtorAccount();
        final CreditorAccount creditorAccountMock = createCreditorAccount();
        final RemittanceInformation remittanceInformationMock = createRemittanceInformation();
        final InstructedAmount instructedAmountMock = createInstructedAmount();

        when(initiationMock.getDebtorAccount()).thenReturn(debtorAccountMock);
        when(initiationMock.getCreditorAccount()).thenReturn(creditorAccountMock);
        when(initiationMock.getRemittanceInformation()).thenReturn(remittanceInformationMock);
        when(initiationMock.getInstructedAmount()).thenReturn(instructedAmountMock);
        when(initiationMock.getInstructionIdentification()).thenReturn(INSTRUCTION_ID);
        when(initiationMock.getRequestedExecutionDateTime()).thenReturn(EXECUTION_DATE_TIME);

        return initiationMock;
    }

    private static Storage createStorageWithConsentId() {
        final Storage storage = new Storage();

        storage.put(UkOpenBankingV31PaymentConstants.Storage.CONSENT_ID, CONSENT_ID);

        return storage;
    }

    private static Storage createStorageWithConsentIdAndPaymentId() {
        final Storage storage = new Storage();

        storage.put(UkOpenBankingV31PaymentConstants.Storage.CONSENT_ID, CONSENT_ID);
        storage.put(UkOpenBankingV31PaymentConstants.Storage.PAYMENT_ID, PAYMENT_ID);

        return storage;
    }
}
