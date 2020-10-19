package se.tink.backend.aggregation.agents.nxgen.dk.banks.danskebank.mapper;

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

public class DkAccountEntityMapperTest {

    private static final String ACCOUNT_NO_EXT = "123234345";
    private static final String ACCOUNT_NO_INT = "567678789";
    private static final String BANK_IDENTIFIER = "bankIdentifier";
    private static final String DK_MARKET_CODE = "dk";
    private static final String ZERO = "0";
    private static final String TEN_DIGIT_ACCOUNT_NO_EXT = ZERO + ACCOUNT_NO_EXT;
    private static final String TEN_DIGIT_ACCOUNT_NO_INT = ZERO + ACCOUNT_NO_INT;

    private DkAccountEntityMapper dkAccountEntityMapper;
    private AccountEntity accountEntity;
    private DanskeBankConfiguration configuration;

    @Before
    public void setUp() {
        dkAccountEntityMapper = new DkAccountEntityMapper();
        accountEntity = getAccountEntity(ACCOUNT_NO_EXT, ACCOUNT_NO_INT);
        configuration = getDanskeBankConfiguration();
    }

    @Test
    public void toCheckingAccountWhenAccountNumberLengthIsShorterThanMinLength() {
        // given & when
        TransactionalAccount result =
                dkAccountEntityMapper.toCheckingAccount(accountEntity).orElse(null);

        // then
        assert result != null;
        assertThat(result.getIdModule().getUniqueId()).isEqualTo(ZERO + ACCOUNT_NO_EXT);
        assertThat(result.getIdentifiers().size()).isEqualTo(1);
        assertThat(result.getIdentifiers().get(0).getIdentifier()).isEqualTo(ACCOUNT_NO_EXT);
        assertThat(result.getIdentifiers().get(0).getType().toString()).isEqualTo(DK_MARKET_CODE);
        assertThat(result.getIdentifiers().get(0).getType().toString()).isEqualTo(DK_MARKET_CODE);
        assertThat(result.getAccountNumber()).isEqualTo(ACCOUNT_NO_EXT);
        assertThat(result.getFromTemporaryStorage(BANK_IDENTIFIER)).isEqualTo(ACCOUNT_NO_INT);
        assertThat(result.getApiIdentifier()).isEqualTo(ACCOUNT_NO_INT);
    }

    @Test
    public void toCheckingAccountWhenAccountNumberLengthHasMinLength() {
        // given
        accountEntity = getAccountEntity(TEN_DIGIT_ACCOUNT_NO_EXT, TEN_DIGIT_ACCOUNT_NO_INT);

        // when
        TransactionalAccount result =
                dkAccountEntityMapper.toCheckingAccount(accountEntity).orElse(null);

        // then
        assert result != null;
        assertThat(result.getIdModule().getUniqueId()).isEqualTo(TEN_DIGIT_ACCOUNT_NO_EXT);
        assertThat(result.getIdentifiers().size()).isEqualTo(1);
        assertThat(result.getIdentifiers().get(0).getIdentifier())
                .isEqualTo(TEN_DIGIT_ACCOUNT_NO_EXT);
        assertThat(result.getAccountNumber()).isEqualTo(TEN_DIGIT_ACCOUNT_NO_EXT);
        assertThat(result.getFromTemporaryStorage(BANK_IDENTIFIER))
                .isEqualTo(TEN_DIGIT_ACCOUNT_NO_INT);
        assertThat(result.getApiIdentifier()).isEqualTo(TEN_DIGIT_ACCOUNT_NO_INT);
    }

    @Test
    public void toSavingsAccountWhenAccountNumberLengthIsShorterThanMinLength() {
        // given
        TransactionalAccount result =
                dkAccountEntityMapper.toSavingsAccount(configuration, accountEntity).orElse(null);

        // then
        assert result != null;
        assertThat(result.getIdModule().getUniqueId()).isEqualTo(ZERO + ACCOUNT_NO_EXT);
        assertThat(result.getIdentifiers().size()).isEqualTo(1);
        assertThat(result.getIdentifiers().get(0).getIdentifier()).isEqualTo(ACCOUNT_NO_EXT);
        assertThat(result.getAccountNumber()).isEqualTo(ACCOUNT_NO_EXT);
        assertThat(result.getFromTemporaryStorage(BANK_IDENTIFIER)).isEqualTo(ACCOUNT_NO_INT);
        assertThat(result.getApiIdentifier()).isEqualTo(ACCOUNT_NO_INT);
    }

