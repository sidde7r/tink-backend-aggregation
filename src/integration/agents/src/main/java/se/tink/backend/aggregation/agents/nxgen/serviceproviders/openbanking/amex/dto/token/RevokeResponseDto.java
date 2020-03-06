package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.dto.token;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.List;
import lombok.Data;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Data
public class RevokeResponseDto {

    private String result;

    private List<String> revokedTokens;

    private List<String> invalidTokens;
}
