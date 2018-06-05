package se.tink.backend.system.api;

import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import se.tink.api.annotations.Team;
import se.tink.api.annotations.TeamOwnership;
import se.tink.backend.core.Account;
import se.tink.backend.core.User;
import se.tink.backend.core.signableoperation.SignableOperation;
import se.tink.backend.rpc.DeleteAccountRequest;
import se.tink.backend.rpc.DeleteUserRequest;
import se.tink.backend.rpc.SupplementalInformationRequest;
import se.tink.backend.rpc.SupplementalInformationResponse;
import se.tink.backend.system.rpc.ProcessAccountsRequest;
import se.tink.backend.system.rpc.Transaction;
import se.tink.backend.system.rpc.UpdateAccountRequest;
import se.tink.backend.system.rpc.UpdateApplicationRequest;
import se.tink.backend.system.rpc.UpdateCredentialsSensitiveRequest;
import se.tink.backend.system.rpc.UpdateCredentialsStatusRequest;
import se.tink.backend.system.rpc.UpdateDocumentRequest;
import se.tink.backend.system.rpc.UpdateDocumentResponse;
import se.tink.backend.system.rpc.UpdateFraudDetailsRequest;
import se.tink.backend.system.rpc.UpdateProductInformationRequest;
import se.tink.backend.system.rpc.UpdateTransferDestinationPatternsRequest;
import se.tink.backend.system.rpc.UpdateTransfersRequest;
import se.tink.libraries.http.annotations.auth.AllowAnonymous;

@Path("/update")
@Consumes({
    MediaType.APPLICATION_JSON
})
@Produces({
    MediaType.APPLICATION_JSON
})
public interface UpdateService {
    @GET
    @Path("/ping")
    @TeamOwnership(Team.DATA)
    @Produces({
            MediaType.TEXT_PLAIN
    })
    @AllowAnonymous
    String ping();

    @DELETE
    @Path("/user/{userid)/transaction/{transactionid}")
    @TeamOwnership(Team.DATA)
    @Consumes({
            MediaType.APPLICATION_JSON
    })
    @Produces({
            MediaType.APPLICATION_JSON
    })
    public void deleteTransaction(@PathParam("userid") String userid, @PathParam("transactionid") String transactionid);

    @POST
    @Path("/transactions/update")
    @TeamOwnership(Team.DATA)
    @Consumes({
            MediaType.APPLICATION_JSON
    })
    @Produces({
            MediaType.APPLICATION_JSON
    })
    public Transaction updateTransaction(Transaction transaction);

    @DELETE
    @Path("/user/{userid)/activities")
    @TeamOwnership(Team.DATA)
    @Consumes({
            MediaType.APPLICATION_JSON
    })
    @Produces({
            MediaType.APPLICATION_JSON
    })
    public void deleteAllActivitiesFor(@PathParam("userid") String userid);

    @DELETE
    @Path("/user/{userid)/activities")
    @TeamOwnership(Team.DATA)
    @Consumes({
            MediaType.APPLICATION_JSON
    })
    @Produces({
            MediaType.APPLICATION_JSON
    })
    public void deleteStatistics(@PathParam("userid") String userid);

    @POST
    @Path("/user/delete")
    @TeamOwnership(Team.DATA)
    @Consumes({
            MediaType.APPLICATION_JSON
    })
    @Produces({
            MediaType.APPLICATION_JSON
    })
    public void deleteUser(DeleteUserRequest deleteUserRequest);

    @POST
    @Path("/credentials/supplementalInformation")
    @TeamOwnership(Team.INTEGRATION)
    @Consumes({
            MediaType.APPLICATION_JSON
    })
    @Produces({
            MediaType.APPLICATION_JSON
    })
    SupplementalInformationResponse getSupplementalInformation(SupplementalInformationRequest request);

    @POST
    @Path("/accounts/update")
    @TeamOwnership(Team.DATA)
    @Consumes({
        MediaType.APPLICATION_JSON
    })
    @Produces({
        MediaType.APPLICATION_JSON
    })
    public Account updateAccount(UpdateAccountRequest request);

    @POST
    @Path("/accounts/transfer-destinations/update")
    @TeamOwnership(Team.DATA)
    @Consumes({
            MediaType.APPLICATION_JSON
    })
    @Produces({
            MediaType.APPLICATION_JSON
    })
    public Response updateTransferDestinationPatterns(UpdateTransferDestinationPatternsRequest request);

    @POST
    @Path("/accounts/process")
    @TeamOwnership(Team.DATA)
    @Consumes({
            MediaType.APPLICATION_JSON
    })
    @Produces({
            MediaType.APPLICATION_JSON
    })
    public Response processAccounts(ProcessAccountsRequest request);

    @PUT
    @Path("/credentials/sensitive")
    @TeamOwnership(Team.INTEGRATION)
    @Consumes({
            MediaType.APPLICATION_JSON
    })
    @Produces({
            MediaType.APPLICATION_JSON
    })
    Response updateCredentialsSensitiveData(UpdateCredentialsSensitiveRequest request);

    @POST
    @Path("/credentials/update")
    @TeamOwnership(Team.DATA)
    @Consumes({
        MediaType.APPLICATION_JSON
    })
    @Produces({
        MediaType.APPLICATION_JSON
    })
    public Response updateCredentials(UpdateCredentialsStatusRequest request);

    @POST
    @Path("/fraud/update")
    @TeamOwnership(Team.DATA)
    @Consumes({
            MediaType.APPLICATION_JSON
    })
    @Produces({
            MediaType.APPLICATION_JSON
    })
    public Response updateFraudDetails(UpdateFraudDetailsRequest request);

    @POST
    @Path("/credentials/operation/update")
    @TeamOwnership(Team.DATA)
    @Consumes({
            MediaType.APPLICATION_JSON
    })
    @Produces({
            MediaType.APPLICATION_JSON
    })
    Response updateSignableOperation(SignableOperation signableOperation);
    
    @POST
    @Path("/transfer/process")
    @TeamOwnership(Team.INTEGRATION)
    @Consumes({
            MediaType.APPLICATION_JSON
    })
    @Produces({
            MediaType.APPLICATION_JSON
    })
    Response processEinvoices(UpdateTransfersRequest request);

    @POST
    @Path("/document")
    @TeamOwnership(Team.INTEGRATION)
    @Consumes({
            MediaType.APPLICATION_JSON
    })
    @Produces({
            MediaType.APPLICATION_JSON
    })
    UpdateDocumentResponse updateDocument(UpdateDocumentRequest request);

    @POST
    @Path("/product/information")
    @TeamOwnership(Team.INTEGRATION)
    @Consumes({
            MediaType.APPLICATION_JSON
    })
    @Produces({
            MediaType.APPLICATION_JSON
    })
    Response updateProductInformation(UpdateProductInformationRequest updateProductInformationRequest);

    @POST
    @Path("/application")
    @TeamOwnership(Team.INTEGRATION)
    @Consumes({
            MediaType.APPLICATION_JSON
    })
    @Produces({
            MediaType.APPLICATION_JSON
    })
    Response updateApplication(UpdateApplicationRequest updateApplicationRequest);

    /**
     * Updates the user's flags.
     */
    @PUT
    @Path("/{token}/flags")
    @TeamOwnership(Team.DATA)
    User updateUserFlags(@PathParam(value = "token") String userId, List<String> userFlags);

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @TeamOwnership(Team.DATA)
    void deleteAccount(DeleteAccountRequest deleteAccountRequest);
}
