package se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.authenticator;

import static java.util.stream.Collectors.toList;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.annotations.JsonObject;

@Data
@JsonObject
public class ConsentAuthorization {

    private static final Set<String> METHODS_EXCLUDED_FROM_SELECTION = ImmutableSet.of("PHOTO_OTP");
    private String scaStatus;
    private String authorisationId;
    private List<ScaMethod> scaMethods = new ArrayList<>();
    private ChallengeData challengeData;
    private ScaMethod chosenScaMethod;

    boolean isScaMethodSelectionRequired() {
        return !getScaMethods().isEmpty();
    }

    List<ScaMethod> getAllowedScaMethods() {
        scaMethods =
                scaMethods.stream()
                        .filter(
                                method ->
                                        !METHODS_EXCLUDED_FROM_SELECTION.contains(
                                                method.getAuthenticationType()))
                        .collect(toList());
        if (scaMethods.isEmpty()) {
            throw LoginError.NO_AVAILABLE_SCA_METHODS.exception();
        }
        return scaMethods;
    }

    void checkIfChallengeDataIsAllowed() {
        if (StringUtils.isNotBlank(challengeData.image)
                || StringUtils.isNotBlank(challengeData.imageLink)) {
            throw LoginError.NO_AVAILABLE_SCA_METHODS.exception();
        }
    }

    @Data
    @JsonObject
    @Accessors(chain = true)
    static class ScaMethod implements SelectableMethod {
        private String name;
        private String authenticationType;

        @JsonProperty("authenticationMethodId")
        private String identifier;
    }

    @Data
    @JsonObject
    @Accessors(chain = true)
    static class ChallengeData {
        private List<String> data = new ArrayList<>();
        private String image;
        private String imageLink;
        private Integer otpMaxLength;
        private String otpFormat;
    }
}
