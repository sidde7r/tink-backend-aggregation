package se.tink.backend.aggregation.aggregationcontroller.v1.api;

import javax.ws.rs.Consumes;
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
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.aggregation.aggregationcontroller.v1.rpc.OptOutAccountsRequest;
import se.tink.backend.aggregation.aggregationcontroller.v1.rpc.ProcessAccountsRequest;
import se.tink.backend.aggregation.aggregationcontroller.v1.rpc.RestrictAccountsRequest;
import se.tink.backend.aggregation.aggregationcontroller.v1.rpc.UpdateAccountRequest;
import se.tink.backend.aggregation.aggregationcontroller.v1.rpc.UpdateCredentialsStatusRequest;
import se.tink.backend.aggregation.aggregationcontroller.v1.rpc.UpdateTransferDestinationPatternsRequest;
import se.tink.backend.aggregation.aggregationcontroller.v1.rpc.UpdateTransfersRequest;
import se.tink.libraries.http.annotations.auth.AllowAnonymous;
import se.tink.libraries.signableoperation.rpc.SignableOperation;

@Path("/aggregation/controller/v1/system/update")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public interface UpdateService {
    @GET
    @Path("/ping")
    @TeamOwnership(Team.AGGREGATION)
    @Produces(MediaType.TEXT_PLAIN)
    @AllowAnonymous
    String ping();

    @POST
    @Path("/accounts/update")
    @TeamOwnership(Team.AGGREGATION)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    Account updateAccount(UpdateAccountRequest request);

    /**
     * This method does currently only update the account's bankid. It may in the future be extended
     * to update further metadata. This method should only be used in very specific situations. Not
     * as a regular update. For regular cases refer to <code>updateAccount</code> on <code>
     * POST /accounts/update</code>.
     *
     * @param accountId the id of the account to update
     * @param newBankId bankId in new format
     * @return
     */
    @PUT
    @Path("/accounts/{id}/bankid")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    Account updateAccountsBankId(@PathParam(value = "id") String accountId, String newBankId);

    @POST
    @Path("/accounts/transfer-destinations/update")
    @TeamOwnership(Team.AGGREGATION)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    Response updateTransferDestinationPatterns(UpdateTransferDestinationPatternsRequest request);

    @POST
    @Path("/accounts/process")
    @TeamOwnership(Team.AGGREGATION)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    Response processAccounts(ProcessAccountsRequest request);

    @POST
    @Path("/accounts/opt-out")
    @TeamOwnership(Team.CORE_AGGREGATION)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    Response optOutAccounts(OptOutAccountsRequest request);

    @POST
    @Path("/accounts/restrict")
    @TeamOwnership(Team.CORE_AGGREGATION)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    Response restrictAccounts(RestrictAccountsRequest request);

    @POST
    @Path("/credentials/update")
    @TeamOwnership(Team.AGGREGATION)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    Response updateCredentials(UpdateCredentialsStatusRequest request);

    @POST
    @Path("/credentials/operation/update")
    @TeamOwnership(Team.AGGREGATION)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    Response updateSignableOperation(SignableOperation signableOperation);

    @POST
    @Path("/transfer/process")
    @TeamOwnership(Team.AGGREGATION_AGENT_PLATFORM)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    Response processEinvoices(UpdateTransfersRequest request);
}
