package se.tink.backend.aggregation.agents.nxgen.be.openbanking.ing.fetcher.rpc;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.ing.IngConstants;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.ing.fetcher.entities.BalancesEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.Amount;

@JsonObject
public class FetchBalancesResponse {

    private List<BalancesEntity> balances;

    public Amount getBalance() {

        return Optional.ofNullable(balances).orElse(Collections.emptyList()).stream()
                .min(Comparator.comparing(BalancesEntity::getBalanceMappingPriority))
                .map(BalancesEntity::getAmount)
                .orElseThrow(
                        () ->
                                new IllegalStateException(
                                        IngConstants.ErrorMessages.ACCOUNT_BALANCE_NOT_FOUND));
    }
}
