package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.luminor.fetcher.entities;

import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class RemittanceInformationStructuredEntity {
    @Getter String reference;
    @Getter String referenceType;
    String referenceIssuer;
}
