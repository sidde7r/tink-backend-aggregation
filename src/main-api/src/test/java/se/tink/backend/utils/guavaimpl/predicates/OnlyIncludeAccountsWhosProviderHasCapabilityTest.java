package se.tink.backend.utils.guavaimpl.predicates;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.Collections;
import org.junit.Test;
import se.tink.backend.core.Account;
import se.tink.backend.core.Credentials;
import se.tink.backend.core.Provider;
import static org.assertj.core.api.Assertions.assertThat;

public class OnlyIncludeAccountsWhosProviderHasCapabilityTest {
    @Test
    public void isFalseWhenProviderWithoutCapability() {
        Provider provider = new Provider();
        provider.setName("provider-name");
        provider.setCapabilities(Collections.<Provider.Capability>emptySet());

        Credentials credentials = new Credentials();
        credentials.setId("123-456");
        credentials.setProviderName("missing-provider");

        OnlyIncludeAccountsWhosProviderHasCapability filter =
                new OnlyIncludeAccountsWhosProviderHasCapability(
                        Provider.Capability.TRANSFERS,
                        ImmutableList.of(credentials),
                        ImmutableMap.of(provider.getName(), provider));

        Account account = new Account();
        account.setCredentialsId("123-456");

        assertThat(filter.apply(account)).isFalse();
    }

    @Test
    public void isFalseWhenProviderMissing() {
        Provider provider = new Provider();
        provider.setName("provider-name");
        provider.setCapabilities(Collections.<Provider.Capability>emptySet());

        Credentials credentials = new Credentials();
        credentials.setId("123-456");
        credentials.setProviderName("missing-provider");

        OnlyIncludeAccountsWhosProviderHasCapability filter =
                new OnlyIncludeAccountsWhosProviderHasCapability(
                        Provider.Capability.TRANSFERS,
                        ImmutableList.of(credentials),
                        ImmutableMap.of(provider.getName(), provider));

        Account account = new Account();
        account.setCredentialsId("123-456");

        assertThat(filter.apply(account)).isFalse();
    }

    @Test
    public void isFalseWhenCredentialMissing() {
        Provider provider = new Provider();
        provider.setName("provider-name");
        provider.setCapabilities(Collections.<Provider.Capability>emptySet());

        Credentials credentials = new Credentials();
        credentials.setId("123-456");
        credentials.setProviderName("provider-name");

        OnlyIncludeAccountsWhosProviderHasCapability filter =
                new OnlyIncludeAccountsWhosProviderHasCapability(
                        Provider.Capability.TRANSFERS,
                        ImmutableList.of(credentials),
                        ImmutableMap.of(provider.getName(), provider));

        Account account = new Account();
        account.setCredentialsId("456-789");

        assertThat(filter.apply(account)).isFalse();
    }

    @Test
    public void isTrueWhenProviderHasCapability() {
        Provider provider = new Provider();
        provider.setName("provider-name");
        provider.setCapabilities(ImmutableSet.of(Provider.Capability.TRANSFERS));

        Credentials credentials = new Credentials();
        credentials.setId("123-456");
        credentials.setProviderName("provider-name");

        OnlyIncludeAccountsWhosProviderHasCapability filter =
                new OnlyIncludeAccountsWhosProviderHasCapability(
                        Provider.Capability.TRANSFERS,
                        ImmutableList.of(credentials),
                        ImmutableMap.of(provider.getName(), provider));

        Account account = new Account();
        account.setCredentialsId("123-456");

        assertThat(filter.apply(account)).isTrue();
    }
}
