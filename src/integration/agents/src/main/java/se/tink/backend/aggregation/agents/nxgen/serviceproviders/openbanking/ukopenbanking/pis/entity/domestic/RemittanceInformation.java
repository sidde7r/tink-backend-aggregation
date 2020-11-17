package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.entity.domestic;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Setter;
import se.tink.backend.aggregation.annotations.JsonObject;

/**
 * https://developer.hsbc.com/assets/docs/HSBC%20Open%20Banking%20TPP%20Implementation%20Guide%20(v3.1).pdf
 */
@JsonObject
@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy.class)
@Setter
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

    // This is a short term fix to cover the following scenario:
    // During a customer onboarding, we have identified that some banks don't display
    // unstructured remittance information at all to the creditor
    // We are currently experimenting with setting both values and observing the behaviour

    public static RemittanceInformation ofUnstructuredAndReference(String value) {
        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.unstructured = value;

        remittanceInformation.reference = value;
        return remittanceInformation;
    }

    public String getReference() {
        return reference;
    }

    public String getUnstructured() {
        return unstructured;
    }
}
