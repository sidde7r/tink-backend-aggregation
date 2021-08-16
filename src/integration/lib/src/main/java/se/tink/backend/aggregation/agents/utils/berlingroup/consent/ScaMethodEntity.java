package se.tink.backend.aggregation.agents.utils.berlingroup.consent;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.agents.utils.authentication.AuthenticationType;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class ScaMethodEntity {
    private String authenticationType;
    private String authenticationVersion;
    private String authenticationMethodId;
    private String name;

    @JsonIgnore
    public Optional<AuthenticationType> getAuthenticationTypeMethod() {
        return AuthenticationType.fromString(authenticationType);
    }
}
