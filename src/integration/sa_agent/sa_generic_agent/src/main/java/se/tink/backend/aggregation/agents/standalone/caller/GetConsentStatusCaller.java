package se.tink.backend.aggregation.agents.standalone.caller;

import java.util.concurrent.Callable;
import se.tink.backend.aggregation.agents.standalone.entity.ConsentStatus;
import se.tink.backend.aggregation.agents.standalone.grpc.AuthenticationService;
import se.tink.sa.model.auth.GetConsentStatusRequest;

public class GetConsentStatusCaller implements Callable<ConsentStatus> {

    private final GetConsentStatusRequest getConsentStatusRequest;
    private final AuthenticationService authenticationService;

    public GetConsentStatusCaller(
            AuthenticationService authenticationService,
            GetConsentStatusRequest getConsentStatusRequest) {
        this.getConsentStatusRequest = getConsentStatusRequest;
        this.authenticationService = authenticationService;
    }

    @Override
    public ConsentStatus call() throws Exception {
        return authenticationService.getConsentStatus(getConsentStatusRequest);
    }
}
