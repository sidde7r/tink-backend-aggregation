package se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.executor.dto;

import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.dto.TypeValuePair;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ParamFieldDto {
    private TypeValuePair type;
    private TypeValuePair param;
    private TypeValuePair paramFormat;
    private TypeValuePair text;
}
