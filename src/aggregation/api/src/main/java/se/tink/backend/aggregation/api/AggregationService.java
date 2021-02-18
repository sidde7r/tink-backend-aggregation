package se.tink.backend.aggregation.api;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import se.tink.api.annotations.Team;
import se.tink.api.annotations.TeamOwnership;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.cluster.annotations.ClientContext;
import se.tink.backend.aggregation.cluster.identification.ClientInfo;
import se.tink.backend.aggregation.rpc.ChangeProviderRateLimitsRequest;
import se.tink.backend.aggregation.rpc.ConfigureWhitelistInformationRequest;
import se.tink.backend.aggregation.rpc.CreateBeneficiaryCredentialsRequest;
import se.tink.backend.aggregation.rpc.KeepAliveRequest;
import se.tink.backend.aggregation.rpc.ReEncryptCredentialsRequest;
import se.tink.backend.aggregation.rpc.RecurringPaymentRequest;
import se.tink.backend.aggregation.rpc.RefreshWhitelistInformationRequest;
import se.tink.backend.aggregation.rpc.SecretsNamesValidationRequest;
import se.tink.backend.aggregation.rpc.SecretsNamesValidationResponse;
import se.tink.backend.aggregation.rpc.SupplementInformationRequest;
import se.tink.backend.aggregation.rpc.TransferRequest;
import se.tink.libraries.credentials.service.CreateCredentialsRequest;
import se.tink.libraries.credentials.service.ManualAuthenticateRequest;
import se.tink.libraries.credentials.service.RefreshInformationRequest;
import se.tink.libraries.credentials.service.UpdateCredentialsRequest;
import se.tink.libraries.http.annotations.auth.AllowAnonymous;

@Path("/aggregation")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public interface AggregationService {
    @POST
    @Path("create")
    @TeamOwnership(Team.AGGREGATION)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    Credentials createCredentials(
            CreateCredentialsRequest request, @ClientContext ClientInfo clientInfo);

    @GET
    @Path("ping")
    @TeamOwnership(Team.AGGREGATION)
    @Produces(MediaType.TEXT_PLAIN)
    @AllowAnonymous
    String ping();

    @GET
    @Path("started")
    @TeamOwnership(Team.AGGREGATION_AGENT_PLATFORM)
    @Produces(MediaType.TEXT_PLAIN)
    @AllowAnonymous
    String started();

    @POST
    @Path("configure/whitelist")
    @TeamOwnership(Team.AGGREGATION)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    void configureWhitelistInformation(
            ConfigureWhitelistInformationRequest request, @ClientContext ClientInfo clientInfo)
            throws Exception;

    @POST
    @Path("refresh/whitelist")
    @TeamOwnership(Team.AGGREGATION)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    void refreshWhitelistInformation(
            RefreshWhitelistInformationRequest request, @ClientContext ClientInfo clientInfo)
            throws Exception;

    @POST
    @Path("refresh")
    @TeamOwnership(Team.AGGREGATION)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    void refreshInformation(RefreshInformationRequest request, @ClientContext ClientInfo clientInfo)
            throws Exception;

    @POST
    @Path("authenticate")
    @TeamOwnership(Team.AGGREGATION)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    void authenticate(ManualAuthenticateRequest request, @ClientContext ClientInfo clientInfo)
            throws Exception;

    @POST
    @Path("transfer")
    @TeamOwnership(Team.PAYMENTS)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    void transfer(TransferRequest request, @ClientContext ClientInfo clientInfo) throws Exception;

    @POST
    @Path("payment")
    @TeamOwnership(Team.PAYMENTS)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    void payment(TransferRequest request, @ClientContext ClientInfo clientInfo);

    @POST
    @Path("recurringPayment")
    @TeamOwnership(Team.PAYMENTS)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    void recurringPayment(RecurringPaymentRequest request, @ClientContext ClientInfo clientInfo);

    @POST
    @Path("transfer/whitelist")
    @TeamOwnership(Team.PAYMENTS)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    void whitelistedTransfer(
            WhitelistedTransferRequest request, @ClientContext ClientInfo clientInfo)
            throws Exception;

    @POST
    @Path("keepalive")
    @TeamOwnership(Team.AGGREGATION)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    void keepAlive(KeepAliveRequest request, @ClientContext ClientInfo clientInfo) throws Exception;

    @PUT
    @Path("update")
    @TeamOwnership(Team.AGGREGATION)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    Credentials updateCredentials(
            UpdateCredentialsRequest request, @ClientContext ClientInfo clientInfo);

    @POST
    @Path("rateLimits/auto")
    @TeamOwnership(Team.AGGREGATION)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    void updateRateLimits(ChangeProviderRateLimitsRequest request);

    @POST
    @Path("supplemental")
    @TeamOwnership(Team.AGGREGATION)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    void setSupplementalInformation(SupplementInformationRequest request);

    @POST
    @Path("reencrypt/credentials")
    @TeamOwnership(Team.AGGREGATION)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    Response reEncryptCredentials(
            ReEncryptCredentialsRequest request, @ClientContext ClientInfo clientInfo);

    @GET
    @Path("secrets-template/{providerName}")
    @TeamOwnership(Team.INTEGRATION)
    @Produces(MediaType.APPLICATION_JSON)
    String getSecretsTemplate(
            @PathParam("providerName") String providerName,
            @DefaultValue("true") @QueryParam("includeDescriptions") boolean includeDescriptions,
            @DefaultValue("true") @QueryParam("includeExamples") boolean includeExamples,
            @ClientContext ClientInfo clientInfo);

    @GET
    @Path("secrets-json-schema/{providerName}")
    @TeamOwnership(Team.AGGREGATION_THIRD_PARTY_ACCESS)
    @Produces(MediaType.APPLICATION_JSON)
    String getSecretsJsonSchema(
            @PathParam("providerName") String providerName, @ClientContext ClientInfo clientInfo);

    @POST
    @Path("validate-secrets-names")
    @TeamOwnership(Team.INTEGRATION)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    SecretsNamesValidationResponse validateSecretsNames(
            SecretsNamesValidationRequest request, @ClientContext ClientInfo clientInfo);

    @POST
    @Path("beneficiary")
    @TeamOwnership(Team.PAYMENTS)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    void createBeneficiary(
            CreateBeneficiaryCredentialsRequest request, @ClientContext ClientInfo clientInfo)
            throws Exception;
}
