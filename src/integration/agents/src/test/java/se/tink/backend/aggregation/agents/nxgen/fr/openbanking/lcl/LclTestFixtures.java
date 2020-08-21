package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.apiclient.dto.accesstoken.TokenResponseDto;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.apiclient.dto.account.AccountIdentificationDto;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.apiclient.dto.account.AccountResourceDto;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.apiclient.dto.account.AccountsResponseDto;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.apiclient.dto.account.BalanceResourceDto;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.apiclient.dto.account.BalanceType;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.apiclient.dto.account.CashAccountType;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.apiclient.dto.common.AmountDto;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.apiclient.dto.identity.EndUserIdentityResponseDto;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.apiclient.dto.transaction.CreditDebitIndicator;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.apiclient.dto.transaction.RemittanceInformationDto;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.apiclient.dto.transaction.TransactionResourceDto;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.apiclient.dto.transaction.TransactionStatus;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.apiclient.dto.transaction.TransactionsResponseDto;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.configuration.LclConfiguration;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.fecther.converter.LclDataConverter;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class LclTestFixtures {

    public static final String CLIENT_ID = "DUMMY_CLIENT_ID";
    public static final String SERVER_URL = "https://base-url";
    public static final String REDIRECT_URL = "https://redirect-url";
    public static final String ACCESS_TOKEN = "DUMMY_ACCESS_TOKEN";
    public static final String REFRESH_TOKEN = "DUMMY_REFRESH_TOKEN";
    public static final String DIGEST = "DUMMY_DIGEST";
    public static final String SIGNATURE = "DUMMY_SIGNATURE";
    public static final String RESOURCE_ID = "DUMMY_RESOURCE_ID";
    public static final String DATE = "DUMMY_DATE";
    public static final String REQUEST_ID = "DUMMY_REQUEST_ID";
    public static final String QSEALC_KEY_ID = "DUMMY_QSEALC_KEY_ID";
    public static final Instant NOW = Instant.ofEpochMilli(0L);
    public static final ZoneId ZONE_ID = ZoneId.of("CET");
    public static final String AMOUNT = "234.56";
    public static final String CURRENCY = "EUR";
    public static final String IBAN = "FR6720041010050008697430710";
    public static final String PSU_NAME = "DUMMY_PSU_NAME";
    public static final String TRANSACTION_DESCRIPTION = "DUMMY_DESCRIPTION";
    public static final String AUTH_CODE = "DUMMY_AUTH_CODE";
    public static final String TOKEN_TYPE = "Bearer";
    private static final String BIC_FI = "DUMMY_BIC_FI";
    private static final String ACCOUNT_NAME = "DUMMY_ACCOUNT_NAME";
    private static final LocalDate LOCAL_DATE = LocalDate.now();
    private static final long TOKEN_EXPIRES_IN = 3600L;

    public static OAuth2Token createOAuth2Token() {
        return OAuth2Token.create(TOKEN_TYPE, ACCESS_TOKEN, REFRESH_TOKEN, TOKEN_EXPIRES_IN);
    }

    public static AccountsResponseDto createAccountsResponseDto() {
        return mock(AccountsResponseDto.class);
    }

    public static TransactionsResponseDto createTransactionsResponseDto() {
        return mock(TransactionsResponseDto.class);
    }

    public static EndUserIdentityResponseDto createEndUserIdentityResponseDto() {
        final EndUserIdentityResponseDto endUserIdentityResponseDtoMock =
                mock(EndUserIdentityResponseDto.class);

        when(endUserIdentityResponseDtoMock.getConnectedPsu()).thenReturn(PSU_NAME);

        return endUserIdentityResponseDtoMock;
    }

    public static TokenResponseDto createTokenResponseDto() {
        final TokenResponseDto tokenResponseDtoMock = mock(TokenResponseDto.class);

        when(tokenResponseDtoMock.getAccessToken()).thenReturn(ACCESS_TOKEN);
        when(tokenResponseDtoMock.getRefreshToken()).thenReturn(REFRESH_TOKEN);
        when(tokenResponseDtoMock.getTokenType()).thenReturn(TOKEN_TYPE);
        when(tokenResponseDtoMock.getExpiresIn()).thenReturn(TOKEN_EXPIRES_IN);

        return tokenResponseDtoMock;
    }

    @SuppressWarnings("unchecked")
    public static AgentConfiguration<LclConfiguration> createAgentConfigurationMock() {
        final LclConfiguration configurationMock = mock(LclConfiguration.class);

        when(configurationMock.getBaseUrl()).thenReturn(SERVER_URL);
        when(configurationMock.getQsealcKeyId()).thenReturn(QSEALC_KEY_ID);
        when(configurationMock.getClientId()).thenReturn(CLIENT_ID);

        final AgentConfiguration<LclConfiguration> agentConfigurationMock =
                mock(AgentConfiguration.class);

        when(agentConfigurationMock.getProviderSpecificConfiguration())
                .thenReturn(configurationMock);
        when(agentConfigurationMock.getRedirectUrl()).thenReturn(REDIRECT_URL);

        return agentConfigurationMock;
    }

    public static ExactCurrencyAmount createExactCurrencyAmount() {
        return new ExactCurrencyAmount(new BigDecimal(AMOUNT), CURRENCY);
    }

    public static LclDataConverter createLclDataConverterMock() {
        final LclDataConverter dataConverterMock = mock(LclDataConverter.class);

        when(dataConverterMock.convertAmountDtoToExactCurrencyAmount(any()))
                .thenReturn(createExactCurrencyAmount());

        return dataConverterMock;
    }

    public static AccountResourceDto createAccountResourceDtoMock(
            CashAccountType accountType, List<BalanceResourceDto> balances) {
        final AccountResourceDto accountResourceDtoMock = mock(AccountResourceDto.class);

        when(accountResourceDtoMock.getCashAccountType()).thenReturn(accountType);
        when(accountResourceDtoMock.getBalances()).thenReturn(balances);
        when(accountResourceDtoMock.getBicFi()).thenReturn(BIC_FI);

        final AccountIdentificationDto accountIdentificationDtoMock =
                mock(AccountIdentificationDto.class);
        when(accountIdentificationDtoMock.getIban()).thenReturn(IBAN);
        when(accountResourceDtoMock.getAccountId()).thenReturn(accountIdentificationDtoMock);
        when(accountResourceDtoMock.getName()).thenReturn(ACCOUNT_NAME);
        when(accountResourceDtoMock.getResourceId()).thenReturn(RESOURCE_ID);

        return accountResourceDtoMock;
    }

    public static BalanceResourceDto createBalanceResourceDtoMock(BalanceType balanceType) {
        final BalanceResourceDto balanceResourceDtoMock = mock(BalanceResourceDto.class);

        when(balanceResourceDtoMock.getBalanceAmount()).thenReturn(createAmountDto());
        when(balanceResourceDtoMock.getBalanceType()).thenReturn(balanceType);

        return balanceResourceDtoMock;
    }

    public static TransactionResourceDto createTransactionResourceDtoMock(
            CreditDebitIndicator creditDebitIndicator, TransactionStatus transactionStatus) {
        final TransactionResourceDto transactionResourceDtoMock =
                mock(TransactionResourceDto.class);

        when(transactionResourceDtoMock.getBookingDate()).thenReturn(LOCAL_DATE);
        when(transactionResourceDtoMock.getCreditDebitIndicator()).thenReturn(creditDebitIndicator);

        final RemittanceInformationDto remittanceInformationDtoMock =
                mock(RemittanceInformationDto.class);
        when(remittanceInformationDtoMock.getUnstructured())
                .thenReturn(ImmutableList.of(TRANSACTION_DESCRIPTION));
        when(transactionResourceDtoMock.getRemittanceInformation())
                .thenReturn(remittanceInformationDtoMock);

        when(transactionResourceDtoMock.getStatus()).thenReturn(transactionStatus);
        when(transactionResourceDtoMock.getTransactionAmount()).thenReturn(createAmountDto());

        return transactionResourceDtoMock;
    }

    public static TransactionalAccount createTransactionalAccountMock() {
        final TransactionalAccount transactionalAccountMock = mock(TransactionalAccount.class);

        when(transactionalAccountMock.getApiIdentifier()).thenReturn(RESOURCE_ID);

        return transactionalAccountMock;
    }

    public static String createRetrieveTokenRequestString() {
        try {
            return String.format(
                    "client_id=%s&redirect_uri=%s&grant_type=authorization_code&code=%s",
                    CLIENT_ID,
                    URLEncoder.encode(REDIRECT_URL, StandardCharsets.UTF_8.toString()),
                    AUTH_CODE);
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static String createRefreshTokenRequestString() {
        return String.format(
                "client_id=%s&scope=aisp&grant_type=refresh_token&refresh_token=%s",
                CLIENT_ID, REFRESH_TOKEN);
    }

    private static AmountDto createAmountDto() {
        final AmountDto amountDto = new AmountDto();

        amountDto.setAmount(AMOUNT);
        amountDto.setCurrency(CURRENCY);

        return amountDto;
    }
}
