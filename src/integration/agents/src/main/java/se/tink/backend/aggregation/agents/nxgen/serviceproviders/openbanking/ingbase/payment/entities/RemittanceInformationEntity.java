package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.payment.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class RemittanceInformationEntity {

    private String referenceType;
    private String referenceIssuer;
    private String reference;
}
