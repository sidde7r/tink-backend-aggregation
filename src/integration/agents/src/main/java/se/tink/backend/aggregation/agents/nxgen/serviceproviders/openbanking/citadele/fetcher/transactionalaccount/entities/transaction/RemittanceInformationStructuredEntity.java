package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.citadele.fetcher.transactionalaccount.entities.transaction;

import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class RemittanceInformationStructuredEntity {
    private String reference;
    private String referenceType;
    private String referenceIssuer;
}
