package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.fetcher.loan.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LoanAccessEntity {
    private boolean agreementPermission;
    private boolean paymentPermission;
    private boolean accessPermission;
    private boolean transferFromPermission;
    private boolean transferToPermission;
}
