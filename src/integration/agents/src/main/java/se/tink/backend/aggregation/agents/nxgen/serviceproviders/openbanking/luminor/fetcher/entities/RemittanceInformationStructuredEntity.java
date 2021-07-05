package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.luminor.fetcher.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class RemittanceInformationStructuredEntity {
    String reference;
    String referenceType;
    String referenceIssuer;
}
