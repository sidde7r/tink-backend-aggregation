package se.tink.backend.aggregation.nxgen.controllers.authentication;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.ProgressiveAuthAgent;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;

public final class ProgressiveLoginExecutor {

    private final SupplementalInformationController supplementalInformationController;
    private final ProgressiveAuthAgent agent;

    public ProgressiveLoginExecutor(
            final SupplementalInformationController supplementalInformationController,
            final ProgressiveAuthAgent agent) {
        this.supplementalInformationController = supplementalInformationController;
        this.agent = agent;
    }

    public void login() throws Exception {
        SteppableAuthenticationResponse response =
                agent.login(SteppableAuthenticationRequest.initialRequest());
        while (response.getStep().isPresent()) {
            response = agent.login(handleResponse(response.getStep().get(), response.getPayload()));
        }
    }

    private SteppableAuthenticationRequest handleResponse(
            final Class<? extends AuthenticationStep> step, final AuthenticationResponse payload)
            throws Exception {

        if (payload.getThirdPartyAppPayload().isPresent()) {
            supplementalInformationController.openThirdPartyApp(
                    payload.getThirdPartyAppPayload().get());
            return SteppableAuthenticationRequest.subsequentRequest(step, Collections.emptyList());
        }

        final List<Field> fields = payload.getFields();
        final Map<String, String> map =
                supplementalInformationController.askSupplementalInformation(
                        fields.toArray(new Field[fields.size()]));

        return SteppableAuthenticationRequest.subsequentRequest(
                step, new ArrayList<>(map.values()));
    }
}
