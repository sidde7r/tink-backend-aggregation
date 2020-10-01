package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.n26.fetcher.rpc;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Field;
import java.util.List;
import org.junit.Test;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.core.account.transactional.CheckingAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class AccountResponseTest {

    @Test
    public void toTransactionalAccount() {
        // given
        AccountResponse accountResponse =
                SerializationUtils.deserializeFromString(
                        "{"
                                + "\"id\": \"sample_id\", "
                                + "\"availableBalance\": 123.45, "
                                + "\"usableBalance\": 234.56, "
                                + "\"bankBalance\": 345.67, "
                                + "\"iban\": \"DE95100110016601026293\", "
                                + "\"bic\": \"sample bic\", "
                                + "\"bankName\": \"sample bank name\", "
                                + "\"seized\": true, "
                                + "\"currency\": \"EUR\""
                                + "}",
                        AccountResponse.class);

        // when
        TransactionalAccount result = accountResponse.toTransactionalAccount();

        // then
        assertThat(result).isInstanceOf(CheckingAccount.class);
        assertThat(result.getAccountNumber()).isEqualTo("DE95100110016601026293");
        assertThat(result.getName()).isEqualTo("sample bank name");
        assertThat(result.getExactBalance().compareTo(ExactCurrencyAmount.inEUR(123.45)))
                .isEqualTo(0);
        // and identifiers
        assertIdentifiers(result);
        // and
        assertUniqueIdentifier(result);
    }

    private <T extends Account> void assertIdentifiers(T account) {
        List<AccountIdentifier> accountIdentifiers = account.getIdentifiers();
        assertThat(accountIdentifiers.size()).isEqualTo(1);
        assertThat(accountIdentifiers.get(0)).isInstanceOf(IbanIdentifier.class);
        assertThat(accountIdentifiers.get(0).getIdentifier()).isEqualTo("DE95100110016601026293");
    }

    private <T extends Account> void assertUniqueIdentifier(T account) {
        String uniqueIdentifier = "";
        try {
            Field field = Account.class.getDeclaredField("uniqueIdentifier");
            field.setAccessible(true);
            uniqueIdentifier = (String) field.get(account);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            // NOP
        }
        assertThat(uniqueIdentifier).isEqualTo("DE95100110016601026293");
    }
}
