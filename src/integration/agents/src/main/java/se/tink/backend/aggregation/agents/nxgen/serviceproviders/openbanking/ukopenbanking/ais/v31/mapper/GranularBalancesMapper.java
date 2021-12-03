package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.mapper;

import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.AccountBalanceType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.AccountBalanceEntity;
import se.tink.libraries.amount.ExactCurrencyAmount;

public interface GranularBalancesMapper {

    Logger log = LoggerFactory.getLogger(GranularBalancesMapper.class);

    static Map<AccountBalanceType, Pair<ExactCurrencyAmount, Instant>> toGranularBalances(
            Collection<AccountBalanceEntity> balances) {

        Map<AccountBalanceType, Pair<ExactCurrencyAmount, Instant>> granularBalances =
                new HashMap<>();
        try {
            granularBalances =
                    balances.stream()
                            .collect(
                                    Collectors.toMap(
                                            balance ->
                                                    BalanceTypeMapper.toTinkAccountBalanceType(
                                                            balance.getType()),
                                            balance ->
                                                    Pair.of(
                                                            balance.getAmount(),
                                                            balance.getDateTime()),
                                            GranularBalancesMapper
                                                    ::getLatestBalanceWithSnapshotTime));

        } catch (Exception e) {
            log.warn("[GRANULAR BALANCES] Balance type mapping failed", e);
        }

        return granularBalances;
    }

    static Pair<ExactCurrencyAmount, Instant> getLatestBalanceWithSnapshotTime(
            Pair<ExactCurrencyAmount, Instant> currentPair,
            Pair<ExactCurrencyAmount, Instant> nextPair) {
        if (nextPair.getRight().isAfter(currentPair.getRight())) {
            return nextPair;
        }
        return currentPair;
    }
}
