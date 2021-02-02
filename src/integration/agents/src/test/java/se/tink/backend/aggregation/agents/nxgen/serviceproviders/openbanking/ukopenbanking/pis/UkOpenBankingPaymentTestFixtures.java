package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis;

import static java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableMap;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Map;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.OpenIdConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.configuration.ClientInfo;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.common.dto.CreditorAccount;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.common.dto.DebtorAccount;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.common.dto.InstructedAmount;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.common.dto.RemittanceInformationDto;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.domestic.dto.DomesticPaymentConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.domestic.dto.DomesticPaymentConsentResponseData;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.domestic.dto.DomesticPaymentInitiation;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.domestic.dto.DomesticPaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.domestic.dto.DomesticPaymentResponseData;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.domesticscheduled.dto.DomesticScheduledPaymentConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.domesticscheduled.dto.DomesticScheduledPaymentConsentResponseData;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.domesticscheduled.dto.DomesticScheduledPaymentInitiation;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.domesticscheduled.dto.DomesticScheduledPaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.domesticscheduled.dto.DomesticScheduledPaymentResponseData;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.entity.ExecutorSignStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.backend.aggregation.nxgen.controllers.signing.SigningStepConstants;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.storage.Storage;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.pair.Pair;
import se.tink.libraries.payment.enums.PaymentStatus;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.transfer.enums.RemittanceInformationType;
import se.tink.libraries.transfer.rpc.RemittanceInformation;

public class UkOpenBankingPaymentTestFixtures {

    public static final String PAYMENT_ID = "DUMMY_PAYMENT_ID";
    public static final String CONSENT_ID = "DUMMY_CONSENT_ID";
    public static final String END_TO_END_ID = "DUMMY_END_TO_END_ID";
    public static final String INSTRUCTION_ID = "DUMMY_INSTRUCTION_ID";
    public static final String NAME = "DUMMY_NAME";
    public static final String AMOUNT = "10.23";
    public static final String CURRENCY = "GBP";
    public static final String AUTHORIZE_URL = "https://authorize-url";
    public static final String CALLBACK_URL = "https://callback-url";
    public static final String STATE = "DUMMY_STATE";
    public static final String AUTH_CODE = "DUMMY_AUTH_CODE";
    public static final String SUPPLEMENTAL_KEY = "DUMMY_SUPPLEMENTAL_KEY";
    public static final String SIGNATURE = "DUMMY_SIGNATURE";
    public static final String SOFTWARE_ID = "DUMMY_SOFTWARE_ID";

    private static final String ACCOUNT_NUMBER = "12345678901234";
    private static final String REMITTANCE_INFORMATION = "DUMMY_REMITTANCE_INFORMATION";
    private static final String CONSENT_RESPONSE_STATUS = "AwaitingAuthorisation";
    private static final String RESPONSE_STATUS = "Authorised";
    private static final String RESPONSE_STATUS_FOR_SCHEDULED_PAYMENTS = "InitiationPending";
    private static final String EXECUTION_DATE_TIME = "2020-12-20T12:23:45Z";
    private static final LocalDate EXECUTION_DATE =
            LocalDate.parse(EXECUTION_DATE_TIME, ISO_OFFSET_DATE_TIME);
    private static final String SCHEME_NAME = "UK.OBIE.SortCodeAccountNumber";
    private static final Instant NOW = Instant.now();
    private static final String CLIENT_ID = "DUMMY_CLIENT_ID";
    private static final String ID_TOKEN = "DUMMY_ID_TOKEN";

    public static Payment createPayment() {
        final RemittanceInformation remittanceInformation =
                createUnstructuredRemittanceInformation();
        return createPaymentWithRemittanceInfo(remittanceInformation);
    }

    public static Payment createPaymentWithRemittanceInfo(
            RemittanceInformation remittanceInformation) {
        return createPaymentWithStatusAndRemittanceInfo(
                PaymentStatus.PENDING, remittanceInformation);
    }

    public static RemittanceInformation createUnstructuredRemittanceInformation() {
        final RemittanceInformation remittanceInformationMock = mock(RemittanceInformation.class);

        when(remittanceInformationMock.getValue()).thenReturn(REMITTANCE_INFORMATION);
        when(remittanceInformationMock.getType())
                .thenReturn(RemittanceInformationType.UNSTRUCTURED);

        return remittanceInformationMock;
    }

    public static RemittanceInformation createReferenceRemittanceInformation() {
        final RemittanceInformation remittanceInformationMock = mock(RemittanceInformation.class);

        when(remittanceInformationMock.getValue()).thenReturn(REMITTANCE_INFORMATION);
        when(remittanceInformationMock.getType()).thenReturn(RemittanceInformationType.REFERENCE);

        return remittanceInformationMock;
    }

