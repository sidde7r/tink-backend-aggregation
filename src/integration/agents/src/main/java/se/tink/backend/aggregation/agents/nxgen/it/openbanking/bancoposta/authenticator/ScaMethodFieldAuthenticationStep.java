package se.tink.backend.aggregation.agents.nxgen.it.openbanking.bancoposta.authenticator;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.bancoposta.BancoPostaConstants.ErrorValues;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.bancoposta.BancoPostaConstants.UserMessages;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.CbiUserState;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.rpc.ScaMethodEntity;
import se.tink.backend.aggregation.nxgen.controllers.authentication.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.AuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.SupplementInformationRequester;

public class ScaMethodFieldAuthenticationStep implements AuthenticationStep {
    static final String CHOSEN_SCA_METHOD = "chosenScaMethod";

    private final String stepId;
    private final CbiUserState userState;

    ScaMethodFieldAuthenticationStep(final String stepId, final CbiUserState userState) {
        this.stepId = stepId;
        this.userState = userState;
    }

    @Override
    public Optional<SupplementInformationRequester> execute(AuthenticationRequest request)
            throws AuthenticationException, AuthorizationException {
        if (request.getUserInputs() == null || request.getUserInputs().isEmpty()) {
            List<ScaMethodEntity> methods = userState.getScaMethods();
            return Optional.of(
                    new SupplementInformationRequester.Builder()
                            .withFields(Collections.singletonList(buildScaMethodsField(methods)))
                            .build());
        }

        saveScaMethod(request.getUserInputs().get(CHOSEN_SCA_METHOD));

        return Optional.empty();
    }

    @Override
    public String getIdentifier() {
        return stepId;
    }

    private void saveScaMethod(String chosenScaMethodId) {
        List<ScaMethodEntity> methods = userState.getScaMethods();
        int index = Integer.parseInt(chosenScaMethodId) - 1;
        ScaMethodEntity chosenScaMethod = methods.get(index);
        userState.saveChosenAuthenticationMethod(chosenScaMethod.getAuthenticationMethodId());
    }

    private Field buildScaMethodsField(List<ScaMethodEntity> methods) {
        int maxNumber = methods.size();
        String description =
                IntStream.range(0, maxNumber)
                        .mapToObj(i -> String.format("(%d) %s", i + 1, methods.get(i).toString()))
                        .collect(Collectors.joining(";\n"));

        return Field.builder()
                .description(UserMessages.INPUT_FIELD)
                .helpText(String.format(UserMessages.SELECT_INFO, maxNumber).concat(description))
                .name(CHOSEN_SCA_METHOD)
                .numeric(true)
                .minLength(1)
                .pattern(String.format("([1-%d])", maxNumber))
                .patternError(ErrorValues.INVALID_CODE_MESSAGE)
                .build();
    }
}
