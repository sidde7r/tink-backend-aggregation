package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.authenticator.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ConsentRequestForTransactionsOver90Days<T> {

    private Boolean recurringIndicator;
    private Boolean combinedServiceIndicator;
    private T access;

    public ConsentRequestForTransactionsOver90Days(
            Boolean recurringIndicator, Boolean combinedServiceIndicator, T access) {
        this.recurringIndicator = recurringIndicator;
        this.combinedServiceIndicator = combinedServiceIndicator;
        this.access = access;
    }
}
