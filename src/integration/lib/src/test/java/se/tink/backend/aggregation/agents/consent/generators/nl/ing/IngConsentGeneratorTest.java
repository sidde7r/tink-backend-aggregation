package se.tink.backend.aggregation.agents.consent.generators.nl.ing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import com.google.common.collect.Sets;
import java.util.LinkedHashSet;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.libraries.credentials.service.ManualAuthenticateRequest;
import se.tink.libraries.credentials.service.RefreshScope;
import se.tink.libraries.credentials.service.RefreshableItem;

public class IngConsentGeneratorTest {
    private IngConsentGenerator generator;
    private AgentComponentProvider componentProvider;
    private ManualAuthenticateRequest manualAuthenticateRequest;
    private RefreshScope refreshScope = new RefreshScope();

    @Before
    public void setUp() throws Exception {
        this.componentProvider = mock(AgentComponentProvider.class);
        this.manualAuthenticateRequest = mock(ManualAuthenticateRequest.class);
        this.refreshScope = new RefreshScope();
        this.manualAuthenticateRequest.setRefreshScope(refreshScope);
    }

    @Test
    public void shouldMapAllItemsCorrectly() {
        // given
        refreshScope.setRefreshableItemsIn(Sets.newHashSet(RefreshableItem.CHECKING_ACCOUNTS, RefreshableItem.CHECKING_TRANSACTIONS));
        given(manualAuthenticateRequest.getRefreshScope()).willReturn(refreshScope);
        given(componentProvider.getCredentialsRequest()).willReturn(manualAuthenticateRequest);
        generator = IngConsentGenerator.of(manualAuthenticateRequest, getIngScopes());

        // when
        String scopes = generator.generate();

        // then
        assertThat(scopes).isEqualTo(String.join(" ", getAllScopes()));

    }

    @Test
    public void shouldMapAccountItemsCorrectly() {
        // given
        refreshScope.setRefreshableItemsIn(Sets.newHashSet(RefreshableItem.CHECKING_ACCOUNTS));
        given(manualAuthenticateRequest.getRefreshScope()).willReturn(refreshScope);
        given(componentProvider.getCredentialsRequest()).willReturn(manualAuthenticateRequest);
        generator = IngConsentGenerator.of(manualAuthenticateRequest, getIngScopes());

        // when
        String scopes = generator.generate();

        // then
        assertThat(scopes).isEqualTo(String.join(" ", getAccountsScopes()));
    }

    @Test
    public void shouldMapTransactionsItemsCorrectly() {
        // given
        refreshScope.setRefreshableItemsIn(Sets.newHashSet(RefreshableItem.CHECKING_TRANSACTIONS));
        given(manualAuthenticateRequest.getRefreshScope()).willReturn(refreshScope);
        given(componentProvider.getCredentialsRequest()).willReturn(manualAuthenticateRequest);
        generator = IngConsentGenerator.of(manualAuthenticateRequest, getIngScopes());

        // when
        String scopes = generator.generate();

        // then
        assertThat(scopes).isEqualTo(String.join(" ", getTransactionsScopes()));
    }

    private static Set<IngScope> getIngScopes() {
        return Sets.newHashSet(
                IngScope.VIEW_PAYMENT_BALANCES,
                IngScope.VIEW_PAYMENT_TRANSACTIONS
        );
    }

    private Set<String> getAllScopes() {
        LinkedHashSet<String> set = new LinkedHashSet<>();
        set.add(IngScope.VIEW_PAYMENT_BALANCES.toString());
        set.add(IngScope.VIEW_PAYMENT_TRANSACTIONS.toString());
        return set;
    }

    private Set<String> getAccountsScopes() {
        LinkedHashSet<String> set = new LinkedHashSet<>();
        set.add(IngScope.VIEW_PAYMENT_BALANCES.toString());
        return set;
    }

    private Set<String> getTransactionsScopes() {
        LinkedHashSet<String> set = new LinkedHashSet<>();
        set.add(IngScope.VIEW_PAYMENT_TRANSACTIONS.toString());
        return set;
    }

}
