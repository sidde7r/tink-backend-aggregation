package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.danskebank.rcp;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.List;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.danskebank.entity.ErrorEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy.class)
public class ErrorResponse {

    @Getter private String code;
    @Getter private String id;
    @Getter private String message;
    @Getter private List<ErrorEntity> errors;
}
