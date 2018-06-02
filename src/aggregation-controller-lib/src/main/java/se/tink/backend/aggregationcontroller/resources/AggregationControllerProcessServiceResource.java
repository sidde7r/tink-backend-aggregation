package se.tink.backend.aggregationcontroller.resources;

import com.google.inject.Inject;
import javax.ws.rs.core.Response;
import se.tink.backend.aggregationcontroller.v1.api.AggregationControllerProcessService;
import se.tink.backend.aggregationcontroller.client.SystemServiceClient;
import se.tink.backend.aggregationcontroller.v1.rpc.system.process.GenerateStatisticsAndActivitiesRequest;
import se.tink.backend.aggregationcontroller.v1.rpc.system.process.UpdateTransactionsRequest;
import se.tink.libraries.http.utils.HttpResponseHelper;

public class AggregationControllerProcessServiceResource implements AggregationControllerProcessService {
    private final SystemServiceClient serviceClient;

    @Inject
    public AggregationControllerProcessServiceResource(SystemServiceClient serviceClient) {
        this.serviceClient = serviceClient;
    }

    @Override
    public Response generateStatisticsAndActivityAsynchronously(GenerateStatisticsAndActivitiesRequest request) {
        serviceClient.generateStatisticsAndActivityAsynchronously(request);
        return HttpResponseHelper.ok();
    }

    @Override
    public Response updateTransactionsAsynchronously(UpdateTransactionsRequest request) {
        serviceClient.updateTransactionsAsynchronously(request);
        return HttpResponseHelper.ok();
    }
}
