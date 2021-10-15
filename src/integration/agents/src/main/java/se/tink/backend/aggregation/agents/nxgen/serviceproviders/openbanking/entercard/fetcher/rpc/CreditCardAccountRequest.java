package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.entercard.fetcher.rpc;

import lombok.AllArgsConstructor;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
@AllArgsConstructor
public class CreditCardAccountRequest {

    private String ssn;
}
