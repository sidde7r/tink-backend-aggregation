package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bpcegroup.apiclient.dto.authorize;

import lombok.Data;
import lombok.EqualsAndHashCode;
import se.tink.backend.aggregation.annotations.JsonObject;

@EqualsAndHashCode(callSuper = true)
@Data
@JsonObject
public class AuthTransactionResponseDto extends StepDto {

    private ContextDto context;

    private StepDto step;

    private String id;

    private String locale;

    private SamlResponseDto response;
}
