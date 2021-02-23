package se.tink.backend.aggregation.agents.nxgen.no.banks.danskebank.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc.AccountDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc.AccountEntity;
import se.tink.backend.aggregation.compliance.account_capabilities.AccountCapabilities;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class NoAccountEntityMapperTest {

    private static final String BBAN = "bban";
    private static final String IBAN = "iban";
    private static final String IBAN_NUMBER = "NO6402401234567";
    private static final String BANK_IDENTIFIER = "bankIdentifier";
    private static final String ACCOUNT_EXT_NO = "12345678901";
    private static final String ACCOUNT_INT_NO = "1234567890";

    private DanskeBankApiClient apiClient;
    private DanskeBankConfiguration configuration;
    private NoAccountEntityMapper noAccountEntityMapper;
    private AccountEntity accountEntity;
    private AccountDetailsResponse accountDetailsResponse;

    @Before
    public void setUp() {
        apiClient = mock(DanskeBankApiClient.class);
        noAccountEntityMapper = new NoAccountEntityMapper();
        configuration = getDanskeBankConfiguration();
        accountEntity = getAccountEntity();
        accountDetailsResponse = getAccountDetailsReponse();
    }

    @Test
    public void checkingAccountShouldHaveBbanAsUniqueIdentifier() {
        // given & when
        TransactionalAccount result =
                noAccountEntityMapper
                        .toCheckingAccount(accountEntity, accountDetailsResponse)
                        .orElse(null);

        // then
        assert result != null;
        assertResultHasProperFieldsValues(result);
    }

    private void assertResultHasProperFieldsValues(TransactionalAccount result) {
        assertThat(result.getIdModule().getUniqueId()).isEqualTo(ACCOUNT_EXT_NO);
        assertThat(result.getIdentifiers().size()).isEqualTo(2);
        assertThat(
                        result.getIdentifiers().stream()
                                .filter(id -> id.getIdentifier().equals(ACCOUNT_EXT_NO))
                                .filter(id -> id.getType().toString().equals(BBAN))
                                .findFirst()
                                .get()
                                .getIdentifier())
                .isNotEmpty();
        assertThat(
                        result.getIdentifiers().stream()
                                .filter(id -> id.getIdentifier().equals(IBAN_NUMBER))
                                .filter(id -> id.getType().toString().equals(IBAN))
                                .findFirst()
                                .get()
                                .getIdentifier())
                .isNotEmpty();
        assertThat(result.getAccountNumber()).isEqualTo(ACCOUNT_EXT_NO);
        assertThat(result.getFromTemporaryStorage(BANK_IDENTIFIER)).isEqualTo(ACCOUNT_INT_NO);
        assertThat(result.getApiIdentifier()).isEqualTo(ACCOUNT_INT_NO);
    }

    @Test
    public void savingsAccountShouldHaveBbanAsUniqueIdentifier() {
        // given
        TransactionalAccount result =
                noAccountEntityMapper
                        .toSavingsAccount(configuration, accountEntity, accountDetailsResponse)
                        .orElse(null);

        // then
        assert result != null;
        assertResultHasProperFieldsValues(result);
    }

    @Test
    public void toCreditCardAccountWhenAccountNoLengthIsShorterThanMinLength() {
        // given & when
        CreditCardAccount result =
                noAccountEntityMapper.toCreditCardAccount(configuration, accountEntity);

        // then
        assertThat(result.getAccountNumber()).isEqualTo(ACCOUNT_EXT_NO);
        assertThat(result.getFromTemporaryStorage(BANK_IDENTIFIER)).isEqualTo(ACCOUNT_INT_NO);
        assertThat(result.getApiIdentifier()).isEqualTo(ACCOUNT_INT_NO);
    }

    private DanskeBankConfiguration getDanskeBankConfiguration() {
        DanskeBankConfiguration danskeBankConfiguration = mock(DanskeBankConfiguration.class);
        given(danskeBankConfiguration.canReceiveExternalTransfer(anyString()))
                .willReturn(AccountCapabilities.Answer.UNKNOWN);
        given(danskeBankConfiguration.canPlaceFunds(anyString()))
                .willReturn(AccountCapabilities.Answer.UNKNOWN);
        given(danskeBankConfiguration.canWithdrawCash(anyString()))
                .willReturn(AccountCapabilities.Answer.UNKNOWN);
        given(danskeBankConfiguration.canExecuteExternalTransfer(anyString()))
                .willReturn(AccountCapabilities.Answer.UNKNOWN);
        return danskeBankConfiguration;
    }

    private AccountEntity getAccountEntity() {
        return SerializationUtils.deserializeFromString(
                "{\n"
                        + "    \"currency\": \"EUR\",\n"
                        + "    \"accountName\": \"danske no account name\",\n"
                        + "    \"accountProduct\": \"danske no account product\",\n"
                        + "    \"accountNoExt\": \""
                        + ACCOUNT_EXT_NO
                        + "\",\n"
                        + "    \"accountNoInt\": \""
                        + ACCOUNT_INT_NO
                        + "\",\n"
                        + "    \"balance\": -1234.45\n"
                        + "}",
                AccountEntity.class);
    }

    private AccountDetailsResponse getAccountDetailsReponse() {
        return SerializationUtils.deserializeFromString(
                "{"
                        + "\"traceId\":null,"
                        + "\"statusCode\":200,"
                        + "\"responseCode\":200,"
                        + "\"feeAmount\":0,"
                        + "\"feeCurrency\":\"EUR\","
                        + "\"accountType\":\"Danske account\","
                        + "\"accountNumber\":null,"
                        + "\"registrationNumber\":null,"
                        + "\"iban\":\""
                        + IBAN_NUMBER
                        + "\","
                        + "\"bic\":\"DABANO22"
                        + "\"}",
                AccountDetailsResponse.class);
    }
}
