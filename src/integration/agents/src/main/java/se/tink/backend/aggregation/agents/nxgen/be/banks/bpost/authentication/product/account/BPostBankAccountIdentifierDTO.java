package se.tink.backend.aggregation.agents.nxgen.be.banks.bpost.authentication.product.account;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BPostBankAccountIdentifierDTO {

    String scheme;
    String id;
}
