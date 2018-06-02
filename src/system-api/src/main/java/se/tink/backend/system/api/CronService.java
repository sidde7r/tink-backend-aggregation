package se.tink.backend.system.api;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import se.tink.api.annotations.Team;
import se.tink.api.annotations.TeamOwnership;
import se.tink.backend.rpc.RefreshCredentialSchedulationRequest;
import se.tink.backend.system.rpc.SendMonthlyEmailsRequest;
import se.tink.backend.system.rpc.UpdateFacebookProfilesRequest;

@Path("/cron")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public interface CronService {
    @POST
    @Path("/credentials/refresh")
    @TeamOwnership(Team.DATA)
    @Produces(MediaType.APPLICATION_JSON)
    Response refreshCredentials(RefreshCredentialSchedulationRequest request);

    @POST
    @Path("/credentials/resetHangingCredentials")
    @TeamOwnership(Team.DATA)
    @Produces(MediaType.APPLICATION_JSON)
    Response resetHangingCredentials();

    @POST
    @Path("/credentials/refreshFailed")
    @TeamOwnership(Team.DATA)
    @Produces(MediaType.APPLICATION_JSON)
    Response refreshFailedCredentials();

    @POST
    @Path("/credentials/refreshChangedFraud")
    @TeamOwnership(Team.DATA)
    @Produces(MediaType.APPLICATION_JSON)
    Response refreshChangedFraudCredentials();

    @POST
    @Path("/statistics/report")
    @TeamOwnership(Team.DATA)
    @Produces(MediaType.APPLICATION_JSON)
    Response reportSystemStatistics();

    @POST
    @Path("/notifications/sendUnsent")
    @TeamOwnership(Team.DATA)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    Response sendUnsentNotifications();

    @POST
    @Path("/notifications/sendFallback")
    @TeamOwnership(Team.DATA)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    Response sendFallbackNotifications();

    @POST
    @Path("/facebook/updateProfiles")
    @TeamOwnership(Team.DATA)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    Response updateFacebookProfiles(UpdateFacebookProfilesRequest request);

    @POST
    @Path("/credentials/relabelOldAuthErrors")
    @TeamOwnership(Team.DATA)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    Response relabelOldAuthErrors();

    @POST
    @Path("/credentials/sendMessageForFailing")
    @TeamOwnership(Team.DATA)
    @Produces(MediaType.APPLICATION_JSON)
    Response sendMessageForFailingCredentials();

    @POST
    @Path("/credentials/sendFraudReminder")
    @TeamOwnership(Team.PFM)
    @Produces(MediaType.APPLICATION_JSON)
    Response sendFraudReminder();

    @POST
    @Path("/credentials/sendManualRefreshReminder")
    @TeamOwnership(Team.DATA)
    @Produces(MediaType.APPLICATION_JSON)
    Response sendManualRefreshReminder();

    @POST
    @Path("/user/sendActivationReminder")
    @TeamOwnership(Team.DATA)
    @Produces(MediaType.APPLICATION_JSON)
    Response sendUserActivationReminder();

    @POST
    @Path("/user/cleanOAuth2ClientsUsers")
    @TeamOwnership(Team.GROWTH)
    @Produces(MediaType.APPLICATION_JSON)
    Response cleanOAuth2Users();

    @POST
    @Path("/credentials/sendPaydayReminders")
    @TeamOwnership(Team.PFM)
    @Produces(MediaType.APPLICATION_JSON)
    Response sendPaydayReminders();

    @POST
    @Path("/user/sendMonthlySummaryEmails")
    @TeamOwnership(Team.PFM)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    Response sendMonthlySummaryEmails(SendMonthlyEmailsRequest request);

    @POST
    @Path("/user/deletePartiallyDeletedUsers")
    @TeamOwnership(Team.PFM)
    @Produces(MediaType.APPLICATION_JSON)
    Response deletePartiallyDeletedUsers();

    @POST
    @Path("/applications/refresh")
    @TeamOwnership(Team.DATA)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    Response refreshApplications();

    @POST
    @Path("/applications/reporting")
    @TeamOwnership(Team.PFM)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    Response applicationsReporting();

    @POST
    @Path("/products/refresh")
    @TeamOwnership(Team.PFM)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    Response refreshProducts();

    @POST
    @Path("/transactions/detectIndexDiverge")
    @TeamOwnership(Team.DATA)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    void detectTransactionIndexDivergence();

    @POST
    @Path("/authentication/cleanup")
    @TeamOwnership(Team.GROWTH)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    void authenticationCleanup();

    @POST
    @Path("/accounts/calculateBalance")
    @TeamOwnership(Team.DATA)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    void calculateAccountBalance();

    @POST
    @Path("/fasttext/trainModel")
    @TeamOwnership(Team.DATA)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    void trainModel();
}
