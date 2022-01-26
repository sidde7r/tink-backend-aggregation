package se.tink.backend.aggregation.agents.nxgen.demo.banks.dk;

import lombok.RequiredArgsConstructor;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.TypedAuthenticator;

@RequiredArgsConstructor
public class DkDemoAuthenticator implements TypedAuthenticator {

    private final Provider provider;
    private final DkDemoNemIdAuthenticator nemIdAuthenticator;
    private final DkDemoMitIdAuthenticator mitIdAuthenticator;

    @Override
    public CredentialsTypes getType() {
        return CredentialsTypes.PASSWORD;
    }

    @Override
    public void authenticate(Credentials credentials) {
        if (!credentials.hasField(Field.Key.AUTH_METHOD_SELECTOR)) {
            throw new IllegalStateException("No selector field");
        }
        String selectedMethodKey = credentials.getField(Field.Key.AUTH_METHOD_SELECTOR);
        DkAuthMethod authMethod = DkAuthMethod.getBySupplementalInfoKey(selectedMethodKey);

        DkDemoFlow flow = DkDemoFlows.getFlowByName(provider.getPayload());

        if (authMethod == DkAuthMethod.NEM_ID) {
            nemIdAuthenticator.authenticate(flow);
        } else if (authMethod == DkAuthMethod.MIT_ID) {
            mitIdAuthenticator.authenticate(flow);
        } else {
            throw new IllegalStateException("Unexpected auth method: " + authMethod);
        }
    }
}
