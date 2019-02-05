package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v11.pis.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class RemittanceInformationEntity {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("Reference")
    private String reference; // Max35Text

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("Unstructured")
    private String unstructured; // Max140Text

    private RemittanceInformationEntity(
            @JsonProperty("Reference") String reference,
            @JsonProperty("Unstructured") String unstructured) {
        this.reference = reference;
        this.unstructured = unstructured;
    }

    private RemittanceInformationEntity(String reference) {
        this(reference, null);
    }

    @JsonIgnore
    public static RemittanceInformationEntity create(String reference) {
        return new RemittanceInformationEntity(reference);
    }

    @JsonIgnore
    public static RemittanceInformationEntity create(String reference, String unstructuredMessage) {
        return new RemittanceInformationEntity(reference, unstructuredMessage);
    }
}
