package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.pis.entity.domestic;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
        this.reference = String.format("%s %s", reference.getType(), reference.getValue());
    }

    // TODO: when I use "getReference" it breaks the serialization (in the serialized object
    // instead of having a string we get a Reference object and we don't want that as it
    // breaks the communication with UK API. This is why I had to rename the function
    // I need to find a more elegant way

    @JsonIgnore
    public Reference createTinkReference() {
        String[] res = reference.split(" ");
        return new Reference(res[0], res[1]);
    }
}
