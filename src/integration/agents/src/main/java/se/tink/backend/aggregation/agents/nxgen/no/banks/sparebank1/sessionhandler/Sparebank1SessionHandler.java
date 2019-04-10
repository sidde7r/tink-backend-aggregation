package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.sessionhandler;

import com.google.api.client.http.HttpStatusCodes;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.Sparebank1ApiClient;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.Sparebank1Constants;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.authenticator.rpc.authentication.RestRootResponse;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;

public class Sparebank1SessionHandler implements SessionHandler {
    private static final AggregationLogger log =
            new AggregationLogger(Sparebank1SessionHandler.class);
    private final Sparebank1ApiClient apiClient;
    private final RestRootResponse restRootResponse;

    public Sparebank1SessionHandler(
            Sparebank1ApiClient apiClient, RestRootResponse restRootResponse) {
        this.apiClient = apiClient;
        this.restRootResponse = restRootResponse;
    }

    @Override
    public void logout() {
        HttpResponse response =
                apiClient.logout(
                        restRootResponse
                                .getLinks()
                                .get(Sparebank1Constants.Keys.LOGOUT_KEY)
                                .getHref());
        if (response.getStatus() != HttpStatusCodes.STATUS_CODE_NO_CONTENT) {
            log.warn(String.format("Logout failed with status: %d", response.getStatus()));
        }
    }

    @Override
    public void keepAlive() throws SessionException {
        try {
            HttpResponse response =
                    apiClient.get(
                            restRootResponse
                                    .getLinks()
                                    .get(Sparebank1Constants.Keys.KEEP_ALIVE_KEY)
                                    .getHref(),
                            HttpResponse.class);

            if (response.getStatus() != HttpStatusCodes.STATUS_CODE_OK) {
                throw SessionError.SESSION_EXPIRED.exception();
            }
        } catch (HttpResponseException e) {
            throw SessionError.SESSION_EXPIRED.exception();
        }
    }
}
