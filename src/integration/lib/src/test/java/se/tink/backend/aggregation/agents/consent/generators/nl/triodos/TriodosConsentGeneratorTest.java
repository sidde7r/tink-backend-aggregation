package se.tink.backend.aggregation.agents.consent.generators.nl.triodos;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import com.google.common.collect.Sets;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.authenticator.entity.AccessEntity;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.libraries.credentials.service.ManualAuthenticateRequest;
import se.tink.libraries.credentials.service.RefreshScope;
import se.tink.libraries.credentials.service.RefreshableItem;

public class TriodosConsentGeneratorTest {
    private TriodosConsentGenerator generator;
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
    public void shouldMapAccountItemsCorrectly() throws NoSuchFieldException {
        // given
        scope.setRefreshableItemsIn(Sets.newHashSet(RefreshableItem.CHECKING_ACCOUNTS));
        given(requestMock.getRefreshScope()).willReturn(scope);
        given(componentProviderMock.getCredentialsRequest()).willReturn(requestMock);
        generator = TriodosConsentGenerator.of(componentProviderMock, getTriodosScopes());

        // when
        AccessEntity result = generator.generate();

        // then
        assertThat(result.getAccounts()).isEmpty();
        assertThat(result.getBalances()).isEmpty();
        assertThat(result.getTransactions()).isNull();
    }

    @Test
    public void shouldMapAllItemsCorrectly() {
        // given
        scope.setRefreshableItemsIn(
                Sets.newHashSet(
                        RefreshableItem.CHECKING_ACCOUNTS, RefreshableItem.CHECKING_TRANSACTIONS));
        given(requestMock.getRefreshScope()).willReturn(scope);
        given(componentProviderMock.getCredentialsRequest()).willReturn(requestMock);
        generator = TriodosConsentGenerator.of(componentProviderMock, getTriodosScopes());

        // when
        AccessEntity result = generator.generate();

        // then
        assertThat(result.getAccounts()).isEmpty();
        assertThat(result.getBalances()).isEmpty();
        assertThat(result.getTransactions()).isEmpty();
    }

    public static Set<TriodosScope> getTriodosScopes() {
        return Sets.newHashSet(
                TriodosScope.ACCOUNTS, TriodosScope.BALANCES, TriodosScope.TRANSACTIONS);
    }
}
