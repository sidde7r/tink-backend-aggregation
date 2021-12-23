package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.agents.rpc.AccountBalanceType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.api.UkOpenBankingApiDefinitions.UkObBalanceType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.calculator.DefaultBalancePreCalculator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.AccountBalanceEntity;
import se.tink.libraries.amount.ExactCurrencyAmount;

@RunWith(MockitoJUnitRunner.class)
public class GranularBalancesMapperTest {

    @Mock private AccountBalanceEntity forwardAvailable1;
    @Mock private AccountBalanceEntity forwardAvailable2;
    @Mock private AccountBalanceEntity forwardAvailable3;

    private final GranularBalancesMapper granularBalancesMapper =
            new GranularBalancesMapper(new DefaultBalancePreCalculator());

    @Test
    public void shouldMapLatestBalanceIfDuplicatedBalanceTypesFound() {
        given(forwardAvailable1.getType()).willReturn(UkObBalanceType.FORWARD_AVAILABLE);
        given(forwardAvailable1.getAmount()).willReturn(ExactCurrencyAmount.of(10.00, "GBP"));
        given(forwardAvailable1.getDateTime())
                .willReturn(
                        LocalDate.of(2000, 1, 1)
                                .atStartOfDay()
                                .atOffset(ZoneOffset.UTC)
                                .toInstant());

        given(forwardAvailable2.getType()).willReturn(UkObBalanceType.FORWARD_AVAILABLE);
        given(forwardAvailable2.getAmount()).willReturn(ExactCurrencyAmount.of(20.00, "GBP"));
        given(forwardAvailable2.getDateTime())
                .willReturn(
                        LocalDate.of(2000, 1, 2)
                                .atStartOfDay()
                                .atOffset(ZoneOffset.UTC)
                                .toInstant());

        given(forwardAvailable3.getType()).willReturn(UkObBalanceType.FORWARD_AVAILABLE);
        given(forwardAvailable3.getAmount()).willReturn(ExactCurrencyAmount.of(30.00, "GBP"));
        given(forwardAvailable3.getDateTime())
                .willReturn(
                        LocalDate.of(2000, 1, 3)
                                .atStartOfDay()
                                .atOffset(ZoneOffset.UTC)
                                .toInstant());

        Collection<AccountBalanceEntity> givenBalances =
                Arrays.asList(forwardAvailable1, forwardAvailable3, forwardAvailable2);

        Map<AccountBalanceType, Pair<ExactCurrencyAmount, Instant>> granularBalances =
                granularBalancesMapper.toGranularBalances(givenBalances);

        assertThat(granularBalances.get(AccountBalanceType.FORWARD_AVAILABLE))
                .isNotNull()
                .isEqualTo(
                        Pair.of(
                                ExactCurrencyAmount.of(30.00, "GBP"),
                                LocalDate.of(2000, 1, 3)
                                        .atStartOfDay()
                                        .atOffset(ZoneOffset.UTC)
                                        .toInstant()));
    }
}
