package se.tink.backend.aggregation.agents.nxgen.dk.banks.danskebank.rpc;

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
import se.tink.backend.aggregation.compliance.account_capabilities.AccountCapabilities;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class DkAccountEntityTest {

    private static final String ACCOUNT_NO_EXT = "123234345";
    private static final String ACCOUNT_NO_INT = "567678789";
    private static final String BANK_IDENTIFIER = "bankIdentifier";
    private static final String ZERO = "0";

    private DkAccountEntity accountEntity;

    @Before
    public void setUp() {
        accountEntity = getAccountEntity(ACCOUNT_NO_EXT, ACCOUNT_NO_INT);
    }

    @Test
    public void toCheckingAccountWhenAccountNumberLengthIsShorterThanMinLength() {
        // given & when
        TransactionalAccount result = accountEntity.toCheckingAccount();

        // then
        assertThat(result.getAccountNumber()).isEqualTo(ZERO + ACCOUNT_NO_EXT);
        assertThat(result.getFromTemporaryStorage(BANK_IDENTIFIER))
                .isEqualTo(ZERO + ACCOUNT_NO_INT);
        assertThat(result.getApiIdentifier()).isEqualTo(ZERO + ACCOUNT_NO_INT);
    }

    @Test
    public void toCheckingAccountWhenAccountNumberLengthHasMinLength() {
        // given
        String tenDigitAccountNoExt = ACCOUNT_NO_EXT.concat("1");
        String tenDigitAccountNoInt = ACCOUNT_NO_INT.concat("1");
        accountEntity = getAccountEntity(tenDigitAccountNoExt, tenDigitAccountNoInt);

        // when
        TransactionalAccount result = accountEntity.toCheckingAccount();

        // then
        assertThat(result.getAccountNumber()).isEqualTo(tenDigitAccountNoExt);
        assertThat(result.getFromTemporaryStorage(BANK_IDENTIFIER)).isEqualTo(tenDigitAccountNoInt);
        assertThat(result.getApiIdentifier()).isEqualTo(tenDigitAccountNoInt);
    }

    @Test
    public void toSavingsAccountWhenAccountNumberLengthIsShorterThanMinLength() {
        // given
        DanskeBankConfiguration configuration = getDanskeBankConfiguration();

        // given
        TransactionalAccount result = accountEntity.toSavingsAccount(configuration);

        // then
        assertThat(result.getAccountNumber()).isEqualTo(ZERO + ACCOUNT_NO_EXT);
        assertThat(result.getFromTemporaryStorage(BANK_IDENTIFIER))
                .isEqualTo(ZERO + ACCOUNT_NO_INT);
        assertThat(result.getApiIdentifier()).isEqualTo(ZERO + ACCOUNT_NO_INT);
    }

    @Test
    public void toSavingsAccountWhenAccountNumberLengthHasMinLength() {
        // given
        DanskeBankConfiguration configuration = getDanskeBankConfiguration();
        String tenDigitAccountNoExt = ACCOUNT_NO_EXT.concat("1");
        String tenDigitAccountNoInt = ACCOUNT_NO_INT.concat("1");
        accountEntity = getAccountEntity(tenDigitAccountNoExt, tenDigitAccountNoInt);

        // when
        TransactionalAccount result = accountEntity.toSavingsAccount(configuration);

        // then
        assertThat(result.getAccountNumber()).isEqualTo(tenDigitAccountNoExt);
        assertThat(result.getFromTemporaryStorage(BANK_IDENTIFIER)).isEqualTo(tenDigitAccountNoInt);
        assertThat(result.getApiIdentifier()).isEqualTo(tenDigitAccountNoInt);
    }

    @Test
    public void toCreditCardAccountWhenAccountNumberLengthIsShorterThanMinLength() {
        // given
        DanskeBankConfiguration configuration = getDanskeBankConfiguration();

        // when
        CreditCardAccount result = accountEntity.toCreditCardAccount(configuration);

        // then
        assertThat(result.getAccountNumber()).isEqualTo(ZERO + ACCOUNT_NO_EXT);
        assertThat(result.getFromTemporaryStorage(BANK_IDENTIFIER))
                .isEqualTo(ZERO + ACCOUNT_NO_INT);
        assertThat(result.getApiIdentifier()).isEqualTo(ZERO + ACCOUNT_NO_INT);
    }

    @Test
    public void toCreditCardAccountWhenAccountNumberLengthHasMinLength() {
        // given
        DanskeBankConfiguration configuration = getDanskeBankConfiguration();
        String tenDigitAccountNoExt = ACCOUNT_NO_EXT.concat("1");
        String tenDigitAccountNoInt = ACCOUNT_NO_INT.concat("1");
        accountEntity = getAccountEntity(tenDigitAccountNoExt, tenDigitAccountNoInt);

        // when
        CreditCardAccount result = accountEntity.toCreditCardAccount(configuration);

        // then
        assertThat(result.getAccountNumber()).isEqualTo(tenDigitAccountNoExt);
        assertThat(result.getFromTemporaryStorage(BANK_IDENTIFIER)).isEqualTo(tenDigitAccountNoInt);
        assertThat(result.getApiIdentifier()).isEqualTo(tenDigitAccountNoInt);
    }

    private DanskeBankConfiguration getDanskeBankConfiguration() {
        return getDanskeBankConfiguration(new HashMap<>());
    }

    private DanskeBankConfiguration getDanskeBankConfiguration(Map<String, Type> loanAccountTypes) {
        DanskeBankConfiguration danskeBankConfiguration = mock(DanskeBankConfiguration.class);
        given(danskeBankConfiguration.getLoanAccountTypes()).willReturn(loanAccountTypes);
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

    private DkAccountEntity getAccountEntity(String accountExtNo, String accountIntNo) {
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
                DkAccountEntity.class);
    }
}
