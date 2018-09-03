package se.tink.backend.aggregation.aggregationcontroller.v1.api;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import se.tink.api.annotations.Team;
import se.tink.api.annotations.TeamOwnership;
import se.tink.backend.aggregation.aggregationcontroller.v1.rpc.ProcessAccountsRequest;
import se.tink.backend.aggregation.aggregationcontroller.v1.rpc.SupplementalInformationRequest;
import se.tink.backend.aggregation.aggregationcontroller.v1.rpc.SupplementalInformationResponse;
import se.tink.backend.aggregation.aggregationcontroller.v1.rpc.UpdateAccountRequest;
import se.tink.backend.aggregation.aggregationcontroller.v1.rpc.UpdateApplicationRequest;
import se.tink.backend.aggregation.aggregationcontroller.v1.rpc.UpdateCredentialsStatusRequest;
import se.tink.backend.aggregation.aggregationcontroller.v1.rpc.UpdateDocumentRequest;
import se.tink.backend.aggregation.aggregationcontroller.v1.rpc.UpdateDocumentResponse;
import se.tink.backend.aggregation.aggregationcontroller.v1.rpc.UpdateProductInformationRequest;
import se.tink.backend.aggregation.aggregationcontroller.v1.rpc.UpdateTransferDestinationPatternsRequest;
import se.tink.backend.aggregation.aggregationcontroller.v1.rpc.UpdateTransfersRequest;
import se.tink.backend.aggregation.rpc.Account;
import se.tink.backend.core.signableoperation.SignableOperation;
import se.tink.backend.system.rpc.UpdateFraudDetailsRequest;
import se.tink.libraries.http.annotations.auth.AllowAnonymous;

@Path("/aggregation/controller/v1/system/update")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public interface UpdateService {
    @GET
    @Path("/ping")
    @TeamOwnership(Team.DATA)
    @Produces(MediaType.TEXT_PLAIN)
    @AllowAnonymous
    String ping();

    @POST
    @Path("/credentials/supplementalInformation")
    @TeamOwnership(Team.DATA)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    SupplementalInformationResponse getSupplementalInformation(SupplementalInformationRequest request);

    @POST
    @Path("/accounts/update")
    @TeamOwnership(Team.DATA)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    Account updateAccount(UpdateAccountRequest request);

    @POST
    @Path("/accounts/transfer-destinations/update")
    @TeamOwnership(Team.DATA)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    Response updateTransferDestinationPatterns(UpdateTransferDestinationPatternsRequest request);

    @POST
    @Path("/accounts/process")
    @TeamOwnership(Team.DATA)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    Response processAccounts(ProcessAccountsRequest request);

    @POST
    @Path("/credentials/update")
    @TeamOwnership(Team.DATA)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    Response updateCredentials(UpdateCredentialsStatusRequest request);

    @POST
    @Path("/credentials/operation/update")
    @TeamOwnership(Team.DATA)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    Response updateSignableOperation(SignableOperation signableOperation);

    @POST
    @Path("/transfer/process")
    @TeamOwnership(Team.DATA)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    Response processEinvoices(UpdateTransfersRequest request);

    @POST
    @Path("/document")
    @TeamOwnership(Team.DATA)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    UpdateDocumentResponse updateDocument(UpdateDocumentRequest request);

    @POST
    @Path("/product/information")
    @TeamOwnership(Team.DATA)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    Response updateProductInformation(UpdateProductInformationRequest updateProductInformationRequest);

    @POST
    @Path("/application")
    @TeamOwnership(Team.DATA)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    Response updateApplication(UpdateApplicationRequest updateApplicationRequest);


    @POST
    @Path("/fraud/update")
    @TeamOwnership(Team.DATA)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateFraudDetails(UpdateFraudDetailsRequest request);

}
