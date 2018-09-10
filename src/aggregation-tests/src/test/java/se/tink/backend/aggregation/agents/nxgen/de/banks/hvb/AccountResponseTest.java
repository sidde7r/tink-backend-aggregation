package se.tink.backend.aggregation.agents.nxgen.de.banks.hvb;

import java.util.Collection;
import java.util.Collections;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.fetcher.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.fetcher.rpc.AccountResponse;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.backend.aggregation.rpc.AccountTypes;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;

public final class AccountResponseTest {
    @Test
    public void ensureGetTransactionalAccounts_withUnspecifiedAccountList_returnsAnEmptyCollection() {
        final AccountResponse response = new AccountResponse();
        assertThat(response.getTransactionalAccounts(), is(empty()));
    }

    @Test
    public void ensureGetTransactionalAccounts_withEmptyAccountList_returnsAnEmptyCollection() {
        final AccountResponse response = new AccountResponse(Collections.emptyList());
        assertThat(response.getTransactionalAccounts(), is(empty()));
    }

    @Test(expected = NullPointerException.class)
    public void ensureGetTransactionalAccounts_withListContainingNull_throwsNPE() {
        final AccountResponse response = new AccountResponse(Collections.singletonList(null));
        response.getTransactionalAccounts();
    }

    @Test(expected = NullPointerException.class)
    public void ensureGetTransactionalAccounts_withNullType_throwsNPE() {
        final AccountResponse response = new AccountResponse(Collections.singletonList(new AccountEntity(
                7.0, "iban", "7", "SEK", "hoy", "bic", null
        )));
        response.getTransactionalAccounts();
    }

    @Test
    public void ensureGetTransactionalAccounts_withUnrecognizedType_returnsAnEmptyCollection() {
        final AccountResponse response = new AccountResponse(Collections.singletonList(new AccountEntity(
                7.0, "iban", "7", "SEK", "hoy", "bic", "hoy"
        )));
        assertThat(response.getTransactionalAccounts(), is(empty()));
    }

    @Test
    public void ensureGetTransactionalAccounts_withRecognizedType_returnsAnAccountOfThatType() {
        final AccountResponse response = new AccountResponse(Collections.singletonList(new AccountEntity(
                7.0, "iban", "7", "SEK", "HVB AktivKonto", "bic", "hoy"
        )));
        final Collection<TransactionalAccount.Builder<?, ?>> accounts = response.getTransactionalAccounts();
        assertThat(accounts, hasSize(1));
        final TransactionalAccount account = accounts.iterator().next().build();
        assertThat(account.getType(), is(AccountTypes.CHECKING));
    }

    @Test
    public void ensureGetTransactionalAccounts_withKnownType_returnsAnAccountOfThatType() {
        final AccountResponse response = new AccountResponse(Collections.singletonList(new AccountEntity(
                7.0, "iban", "7", "SEK", "Misleading HVB Konto Start", "bic", "6"
        )));
        final Collection<TransactionalAccount.Builder<?, ?>> accounts = response.getTransactionalAccounts();
        assertThat(accounts, hasSize(1));
        final TransactionalAccount account = accounts.iterator().next().build();
        assertThat(account.getType(), is(AccountTypes.SAVINGS));
    }
}
