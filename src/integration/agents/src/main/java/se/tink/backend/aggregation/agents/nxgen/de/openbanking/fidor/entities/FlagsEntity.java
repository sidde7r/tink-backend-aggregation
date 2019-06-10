package se.tink.backend.aggregation.agents.nxgen.de.openbanking.fidor.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FlagsEntity {
    private boolean onboarding;
    private boolean balance;
    private boolean transactions;

    public FlagsEntity() {}

    public FlagsEntity(boolean onboarding, boolean balance, boolean transactions) {
        this.onboarding = onboarding;
        this.balance = balance;
        this.transactions = transactions;
    }
}
