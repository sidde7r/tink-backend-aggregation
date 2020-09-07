package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bpcegroup.apiclient.dto.authorize;

import lombok.Data;
import se.tink.backend.aggregation.annotations.JsonObject;

@Data
@JsonObject
public class AuthorizeResponseDto {

    private String enctype;

    private String method;

    private String action;

    private ParametersDto parameters;
}
