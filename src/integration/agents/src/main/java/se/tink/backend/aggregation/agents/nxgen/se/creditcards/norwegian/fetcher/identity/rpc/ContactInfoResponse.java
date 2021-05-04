package se.tink.backend.aggregation.agents.nxgen.se.creditcards.norwegian.fetcher.identity.rpc;

import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class ContactInfoResponse {
    private String firstName;
    private String lastName;
}
