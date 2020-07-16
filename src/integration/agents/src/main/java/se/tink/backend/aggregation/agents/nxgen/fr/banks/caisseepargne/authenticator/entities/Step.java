package se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.beneficiary.BeneficiaryException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.CaisseEpargneConstants.ResponseValues;
import se.tink.libraries.streamutils.StreamUtils;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Step {

    @JsonProperty("phase")
    private Phase phase;

    @JsonProperty("validationUnits")
    private List<Map<String, List<ValidationUnit>>> validationUnits;

    @JsonIgnore
    public Optional<String> getValidationId() {
        return Optional.ofNullable(
                validationUnits.stream()
                        .map(Map::keySet)
                        .flatMap(Set::stream)
                        .collect(StreamUtils.toSingleton()));
    }

    @JsonIgnore
    public List<ValidationUnit> getValidationUnits() {
        List<ValidationUnit> virtualKeyboardValidationItems = new ArrayList<>();
        validationUnits.stream()
                .map(Map::values)
                .flatMap(Collection::stream)
                .forEach(virtualKeyboardValidationItems::addAll);
        return virtualKeyboardValidationItems;
    }

    public void throwIfFailedAuthentication() throws LoginException, AuthorizationException {
        throwExeption();
    }

    public void throwBeneficiaryExceptionIfFailedAuthentication()
            throws BeneficiaryException, LoginException {
        throwExeption();
    }

    private void throwExeption() throws LoginException {
        if (Objects.isNull(phase)) {
            return;
        }
        if (ResponseValues.FAILED_AUTHENTICATION.equals(phase.getPreviousResult())
                && phase.getRetryCounter() == 1) {
            throw LoginError.INCORRECT_CREDENTIALS_LAST_ATTEMPT.exception();
        } else if (ResponseValues.FAILED_AUTHENTICATION.equals(phase.getPreviousResult())) {
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        }
    }
}
