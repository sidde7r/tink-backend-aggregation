package se.tink.backend.utils.guavaimpl.predicates;

import com.google.common.collect.ImmutableList;
import java.util.List;
import org.junit.Test;
import se.tink.backend.core.Account;
import se.tink.backend.core.Credentials;
import static org.assertj.core.api.Assertions.assertThat;

public class OnlyIncludeAccountsWhosCredentialsAreActiveTest {
    @Test
    public void isFalseForAccountCredentialNotInCredentialsList() {
        Account account = new Account();
        account.setCredentialsId("TheCredentialsId");

        List<Credentials> credentials = ImmutableList.of();

        OnlyIncludeAccountsWhosCredentialsAreActive filter = new OnlyIncludeAccountsWhosCredentialsAreActive(credentials);
        assertThat(filter.apply(account)).isFalse();
    }

    @Test
    public void isTrueForAccountCredentialInCredentialsList() {
        Account account = new Account();
        account.setCredentialsId("TheCredentialsId");

        Credentials c = new Credentials();
        c.setId("TheCredentialsId");
        c.setProviderName("TheProviderName");

        List<Credentials> credentials = ImmutableList.of(c);

        OnlyIncludeAccountsWhosCredentialsAreActive filter = new OnlyIncludeAccountsWhosCredentialsAreActive(credentials);
        assertThat(filter.apply(account)).isTrue();
    }
}
