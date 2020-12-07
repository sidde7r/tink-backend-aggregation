package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy.class)
@Setter
@Getter
@EqualsAndHashCode
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RemittanceInformation {

    @JsonInclude(Include.NON_NULL)
    private String unstructured;

    @JsonInclude(Include.NON_NULL)
    private String reference;

    // This is a short term fix to cover the following scenario:
    // During a customer onboarding, we have identified that some banks don't display
    // unstructured remittance information at all to the creditor
    // We are currently experimenting with setting both values and observing the behaviour

    public static RemittanceInformation ofUnstructuredAndReference(String value) {
        return new RemittanceInformation(value, value);
    }
}
