package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.junit.Ignore;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.apiclient.dto.data.ErrorCode;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.apiclient.dto.response.AccountsSummaryResponseDto;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.apiclient.dto.response.ErrorResponseDto;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.apiclient.dto.response.LoginDetailsResponseDto;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.apiclient.dto.response.OpenAmErrorResponseDto;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.apiclient.dto.response.OtpAuthenticationResponseDto;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.apiclient.dto.response.OtpInfoDto;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.apiclient.dto.response.TokenResponseDto;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.apiclient.dto.response.TransactionsAndLockedEventsResponseDto;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.apiclient.response.AccountsSummaryResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.apiclient.response.LoginDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.apiclient.response.OtpAuthenticationResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.apiclient.response.TransactionsAndLockedEventsResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.serialization.utils.SerializationUtils;

@Ignore
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AktiaTestFixtures {

    public static final String AUTH_SERVER_URL = "https://mobile-auth2.aktia.fi";
    public static final String API_SERVER_URL = "https://mobile-gateway2.aktia.fi";
    public static final String BASIC_AUTH_HEADER_VALUE = "Zm9vOmJhcgo=";
    public static final String ACCESS_TOKEN = "1234";
    public static final String USERNAME = "user1";
    public static final String CORRECT_PASSWORD = "xyz";
    public static final String OTP_CODE = "002233";
    public static final String CONTINUATION_KEY = "911";
    public static final String ACCOUNT_ID =
            "8A04019F870973400A945908C062CA044C94689D70BEA79ED43B9A31336FA7B4";
    public static final String CURRENT_OTP_CARD = "HJ11111";
    public static final String NEXT_OTP_INDEX = "E4";
    public static final String IBAN = "FI7340550012203328";
    public static final String AMOUNT = "100.32";
    public static final String TRANSACTION_MESSAGE = "Outgoing transfer";

    private static final String TRANSACTION_ID = "CC1725813448487998453AE2E7CB7FF4";
    private static final String ACCOUNT_NAME = "Oma tili";
    private static final String TRANSACTION_DATE = "2020-01-30";

    public static TokenResponseDto createTokenResponseDto() {
        return SerializationUtils.deserializeFromString(
                createTokenResponseJsonString(), TokenResponseDto.class);
    }

    private static String createTokenResponseJsonString() {
        return "{\n"
                + " \"access_token\":\""
                + ACCESS_TOKEN
                + " \",\n"
                + " \"token_type\":\"Bearer\",\n"
                + " \"expires_in\":604800,\n"
                + " \"scope\":\"aktiaUser\"\n"
                + "}";
    }

    public static String getTokenRequest() {
        return String.format(
                "grant_type=password&username=%s&password=%s&scope=aktiaUser",
                USERNAME, CORRECT_PASSWORD);
    }

    public static LoginDetailsResponse createSuccessfulLoginDetailsResponseWithOtpRequired() {
        return new LoginDetailsResponse(createLoginDetailsResponseDtoWithOtpRequired());
    }

    public static LoginDetailsResponse createSuccessfulLoginDetailsResponseWithoutOtpRequired() {
        return new LoginDetailsResponse(createLoginDetailsResponseDtoWithoutOtpRequired());
    }

    public static LoginDetailsResponse createNotSuccessfulLoginDetailsResponse() {
        return new LoginDetailsResponse(createOpenAmErrorResponseDto());
    }

    public static LoginDetailsResponse createLoginDetailsResponseForPasswordChange() {
        return new LoginDetailsResponse(createLoginDetailsResponseDtoForPasswordChange());
    }

    public static LoginDetailsResponse createLoginDetailsResponseForLockedAccount() {
        return new LoginDetailsResponse(createLoginDetailsResponseDtoForLockedAccount());
    }

    public static LoginDetailsResponse createLoginDetailsResponseForMustAcceptTerms() {
        return new LoginDetailsResponse(createLoginDetailsResponseDtoForMustAcceptTerms());
    }

    private static LoginDetailsResponseDto createLoginDetailsResponseDtoWithOtpRequired() {
        return SerializationUtils.deserializeFromString(
                createLoginDetailsResponseWithOtpRequiredJsonString(),
                LoginDetailsResponseDto.class);
    }

    private static LoginDetailsResponseDto createLoginDetailsResponseDtoWithoutOtpRequired() {
        return SerializationUtils.deserializeFromString(
                createLoginDetailsResponseWithoutOtpRequiredJsonString(),
                LoginDetailsResponseDto.class);
    }

    private static LoginDetailsResponseDto createLoginDetailsResponseDtoForPasswordChange() {
        return SerializationUtils.deserializeFromString(
                createLoginDetailsResponseForPasswordChangeRequiredJsonString(),
                LoginDetailsResponseDto.class);
    }

    private static LoginDetailsResponseDto createLoginDetailsResponseDtoForLockedAccount() {
        return SerializationUtils.deserializeFromString(
                createLoginDetailsResponseForLockedAccountJsonString(),
                LoginDetailsResponseDto.class);
    }

    private static LoginDetailsResponseDto createLoginDetailsResponseDtoForMustAcceptTerms() {
        return SerializationUtils.deserializeFromString(
                createLoginDetailsResponseForMustAcceptTermsJsonString(),
                LoginDetailsResponseDto.class);
    }

    private static String createLoginDetailsResponseWithOtpRequiredJsonString() {
        return "{\n"
                + "   \"domainSettings\":{\n"
                + "       \"pinTouchActivationAvailable\":true,\n"
                + "       \"pinTouchTermsPdfUrl\":\"https://www.aktia.fi/documents/10556/50573/helppo-kirjautuminen-kayttoehdot-fi.pdf\",\n"
                + "       \"ownTransferWithoutOtp\":true"
                + "   },\n"
                + "   \"otpChallenge\":{\n"
                + "       \"otpRequired\":true,\n"
                + "       \"otpInfo\":{"
                + "           \"nextOtpIndex\":\""
                + NEXT_OTP_INDEX
                + "\",\n"
                + "           \"currentOtpCard\":\""
                + CURRENT_OTP_CARD
                + "\",\n"
                + "           \"fixedOtpCard\":false,\n"
                + "           \"nextOtpCard\":null"
                + "       },\n"
                + "       \"readMessagesWithoutOtp\":true"
                + "   },\n"
                + "   \"userAccountInfo\":{\n"
                + "       \"customerName\":\"Surname Name1 Name2 Name3\",\n"
                + "       \"passwordUpdateRequired\":false,\n"
                + "       \"accountLocked\":false,\n"
                + "       \"updateOrCreateKYCInfo\":false,\n"
                + "       \"customerType\":\"PERSON\",\n"
                + "       \"customerServiceInfo\":{\n"
                + "           \"serviceClass\":\"Asiakaspalvelu\","
                + "           \"phone\":\"010247010\","
                + "           \"openHours\":\"8 - 20\""
                + "       },\n"
                + "       \"personalAdvisor\":{"
                + "           \"name\":null,"
                + "           \"phone\":null,\n"
                + "           \"advisorServiceClass\":\"AdvisorServiceClass.PRODUCT.name\""
                + "       }"
                + "   },\n"
                + "   \"termsAcceptanceInfo\":{"
                + "       \"mustAcceptTerms\":false"
                + "   }"
                + "}";
    }

    private static String createLoginDetailsResponseWithoutOtpRequiredJsonString() {
        return "{\n"
                + "   \"domainSettings\":{\n"
                + "       \"pinTouchActivationAvailable\":true,\n"
                + "       \"pinTouchTermsPdfUrl\":\"https://www.aktia.fi/documents/10556/50573/helppo-kirjautuminen-kayttoehdot-fi.pdf\",\n"
                + "       \"ownTransferWithoutOtp\":true"
                + "   },\n"
                + "   \"otpChallenge\":{\n"
                + "       \"otpRequired\":false,\n"
                + "       \"readMessagesWithoutOtp\":true"
                + "   },\n"
                + "   \"userAccountInfo\":{\n"
                + "       \"customerName\":\"Surname Name1 Name2 Name3\",\n"
                + "       \"passwordUpdateRequired\":false,\n"
                + "       \"accountLocked\":false,\n"
                + "       \"updateOrCreateKYCInfo\":false,\n"
                + "       \"customerType\":\"PERSON\",\n"
                + "       \"customerServiceInfo\":{\n"
                + "           \"serviceClass\":\"Asiakaspalvelu\","
                + "           \"phone\":\"010247010\","
                + "           \"openHours\":\"8 - 20\""
                + "       },\n"
                + "       \"personalAdvisor\":{"
                + "           \"name\":null,"
                + "           \"phone\":null,\n"
                + "           \"advisorServiceClass\":\"AdvisorServiceClass.PRODUCT.name\""
                + "       }"
                + "   },\n"
                + "   \"termsAcceptanceInfo\":{"
                + "       \"mustAcceptTerms\":false"
                + "   }"
                + "}";
    }

    private static String createLoginDetailsResponseForPasswordChangeRequiredJsonString() {
        return "{\n"
                + "   \"otpChallenge\":{\n"
                + "       \"otpRequired\":false,\n"
                + "       \"readMessagesWithoutOtp\":true"
                + "   },\n"
                + "   \"userAccountInfo\":{\n"
                + "       \"customerName\":\"Surname Name1 Name2 Name3\",\n"
                + "       \"passwordUpdateRequired\":true,\n"
                + "       \"accountLocked\":false\n"
                + "   },\n"
                + "   \"termsAcceptanceInfo\":{"
                + "       \"mustAcceptTerms\":false"
                + "   }"
                + "}";
    }

    private static String createLoginDetailsResponseForLockedAccountJsonString() {
        return "{\n"
                + "   \"otpChallenge\":{\n"
                + "       \"otpRequired\":false\n"
                + "   },\n"
                + "   \"userAccountInfo\":{\n"
                + "       \"customerName\":\"Surname Name1 Name2 Name3\",\n"
                + "       \"passwordUpdateRequired\":false,\n"
                + "       \"accountLocked\":true\n"
                + "   },\n"
                + "   \"termsAcceptanceInfo\":{"
                + "       \"mustAcceptTerms\":false"
                + "   }"
                + "}";
    }

    private static String createLoginDetailsResponseForMustAcceptTermsJsonString() {
        return "{\n"
                + "   \"otpChallenge\":{\n"
                + "       \"otpRequired\":false\n"
                + "   },\n"
                + "   \"userAccountInfo\":{\n"
                + "       \"customerName\":\"Surname Name1 Name2 Name3\",\n"
                + "       \"passwordUpdateRequired\":false,\n"
                + "       \"accountLocked\":false\n"
                + "   },\n"
                + "   \"termsAcceptanceInfo\":{"
                + "       \"mustAcceptTerms\":true"
                + "   }"
                + "}";
    }

    private static OpenAmErrorResponseDto createOpenAmErrorResponseDto() {
        return new OpenAmErrorResponseDto("Error occurred", "There was problem with the request.");
    }

    public static OtpAuthenticationResponse createSuccessfulOtpAuthenticationResponse() {
        return new OtpAuthenticationResponse(createSuccessfulOtpAuthenticationResponseDto());
    }

    public static OtpAuthenticationResponse
            createOtpAuthenticationResponseWhenOtpCodeNotAccepted() {
        return new OtpAuthenticationResponse(
                createOtpAuthenticationResponseDtoWhenOtpCodeNotAccepted());
    }

    public static OtpAuthenticationResponse createOtpAuthenticationResponseWhenAccountIsLocked() {
        return new OtpAuthenticationResponse(createErrorResponseDtoForLockedAccount());
    }

    public static OtpAuthenticationResponse createOtpAuthenticationResponseForOtherError() {
        return new OtpAuthenticationResponse(createErrorResponseDtoForOtherError());
    }

    private static OtpAuthenticationResponseDto createSuccessfulOtpAuthenticationResponseDto() {
        return SerializationUtils.deserializeFromString(
                createOtpAuthenticationResponseJsonString(), OtpAuthenticationResponseDto.class);
    }

    private static OtpAuthenticationResponseDto
            createOtpAuthenticationResponseDtoWhenOtpCodeNotAccepted() {
        return new OtpAuthenticationResponseDto(false, null);
    }

    private static String createOtpAuthenticationResponseJsonString() {
        return "{"
                + "   \"otpAccepted\":true,"
                + "   \"otpInfo\":{"
                + "       \"nextOtpIndex\":\"E9\","
                + "       \"currentOtpCard\":\"H11111\","
                + "       \"fixedOtpCard\":false,"
                + "       \"nextOtpCard\":null"
                + "   }"
                + "}";
    }

    private static ErrorResponseDto createErrorResponseDtoForLockedAccount() {
        return new ErrorResponseDto(ErrorCode.ACCOUNT_LOCKED, "Locked account.");
    }

    private static ErrorResponseDto createErrorResponseDtoForOtherError() {
        return new ErrorResponseDto(ErrorCode.UNEXPECTED_ERROR, "Unexpected error.");
    }

    public static AccountsSummaryResponse createSuccessfulAccountsSummaryResponse() {
        return new AccountsSummaryResponse(createAccountsSummaryResponseDto("Savings Account"));
    }

    public static AccountsSummaryResponse createSuccessfulAccountsSummaryResponse(
            String accountType) {
        return new AccountsSummaryResponse(createAccountsSummaryResponseDto(accountType));
    }

    public static AccountsSummaryResponse createAccountsSummaryResponseWithError() {
        return new AccountsSummaryResponse(createOpenAmErrorResponseDto());
    }

    private static AccountsSummaryResponseDto createAccountsSummaryResponseDto(String accountType) {
        return SerializationUtils.deserializeFromString(
                createAccountsSummaryResponseJsonString(accountType),
                AccountsSummaryResponseDto.class);
    }

    private static String createAccountsSummaryResponseJsonString(String accountType) {
        return "{"
                + "   \"frontPageHighlight\":{"
                + "       \"showHighlight\":false,"
                + "       \"idType\":null,"
                + "       \"id\":null,"
                + "       \"totalAmount\":null"
                + "   },\n"
                + "   \"accountSummary\":{"
                + "       \"accountSummaryList\":[\n"
                + "           {"
                + "               \"id\":\""
                + ACCOUNT_ID
                + "\",\n"
                + "               \"name\":\""
                + ACCOUNT_NAME
                + "\",\n"
                + "               \"primaryOwnerName\":\"Testi Teemu Markus Erik\",\n"
                + "               \"iban\":\"FI7340550012203328\","
                + "               \"bic\":\"HELSFIHH\",\n"
                + "               \"accountType\":{\n"
                + "                   \"accountType\": \""
                + accountType
                + "\","
                + "                   \"categoryCode\":\"OTHER\","
                + "                   \"productCode\":\"1410\","
                + "                   \"longTermSavings\":false"
                + "               },"
                + "               \"balance\":2.44,"
                + "               \"balanceTotal\":2.44,\n"
                + "               \"duePaymentsTotal\":0.00,"
                + "               \"hideFromSummary\":false,"
                + "               \"sortingOrder\":1,\n"
                + "               \"fabInfo\":{"
                + "                   \"fabOperations\":[\n"
                + "                       {"
                + "                           \"fabOperationCode\":\"COPY_IBAN\","
                + "                           \"fabOperation\":\"Kopioi tilinumero\""
                + "                       },\n"
                + "                       {"
                + "                           \"fabOperationCode\":\"NEW_PAYMENT\","
                + "                           \"fabOperation\":\"Uusi maksu\""
                + "                       },\n"
                + "                       {"
                + "                           \"fabOperationCode\":\"OWN_TRANSFER\","
                + "                           \"fabOperation\":\"Oma tilisiirto\""
                + "                       }"
                + "                   ]"
                + "               },\n"
                + "               \"parties\":["
                + "                   {"
                + "                       \"name\":\"Testi Teemu Markus Erik\","
                + "                       \"customerTypeCode\":\"PRIVATE\","
                + "                       \"roleCode\":\"PRIMARY_OWNER\""
                + "                   }"
                + "               ]"
                + "           }"
                + "       ]"
                + "   },\n"
                + "   \"paymentsTodoItemCount\":0,\n"
                + "   \"paymentAccounts\":{"
                + "       \"paymentAccounts\":["
                + "           {"
                + "               \"id\":\"8A04019F870973400A945908C062CA044C94689D70BEA79ED43B9A31336FA7B4\","
                + "               \"name\":\""
                + ACCOUNT_NAME
                + "\",\n"
                + "               \"iban\":\"FI73 4055 0012 2033 28\","
                + "               \"balance\":2.44,"
                + "               \"defaultPaymentAccount\":true"
                + "           }"
                + "       ],"
                + "       \"ownTransferFromAccounts\":["
                + "           {"
                + "               \"id\":\"8A04019F870973400A945908C062CA044C94689D70BEA79ED43B9A31336FA7B4\","
                + "               \"name\":\""
                + ACCOUNT_NAME
                + "\",\n"
                + "               \"iban\":\"FI73 4055 0012 2033 28\","
                + "               \"balance\":2.44"
                + "           }"
                + "       ],"
                + "       \"ownTransferToAccounts\":["
                + "           {"
                + "               \"id\":\"8A04019F870973400A945908C062CA044C94689D70BEA79ED43B9A31336FA7B4\","
                + "               \"name\":\""
                + ACCOUNT_NAME
                + "\",\n"
                + "               \"iban\":\"FI73 4055 0012 2033 28\","
                + "               \"balance\":2.44"
                + "           }"
                + "       ]"
                + "   }"
                + "}";
    }

    public static TransactionsAndLockedEventsResponse
            createTransactionsAndLockedEventsResponseWithContinuationKey() {
        return new TransactionsAndLockedEventsResponse(
                createTransactionsAndLockedEventsResponseDtoWithContinuationKey());
    }

    public static TransactionsAndLockedEventsResponse
            createTransactionsAndLockedEventsResponseWithoutContinuationKey() {
        return new TransactionsAndLockedEventsResponse(
                createTransactionsAndLockedEventsResponseDtoWithoutContinuationKey());
    }

    public static TransactionsAndLockedEventsResponse
            createTransactionsAndLockedEventsResponseForError() {
        return new TransactionsAndLockedEventsResponse(createOpenAmErrorResponseDto());
    }

    private static TransactionsAndLockedEventsResponseDto
            createTransactionsAndLockedEventsResponseDtoWithContinuationKey() {
        return SerializationUtils.deserializeFromString(
                createTransactionsAndLockedEventsResponseWithContinuationKeyJsonString(),
                TransactionsAndLockedEventsResponseDto.class);
    }

    private static TransactionsAndLockedEventsResponseDto
            createTransactionsAndLockedEventsResponseDtoWithoutContinuationKey() {
        return SerializationUtils.deserializeFromString(
                createTransactionsAndLockedEventsResponseWithoutContinuationKeyJsonString(),
                TransactionsAndLockedEventsResponseDto.class);
    }

    private static String createTransactionsAndLockedEventsResponseWithContinuationKeyJsonString() {
        return "{"
                + "   \"account\":{"
                + "       \"balance\":2.44,"
                + "       \"id\":\""
                + ACCOUNT_ID
                + "\",\n"
                + "       \"iban\":\""
                + IBAN
                + "\","
                + "       \"bic\":\"HELSFIHH\",\n"
                + "       \"name\":\""
                + ACCOUNT_NAME
                + "\",\n"
                + "       \"primaryOwnerName\":\"Testi Teemu Markus Erik\"\n"
                + "   },\n"
                + "   \"continuationKey\": \""
                + CONTINUATION_KEY
                + "\","
                + "   \"lockedEvents\": [],"
                + "   \"transactions\": ["
                + "       {"
                + "           \"amount\": "
                + AMOUNT
                + ", "
                + "           \"bookingDate\": \""
                + TRANSACTION_DATE
                + "\","
                + "           \"message\": \""
                + TRANSACTION_MESSAGE
                + "\","
                + "           \"receiverOrPayerName\": \"Mr. T\","
                + "           \"reference\": \"1\","
                + "           \"transactionId\": \""
                + TRANSACTION_ID
                + "\","
                + "           \"transactionType\": \"Account transfer\""
                + "       }"
                + "   ]"
                + "}";
    }

    private static String
            createTransactionsAndLockedEventsResponseWithoutContinuationKeyJsonString() {
        return "{"
                + "   \"account\":{"
                + "       \"balance\":2.44,"
                + "       \"id\":\""
                + ACCOUNT_ID
                + "\",\n"
                + "       \"iban\":\""
                + IBAN
                + "\","
                + "       \"bic\":\"HELSFIHH\",\n"
                + "       \"name\":\""
                + ACCOUNT_NAME
                + "\",\n"
                + "       \"primaryOwnerName\":\"Testi Teemu Markus Erik\"\n"
                + "   },\n"
                + "   \"lockedEvents\": [],"
                + "   \"transactions\": ["
                + "       {"
                + "           \"amount\": "
                + AMOUNT
                + ", "
                + "           \"bookingDate\": \""
                + TRANSACTION_DATE
                + "\", "
                + "           \"message\": \""
                + TRANSACTION_MESSAGE
                + "\", "
                + "           \"receiverOrPayerName\": \"Mr. T\", "
                + "           \"reference\": \"1\", "
                + "           \"transactionId\": \""
                + TRANSACTION_ID
                + "\", "
                + "           \"transactionType\": \"Account transfer\""
                + "       }"
                + "   ]"
                + "}";
    }

    public static OtpInfoDto createOtpInfoDto() {
        return new OtpInfoDto(NEXT_OTP_INDEX, CURRENT_OTP_CARD, false, null);
    }

    public static OAuth2Token createOAuth2Token() {
        return OAuth2Token.create("Bearer", ACCESS_TOKEN, null, 3600);
    }

    public static OAuth2Token createExpiredOAuth2Token() {
        return OAuth2Token.create("Bearer", ACCESS_TOKEN, null, 0);
    }

    public static AuthenticationRequest createAuthenticationRequest() {
        final Credentials credentialsMock = mock(Credentials.class);
        when(credentialsMock.getField(Field.Key.USERNAME)).thenReturn(USERNAME);
        when(credentialsMock.getField(Field.Key.PASSWORD)).thenReturn(CORRECT_PASSWORD);

        return new AuthenticationRequest(credentialsMock);
    }

    public static TransactionalAccount getTransactionalAccount() {
        return TransactionalAccount.nxBuilder()
                .withType(TransactionalAccountType.CHECKING)
                .withPaymentAccountFlag()
                .withBalance(BalanceModule.of(createExactCurrencyAmount()))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(IBAN)
                                .withAccountNumber(IBAN)
                                .withAccountName(ACCOUNT_NAME)
                                .addIdentifier(new IbanIdentifier(IBAN))
                                .build())
                .setApiIdentifier(ACCOUNT_ID)
                .build()
                .orElse(null);
    }

    private static Transaction createTransaction() {
        return Transaction.builder()
                .setAmount(createExactCurrencyAmount())
                .setDate(LocalDate.parse(TRANSACTION_DATE))
                .setDescription(TRANSACTION_MESSAGE)
                .setPending(false)
                .build();
    }

    private static ExactCurrencyAmount createExactCurrencyAmount() {
        return new ExactCurrencyAmount(new BigDecimal(AMOUNT), "EUR");
    }

    public static TransactionKeyPaginatorResponse<String> createPaginatorResponse() {
        final List<Transaction> transactions = Collections.singletonList(createTransaction());

        return new TransactionKeyPaginatorResponseImpl<>(transactions, CONTINUATION_KEY);
    }
}
