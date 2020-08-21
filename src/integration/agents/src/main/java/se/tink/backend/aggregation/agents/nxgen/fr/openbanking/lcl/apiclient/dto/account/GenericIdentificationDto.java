package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.apiclient.dto.account;

import lombok.Data;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Data
public class GenericIdentificationDto {

    private String identification;

    private IdentificationSchemeName schemeName;
}
