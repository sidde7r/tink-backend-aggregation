package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.danskebank;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.api.UkOpenBankingApiDefinitions.AccountBalanceType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.AccountIdentifierEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.fixtures.IdentifierFixtures;
import se.tink.libraries.mapper.PrioritizedValueExtractor;

public class DanskebankIdentifierMapperTest {

    protected PrioritizedValueExtractor valueExtractor;
    private DanskeBankIdentifierMapper identifierMapper;

    @Before
    public void setUp() {
        valueExtractor = mock(PrioritizedValueExtractor.class);
        identifierMapper = new DanskeBankIdentifierMapper(valueExtractor);
    }

    @Test
    public void shouldGetCreditCardSortCodeIdentifier() {
        // given
        AccountIdentifierEntity sortCodeIdentifier = IdentifierFixtures.sortCodeIdentifier();
        // when
        ArgumentCaptor<List<AccountBalanceType>> argument = ArgumentCaptor.forClass(List.class);
        when(valueExtractor.pickByValuePriority(anyList(), any(), argument.capture()))
                .thenReturn(Optional.of(sortCodeIdentifier));

        AccountIdentifierEntity sortCodeResult =
                identifierMapper.getCreditCardIdentifier(mock(List.class));

        // then
        assertThat(sortCodeResult).isEqualTo(sortCodeIdentifier);
    }

    @Test
    public void shouldGetCreditCardIBANIdentifier() {
        // given
        AccountIdentifierEntity ibanIdentifier = IdentifierFixtures.ibanIdentifier();
        // when
        ArgumentCaptor<List<AccountBalanceType>> argument = ArgumentCaptor.forClass(List.class);
        when(valueExtractor.pickByValuePriority(anyList(), any(), argument.capture()))
                .thenReturn(Optional.of(ibanIdentifier));

        AccountIdentifierEntity sortCodeResult =
                identifierMapper.getCreditCardIdentifier(mock(List.class));

        // then
        assertThat(sortCodeResult).isEqualTo(ibanIdentifier);
    }

    @Test
    public void shouldGetCreditCardEmptyIdentifier() {
        // when
        ArgumentCaptor<List<AccountBalanceType>> argument = ArgumentCaptor.forClass(List.class);
        when(valueExtractor.pickByValuePriority(anyList(), any(), argument.capture()))
                .thenReturn(Optional.empty());

        Throwable throwable =
                catchThrowable(() -> identifierMapper.getCreditCardIdentifier(mock(List.class)));

        // then
        assertThat(throwable).isInstanceOf(NoSuchElementException.class);
    }
}
