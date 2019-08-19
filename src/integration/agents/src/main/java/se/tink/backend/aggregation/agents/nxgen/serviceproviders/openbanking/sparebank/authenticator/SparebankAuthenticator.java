package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.authenticator;

import static io.vavr.Predicates.not;

import com.google.common.base.Strings;
import io.vavr.control.Try;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.SparebankApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.authenticator.rpc.ScaResponse;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;

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
                .getOrElseThrow(() -> new IllegalStateException("SCA redirect missing"));
    }

    private Try<String> maybeGetRedirectUri(HttpResponseException e) {
        return e.getResponse().getBody(String.class).contains("scaRedirect")
                ? Try.success(e.getResponse().getBody(ScaResponse.class).getRedirectUri())
                : Try.failure(e);
    }

    void setUpPsuAndSession(String psuId, String tppSessionId) {
        apiClient.setPsuId(psuId);
        apiClient.setTppSessionId(tppSessionId);
    }
}
