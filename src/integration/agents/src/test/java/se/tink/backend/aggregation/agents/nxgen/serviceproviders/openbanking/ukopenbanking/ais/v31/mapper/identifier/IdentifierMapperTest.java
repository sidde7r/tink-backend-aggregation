package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.mapper.identifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.api.UkOpenBankingApiDefinitions.ExternalAccountIdentification4Code.BBAN;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.api.UkOpenBankingApiDefinitions.ExternalAccountIdentification4Code.IBAN;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.api.UkOpenBankingApiDefinitions.ExternalAccountIdentification4Code.NWB_CURRENCY_ACCOUNT;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.api.UkOpenBankingApiDefinitions.ExternalAccountIdentification4Code.RBS_CURRENCY_ACCOUNT;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.api.UkOpenBankingApiDefinitions.ExternalAccountIdentification4Code.SAVINGS_ROLL_NUMBER;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.api.UkOpenBankingApiDefinitions.ExternalAccountIdentification4Code.SORT_CODE_ACCOUNT_NUMBER;

import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.api.UkOpenBankingApiDefinitions.ExternalAccountIdentification4Code;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.api.UkOpenBankingApiDefinitions.UkObBalanceType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.AccountIdentifierEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.fixtures.IdentifierFixtures;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.mapper.PrioritizedValueExtractor;

public class IdentifierMapperTest {

    private DefaultIdentifierMapper identifierMapper;
    private PrioritizedValueExtractor valueExtractor;

    @Before
    public void setUp() {
        valueExtractor = mock(PrioritizedValueExtractor.class);
        identifierMapper = new DefaultIdentifierMapper(valueExtractor);
    }

    @Test
    public void transactionalAccountId_isPickedAccordingToPriority() {
        // given
        AccountIdentifierEntity sortCodeIdentifier = IdentifierFixtures.sortCodeIdentifier();

        // when
        ArgumentCaptor<List<UkObBalanceType>> argument = ArgumentCaptor.forClass(List.class);
        when(valueExtractor.pickByValuePriority(anyList(), any(), argument.capture()))
                .thenReturn(Optional.of(sortCodeIdentifier));
        AccountIdentifierEntity returnedId =
                identifierMapper.getTransactionalAccountPrimaryIdentifier(mock(List.class));

        // then
        ImmutableList<ExternalAccountIdentification4Code> expectedIdPriority =
                ImmutableList.of(
                        SORT_CODE_ACCOUNT_NUMBER,
                        IBAN,
                        BBAN,
                        SAVINGS_ROLL_NUMBER,
                        NWB_CURRENCY_ACCOUNT,
                        RBS_CURRENCY_ACCOUNT);
        assertThat(argument.getValue()).asList().isEqualTo(expectedIdPriority);
        assertThat(returnedId).isEqualTo(sortCodeIdentifier);
    }

    @Test
    public void whenNoTransactionalAccountIdIsFound_exceptionIsThrown() {
        // when
        when(valueExtractor.pickByValuePriority(anyList(), any(), anyList()))
                .thenReturn(Optional.empty());

        Throwable throwable =
                catchThrowable(
                        () ->
                                identifierMapper.getTransactionalAccountPrimaryIdentifier(
                                        mock(List.class)));

        // then
        assertThat(throwable).isInstanceOf(NoSuchElementException.class);
    }

    @Test
    public void shouldGetSortCodeIdentifierOfCreditCard() {
        // given
        final List<ExternalAccountIdentification4Code> expectedPriorityList =
                ImmutableList.of(
                        ExternalAccountIdentification4Code.PAN,
                        ExternalAccountIdentification4Code.SORT_CODE_ACCOUNT_NUMBER);
        AccountIdentifierEntity sortCodeIdentifier = IdentifierFixtures.sortCodeIdentifier();
        // when
        ArgumentCaptor<List<UkObBalanceType>> argument = ArgumentCaptor.forClass(List.class);
        when(valueExtractor.pickByValuePriority(anyList(), any(), argument.capture()))
                .thenReturn(Optional.of(sortCodeIdentifier));

        AccountIdentifierEntity sortCodeResult =
                identifierMapper.getCreditCardIdentifier(Collections.emptyList());

        // then
        assertThat(argument.getValue()).asList().isEqualTo(expectedPriorityList);
        assertThat(sortCodeResult).isEqualTo(sortCodeIdentifier);
    }

    @Test
    public void shouldGetEmptyIdentifierOfCreditCard() {
        // when
        ArgumentCaptor<List<UkObBalanceType>> argument = ArgumentCaptor.forClass(List.class);
        when(valueExtractor.pickByValuePriority(anyList(), any(), argument.capture()))
                .thenReturn(Optional.empty());

        Throwable throwable =
                catchThrowable(
                        () -> identifierMapper.getCreditCardIdentifier(Collections.emptyList()));

        // then
        assertThat(throwable)
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("Missing allowed credit card account identifiers like PAN or SortCode");
    }

    @Test
    public void shouldNotThrowExceptionWhenTransactionalAccountHasCurrencyAccountIdentifier() {
        // given
        PrioritizedValueExtractor valueExtractor = new PrioritizedValueExtractor();
        identifierMapper = new DefaultIdentifierMapper(valueExtractor);
        Collection<AccountIdentifierEntity> identifiers =
                ImmutableList.of(IdentifierFixtures.currencyAccountIdentifierForNatWest());

        // when
        Throwable throwable =
                catchThrowable(
                        () ->
                                identifierMapper.getTransactionalAccountPrimaryIdentifier(
                                        identifiers));

        // then
        assertThat(throwable).doesNotThrowAnyException();
    }

    @Test
    public void shouldReturnNatwestCurrencyAccountIdentifier() {
        // given
        AccountIdentifierEntity accountIdentifierEntity =
                IdentifierFixtures.currencyAccountIdentifierForNatWest();
        // when
        AccountIdentifier accountIdentifier =
                identifierMapper.mapIdentifier(accountIdentifierEntity);
        // then
        assertThat(accountIdentifier.getIdentifier())
                .isEqualTo("UK.NWB.CurrencyAccount/111/11/11111111");
    }

    @Test
    public void shouldReturnRbsCurrencyAccountIdentifier() {
        // given
        AccountIdentifierEntity accountIdentifierEntity =
                IdentifierFixtures.currencyAccountIdentifierForRbs();
        // when
        AccountIdentifier accountIdentifier =
                identifierMapper.mapIdentifier(accountIdentifierEntity);
        // then
        assertThat(accountIdentifier.getIdentifier())
                .isEqualTo("UK.RBS.CurrencyAccount/LULULUL-USDC");
    }
}
