package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.pis.entity.domestic;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import se.tink.backend.aggregation.annotations.JsonObject;

/**
 * https://developer.hsbc.com/assets/docs/HSBC%20Open%20Banking%20TPP%20Implementation%20Guide%20(v3.1).pdf
 */
@JsonObject
@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy.class)
public class RemittanceInformation {
    @JsonInclude(Include.NON_NULL)
    private String unstructured;

    @JsonInclude(Include.NON_NULL)
    private String reference;

    // Used in serialization unit tests
    protected RemittanceInformation() {}

    public static RemittanceInformation ofUnstructured(String unstructured) {
        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.unstructured = unstructured;

        return remittanceInformation;
    }

    public String getReference() {
        return reference;
    }

    public String getUnstructured() {
        return unstructured;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }
}
