package se.tink.backend.aggregation.agents.nxgen.es.openbanking.abanca.fetcher.transactionalaccount.entity.balance;

import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.abanca.AbancaConstants;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.abanca.fetcher.transactionalaccount.entity.common.AmountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BalanceAttributesEntity {

    private AmountEntity availableBalance;

    public AmountEntity getAvailableBalance() {
        return Optional.ofNullable(availableBalance)
                .orElseThrow(
                        () ->
                                new IllegalStateException(
                                        AbancaConstants.ErrorMessages.INVALID_BALANCE_RESPONSE));
    }
}
