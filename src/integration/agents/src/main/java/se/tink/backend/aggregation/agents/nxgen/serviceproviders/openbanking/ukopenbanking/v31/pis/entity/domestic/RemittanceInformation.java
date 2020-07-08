package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.pis.entity.domestic;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.payment.rpc.Reference;

@JsonObject
@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy.class)
public class RemittanceInformation {
    private String unstructured;
    private String reference;

    // Used in serialization unit tests
    protected RemittanceInformation() {}

    public RemittanceInformation(String unstructured, Reference reference) {
        this.unstructured = unstructured;
        this.reference = reference.getValue();
    }

    public RemittanceInformation(Reference reference) {
        this("", reference);
    }

    public String getReference() {
        return reference;
    }
}
