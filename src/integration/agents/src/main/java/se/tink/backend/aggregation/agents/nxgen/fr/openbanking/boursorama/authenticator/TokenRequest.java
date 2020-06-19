package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.authenticator;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
@EqualsAndHashCode
@ToString
@AllArgsConstructor
public class TokenRequest {

    private String grantType;

    private String authorizationCode;

    private String clientId;
}
