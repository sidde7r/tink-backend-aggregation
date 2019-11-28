package se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.fetchers.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.dto.TypeValuePair;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AssetJarsDto {
    private TypeValuePair name;
    private TypeValuePair reference;
    private TypeValuePair presentation;
    private TypeValuePair variation;

    public AssetJarsDto() {}

    public AssetJarsDto(TypeValuePair reference) {
        this.reference = reference;
    }

    public TypeValuePair getName() {
        return name;
    }

    public TypeValuePair getReference() {
        return reference;
    }

    public TypeValuePair getPresentation() {
        return presentation;
    }

    public TypeValuePair getVariation() {
        return variation;
    }
}
