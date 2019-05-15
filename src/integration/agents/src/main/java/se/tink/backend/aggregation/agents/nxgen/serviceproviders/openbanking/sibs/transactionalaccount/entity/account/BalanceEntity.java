package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.transactionalaccount.entity.account;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BalanceEntity {

    private BalanceSingleEntity closingBooked;
    private BalanceSingleEntity authorised;
    private BalanceSingleEntity interimAvailable;

    public BalanceSingleEntity getInterimAvailable() {
        return interimAvailable;
    }
}
