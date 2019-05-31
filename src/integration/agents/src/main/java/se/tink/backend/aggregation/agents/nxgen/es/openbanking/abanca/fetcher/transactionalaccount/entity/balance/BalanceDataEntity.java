package se.tink.backend.aggregation.agents.nxgen.es.openbanking.abanca.fetcher.transactionalaccount.entity.balance;

import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.abanca.AbancaConstants;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BalanceDataEntity {

    private String type;
    private String id;
    private BalanceAttributesEntity attributes;
    private BalanceLinkEntity links;

    public BalanceAttributesEntity getAttributes() {
        return Optional.ofNullable(attributes)
                .orElseThrow(
                        () ->
                                new IllegalStateException(
                                        AbancaConstants.ErrorMessages.INVALID_BALANCE_RESPONSE));
    }
}
