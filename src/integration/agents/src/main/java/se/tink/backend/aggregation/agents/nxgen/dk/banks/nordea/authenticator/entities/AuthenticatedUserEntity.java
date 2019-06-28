package se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AuthenticatedUserEntity {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private AuthenticationTokenEntity authenticationToken;
    private List<AgreementEntity> agreements;
    private ErrorMessageEntity errorMessage;

    public AuthenticationTokenEntity getAuthenticationToken() {
        return authenticationToken;
    }

    public List<AgreementEntity> getAgreements() {
        return agreements != null ? agreements : Collections.emptyList();
    }

    public ErrorMessageEntity getErrorMessage() {
        return errorMessage;
    }

    /**
     * Nordea API is a bit weird and sends "agreements" object in different formats according to the
     * amount of bank agreements a user has. If user has only one bank agreement then the bank API
     * sends an AgreementEntity object whereas if the user has multiple bank agreements we get a
     * list of AgreementEntity objects. For this reason we need the function below.
     */
    @JsonProperty("agreements")
    public void setAgreements(JsonNode internal) {

        if (internal == null) return;

        agreements = new ArrayList<>();
        internal = internal.get("agreement");

        if (internal.isArray()) {
            for (final JsonNode objNode : internal) {
                agreements.add(MAPPER.convertValue(objNode, AgreementEntity.class));
            }
        } else {
            agreements.add(MAPPER.convertValue(internal, AgreementEntity.class));
        }
    }

    @JsonIgnore
    public Optional<String> getErrorCode() {
        return Optional.ofNullable(errorMessage != null ? errorMessage.getErrorCode() : null);
    }
}
