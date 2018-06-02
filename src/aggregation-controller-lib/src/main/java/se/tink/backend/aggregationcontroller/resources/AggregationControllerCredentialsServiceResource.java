package se.tink.backend.aggregationcontroller.resources;

import com.google.inject.Inject;
import javax.ws.rs.core.Response;
import se.tink.backend.aggregationcontroller.client.SystemServiceClient;
import se.tink.backend.aggregationcontroller.v1.api.AggregationControllerCredentialsService;
import se.tink.backend.aggregationcontroller.v1.rpc.credentialsservice.UpdateCredentialsSensitiveRequest;
import se.tink.libraries.http.utils.HttpResponseHelper;

public class AggregationControllerCredentialsServiceResource implements AggregationControllerCredentialsService {
    private final SystemServiceClient serviceClient;

    @Inject
    public AggregationControllerCredentialsServiceResource(SystemServiceClient serviceClient) {
        this.serviceClient = serviceClient;
    }

    @Override
    public Response updateSensitive(UpdateCredentialsSensitiveRequest request) {
        // todo: Move the code from system to here (the controller).
        serviceClient.updateCredentialsSensitiveData(request);
        return HttpResponseHelper.ok();
    }
}
