package se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AuthenticatedUserEntity {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private AuthenticationTokenEntity authenticationToken;
    private List<AgreementListEntity> agreements;
    private ErrorMessageEntity errorMessage;

    public AuthenticationTokenEntity getAuthenticationToken() {
        return authenticationToken;
    }

    public List<AgreementListEntity> getAgreements() {
        return agreements != null ? agreements : Collections.emptyList();
    }

    public ErrorMessageEntity getErrorMessage() {
        return errorMessage;
    }

    @JsonIgnore
    public Optional<String> getErrorCode() {
        return Optional.ofNullable(errorMessage != null ? errorMessage.getErrorCode() : null);
    }

    /**
     * Nordea API is a bit weird and send items on different formats depending on the number of
     * items. Multiple rows means that we will get an List of items and one row will not be typed as
     * an array.
     */
    @JsonProperty("agreements")
    public void setAgreements(Object input) {
        if (input == null) {
            return;
        }

        if (input instanceof Map) {
            agreements = Lists.newArrayList(MAPPER.convertValue(input, AgreementListEntity.class));
        } else {
            agreements =
                    MAPPER.convertValue(input, new TypeReference<List<AgreementListEntity>>() {});
        }
    }
}
