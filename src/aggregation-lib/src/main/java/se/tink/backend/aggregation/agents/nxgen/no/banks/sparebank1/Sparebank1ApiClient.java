package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1;

import com.google.common.base.Objects;
import java.util.Optional;
import javax.ws.rs.core.MediaType;
import org.apache.http.cookie.Cookie;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.authenticator.rpc.authentication.FinishAuthenticationRequest;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.authenticator.rpc.authentication.FinishAuthenticationResponse;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.authenticator.rpc.authentication.InitiateAuthenticationRequest;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.authenticator.rpc.authentication.InitiateAuthenticationResponse;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.authenticator.rpc.useractivation.AgreementsResponse;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.authenticator.rpc.useractivation.FinishActivationRequest;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.authenticator.rpc.useractivation.FinishActivationResponse;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.authenticator.rpc.useractivation.InitBankIdBody;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.authenticator.rpc.useractivation.InitLoginBody;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.authenticator.rpc.useractivation.TargetUrlRequest;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.authenticator.rpc.useractivation.TargetUrlResponse;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;

public class Sparebank1ApiClient {
    private TinkHttpClient client;

    public Sparebank1ApiClient(TinkHttpClient client) {
        this.client = client;
    }

    public <T> T get(String url, Class<T> responseClass) {
        return get(new URL(url), responseClass);
    }

    public <T> T get(URL url, Class<T> responseClass) {
        return client.request(url).get(responseClass);
    }

    public HttpResponse postLoginInformation(URL url, InitLoginBody loginBody) {
        return client.request(url)
                .header("Origin", Sparebank1Constants.BASE_LOGIN)
                .type(MediaType.APPLICATION_FORM_URLENCODED)
                .post(HttpResponse.class, loginBody);
    }

    public String initBankIdLogin(URL url, InitBankIdBody bankIdBody) {
        return client.request(url)
                .header("Origin", Sparebank1Constants.BASE_LOGIN)
                .type(MediaType.APPLICATION_FORM_URLENCODED)
                .post(String.class, bankIdBody);
    }

    public HttpResponse pollBankId(URL url) {
        return client.request(url)
                .header("X-Requested-With", "XMLHttpRequest")
                .header("Origin", Sparebank1Constants.BASE_LOGIN)
                .type(MediaType.APPLICATION_JSON)
                .post(HttpResponse.class);
    }

    public HttpResponse continueActivation(URL url) {
        return client.request(url)
                .accept(Sparebank1Constants.TEXT_HTML_APPLICATION_XHTML_XML)
                .get(HttpResponse.class);
    }

    public AgreementsResponse getAgreement(URL url) {
        return client.request(url)
                .accept(MediaType.WILDCARD)
                .header("X-Requested-With", "XMLHttpRequest")
                .get(AgreementsResponse.class);
    }

    public TargetUrlResponse finishAgreementSession(URL url, TargetUrlRequest request, String bankName) {
        // Find the cookie DSESSIONID cookie, need to set the X-CSRFToken header to the value of this cookie.
        Cookie dSessionIdCookie = getDSessionIdCookie();

        return client.request(url)
                .header("Origin", Sparebank1Constants.BASE)
                .header("X-Requested-With", "XMLHttpRequest")
                .header("Referer", Sparebank1Constants.BASE + bankName +
                        Sparebank1Constants.REFERER_FOR_FINISH_AGREEMENT_SESSION)
                .header("X-CSRFToken", dSessionIdCookie.getValue())
                .accept(MediaType.WILDCARD)
                .type(MediaType.APPLICATION_JSON)
                .put(TargetUrlResponse.class, request);
    }

    public FinishActivationResponse finishActivation(String url, FinishActivationRequest request) {
        // Find the cookie DSESSIONID cookie, need to set the X-CSRFToken header to the value of this cookie.
        Cookie dSessionIdCookie = getDSessionIdCookie();

        return client.request(url)
                .header("X-CSRFToken", dSessionIdCookie.getValue())
                .header("X-SB1-Rest-Version", Sparebank1Constants.X_SB1_REST_VERSION)
                .accept(Sparebank1Constants.APPLICATION_JSON_CHARSET_UTF8)
                .type(MediaType.APPLICATION_JSON)
                .post(FinishActivationResponse.class, request);
    }

    public InitiateAuthenticationResponse initAuthentication(String url, InitiateAuthenticationRequest request) {
        return client.request(url)
                .header("X-SB1-Rest-Version", Sparebank1Constants.X_SB1_REST_VERSION)
                .accept(Sparebank1Constants.APPLICATION_JSON_CHARSET_UTF8)
                .type(MediaType.APPLICATION_JSON)
                .post(InitiateAuthenticationResponse.class, request);
    }

    public FinishAuthenticationResponse finishAuthentication(String url, FinishAuthenticationRequest request) {
        // Find the cookie DSESSIONID cookie, need to set the X-CSRFToken header to the value of this cookie.
        Cookie dSessionIdCookie = getDSessionIdCookie();

        return client.request(url)
                .header("X-CSRFToken", dSessionIdCookie.getValue())
                .header("X-SB1-Rest-Version", Sparebank1Constants.X_SB1_REST_VERSION)
                .accept(Sparebank1Constants.APPLICATION_JSON_CHARSET_UTF8)
                .type(MediaType.APPLICATION_JSON)
                .put(FinishAuthenticationResponse.class, request);
    }

    public HttpResponse logout(String url) {
        // Find the cookie DSESSIONID cookie, need to set the X-CSRFToken header to the value of this cookie.
        Cookie dSessionIdCookie = getDSessionIdCookie();

        return client.request(url)
                .header("X-CSRFToken", dSessionIdCookie.getValue())
                .header("X-SB1-Rest-Version", Sparebank1Constants.X_SB1_REST_VERSION)
                .accept(Sparebank1Constants.APPLICATION_JSON_CHARSET_UTF8)
                .type(MediaType.APPLICATION_JSON)
                .delete(HttpResponse.class);
    }

    private Cookie getDSessionIdCookie() {
        Optional<Cookie> dSessionIdCookie = client.getCookies().stream()
                .filter(cookie -> Objects.equal(cookie.getName().toLowerCase(), "dsessionid"))
                .findFirst();

        if (!dSessionIdCookie.isPresent()) {
            throw new IllegalStateException("DSESSIONID cookie is not present");
        }

        return dSessionIdCookie.get();
    }
}
