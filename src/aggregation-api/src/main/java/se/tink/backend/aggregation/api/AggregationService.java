package se.tink.backend.aggregation.api;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import se.tink.api.annotations.Team;
import se.tink.api.annotations.TeamOwnership;
import se.tink.backend.aggregation.cluster.annotations.ClusterContext;
import se.tink.backend.aggregation.cluster.identification.ClusterInfo;
import se.tink.backend.aggregation.rpc.ChangeProviderRateLimitsRequest;
import se.tink.backend.aggregation.rpc.ConfigureWhitelistInformationRequest;
import se.tink.backend.aggregation.rpc.CreateCredentialsRequest;
import se.tink.backend.aggregation.rpc.Credentials;
import se.tink.backend.aggregation.rpc.DeleteCredentialsRequest;
import se.tink.backend.aggregation.rpc.KeepAliveRequest;
import se.tink.backend.aggregation.rpc.ReEncryptCredentialsRequest;
import se.tink.backend.aggregation.rpc.RefreshWhitelistInformationRequest;
import se.tink.backend.aggregation.rpc.RefreshInformationRequest;
import se.tink.backend.aggregation.rpc.SupplementInformationRequest;
import se.tink.backend.aggregation.rpc.TransferRequest;
import se.tink.backend.aggregation.rpc.UpdateCredentialsRequest;
import se.tink.libraries.http.annotations.auth.AllowAnonymous;

@Path("/aggregation")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public interface AggregationService {
    @POST
    @Path("create")
    @TeamOwnership(Team.INTEGRATION)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    Credentials createCredentials(CreateCredentialsRequest request, @ClusterContext ClusterInfo clusterInfo);

    @POST
    @Path("delete")
    @TeamOwnership(Team.INTEGRATION)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    void deleteCredentials(DeleteCredentialsRequest request, @ClusterContext ClusterInfo clusterInfo);

    @GET
    @Path("ping")
    @TeamOwnership(Team.INTEGRATION)
    @Produces(MediaType.TEXT_PLAIN)
    @AllowAnonymous
    String ping();

    @POST
    @Path("configure/whitelist")
    @TeamOwnership(Team.INTEGRATION)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    void configureWhitelistInformation(ConfigureWhitelistInformationRequest request, @ClusterContext ClusterInfo clusterInfo) throws Exception;

    @POST
    @Path("refresh/whitelist")
    @TeamOwnership(Team.INTEGRATION)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    void refreshWhitelistInformation(RefreshWhitelistInformationRequest request, @ClusterContext ClusterInfo clusterInfo) throws Exception;

    @POST
    @Path("refresh")
    @TeamOwnership(Team.INTEGRATION)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    void refreshInformation(RefreshInformationRequest request, @ClusterContext ClusterInfo clusterInfo) throws Exception;

    @POST
    @Path("transfer")
    @TeamOwnership(Team.INTEGRATION)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    void transfer(TransferRequest request, @ClusterContext ClusterInfo clusterInfo) throws Exception;

    @POST
    @Path("transfer/whitelist")
    @TeamOwnership(Team.INTEGRATION)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    void whitelistedTransfer(WhitelistedTransferRequest request, @ClusterContext ClusterInfo clusterInfo) throws Exception;

    @POST
    @Path("keepalive")
    @TeamOwnership(Team.INTEGRATION)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    void keepAlive(KeepAliveRequest request, @ClusterContext ClusterInfo clusterInfo) throws Exception;

    @PUT
    @Path("update")
    @TeamOwnership(Team.INTEGRATION)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    Credentials updateCredentials(UpdateCredentialsRequest request, @ClusterContext ClusterInfo clusterInfo);

    @POST
    @Path("rateLimits/auto")
    @TeamOwnership(Team.INTEGRATION)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    void updateRateLimits(ChangeProviderRateLimitsRequest request);

    @POST
    @Path("supplemental")
    @TeamOwnership(Team.INTEGRATION)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    void setSupplementalInformation(SupplementInformationRequest request);

    @POST
    @Path("reencrypt/credentials")
    @TeamOwnership(Team.INTEGRATION)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    Response reEncryptCredentials(ReEncryptCredentialsRequest request, @ClusterContext ClusterInfo clusterInfo);
}
