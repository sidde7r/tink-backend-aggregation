package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.revolut;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.revolut.common.RevolutTransactionalAccountMapper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.revolut.common.RevolutTransactionalAccountMapperDecorator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.fixtures.TransactionalAccountFixtures;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class RevolutTransactionalAccountMapperDecoratorTest {

    private RevolutTransactionalAccountMapperDecorator mapperDecorator;
    private RevolutTransactionalAccountMapper transactionalAccountMapperMock;

    @Before
    public void setUp() {
        transactionalAccountMapperMock = mock(RevolutTransactionalAccountMapper.class);
        mapperDecorator =
                new RevolutTransactionalAccountMapperDecorator(transactionalAccountMapperMock);
    }

    @Test
    public void shouldNotMapAccountsWithEmptyListOfIdentifiers() {
        // given
        AccountEntity incorrectAccount = TransactionalAccountFixtures.savingsAccountWithoutId();

        // when
        Optional<TransactionalAccount> transactionalAccount =
                mapperDecorator.map(incorrectAccount, mock(List.class), mock(List.class));

        assertThat(transactionalAccount).isEmpty();
        verifyZeroInteractions(transactionalAccountMapperMock);
    }

    @Test
    public void shouldNotMapAccountsWithNullListOfIdentifiers() {
        // given
        AccountEntity incorrectAccount = TransactionalAccountFixtures.currentAccount();
        incorrectAccount.setIdentifiers(null);

        // when
        Optional<TransactionalAccount> transactionalAccount =
                mapperDecorator.map(incorrectAccount, mock(List.class), mock(List.class));

        assertThat(transactionalAccount).isEmpty();
        verifyZeroInteractions(transactionalAccountMapperMock);
    }

    @Test
    public void ifMappingIsPossibleShouldUseOriginalMapperToMapAccounts() {
        // given
        AccountEntity accountEntity = TransactionalAccountFixtures.savingsAccount();
        Optional<TransactionalAccount> mapperResult = Optional.of(mock(TransactionalAccount.class));

        // when
        when(transactionalAccountMapperMock.map(any(), any(), any())).thenReturn(mapperResult);

        Optional<TransactionalAccount> decoratorResult =
                mapperDecorator.map(accountEntity, mock(List.class), mock(List.class));

        assertThat(decoratorResult).isEqualTo(mapperResult);
    }
}
