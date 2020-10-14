package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.annotations.JsonObject;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonObject
public class AuthenticateAccessTokenEntity {

    private String accessToken;

    private Date accessTokenExpirationDate;

    private int accessTokenTimeToLive;

    private String refreshToken;

    private Date refreshTokenExpirationDate;

    private int refreshTokenTimeToLive;

    private String profileId;

    private String profileName;

    private String profileType;

    @JsonProperty("default")
    private boolean defaultFlag;

    private String jwtId;

    private String setId;

    private int executorLevelOfAssurance;
}
