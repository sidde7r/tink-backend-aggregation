package se.tink.backend.aggregation.agents.nxgen.no.banks.danskebank.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.models.Loan.Type;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc.AccountEntity;
import se.tink.backend.aggregation.compliance.account_capabilities.AccountCapabilities;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class NoAccountEntityMapperTest {

    private static final String IDENTIFIER_TYPE = "bban";
    private static final String BANK_IDENTIFIER = "bankIdentifier";
    private static final String ACCOUNT_EXT_NO = "1234567890";
    private static final String ACCOUNT_INT_NO = "1234567890";
    private static final String ZERO = "0";

    private DanskeBankConfiguration configuration;
    private NoAccountEntityMapper noAccountEntityMapper;
    private AccountEntity accountEntity;

    @Before
    public void setUp() {
        noAccountEntityMapper = new NoAccountEntityMapper();
        configuration = getDanskeBankConfiguration();
        accountEntity = getAccountEntity(ACCOUNT_EXT_NO, ACCOUNT_INT_NO);
    }

    @Test
    public void toCheckingAccountWhenAccountNoLengthIsShorterThanMinLength() {
        // given & when
        TransactionalAccount result =
                noAccountEntityMapper.toCheckingAccount(accountEntity).orElse(null);

        // then
        assert result != null;
        assertThat(result.getIdModule().getUniqueId()).isEqualTo(ZERO + ACCOUNT_EXT_NO);
        assertThat(result.getIdentifiers().size()).isEqualTo(1);
        assertThat(result.getIdentifiers().get(0).getIdentifier()).isEqualTo(ACCOUNT_EXT_NO);
        assertThat(result.getIdentifiers().get(0).getType().toString()).isEqualTo(IDENTIFIER_TYPE);
        assertThat(result.getIdentifiers().get(0).getType().toString()).isEqualTo(IDENTIFIER_TYPE);
        assertThat(result.getAccountNumber()).isEqualTo(ACCOUNT_EXT_NO);
        assertThat(result.getFromTemporaryStorage(BANK_IDENTIFIER)).isEqualTo(ACCOUNT_INT_NO);
        assertThat(result.getApiIdentifier()).isEqualTo(ACCOUNT_INT_NO);
    }

    @Test
    public void toCheckingAccountWhenAccountNoLengthHasMinLength() {
        // given
        String elevenDigitAccountNoExt = ACCOUNT_EXT_NO.concat(ZERO);
        String elevenDigitAccountNoInt = ACCOUNT_INT_NO.concat(ZERO);
        accountEntity = getAccountEntity(elevenDigitAccountNoExt, elevenDigitAccountNoInt);

        // when
        TransactionalAccount result =
                noAccountEntityMapper.toCheckingAccount(accountEntity).orElse(null);

        // then
        assert result != null;
        assertThat(result.getIdModule().getUniqueId()).isEqualTo(elevenDigitAccountNoExt);
        assertThat(result.getIdentifiers().size()).isEqualTo(1);
        assertThat(result.getIdentifiers().get(0).getIdentifier())
                .isEqualTo(elevenDigitAccountNoExt);
        assertThat(result.getAccountNumber()).isEqualTo(elevenDigitAccountNoExt);
        assertThat(result.getFromTemporaryStorage(BANK_IDENTIFIER))
                .isEqualTo(elevenDigitAccountNoInt);
        assertThat(result.getApiIdentifier()).isEqualTo(elevenDigitAccountNoInt);
    }

    @Test
    public void toSavingsAccountWhenAccountNoLengthIsShorterThanMinLength() {
        // given
        TransactionalAccount result =
                noAccountEntityMapper.toSavingsAccount(configuration, accountEntity).orElse(null);

        // then
        assert result != null;
        assertThat(result.getIdModule().getUniqueId()).isEqualTo(ZERO + ACCOUNT_EXT_NO);
        assertThat(result.getIdentifiers().size()).isEqualTo(1);
        assertThat(result.getIdentifiers().get(0).getIdentifier()).isEqualTo(ACCOUNT_EXT_NO);
        assertThat(result.getAccountNumber()).isEqualTo(ACCOUNT_EXT_NO);
        assertThat(result.getFromTemporaryStorage(BANK_IDENTIFIER)).isEqualTo(ACCOUNT_INT_NO);
        assertThat(result.getApiIdentifier()).isEqualTo(ACCOUNT_INT_NO);
    }

    @Test
    public void toSavingsAccountWhenAccountNoLengthHasMinLength() {
        // given
        String elevenDigitAccountNoExt = ACCOUNT_EXT_NO.concat(ZERO);
        String elevenDigitAccountNoInt = ACCOUNT_INT_NO.concat(ZERO);
        accountEntity = getAccountEntity(elevenDigitAccountNoExt, elevenDigitAccountNoInt);

        // when
        TransactionalAccount result =
                noAccountEntityMapper.toSavingsAccount(configuration, accountEntity).orElse(null);

        // then
        assert result != null;
        assertThat(result.getIdModule().getUniqueId()).isEqualTo(elevenDigitAccountNoExt);
        assertThat(result.getIdentifiers().size()).isEqualTo(1);
        assertThat(result.getIdentifiers().get(0).getIdentifier())
                .isEqualTo(elevenDigitAccountNoExt);
        assertThat(result.getAccountNumber()).isEqualTo(elevenDigitAccountNoExt);
        assertThat(result.getFromTemporaryStorage(BANK_IDENTIFIER))
                .isEqualTo(elevenDigitAccountNoInt);
        assertThat(result.getApiIdentifier()).isEqualTo(elevenDigitAccountNoInt);
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

    @Test
    public void toCreditCardAccountWhenAccountNoLengthHasMinLength() {
        // given
        String elevenDigitAccountNoExt = ACCOUNT_EXT_NO.concat(ZERO);
        String elevenDigitAccountNoInt = ACCOUNT_INT_NO.concat(ZERO);
        accountEntity = getAccountEntity(elevenDigitAccountNoExt, elevenDigitAccountNoInt);

        // when
        CreditCardAccount result =
                noAccountEntityMapper.toCreditCardAccount(configuration, accountEntity);

        // then
        assertThat(result.getAccountNumber()).isEqualTo(elevenDigitAccountNoExt);
        assertThat(result.getFromTemporaryStorage(BANK_IDENTIFIER))
                .isEqualTo(elevenDigitAccountNoInt);
        assertThat(result.getApiIdentifier()).isEqualTo(elevenDigitAccountNoInt);
    }

    private DanskeBankConfiguration getDanskeBankConfiguration() {
        return getDanskeBankConfiguration(new HashMap<>());
    }

    private DanskeBankConfiguration getDanskeBankConfiguration(
            Map<String, Type> loanAccountsTypes) {
        DanskeBankConfiguration danskeBankConfiguration = mock(DanskeBankConfiguration.class);
        given(danskeBankConfiguration.getLoanAccountTypes()).willReturn(loanAccountsTypes);
        given(danskeBankConfiguration.canExecuteExternalTransfer(anyString()))
                .willReturn(AccountCapabilities.Answer.YES);
        given(danskeBankConfiguration.canReceiveExternalTransfer(anyString()))
                .willReturn(AccountCapabilities.Answer.YES);
        given(danskeBankConfiguration.canPlaceFunds(anyString()))
                .willReturn(AccountCapabilities.Answer.YES);
        given(danskeBankConfiguration.canWithdrawCash(anyString()))
                .willReturn(AccountCapabilities.Answer.YES);
        return danskeBankConfiguration;
    }

    private AccountEntity getAccountEntity(String accountIntNo, String accountExtNo) {
        return SerializationUtils.deserializeFromString(
                "{\n"
                        + "    \"currency\": \"EUR\",\n"
                        + "    \"accountName\": \"danske no account name\",\n"
                        + "    \"accountProduct\": \"danske no account product\",\n"
                        + "    \"accountNoExt\": \""
                        + accountExtNo
                        + "\",\n"
                        + "    \"accountNoInt\": \""
                        + accountIntNo
                        + "\",\n"
                        + "    \"balance\": -1234.45\n"
                        + "}",
                AccountEntity.class);
    }
}
