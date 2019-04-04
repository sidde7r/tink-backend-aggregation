package se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordnet;

import javax.ws.rs.core.MediaType;
import javax.xml.bind.DatatypeConverter;

import se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordnet.fetcher.transactionalaccount.rpc.GetAccountDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordnet.fetcher.transactionalaccount.rpc.GetAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordnet.authenticator.rpc.GetSessionForm;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordnet.authenticator.rpc.GetSessionKeyResponse;
import se.tink.backend.aggregation.nxgen.http.*;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public final class NordnetApiClient {
    private final TinkHttpClient client;
    private final SessionStorage sessionStorage;

    public NordnetApiClient(TinkHttpClient client, SessionStorage sessionStorage) {
        this.client = client;
        this.sessionStorage = sessionStorage;
    }

    private RequestBuilder createRequest(URL url) {
        return client.request(url)
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON);
    }

    public GetSessionKeyResponse getSessionKey(String encodedAuthParam) {

        GetSessionForm form =
                GetSessionForm.builder()
                        .setAuth(encodedAuthParam)
                        .setService(NordnetConstants.FormValues.SERVICE)
                        .build();

        return createRequest(new URL(NordnetConstants.Urls.LOGIN_PATH))
                .body(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE)
                .accept(MediaType.APPLICATION_JSON)
                .post(GetSessionKeyResponse.class);
    }

    public GetAccountsResponse getAccounts() {
        return createRequest(new URL(NordnetConstants.Urls.GET_ACCOUNTS_PATH))
                .header(NordnetConstants.HeaderKeys.AUTHORIZATION, getKey())
                .accept(MediaType.APPLICATION_JSON)
                .get(GetAccountsResponse.class);
    }

    public void getAccount(long accountNumber) {
        GetAccountDetailsResponse response =
                createRequest(
                                new URL(NordnetConstants.Urls.GET_ACCOUNT_DETAILS_PATH)
                                        .parameter(
                                                NordnetConstants.IdTags.ACCOUNT_NUMBER,
                                                String.valueOf(accountNumber)))
                        .header(NordnetConstants.HeaderKeys.AUTHORIZATION, getKey())
                        .accept(MediaType.APPLICATION_JSON)
                        .get(GetAccountDetailsResponse.class);
    }

    private String getKey() {
        String sessionKey = sessionStorage.get(NordnetConstants.StorageKeys.SESSION_KEY);
        return NordnetConstants.HeaderValues.AUTHORIZATION_PREFIX
                + DatatypeConverter.printBase64Binary((sessionKey + ":" + sessionKey).getBytes());
    }
}
