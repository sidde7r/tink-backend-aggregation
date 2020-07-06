package se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargnenew.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.Getter;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargnenew.CaisseEpargneConstants.ResponseValues;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargnenew.authenticator.entities.Context;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargnenew.authenticator.entities.Step;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargnenew.authenticator.entities.ValidationUnit;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargnenew.authenticator.entities.VirtualKeyboard;
import se.tink.libraries.streamutils.StreamUtils;

@Getter
public class SamlAuthnResponse extends Step {

    @JsonProperty("context")
    private Context context;

    @JsonProperty("step")
    private Step step;

    @JsonProperty("id")
    private String id;

    @JsonProperty("locale")
    private String locale;

    @JsonProperty("response")
    private SamlResponse samlResponse;

    @JsonIgnore
    public Optional<String> getValidationId() {
        return step.getValidationId();
    }

    @JsonIgnore
    public Optional<String> getValidationUnitId() {
        return Optional.ofNullable(
                step.getValidationUnits().stream()
                        .map(ValidationUnit::getId)
                        .collect(StreamUtils.toSingleton()));
    }

    @JsonIgnore
    public Optional<String> getSaml2PostAction() {
        if (Objects.isNull(samlResponse) || Objects.isNull(samlResponse.getSaml2Post())) {
            return Optional.empty();
        }
        return Optional.ofNullable(samlResponse.getSaml2Post().getAction());
    }

    @JsonIgnore
    public Optional<String> getKeyboardImagesUrl() {
        if (Objects.isNull(step)) {
            return Optional.empty();
        }
        List<String> paths =
                step.getValidationUnits().stream()
                        .filter(
                                virtualKeyboardValidationItem ->
                                        Objects.equals(
                                                virtualKeyboardValidationItem.getType(),
                                                ResponseValues.PASSWORD))
                        .map(ValidationUnit::getVirtualKeyboard)
                        .filter(Objects::nonNull)
                        .map(VirtualKeyboard::getExternalRestMediaApiUrl)
                        .collect(Collectors.toList());
        if (paths.size() > 1) {
            return Optional.empty();
        }
        return paths.stream().findFirst();
    }

    @JsonIgnore
    @Override
    public void throwIfFailedAuthentication() throws LoginException, AuthorizationException {
        super.throwIfFailedAuthentication();
        if (Objects.isNull(samlResponse)) {
            return;
        }
        samlResponse.throwIfFailedAuthentication();
    }

    @JsonIgnore
    public Optional<String> getSamlResponseValue() {
        if (Objects.isNull(samlResponse) || Objects.isNull(samlResponse.getSaml2Post())) {
            return Optional.empty();
        }
        return Optional.ofNullable(samlResponse.getSaml2Post().getSamlResponse());
    }
}
