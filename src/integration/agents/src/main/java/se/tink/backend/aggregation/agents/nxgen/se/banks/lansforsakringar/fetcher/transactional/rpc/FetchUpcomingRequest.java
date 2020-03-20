package se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.transactional.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FetchUpcomingRequest {
    private String accountNumber;

    private FetchUpcomingRequest(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    @JsonIgnore
    public static FetchUpcomingRequest of(String accountNumber) {
        return new FetchUpcomingRequest(accountNumber);
    }
}