    public static RemittanceInformation createNoTypeRemittanceInformation() {
        final RemittanceInformation remittanceInformationMock = mock(RemittanceInformation.class);

        when(remittanceInformationMock.getValue()).thenReturn(REMITTANCE_INFORMATION);
        when(remittanceInformationMock.getType()).thenReturn(null);

        return remittanceInformationMock;
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

    public static RemittanceInformationDto createNoTypeRemittanceInformationDto() {
        return RemittanceInformationDto.builder()
                .reference(REMITTANCE_INFORMATION)
                .unstructured(REMITTANCE_INFORMATION)
                .build();
    }

    public static RemittanceInformationDto createReferenceRemittanceInformationDto() {
        return RemittanceInformationDto.builder().reference(REMITTANCE_INFORMATION).build();
    }

    public static RemittanceInformationDto createUnstructuredRemittanceInformationDto() {
        return RemittanceInformationDto.builder().unstructured(REMITTANCE_INFORMATION).build();
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

    public static PaymentRequest createDomesticPaymentRequestForNotExecutedPayment() {
        return createDomesticPaymentRequestForNotExecutedPayment(createClockMock());
    }

    public static PaymentRequest createDomesticPaymentRequestForNotExecutedPayment(
            Clock clockMock) {
        final PaymentRequest paymentRequestMock = mock(PaymentRequest.class);
        final Payment paymentMock = createTodayPayment(clockMock);

        when(paymentRequestMock.getPayment()).thenReturn(paymentMock);
        setConsentId(paymentRequestMock);

        return paymentRequestMock;
    }

    static PaymentRequest createDomesticPaymentRequestForAlreadyExecutedPayment(Clock clockMock) {
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

    public static PaymentResponse createPaymentResponseForConsent() {
        final PaymentResponse paymentResponseMock = mock(PaymentResponse.class);
        final Storage storage = createStorageWithConsentId();
        final Payment paymentMock = createPaymentForConsent();

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
        when(domesticPaymentConsentResponseMock.hasStatusAwaitingAuthorisation())
                .thenReturn(Boolean.TRUE);

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

    public static Clock createClockMock() {
        final Clock clockMock = mock(Clock.class);

        when(clockMock.instant()).thenReturn(NOW);
        when(clockMock.getZone()).thenReturn(ZoneOffset.UTC);

        return clockMock;
    }

    public static ClientInfo createClientInfo() {
        final ClientInfo clientInfoMock = mock(ClientInfo.class);

        when(clientInfoMock.getClientId()).thenReturn(CLIENT_ID);

        return clientInfoMock;
    }

    public static StrongAuthenticationState createStrongAuthenticationState() {
        final StrongAuthenticationState strongAuthenticationStateMock =
                mock(StrongAuthenticationState.class);

        when(strongAuthenticationStateMock.getState()).thenReturn(STATE);
        when(strongAuthenticationStateMock.getSupplementalKey()).thenReturn(SUPPLEMENTAL_KEY);

        return strongAuthenticationStateMock;
    }

    public static Map<String, String> createCorrectCallbackData() {
        return ImmutableMap.of(
                OpenIdConstants.CallbackParams.CODE, AUTH_CODE,
                OpenIdConstants.CallbackParams.ID_TOKEN, ID_TOKEN,
                OpenIdConstants.Params.STATE, STATE);
    }

    public static Map<String, String> createCallbackDataWithNoCode() {
        return ImmutableMap.of(
                OpenIdConstants.CallbackParams.ID_TOKEN, ID_TOKEN,
                OpenIdConstants.Params.STATE, STATE);
    }

    public static Map<String, String> createCallbackDataWithErrorAndDescription() {
        return ImmutableMap.of(
                OpenIdConstants.CallbackParams.ERROR, OpenIdConstants.Errors.ACCESS_DENIED,
                OpenIdConstants.CallbackParams.ERROR_DESCRIPTION,
                        OpenIdConstants.Errors.ACCESS_DENIED,
                OpenIdConstants.Params.STATE, STATE);
    }

    public static Map<String, String> createCallbackDataWithErrorAndNoDescription() {
        return ImmutableMap.of(
                OpenIdConstants.CallbackParams.ERROR,
                OpenIdConstants.Errors.ACCESS_DENIED,
                OpenIdConstants.Params.STATE,
                STATE);
    }

    public static Payment createTodayPayment(Clock clockMock) {
        final LocalDate executionDate = LocalDate.now(clockMock);

        return createPayment(executionDate);
    }

    public static Payment createFutureDatePayment(Clock clockMock) {
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

    public static OAuth2Token createToken() {
        return mock(OAuth2Token.class);
    }

    static PaymentMultiStepRequest createPaymentMultiStepRequestFoAuthenticateStep() {
        return createPaymentMultiStepRequest(SigningStepConstants.STEP_INIT);
    }

    static PaymentMultiStepRequest createPaymentMultiStepRequestForExecutePaymentStep() {
        return createPaymentMultiStepRequest(ExecutorSignStep.EXECUTE_PAYMENT.name());
    }

    private static PaymentMultiStepRequest createPaymentMultiStepRequest(String step) {
        final PaymentMultiStepRequest paymentMultiStepRequestMock =
                mock(PaymentMultiStepRequest.class);
        final Storage storage = createStorageWithConsentId();
        final Payment paymentMock = createPayment();

        when(paymentMultiStepRequestMock.getStep()).thenReturn(step);
        when(paymentMultiStepRequestMock.getStorage()).thenReturn(storage);
        when(paymentMultiStepRequestMock.getPayment()).thenReturn(paymentMock);

        return paymentMultiStepRequestMock;
    }

    public static PaymentRequest createPaymentRequest() {
        final PaymentRequest paymentRequest = mock(PaymentRequest.class);
        final Storage storage = createStorageWithConsentId();
        final Payment paymentMock = createPayment();

        when(paymentRequest.getStorage()).thenReturn(storage);
        when(paymentRequest.getPayment()).thenReturn(paymentMock);

        return paymentRequest;
    }

    private static Payment createPayment(LocalDate executionDate) {
        final Payment paymentMock = mock(Payment.class);

        when(paymentMock.getCreditorAndDebtorAccountType())
                .thenReturn(
                        new Pair<>(
                                AccountIdentifier.Type.SORT_CODE,
                                AccountIdentifier.Type.SORT_CODE));

        when(paymentMock.getExecutionDate()).thenReturn(executionDate);

        return paymentMock;
    }

    private static void setPaymentIdAndConsentId(PaymentRequest paymentRequestMock) {
        final Storage storageMock = mock(Storage.class);

        when(storageMock.containsKey(UkOpenBankingPaymentConstants.PAYMENT_ID_KEY))
                .thenReturn(true);
        when(storageMock.get(UkOpenBankingPaymentConstants.PAYMENT_ID_KEY)).thenReturn(PAYMENT_ID);
        when(storageMock.get(UkOpenBankingPaymentConstants.CONSENT_ID_KEY)).thenReturn(CONSENT_ID);
        when(paymentRequestMock.getStorage()).thenReturn(storageMock);
    }

    private static void setConsentId(PaymentRequest paymentRequestMock) {
        final Storage storageMock = mock(Storage.class);

        when(storageMock.get(UkOpenBankingPaymentConstants.CONSENT_ID_KEY)).thenReturn(CONSENT_ID);
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
        when(responseDataMock.getStatus()).thenReturn(CONSENT_RESPONSE_STATUS);
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
        when(responseDataMock.getStatus()).thenReturn(CONSENT_RESPONSE_STATUS);
        when(responseDataMock.getConsentId()).thenReturn(CONSENT_ID);

        return responseDataMock;
    }

    private static DomesticPaymentInitiation createDomesticPaymentInitiation() {
        final DomesticPaymentInitiation initiationMock = mock(DomesticPaymentInitiation.class);
        final DebtorAccount debtorAccountMock = createDebtorAccount();
        final CreditorAccount creditorAccountMock = createCreditorAccount();
        final RemittanceInformationDto remittanceInformationMock =
                createNoTypeRemittanceInformationDto();
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
        final RemittanceInformationDto remittanceInformationMock =
                createNoTypeRemittanceInformationDto();
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

        storage.put(UkOpenBankingPaymentConstants.CONSENT_ID_KEY, CONSENT_ID);

        return storage;
    }

    private static Storage createStorageWithConsentIdAndPaymentId() {
        final Storage storage = new Storage();

        storage.put(UkOpenBankingPaymentConstants.CONSENT_ID_KEY, CONSENT_ID);
        storage.put(UkOpenBankingPaymentConstants.PAYMENT_ID_KEY, PAYMENT_ID);

        return storage;
    }

    private static Payment createPaymentWithStatusAndRemittanceInfo(
            PaymentStatus paymentStatus, RemittanceInformation remittanceInformation) {
        final Payment paymentMock = createPayment(EXECUTION_DATE);
        final Debtor debtorMock = createDebtor();
        final Creditor creditorMock = createCreditor();
        final ExactCurrencyAmount exactCurrencyAmountMock = createExactCurrencyAmount();

        when(paymentMock.getDebtor()).thenReturn(debtorMock);
        when(paymentMock.getCreditor()).thenReturn(creditorMock);
        when(paymentMock.getRemittanceInformation()).thenReturn(remittanceInformation);
        when(paymentMock.getExactCurrencyAmountFromField()).thenReturn(exactCurrencyAmountMock);
        when(paymentMock.getCurrency()).thenReturn(CURRENCY);
        when(paymentMock.getUniqueId()).thenReturn(INSTRUCTION_ID);
        when(paymentMock.getUniqueIdForUKOPenBanking()).thenReturn(INSTRUCTION_ID);
        when(paymentMock.getExactCurrencyAmount()).thenReturn(exactCurrencyAmountMock);
        when(paymentMock.getStatus()).thenReturn(paymentStatus);

        return paymentMock;
    }

    private static Payment createPaymentForConsent() {
        final RemittanceInformation remittanceInformation =
                createUnstructuredRemittanceInformation();
        return createPaymentWithStatusAndRemittanceInfo(
                PaymentStatus.CREATED, remittanceInformation);
    }
}
