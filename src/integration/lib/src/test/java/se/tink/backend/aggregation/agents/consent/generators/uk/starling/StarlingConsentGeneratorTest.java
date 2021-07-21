package se.tink.backend.aggregation.agents.consent.generators.uk.starling;

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

public class StarlingConsentGeneratorTest {

    private StarlingConsentGenerator generator;
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
        generator = StarlingConsentGenerator.of(componentProviderMock, getAllScopes());

        // when
        Set<String> scopes = generator.generate();

        // then
        assertThat(scopes)
                .containsExactlyInAnyOrder(
                        StarlingScope.ACCOUNT_READ.toString(),
                        StarlingScope.BALANCE_READ.toString(),
                        StarlingScope.ACCOUNT_HOLDER_TYPE_READ.toString(),
                        StarlingScope.ACCOUNT_HOLDER_NAME_READ.toString(),
                        StarlingScope.ACCOUNT_IDENTIFIER_READ.toString());
    }

    @Test
    public void shouldMapAllAccountItemsCorrectly() {
        // given
        scope.setRefreshableItemsIn(
                Sets.newHashSet(
                        RefreshableItem.CHECKING_ACCOUNTS,
                        RefreshableItem.SAVING_ACCOUNTS,
                        RefreshableItem.CREDITCARD_ACCOUNTS,
                        RefreshableItem.LOAN_ACCOUNTS,
                        RefreshableItem.INVESTMENT_ACCOUNTS));
        given(requestMock.getRefreshScope()).willReturn(scope);
        given(componentProviderMock.getCredentialsRequest()).willReturn(requestMock);
        generator = StarlingConsentGenerator.of(componentProviderMock, getAllScopes());

        // when
        Set<String> scopes = generator.generate();

        // then
        assertThat(scopes)
                .containsExactlyInAnyOrder(
                        StarlingScope.ACCOUNT_READ.toString(),
                        StarlingScope.BALANCE_READ.toString(),
                        StarlingScope.ACCOUNT_HOLDER_TYPE_READ.toString(),
                        StarlingScope.ACCOUNT_HOLDER_NAME_READ.toString(),
                        StarlingScope.ACCOUNT_IDENTIFIER_READ.toString());
    }

    @Test
    public void shouldMapTransactionsItemsCorrectly() {
        // given
        scope.setRefreshableItemsIn(
                Sets.newHashSet(
                        RefreshableItem.CHECKING_TRANSACTIONS,
                        RefreshableItem.SAVING_TRANSACTIONS));
        given(requestMock.getRefreshScope()).willReturn(scope);
        given(componentProviderMock.getCredentialsRequest()).willReturn(requestMock);
        generator = StarlingConsentGenerator.of(componentProviderMock, getAllScopes());

        // when
        Set<String> scopes = generator.generate();

        // then
        assertThat(scopes).containsExactlyInAnyOrder(StarlingScope.TRANSACTION_READ.toString());
    }

    @Test
    public void shouldMapAllTransactionsItemsCorrectly() {
        // given
        scope.setRefreshableItemsIn(
                Sets.newHashSet(
                        RefreshableItem.CHECKING_TRANSACTIONS,
                        RefreshableItem.SAVING_TRANSACTIONS,
                        RefreshableItem.CREDITCARD_TRANSACTIONS,
                        RefreshableItem.INVESTMENT_TRANSACTIONS,
                        RefreshableItem.LOAN_TRANSACTIONS));
        given(requestMock.getRefreshScope()).willReturn(scope);
        given(componentProviderMock.getCredentialsRequest()).willReturn(requestMock);
        generator = StarlingConsentGenerator.of(componentProviderMock, getAllScopes());

        // when
        Set<String> scopes = generator.generate();

        // then
        assertThat(scopes).containsExactlyInAnyOrder(StarlingScope.TRANSACTION_READ.toString());
    }

    @Test
    public void shouldMapAccountAndTransactionsItemsCorrectly() {
        // given
        scope.setRefreshableItemsIn(
                Sets.newHashSet(
                        RefreshableItem.CHECKING_ACCOUNTS,
                        RefreshableItem.SAVING_ACCOUNTS,
                        RefreshableItem.CHECKING_TRANSACTIONS,
                        RefreshableItem.SAVING_TRANSACTIONS));
        given(requestMock.getRefreshScope()).willReturn(scope);
        given(componentProviderMock.getCredentialsRequest()).willReturn(requestMock);
        generator = StarlingConsentGenerator.of(componentProviderMock, getAllScopes());

        // when
        Set<String> scopes = generator.generate();

        // then
        assertThat(scopes)
                .containsExactlyInAnyOrder(
                        StarlingScope.ACCOUNT_READ.toString(),
                        StarlingScope.BALANCE_READ.toString(),
                        StarlingScope.ACCOUNT_HOLDER_TYPE_READ.toString(),
                        StarlingScope.ACCOUNT_HOLDER_NAME_READ.toString(),
                        StarlingScope.ACCOUNT_IDENTIFIER_READ.toString(),
                        StarlingScope.TRANSACTION_READ.toString());
    }

    public ImmutableSet<StarlingScope> getAllScopes() {
        Set<StarlingScope> set = new HashSet<>();
        set.add(StarlingScope.ACCOUNT_READ);
        set.add(StarlingScope.BALANCE_READ);
        set.add(StarlingScope.ACCOUNT_HOLDER_TYPE_READ);
        set.add(StarlingScope.ACCOUNT_HOLDER_NAME_READ);
        set.add(StarlingScope.ACCOUNT_IDENTIFIER_READ);
        set.add(StarlingScope.TRANSACTION_READ);

        return ImmutableSet.<StarlingScope>builder().addAll(set).build();
    }
}
