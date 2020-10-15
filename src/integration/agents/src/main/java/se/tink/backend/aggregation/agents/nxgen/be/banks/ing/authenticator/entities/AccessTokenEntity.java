package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.entities;

import java.util.Date;
import lombok.Data;
import se.tink.backend.aggregation.annotations.JsonObject;

@Data
@JsonObject
public class AccessTokenEntity {

    private String accessToken;

    private Date accessTokenExpirationDate;
}
