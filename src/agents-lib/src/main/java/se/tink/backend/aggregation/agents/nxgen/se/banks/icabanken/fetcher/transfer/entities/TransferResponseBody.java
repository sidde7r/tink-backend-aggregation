package se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.transfer.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.authenticator.rpc.BankIdResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransferResponseBody extends BankIdResponse {
    @JsonProperty("ProposedNewDate")
    private String proposedNewDate;

    public String getProposedNewDate() {
        return proposedNewDate;
    }
}