    @Test
    public void toSavingsAccountWhenAccountNumberLengthHasMinLength() {
        // given
        accountEntity = getAccountEntity(TEN_DIGIT_ACCOUNT_NO_EXT, TEN_DIGIT_ACCOUNT_NO_INT);

        // when
        TransactionalAccount result =
                dkAccountEntityMapper.toSavingsAccount(configuration, accountEntity).orElse(null);

        // then
        assert result != null;
        assertThat(result.getIdModule().getUniqueId()).isEqualTo(TEN_DIGIT_ACCOUNT_NO_EXT);
        assertThat(result.getIdentifiers().size()).isEqualTo(1);
        assertThat(result.getIdentifiers().get(0).getIdentifier())
                .isEqualTo(TEN_DIGIT_ACCOUNT_NO_EXT);
        assertThat(result.getAccountNumber()).isEqualTo(TEN_DIGIT_ACCOUNT_NO_EXT);
        assertThat(result.getFromTemporaryStorage(BANK_IDENTIFIER))
                .isEqualTo(TEN_DIGIT_ACCOUNT_NO_INT);
        assertThat(result.getApiIdentifier()).isEqualTo(TEN_DIGIT_ACCOUNT_NO_INT);
    }

    @Test
    public void toCreditCardAccountWhenAccountNumberLengthIsShorterThanMinLength() {
        // given & when
        CreditCardAccount result =
                dkAccountEntityMapper.toCreditCardAccount(configuration, accountEntity);

        // then
        assertThat(result.getAccountNumber()).isEqualTo(ACCOUNT_NO_EXT);
        assertThat(result.getFromTemporaryStorage(BANK_IDENTIFIER)).isEqualTo(ACCOUNT_NO_INT);
        assertThat(result.getApiIdentifier()).isEqualTo(ACCOUNT_NO_INT);
    }

    @Test
    public void toCreditCardAccountWhenAccountNumberLengthHasMinLength() {
        // given
        accountEntity = getAccountEntity(TEN_DIGIT_ACCOUNT_NO_EXT, TEN_DIGIT_ACCOUNT_NO_INT);

        // when
        CreditCardAccount result =
                dkAccountEntityMapper.toCreditCardAccount(configuration, accountEntity);

        // then
        assertThat(result.getAccountNumber()).isEqualTo(TEN_DIGIT_ACCOUNT_NO_EXT);
        assertThat(result.getFromTemporaryStorage(BANK_IDENTIFIER))
                .isEqualTo(TEN_DIGIT_ACCOUNT_NO_INT);
        assertThat(result.getApiIdentifier()).isEqualTo(TEN_DIGIT_ACCOUNT_NO_INT);
    }

    private DanskeBankConfiguration getDanskeBankConfiguration() {
        DanskeBankConfiguration danskeBankConfiguration = mock(DanskeBankConfiguration.class);
        given(danskeBankConfiguration.canExecuteExternalTransfer(anyString()))
                .willReturn(AccountCapabilities.Answer.NO);
        given(danskeBankConfiguration.canReceiveExternalTransfer(anyString()))
                .willReturn(AccountCapabilities.Answer.NO);
        given(danskeBankConfiguration.canPlaceFunds(anyString()))
                .willReturn(AccountCapabilities.Answer.NO);
        given(danskeBankConfiguration.canWithdrawCash(anyString()))
                .willReturn(AccountCapabilities.Answer.NO);
        return danskeBankConfiguration;
    }

    private AccountEntity getAccountEntity(String accountExtNo, String accountIntNo) {
        return SerializationUtils.deserializeFromString(
                "{\n"
                        + "    \"currency\": \"PLN\",\n"
                        + "    \"accountName\": \"sample account name\",\n"
                        + "    \"accountProduct\": \"sample account product\",\n"
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
