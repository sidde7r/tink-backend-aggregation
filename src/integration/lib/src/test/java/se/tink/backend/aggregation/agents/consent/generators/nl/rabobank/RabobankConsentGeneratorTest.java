package se.tink.backend.aggregation.agents.consent.generators.nl.rabobank;

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

public class RabobankConsentGeneratorTest {
    private RabobankConsentGenerator generator;
    private AgentComponentProvider componentProviderMock;
    private ManualAuthenticateRequest requestMock;
    private RefreshScope scope = new RefreshScope();

    @Before
    public void setUp() throws Exception {
        this.componentProviderMock = mock(AgentComponentProvider.class);
        this.requestMock = mock(ManualAuthenticateRequest.class);
        this.scope = new RefreshScope();
        this.requestMock.setRefreshScope(scope);
    }

    @Test
    public void shouldMapAccountItemsCorrectly() {
        // given
        scope.setRefreshableItemsIn(Sets.newHashSet(RefreshableItem.CHECKING_ACCOUNTS));
        given(requestMock.getRefreshScope()).willReturn(scope);
        given(componentProviderMock.getCredentialsRequest()).willReturn(requestMock);
        generator = RabobankConsentGenerator.of(componentProviderMock, getRabobankScopes());

        // when
        String scopes = generator.generate();

        // then
        assertThat(scopes).isEqualTo(String.join(" ", getAccountsScopes()));
    }

    @Test
    public void shouldMapAllItemsCorrectly() {
        // given
        scope.setRefreshableItemsIn(
                Sets.newHashSet(
                        RefreshableItem.CHECKING_ACCOUNTS, RefreshableItem.CHECKING_TRANSACTIONS));
        given(requestMock.getRefreshScope()).willReturn(scope);
        given(componentProviderMock.getCredentialsRequest()).willReturn(requestMock);
        generator = RabobankConsentGenerator.of(componentProviderMock, getRabobankScopes());

        // when
        String scopes = generator.generate();

        // then
        assertThat(scopes).isEqualTo(String.join(" ", getAllScopes()));
    }

    @Test
    public void shouldMapTransactionsItemsCorrectly() {
        // given
        scope.setRefreshableItemsIn(Sets.newHashSet(RefreshableItem.CHECKING_TRANSACTIONS));
        given(requestMock.getRefreshScope()).willReturn(scope);
        given(componentProviderMock.getCredentialsRequest()).willReturn(requestMock);
        generator = RabobankConsentGenerator.of(componentProviderMock, getRabobankScopes());

        // when
        String scopes = generator.generate();

        // then
        assertThat(scopes).isEqualTo(String.join(" ", getTransactionsScopes()));
    }

    private static Set<RabobankScope> getRabobankScopes() {
        return Sets.newHashSet(
                RabobankScope.READ_BALANCES,
                RabobankScope.READ_TRANSACTIONS_90DAYS,
                RabobankScope.READ_TRANSACTIONS_HISTORY);
    }

    private Set<String> getAccountsScopes() {
        LinkedHashSet<String> set = new LinkedHashSet<>();
        set.add(RabobankScope.READ_BALANCES.toString());
        return set;
    }

    private Set<String> getAllScopes() {
        LinkedHashSet<String> set = new LinkedHashSet<>();
        set.add(RabobankScope.READ_BALANCES.toString());
        set.add(RabobankScope.READ_TRANSACTIONS_90DAYS.toString());
        set.add(RabobankScope.READ_TRANSACTIONS_HISTORY.toString());
        return set;
    }

    private Set<String> getTransactionsScopes() {
        LinkedHashSet<String> set = new LinkedHashSet<>();
        set.add(RabobankScope.READ_TRANSACTIONS_90DAYS.toString());
        set.add(RabobankScope.READ_TRANSACTIONS_HISTORY.toString());
        return set;
    }
}
