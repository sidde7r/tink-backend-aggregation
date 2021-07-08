package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.fetcher.entities;

import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class RemittanceInformationStructuredEntity {

    private String referenceType;
    private String referenceIssuer;
    private String reference;
}
