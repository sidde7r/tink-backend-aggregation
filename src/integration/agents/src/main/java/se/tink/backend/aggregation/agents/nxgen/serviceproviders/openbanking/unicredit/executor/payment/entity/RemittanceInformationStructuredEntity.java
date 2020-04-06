package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.executor.payment.entity;

import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.payment.rpc.Reference;

@JsonObject
public class RemittanceInformationStructuredEntity {

    private String reference;
    private String referenceType;
    private String referenceIssuer;

    public RemittanceInformationStructuredEntity(
            String reference, String referenceType, String referenceIssuer) {
        this.reference = reference;
        this.referenceType = referenceType;
        this.referenceIssuer = referenceIssuer;
    }

    // For Jackson
    public RemittanceInformationStructuredEntity() {}

    public Reference toTinkReference() {
        return new Reference(reference, referenceType);
    }
}
