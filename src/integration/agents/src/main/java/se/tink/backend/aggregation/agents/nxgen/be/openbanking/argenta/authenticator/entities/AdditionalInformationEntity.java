package se.tink.backend.aggregation.agents.nxgen.be.openbanking.argenta.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@SuppressWarnings({"FieldCanBeLocal", "FieldMayBeFinal", "unused"})
public class AdditionalInformationEntity {

    @JsonProperty("ownerName")
    private List<IbanEntity> ownerNames;

    public AdditionalInformationEntity(List<IbanEntity> ownerNames) {
        this.ownerNames = ownerNames;
    }
}
