package se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia;

import static se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.RuralviaConstants.Tags.ATTRIBUTE_TAG_HREF;
import static se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.RuralviaConstants.Urls.RURALVIA_SECURE_HOST;
import static se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.RuralviaConstants.Urls.RURALVIA_STILL_ALIVE;

import java.util.Set;
import javax.ws.rs.core.MediaType;
import lombok.Getter;
import lombok.Setter;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.RuralviaConstants.HeaderValues;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class RuralviaApiClient {

    private final TinkHttpClient client;
    private @Getter @Setter String globalPositionHtml;
    private @Getter @Setter String headerReferer;
    private @Getter @Setter boolean isLogged = false;

    public RuralviaApiClient(TinkHttpClient client) {
        this.client = client;
        this.client.setUserAgent(HeaderValues.USER_AGENT);
    }

    private RequestBuilder createRequest(URL url) {
        return client.request(url);
    }

    public RequestBuilder createBodyFormRequest(URL url, String formToBody) {
        return createRequest(url).body(formToBody, MediaType.APPLICATION_FORM_URLENCODED);
    }

    public boolean keepAlive() {
        if (isLogged) {
            String html = client.request(RURALVIA_STILL_ALIVE).get(String.class);
            if (html.contains("'desconectar'")
                    || !html.contains("sesion_caducada.htm")
                    || !html.contains("id=\"error_acceso\"")) {
                return true;
            }
        }
        return false;
    }

    private Cookie convertCookie(org.openqa.selenium.Cookie cookie) {
        BasicClientCookie newCookie = new BasicClientCookie(cookie.getName(), cookie.getValue());
        newCookie.setDomain(cookie.getDomain());
        newCookie.setPath(cookie.getPath());
        newCookie.setExpiryDate(cookie.getExpiry());
        return newCookie;
    }

    public void storeLoginCookies(Set<org.openqa.selenium.Cookie> cookies) {
        cookies.stream().map(this::convertCookie).forEach(client::addCookie);
    }

    public String navigateAccountTransactionFirstRequest(URL url, String form) {
        return createBodyFormRequest(url, form).post(String.class);
    }

    public String navigateAccountTransactionsBetweenDates(URL url, String form) {
        return createBodyFormRequest(url, form).post(String.class);
    }

    public String navigateToCreditCardsMovements(URL url) {
        return client.request(url).get(String.class);
    }

    public String navigateToCreditCardTransactionsByDates(URL url, String form) {
        return createBodyFormRequest(url, form).post(String.class);
    }

    public String requestTransactionsBetweenDates(URL url, String params) {
        return createBodyFormRequest(url, params).post(String.class);
    }

    public String navigateThroughLoan(URL url) {
        return client.request(url).get(String.class);
    }

    public String navigateToLoanDetails(URL url, String params) {
        return createBodyFormRequest(url, params).post(String.class);
    }

    public String navigateToLoanAmortizationTableDetails(URL url, String params) {
        return createBodyFormRequest(url, params).post(String.class);
    }

    public String navigateThroughIdentity(URL url) {
        return client.request(url).get(String.class);
    }

    public void logout() {
        Element html = Jsoup.parse(globalPositionHtml);
        Elements logout = html.select("a:has(img[name=desconectar])");
        if (!logout.isEmpty()) {
            URL url = URL.of(RURALVIA_SECURE_HOST + logout.get(0).attr(ATTRIBUTE_TAG_HREF));
            client.request(url).get(String.class);
        }
    }
}
