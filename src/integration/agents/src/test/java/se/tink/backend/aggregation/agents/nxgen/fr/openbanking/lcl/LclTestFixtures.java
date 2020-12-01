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

    public static final String CLIENT_ID = "PSDSE-FINA-44059_1";
    public static final String SERVER_URL = "https://psd.lcl.fr";
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
    private static final String QWAC =
            "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";
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

        when(configurationMock.getQsealcKeyId()).thenReturn(QSEALC_KEY_ID);

        final AgentConfiguration<LclConfiguration> agentConfigurationMock =
                mock(AgentConfiguration.class);

        when(agentConfigurationMock.getProviderSpecificConfiguration())
                .thenReturn(configurationMock);
        when(agentConfigurationMock.getRedirectUrl()).thenReturn(REDIRECT_URL);
        when(agentConfigurationMock.getQwac()).thenReturn(QWAC);

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
