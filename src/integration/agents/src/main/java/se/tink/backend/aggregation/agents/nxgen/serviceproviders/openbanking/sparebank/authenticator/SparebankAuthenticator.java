package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.authenticator;

import static io.vavr.Predicates.not;

import com.google.common.base.Strings;
import io.vavr.control.Try;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.SparebankApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.SparebankConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.authenticator.rpc.ScaResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.fetcher.transactionalaccount.rpc.AccountResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class SparebankAuthenticator {
    private final SparebankApiClient apiClient;

    public SparebankAuthenticator(SparebankApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public URL buildAuthorizeUrl(String state) {
        return Try.of(() -> apiClient.getScaRedirect(state))
                .map(ScaResponse::getRedirectUri)
                .recoverWith(HttpResponseException.class, this::maybeGetRedirectUri)
                .filter(not(Strings::isNullOrEmpty))
                .map(URL::new)
                .getOrElseThrow(
                        () ->
                                new IllegalStateException(
                                        SparebankConstants.ErrorMessages.SCA_REDIRECT_MISSING));
    }

    private Try<String> maybeGetRedirectUri(HttpResponseException e) {
        return isExceptionWithScaRedirect(e)
                ? Try.success(e.getResponse().getBody(ScaResponse.class).getRedirectUri())
                : Try.failure(e);
    }

    private boolean isExceptionWithScaRedirect(HttpResponseException e) {
        return e.getResponse().hasBody()
                && e.getResponse().getBody(String.class).contains("scaRedirect");
    }

    void setUpPsuAndSession(String psuId, String tppSessionId) {
        apiClient.setPsuId(psuId);
        apiClient.setTppSessionId(tppSessionId);
    }

    void clearSessionData() {
        apiClient.clearSessionData();
    }

    boolean psuAndSessionPresent() {
        return apiClient.getPsuId().isPresent() && apiClient.getSessionId().isPresent();
    }

    boolean isTppSessionStillValid() {
        Optional<AccountResponse> maybeAccounts = apiClient.getStoredAccounts();
        if (!maybeAccounts.isPresent() || maybeAccounts.get().getAccounts().size() == 0) {
            return false;
        }
        try {
            // ITE-1648 No other way to validate the session (that I know of) than to run true
            // operation.
            // This limits number of background refreshes we can make.
            apiClient.fetchBalances(maybeAccounts.get().getAccounts().get(0).getResourceId());
            return true;
        } catch (HttpResponseException e) {
            if (isExceptionWithScaRedirect(e)) {
                // We are sure that the session is invalid and will require full auth again
                return false;
            } else {
                // Something else gone wrong, we don't want to invalidate the session just yet
                throw e;
            }
        }
    }
}
