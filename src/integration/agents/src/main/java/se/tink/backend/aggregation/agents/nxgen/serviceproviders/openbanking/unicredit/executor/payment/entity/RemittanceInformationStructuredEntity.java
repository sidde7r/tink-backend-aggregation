package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.executor.payment.entity;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class RemittanceInformationStructuredEntity {

    private String reference;
    private String referenceType;

    public RemittanceInformationStructuredEntity(String reference, String referenceType) {
        this.reference = reference;
        this.referenceType = referenceType;
    }

    public RemittanceInformationStructuredEntity() {}
}
