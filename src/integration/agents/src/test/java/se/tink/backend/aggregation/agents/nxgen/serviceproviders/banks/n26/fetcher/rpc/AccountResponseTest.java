package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.n26.fetcher.rpc;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.Test;
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
        List<AccountIdentifier> accountIdentifiers = result.getIdentifiers();
        assertThat(accountIdentifiers.size()).isEqualTo(1);
        assertThat(accountIdentifiers.get(0)).isInstanceOf(IbanIdentifier.class);
        assertThat(accountIdentifiers.get(0).getIdentifier()).isEqualTo("DE95100110016601026293");
        // and
        assertThat(result.isUniqueIdentifierEqual("DE95100110016601026293")).isTrue();
    }
}
