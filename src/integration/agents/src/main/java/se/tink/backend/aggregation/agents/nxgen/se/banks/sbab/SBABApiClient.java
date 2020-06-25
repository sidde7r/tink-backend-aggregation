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
import se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.rpc.LinksEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.rpc.StandardResponse;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.form.Form;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class SBABApiClient {
    private final TinkHttpClient client;
    private final SessionStorage sessionStorage;

    public SBABApiClient(TinkHttpClient client, SessionStorage sessionStorage) {
        this.client = client;
        this.sessionStorage = sessionStorage;
    }

    public StandardResponse fetchAuthEndpoint() {
        return client.request(Urls.BASE_URL)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .headers(SBABConstants.HEADERS)
                .get(StandardResponse.class);
    }

    public InitBankIdResponse initBankId(StandardResponse standardResponse) {
        final String initEndpoint = getEndpoint(standardResponse, HrefKeys.AUTHORIZE);

        return client.request(Urls.HOST + initEndpoint)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .headers(SBABConstants.HEADERS)
                .get(InitBankIdResponse.class);
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

        return client.request(Urls.HOST + pollEndpoint)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .headers(SBABConstants.HEADERS)
                .body(formAsString, MediaType.APPLICATION_FORM_URLENCODED)
                .post(PollBankIdResponse.class);
    }

    public AccountsResponse fetchAccounts() {
        final String accountsEndpoint = sessionStorage.get(StorageKeys.ACCOUNTS_ENDPOINT);
        final String bearerToken = sessionStorage.get(StorageKeys.BEARER_TOKEN);

        return client.request(Urls.HOST + accountsEndpoint)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .headers(SBABConstants.HEADERS)
                .header(HeaderKeys.AUTHORIZATION, bearerToken)
                .get(AccountsResponse.class);
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
}
