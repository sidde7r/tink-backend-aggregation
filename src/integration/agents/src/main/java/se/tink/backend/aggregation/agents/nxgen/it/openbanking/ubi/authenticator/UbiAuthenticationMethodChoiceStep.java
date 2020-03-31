package se.tink.backend.aggregation.agents.nxgen.it.openbanking.ubi.authenticator;

import java.util.Collections;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.CbiThirdPartyAppAuthenticationStep;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.entities.ConsentType;
import se.tink.backend.aggregation.nxgen.controllers.authentication.SupplementInformationRequester;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepResponse;

public class UbiAuthenticationMethodChoiceStep implements AuthenticationStep {

    private static final String IS_APP_INSTALLED = "IS_APP_INSTALLED";

    @Override
    public AuthenticationStepResponse execute(AuthenticationRequest request)
            throws AuthenticationException, AuthorizationException {
        if (request.getUserInputs() == null || request.getUserInputs().isEmpty()) {
            return AuthenticationStepResponse.requestForSupplementInformation(
                    new SupplementInformationRequester.Builder()
                            .withFields(Collections.singletonList(buildMethodsField()))
                            .build());
        }

        String chosenMethod = request.getUserInputs().get(IS_APP_INSTALLED);
        if (shouldUseRedirectFlow(chosenMethod)) {
            return AuthenticationStepResponse.executeStepWithId(
                    CbiThirdPartyAppAuthenticationStep.getStepIdentifier(ConsentType.ACCOUNT));
        } else {
            return AuthenticationStepResponse.executeStepWithId(
                    UbiUsernamePasswordAuthenticationStep.getStepIdentifier());
        }
    }

    private Field buildMethodsField() {
        return Field.builder()
                .description("Do you have bank app installed?")
                .helpText("Type Y if you have or N if you don't have")
                .name(IS_APP_INSTALLED)
                .minLength(1)
                .maxLength(1)
                .pattern("y|Y|n|N")
                .patternError("The value you entered is not valid")
                .build();
    }

    private boolean shouldUseRedirectFlow(String chosenMethod) {
        return chosenMethod.equalsIgnoreCase("n");
    }
}
