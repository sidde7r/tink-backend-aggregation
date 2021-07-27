package se.tink.backend.aggregation.agents.consent.generators.nl.ics;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import java.util.HashSet;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.libraries.credentials.service.ManualAuthenticateRequest;
import se.tink.libraries.credentials.service.RefreshScope;
import se.tink.libraries.credentials.service.RefreshableItem;

public class IcsConsentGeneratorTest {

    private IcsConsentGenerator generator;
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
        scope.setRefreshableItemsIn(Sets.newHashSet(RefreshableItem.CREDITCARD_ACCOUNTS));
        given(requestMock.getRefreshScope()).willReturn(scope);
        given(componentProviderMock.getCredentialsRequest()).willReturn(requestMock);
        generator = IcsConsentGenerator.of(componentProviderMock, getIcsScopes());

        // when
        Set<String> scopes = generator.generate();

        // then
        assertThat(scopes)
                .containsExactlyInAnyOrder(
                        IcsScope.READ_ACCOUNT_BASIC.toString(),
                        IcsScope.READ_ACCOUNTS_DETAIL.toString(),
                        IcsScope.READ_BALANCES.toString());
    }

    @Test
    public void shouldMapAllItemsCorrectly() {
        // given
        scope.setRefreshableItemsIn(
                Sets.newHashSet(
                        RefreshableItem.CREDITCARD_ACCOUNTS,
                        RefreshableItem.CREDITCARD_TRANSACTIONS));
        given(requestMock.getRefreshScope()).willReturn(scope);
        given(componentProviderMock.getCredentialsRequest()).willReturn(requestMock);
        generator = IcsConsentGenerator.of(componentProviderMock, getIcsScopes());

        // when
        Set<String> scopes = generator.generate();

        // then
        assertThat(scopes)
                .containsExactlyInAnyOrder(
                        IcsScope.READ_ACCOUNT_BASIC.toString(),
                        IcsScope.READ_ACCOUNTS_DETAIL.toString(),
                        IcsScope.READ_BALANCES.toString(),
                        IcsScope.READ_TRANSACTION_BASIC.toString(),
                        IcsScope.READ_TRANSACTIONS_CREDITS.toString(),
                        IcsScope.READ_TRANSACTIONS_DEBITS.toString(),
                        IcsScope.READ_TRANSACTIONS_DETAIL.toString());
    }

    @Test
    public void shouldMapTransactionsItemsCorrectly() {
        // given
        scope.setRefreshableItemsIn(Sets.newHashSet(RefreshableItem.CREDITCARD_TRANSACTIONS));
        given(requestMock.getRefreshScope()).willReturn(scope);
        given(componentProviderMock.getCredentialsRequest()).willReturn(requestMock);
        generator = IcsConsentGenerator.of(componentProviderMock, getIcsScopes());

        // when
        Set<String> scopes = generator.generate();

        // then
        assertThat(scopes)
                .containsExactlyInAnyOrder(
                        IcsScope.READ_TRANSACTION_BASIC.toString(),
                        IcsScope.READ_TRANSACTIONS_CREDITS.toString(),
                        IcsScope.READ_TRANSACTIONS_DEBITS.toString(),
                        IcsScope.READ_TRANSACTIONS_DETAIL.toString());
    }

    private static ImmutableSet<IcsScope> getIcsScopes() {
        Set<IcsScope> set = new HashSet<>();
        set.add(IcsScope.READ_ACCOUNT_BASIC);
        set.add(IcsScope.READ_ACCOUNTS_DETAIL);
        set.add(IcsScope.READ_BALANCES);
        set.add(IcsScope.READ_TRANSACTION_BASIC);
        set.add(IcsScope.READ_TRANSACTIONS_CREDITS);
        set.add(IcsScope.READ_TRANSACTIONS_DEBITS);
        set.add(IcsScope.READ_TRANSACTIONS_DETAIL);
        return ImmutableSet.<IcsScope>builder().addAll(set).build();
    }
}
