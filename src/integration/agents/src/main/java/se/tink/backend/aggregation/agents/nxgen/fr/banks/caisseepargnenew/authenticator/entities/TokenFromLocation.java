package se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargnenew.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Arrays;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargnenew.CaisseEpargneConstants.QueryKeys;
import se.tink.backend.aggregation.annotations.JsonObject;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@JsonObject
public class TokenFromLocation {

    @JsonIgnore
    public static TokenFromLocation of(String locationUrl) {
        TokenFromLocationBuilder builder = TokenFromLocation.builder();
        Arrays.stream(locationUrl.split("#"))
                .map(string -> string.split("&"))
                .flatMap(Arrays::stream)
                .map(string -> string.split("="))
                .filter(element -> element.length == 2)
                .forEach(
                        element -> {
                            String key = element[0];
                            String value = element[1];
                            if (QueryKeys.ACCESS_TOKEN.equalsIgnoreCase(key)) {
                                builder.accessToken(value);
                            } else if (QueryKeys.TOKEN_TYPE.equalsIgnoreCase(key)) {
                                builder.tokenType(value);
                            } else if (QueryKeys.EXPIRES_IN.equalsIgnoreCase(key)) {
                                builder.expiresIn(Long.parseLong(value));
                            } else if (QueryKeys.ID_TOKEN.equalsIgnoreCase(key)) {
                                builder.idToken(value);
                            }
                        });

        return builder.build();
    }

    @JsonIgnore
    public boolean isValidBearerToken() {
        if (Objects.isNull(tokenType) || Objects.isNull(accessToken)) {
            return false;
        }
        return QueryKeys.BEARER.equalsIgnoreCase(tokenType);
    }

    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("token_type")
    private String tokenType;

    @JsonProperty("expires_in")
    private long expiresIn;

    @JsonProperty("id_token")
    private String idToken;
}
