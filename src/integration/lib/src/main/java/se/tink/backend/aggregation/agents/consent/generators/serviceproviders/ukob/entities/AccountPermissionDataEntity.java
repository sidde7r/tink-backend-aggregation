package se.tink.backend.aggregation.agents.consent.generators.serviceproviders.ukob.entities;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@AllArgsConstructor(staticName = "of")
@RequiredArgsConstructor(staticName = "of")
@Getter
public class AccountPermissionDataEntity {
    @JsonProperty("Permissions")
    @NonNull
    private final Set<String> permissions;

    @JsonProperty("ExpirationDateTime")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String expirationDateTime;
}
