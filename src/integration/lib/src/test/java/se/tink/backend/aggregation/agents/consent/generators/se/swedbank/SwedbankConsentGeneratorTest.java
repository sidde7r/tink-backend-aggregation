package se.tink.backend.aggregation.agents.consent.generators.se.swedbank;

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

public class SwedbankConsentGeneratorTest {
    private SwedbankConsentGenerator generator;
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
        scope.setRefreshableItemsIn(
                Sets.newHashSet(
                        RefreshableItem.CHECKING_ACCOUNTS, RefreshableItem.SAVING_ACCOUNTS));
        given(requestMock.getRefreshScope()).willReturn(scope);
        given(componentProviderMock.getCredentialsRequest()).willReturn(requestMock);
        generator = SwedbankConsentGenerator.of(requestMock, getSwedbankScopes());

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
                        RefreshableItem.CHECKING_ACCOUNTS,
                        RefreshableItem.SAVING_ACCOUNTS,
                        RefreshableItem.CHECKING_TRANSACTIONS,
                        RefreshableItem.SAVING_TRANSACTIONS));
        given(requestMock.getRefreshScope()).willReturn(scope);
        generator = SwedbankConsentGenerator.of(requestMock, getSwedbankScopes());

        // when
        String scopes = generator.generate();

        // then
        assertThat(scopes).isEqualTo(String.join(" ", getAllScopes()));
    }

    public static Set<SwedbankScope> getSwedbankScopes() {
        return Sets.newHashSet(
                SwedbankScope.PSD2,
                SwedbankScope.READ_ACCOUNTS_BALANCES,
                SwedbankScope.READ_TRANSACTIONS_HISTORY,
                SwedbankScope.READ_TRANSACTIONS_HISTORY_OVER90);
    }

    private Set<String> getAccountsScopes() {
        LinkedHashSet<String> set = new LinkedHashSet<>();
        set.add(SwedbankScope.PSD2.toString());
        set.add(SwedbankScope.READ_ACCOUNTS_BALANCES.toString());
        return set;
    }

    private Set<String> getAllScopes() {
        LinkedHashSet<String> set = new LinkedHashSet<>();
        set.add(SwedbankScope.PSD2.toString());
        set.add(SwedbankScope.READ_ACCOUNTS_BALANCES.toString());
        set.add(SwedbankScope.READ_TRANSACTIONS_HISTORY.toString());
        set.add(SwedbankScope.READ_TRANSACTIONS_HISTORY_OVER90.toString());
        return set;
    }
}
