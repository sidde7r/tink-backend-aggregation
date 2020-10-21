package se.tink.backend.aggregation.agents.nxgen.no.banks.danskebank.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc.AccountEntity;
import se.tink.backend.aggregation.compliance.account_capabilities.AccountCapabilities;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class NoAccountEntityMapperTest {

    private static final String BBAN = "bban";
    private static final String BANK_IDENTIFIER = "bankIdentifier";
    private static final String ACCOUNT_EXT_NO = "12345678901";
    private static final String ACCOUNT_INT_NO = "1234567890";

    private DanskeBankConfiguration configuration;
    private NoAccountEntityMapper noAccountEntityMapper;
    private AccountEntity accountEntity;

    @Before
    public void setUp() {
        noAccountEntityMapper = new NoAccountEntityMapper();
        configuration = getDanskeBankConfiguration();
        accountEntity = getAccountEntity();
    }

    @Test
    public void checkingAccountShouldHaveBbanAsUniqueIdentifier() {
        // given & when
        TransactionalAccount result =
                noAccountEntityMapper.toCheckingAccount(accountEntity).orElse(null);

        // then
        assert result != null;
        assertResultHasProperFieldsValues(result);
    }

    private void assertResultHasProperFieldsValues(TransactionalAccount result) {
        assertThat(result.getIdModule().getUniqueId()).isEqualTo(ACCOUNT_EXT_NO);
        assertThat(result.getIdentifiers().size()).isEqualTo(1);
        assertThat(result.getIdentifiers().get(0).getIdentifier()).isEqualTo(ACCOUNT_EXT_NO);
        assertThat(result.getIdentifiers().get(0).getType().toString()).isEqualTo(BBAN);
        assertThat(result.getAccountNumber()).isEqualTo(ACCOUNT_EXT_NO);
        assertThat(result.getFromTemporaryStorage(BANK_IDENTIFIER)).isEqualTo(ACCOUNT_INT_NO);
        assertThat(result.getApiIdentifier()).isEqualTo(ACCOUNT_INT_NO);
    }

    @Test
    public void savingsAccountShouldHaveBbanAsUniqueIdentifier() {
        // given
        TransactionalAccount result =
                noAccountEntityMapper.toSavingsAccount(configuration, accountEntity).orElse(null);

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
}
