package se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter;

import com.google.api.client.http.HttpStatusCodes;
import java.util.Set;
import javax.ws.rs.core.MediaType;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.cookie.BasicClientCookie;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.BankinterConstants.FormKeys;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.BankinterConstants.FormValues;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.BankinterConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.BankinterConstants.HeaderValues;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.BankinterConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.BankinterConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.BankinterConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.fetcher.identitydata.rpc.IdentityDataResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.fetcher.transactionalaccount.rpc.AccountResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.fetcher.transactionalaccount.rpc.GlobalPositionResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.rpc.JsfUpdateResponse;
import se.tink.backend.aggregation.nxgen.http.Form;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.serialization.utils.SerializationUtils;

public final class BankinterApiClient {

    private final TinkHttpClient client;
    private final PersistentStorage persistentStorage;

    public BankinterApiClient(TinkHttpClient client, PersistentStorage persistentStorage) {
        this.client = client;
        this.persistentStorage = persistentStorage;
    }

    public boolean keepAlive() {
        try {
            final HttpResponse response = client.request(Urls.KEEP_ALIVE).get(HttpResponse.class);
            return HttpStatusCodes.isSuccess(response.getStatus());
        } catch (HttpResponseException hre) {
            return false;
        }
    }

    private Cookie convertCookie(org.openqa.selenium.Cookie cookie) {
        BasicClientCookie newCookie = new BasicClientCookie(cookie.getName(), cookie.getValue());
        newCookie.setDomain(cookie.getDomain());
        newCookie.setPath(cookie.getPath());
        newCookie.setExpiryDate(cookie.getExpiry());
        return newCookie;
    }

    public void storeLoginCookies(Set<org.openqa.selenium.Cookie> cookies) {
        cookies.stream()
                .map(cookie -> convertCookie(cookie))
                .forEach(cookie -> client.addCookie(cookie));
    }

    public IdentityDataResponse fetchIdentityData() {
        // response has content-type text/html, but is actually JSON
        final HttpResponse response = client.request(Urls.IDENTITY_DATA).get(HttpResponse.class);
        final String responseBody = response.getBody(String.class);
        return SerializationUtils.deserializeFromString(responseBody, IdentityDataResponse.class);
    }

    public GlobalPositionResponse fetchGlobalPosition() {
        return new GlobalPositionResponse(
                client.request(Urls.GLOBAL_POSITION).get(HttpResponse.class));
    }

    public AccountResponse fetchAccount(int accountIndex) {
        return new AccountResponse(
                client.request(Urls.ACCOUNT)
                        .queryParam(QueryKeys.ACCOUNT_INDEX, Integer.toString(accountIndex))
                        .queryParam(QueryKeys.INDEX, QueryValues.INDEX_N)
                        .get(HttpResponse.class));
    }

    public JsfUpdateResponse fetchJsfUpdate(
            String url, String source, String viewState, String render) {
        final Form.Builder formBuilder = Form.builder();
        final String submitKey = (source.split(":")[0]) + "_SUBMIT";
        formBuilder.put(submitKey, "1");
        formBuilder.put(FormKeys.JSF_VIEWSTATE, viewState);
        formBuilder.put(FormKeys.JSF_PARTIAL_AJAX, FormValues.TRUE);
        formBuilder.put(FormKeys.JSF_SOURCE, source);
        formBuilder.put(FormKeys.JSF_PARTIAL_EXECUTE, FormValues.JSF_EXECUTE_ALL);
        formBuilder.put(FormKeys.JSF_PARTIAL_RENDER, render);
        formBuilder.put(source, source);

        return new JsfUpdateResponse(
                client.request(url)
                        .header(HeaderKeys.JSF_REQUEST, HeaderValues.JSF_PARTIAL)
                        .body(
                                formBuilder.build().serialize(),
                                MediaType.APPLICATION_FORM_URLENCODED)
                        .post(HttpResponse.class));
    }
}
