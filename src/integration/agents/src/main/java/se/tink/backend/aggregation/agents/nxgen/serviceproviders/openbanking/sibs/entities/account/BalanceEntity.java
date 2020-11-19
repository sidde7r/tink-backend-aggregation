package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.entities.account;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BalanceEntity {

    private BalanceSingleEntity closingBooked;
    private BalanceSingleEntity authorised;
    private BalanceSingleEntity interimAvailable;

    public BalanceSingleEntity getInterimAvailable() {
        return interimAvailable;
    }

    public BalanceSingleEntity getClosingBooked() {
        return closingBooked;
    }

    public BalanceSingleEntity getAuthorised() {
        return authorised;
    }
}
