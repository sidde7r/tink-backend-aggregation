package se.tink.backend.aggregation.agents.nxgen.se.banks.sbab;

import java.util.NoSuchElementException;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.SBABConstants.FormKeys;
import se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.SBABConstants.FormValues;
import se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.SBABConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.SBABConstants.HrefKeys;
import se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.SBABConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.SBABConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.authenticator.rpc.InitBankIdResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.authenticator.rpc.PollBankIdResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.rpc.AccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.rpc.ContactInfoResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.rpc.LinksEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.rpc.StandardResponse;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.form.Form;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class SBABApiClient {
    private final TinkHttpClient client;
    private final SessionStorage sessionStorage;
    private AccountsResponse accountsResponse;

    public SBABApiClient(TinkHttpClient client, SessionStorage sessionStorage) {
        this.client = client;
        this.sessionStorage = sessionStorage;
    }

    public StandardResponse fetchAuthEndpoint() {
        final RequestBuilder request = client.request(Urls.BASE_URL);

        return sendGetRequest(request, StandardResponse.class);
    }

    public InitBankIdResponse initBankId(StandardResponse standardResponse) {
        final String initEndpoint = getEndpoint(standardResponse, HrefKeys.AUTHORIZE);
        final RequestBuilder request = client.request(Urls.HOST + initEndpoint);

        return sendGetRequest(request, InitBankIdResponse.class);
    }

    public PollBankIdResponse pollBankId(InitBankIdResponse reference) {
        final String pollEndpoint = getEndpoint(reference, HrefKeys.TOKEN);
        final String pendingCode =
                reference.getPendingAuthCodeResponse().getPendingAuthorizationCode();
        final Form form =
                Form.builder()
                        .put(FormKeys.CLIENT_ID, FormValues.CLIENT_ID)
                        .put(FormKeys.PENDING_CODE, pendingCode)
                        .put(FormKeys.GRANT_TYPE, FormValues.GRANT_TYPE)
                        .build();
        final String formAsString = form.serialize();
        final RequestBuilder request =
                client.request(Urls.HOST + pollEndpoint)
                        .body(formAsString, MediaType.APPLICATION_FORM_URLENCODED);

        return sendPostRequest(request, PollBankIdResponse.class);
    }

    public AccountsResponse fetchAccounts() {
        final String accountsEndpoint = sessionStorage.get(StorageKeys.ACCOUNTS_ENDPOINT);
        final String bearerToken = sessionStorage.get(StorageKeys.BEARER_TOKEN);
        final RequestBuilder request =
                client.request(Urls.HOST + accountsEndpoint)
                        .header(HeaderKeys.AUTHORIZATION, bearerToken);
        if (accountsResponse == null) {
            this.accountsResponse = sendGetRequest(request, AccountsResponse.class);
        }

        return accountsResponse;
    }

    public ContactInfoResponse fetchContactInfo() {
        final String contactInfoEndpoint = sessionStorage.get(StorageKeys.CONTACT_INFO_ENDPOINT);
        final String bearerToken = sessionStorage.get(StorageKeys.BEARER_TOKEN);
        final RequestBuilder request =
                client.request(Urls.HOST + contactInfoEndpoint)
                        .header(HeaderKeys.AUTHORIZATION, bearerToken);

        return sendGetRequest(request, ContactInfoResponse.class);
    }

    public String getEndpoint(StandardResponse standardResponse, String hrefKey) {
        return standardResponse.getLinks().stream()
                .filter(a -> hrefKey.equalsIgnoreCase(a.getRel()))
                .findAny()
                .map(LinksEntity::getHref)
                .orElseThrow(
                        () ->
                                new NoSuchElementException(
                                        String.format("Could not find %s endpoint", hrefKey)));
    }

    private <T> T sendGetRequest(RequestBuilder request, Class<T> responseType) {
        return request.accept(MediaType.APPLICATION_JSON_TYPE)
                .headers(SBABConstants.HEADERS)
                .get(responseType);
    }

    private <T> T sendPostRequest(RequestBuilder request, Class<T> responseType) {
        return request.accept(MediaType.APPLICATION_JSON_TYPE)
                .headers(SBABConstants.HEADERS)
                .post(responseType);
    }
}
