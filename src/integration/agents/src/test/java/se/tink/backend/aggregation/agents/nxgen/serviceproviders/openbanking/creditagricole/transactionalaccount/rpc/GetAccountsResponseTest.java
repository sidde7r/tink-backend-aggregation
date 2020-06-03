package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.transactionalaccount.rpc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.transactionalaccount.entities.LinksEntity;

public class GetAccountsResponseTest {

    private LinksEntity linksEntityMock;
    private AccountEntity accountEntityMock;

    @Before
    public void setUp() {
        linksEntityMock = mock(LinksEntity.class);
        accountEntityMock = mock(AccountEntity.class);

        when(accountEntityMock.areConsentsNecessary()).thenReturn(Boolean.FALSE);
    }

    @Test
    public void shouldReturnNoConsentsAreNecessary() {
        // given
        when(linksEntityMock.hasBalances()).thenReturn(Boolean.TRUE);
        when(linksEntityMock.hasEndUserIdentity()).thenReturn(Boolean.TRUE);
        when(linksEntityMock.hasBeneficiaries()).thenReturn(Boolean.TRUE);

        final GetAccountsResponse getAccountsResponse = new GetAccountsResponse();
        getAccountsResponse.setLinks(linksEntityMock);
        getAccountsResponse.setAccounts(Collections.singletonList(accountEntityMock));

        // when
        final boolean response = getAccountsResponse.areConsentsNecessary();

        // then
        assertThat(response).isFalse();
    }

    @Test
    public void shouldReturnNoConsentsAreNecessaryWhenNoAccounts() {
        // given
        when(linksEntityMock.hasBalances()).thenReturn(Boolean.TRUE);
        when(linksEntityMock.hasEndUserIdentity()).thenReturn(Boolean.TRUE);
        when(linksEntityMock.hasBeneficiaries()).thenReturn(Boolean.TRUE);

        final GetAccountsResponse getAccountsResponse = new GetAccountsResponse();
        getAccountsResponse.setLinks(linksEntityMock);

        // when
        final boolean response = getAccountsResponse.areConsentsNecessary();

        // then
        assertThat(response).isFalse();
    }

    @Test
    public void shouldReturnConsentsAreNecessaryForNullBeneficiaries() {
        // given
        when(linksEntityMock.hasBalances()).thenReturn(Boolean.TRUE);
        when(linksEntityMock.hasEndUserIdentity()).thenReturn(Boolean.TRUE);
        when(linksEntityMock.hasBeneficiaries()).thenReturn(Boolean.FALSE);

        final GetAccountsResponse getAccountsResponse = new GetAccountsResponse();
        getAccountsResponse.setLinks(linksEntityMock);
        getAccountsResponse.setAccounts(Collections.singletonList(accountEntityMock));

        // when
        final boolean response = getAccountsResponse.areConsentsNecessary();

        // then
        assertThat(response).isTrue();
    }
}
