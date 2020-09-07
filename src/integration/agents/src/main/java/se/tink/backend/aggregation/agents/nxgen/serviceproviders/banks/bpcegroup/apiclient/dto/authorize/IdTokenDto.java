package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bpcegroup.apiclient.dto.authorize;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Data
public class IdTokenDto {

    private String lastLogin;

    private AuthTimeDto authTime = new AuthTimeDto();
}
