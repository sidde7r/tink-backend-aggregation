package se.tink.backend.aggregationcontroller.resources;

import com.google.inject.Inject;
import javax.ws.rs.core.Response;
import se.tink.backend.aggregationcontroller.client.SystemServiceClient;
import se.tink.backend.aggregationcontroller.v1.api.AggregationControllerUpdateService;
import se.tink.backend.aggregationcontroller.v1.rpc.entities.Account;
import se.tink.backend.aggregationcontroller.v1.rpc.system.update.ProcessAccountsRequest;
import se.tink.backend.aggregationcontroller.v1.rpc.system.update.SupplementalInformationRequest;
import se.tink.backend.aggregationcontroller.v1.rpc.system.update.SupplementalInformationResponse;
import se.tink.backend.aggregationcontroller.v1.rpc.system.update.UpdateAccountRequest;
import se.tink.backend.aggregationcontroller.v1.rpc.system.update.UpdateApplicationRequest;
import se.tink.backend.aggregationcontroller.v1.rpc.system.update.UpdateCredentialsStatusRequest;
import se.tink.backend.aggregationcontroller.v1.rpc.system.update.UpdateDocumentRequest;
import se.tink.backend.aggregationcontroller.v1.rpc.system.update.UpdateDocumentResponse;
import se.tink.backend.aggregationcontroller.v1.rpc.system.update.UpdateProductInformationRequest;
import se.tink.backend.aggregationcontroller.v1.rpc.system.update.UpdateTransferDestinationPatternsRequest;
import se.tink.backend.aggregationcontroller.v1.rpc.system.update.UpdateTransfersRequest;
import se.tink.backend.core.signableoperation.SignableOperation;
import se.tink.libraries.http.utils.HttpResponseHelper;

public class AggregationControllerUpdateServiceResource implements AggregationControllerUpdateService {
    private final SystemServiceClient serviceClient;

    @Inject
    public AggregationControllerUpdateServiceResource(SystemServiceClient serviceClient) {
        this.serviceClient = serviceClient;
    }

    @Override
    public String ping() {
        return serviceClient.ping();
    }

    @Override
    public SupplementalInformationResponse getSupplementalInformation(SupplementalInformationRequest request) {
        return serviceClient.getSupplementalInformation(request);
    }

    @Override
    public Account updateAccount(UpdateAccountRequest request) {
        return serviceClient.updateAccount(request);
    }

    @Override
    public Response updateTransferDestinationPatterns(UpdateTransferDestinationPatternsRequest request) {
        serviceClient.updateTransferDestinationPatterns(request);
        return HttpResponseHelper.ok();
    }

    @Override
    public Response processAccounts(ProcessAccountsRequest request) {
        serviceClient.processAccounts(request);
        return HttpResponseHelper.ok();
    }

    @Override
    public Response updateCredentials(UpdateCredentialsStatusRequest request) {
        serviceClient.updateCredentials(request);
        return HttpResponseHelper.ok();
    }

    @Override
    public Response updateSignableOperation(SignableOperation signableOperation) {
        serviceClient.updateSignableOperation(signableOperation);
        return HttpResponseHelper.ok();
    }

    @Override
    public Response processEinvoices(UpdateTransfersRequest request) {
        serviceClient.processEinvoices(request);
        return HttpResponseHelper.ok();
    }

    @Override
    public UpdateDocumentResponse updateDocument(UpdateDocumentRequest request) {
        return serviceClient.updateDocument(request);
    }

    @Override
    public Response updateProductInformation(UpdateProductInformationRequest updateProductInformationRequest) {
        serviceClient.updateProductInformation(updateProductInformationRequest);
        return HttpResponseHelper.ok();
    }

    @Override
    public Response updateApplication(UpdateApplicationRequest updateApplicationRequest) {
        serviceClient.updateApplication(updateApplicationRequest);
        return HttpResponseHelper.ok();
    }
}
