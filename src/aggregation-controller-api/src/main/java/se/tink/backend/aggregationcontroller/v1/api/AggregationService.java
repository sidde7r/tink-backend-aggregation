package se.tink.backend.aggregationcontroller.v1.api;

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
import se.tink.backend.aggregationcontroller.v1.rpc.aggregation.aggregation.ChangeProviderRateLimitsRequest;
import se.tink.backend.aggregationcontroller.v1.rpc.aggregation.aggregation.CreateCredentialsRequest;
import se.tink.backend.aggregationcontroller.v1.rpc.aggregation.aggregation.CreateProductRequest;
import se.tink.backend.aggregationcontroller.v1.rpc.aggregation.aggregation.DeleteCredentialsRequest;
import se.tink.backend.aggregationcontroller.v1.rpc.aggregation.aggregation.KeepAliveRequest;
import se.tink.backend.aggregationcontroller.v1.rpc.aggregation.aggregation.MigrateCredentialsDecryptRequest;
import se.tink.backend.aggregationcontroller.v1.rpc.aggregation.aggregation.MigrateCredentialsReencryptRequest;
import se.tink.backend.aggregationcontroller.v1.rpc.aggregation.aggregation.ProductInformationRequest;
import se.tink.backend.aggregationcontroller.v1.rpc.aggregation.aggregation.ReencryptionRequest;
import se.tink.backend.aggregationcontroller.v1.rpc.aggregation.aggregation.RefreshApplicationRequest;
import se.tink.backend.aggregationcontroller.v1.rpc.aggregation.aggregation.RefreshInformationRequest;
import se.tink.backend.aggregationcontroller.v1.rpc.aggregation.aggregation.SupplementInformationRequest;
import se.tink.backend.aggregationcontroller.v1.rpc.aggregation.aggregation.TransferRequest;
import se.tink.backend.aggregationcontroller.v1.rpc.aggregation.aggregation.UpdateCredentialsRequest;
import se.tink.backend.aggregationcontroller.v1.rpc.entities.Credentials;
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
    Credentials createCredentials(CreateCredentialsRequest request);

    @POST
    @Path("reencrypt")
    @TeamOwnership(Team.INTEGRATION)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    Credentials reencryptCredentials(ReencryptionRequest request);

    @POST
    @Path("delete")
    @TeamOwnership(Team.INTEGRATION)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    void deleteCredentials(DeleteCredentialsRequest request);

    @GET
    @Path("ping")
    @TeamOwnership(Team.INTEGRATION)
    @Produces(MediaType.TEXT_PLAIN)
    @AllowAnonymous
    String ping();

    @POST
    @Path("refresh")
    @TeamOwnership(Team.INTEGRATION)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    void refreshInformation(RefreshInformationRequest request) throws Exception;

    @POST
    @Path("transfer")
    @TeamOwnership(Team.INTEGRATION)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    void transfer(TransferRequest request) throws Exception;

    @POST
    @Path("keepalive")
    @TeamOwnership(Team.INTEGRATION)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    void keepAlive(KeepAliveRequest request) throws Exception;

    @PUT
    @Path("update")
    @TeamOwnership(Team.INTEGRATION)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    Credentials updateCredentials(UpdateCredentialsRequest request);

    @POST
    @Path("rateLimits/auto")
    @TeamOwnership(Team.INTEGRATION)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    void updateRateLimits(ChangeProviderRateLimitsRequest request);

    @POST
    @Path("product")
    @TeamOwnership(Team.INTEGRATION)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    void createProduct(CreateProductRequest request) throws Exception;

    @POST
    @Path("product/information")
    @TeamOwnership(Team.INTEGRATION)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    void fetchProductInformation(ProductInformationRequest request) throws Exception;

    @POST
    @Path("application/refresh")
    @TeamOwnership(Team.INTEGRATION)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    void refreshApplication(RefreshApplicationRequest request) throws Exception;

    @POST
    @Path("supplemental")
    @TeamOwnership(Team.INTEGRATION)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    void setSupplementalInformation(SupplementInformationRequest request);

    @POST
    @Path("migrate/decrypt")
    @TeamOwnership(Team.INTEGRATION)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    Credentials migrateDecryptCredentials(MigrateCredentialsDecryptRequest request);

    @POST
    @Path("migrate/reencrypt")
    @TeamOwnership(Team.INTEGRATION)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    Response migrateReencryptCredentials(MigrateCredentialsReencryptRequest request);
}
