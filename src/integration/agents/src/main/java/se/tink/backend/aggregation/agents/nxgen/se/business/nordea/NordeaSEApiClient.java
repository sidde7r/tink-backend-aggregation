package se.tink.backend.aggregation.agents.nxgen.se.business.nordea;

import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.nxgen.se.business.nordea.NordeaSEConstants.Headers;
import se.tink.backend.aggregation.agents.nxgen.se.business.nordea.NordeaSEConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.se.business.nordea.NordeaSEConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.se.business.nordea.authenticator.rpc.FetchTokenRequest;
import se.tink.backend.aggregation.agents.nxgen.se.business.nordea.authenticator.rpc.FetchTokenResponse;
import se.tink.backend.aggregation.agents.nxgen.se.business.nordea.authenticator.rpc.InitBankIdRequest;
import se.tink.backend.aggregation.agents.nxgen.se.business.nordea.authenticator.rpc.InitBankIdResponse;
import se.tink.backend.aggregation.agents.nxgen.se.business.nordea.authenticator.rpc.ResultBankIdResponse;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class NordeaSEApiClient {
    private final TinkHttpClient client;
    private final SessionStorage sessionStorage;

    public NordeaSEApiClient(TinkHttpClient client, SessionStorage sessionStorage) {
        this.client = client;
        this.sessionStorage = sessionStorage;
    }

    public InitBankIdResponse initBankId(InitBankIdRequest initiBankIdRequest) {
        return client.request(Urls.INIT_BANKID)
                .headers(NordeaSEConstants.NORDEA_CUSTOM_HEADERS)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .accept(MediaType.WILDCARD_TYPE)
                .post(InitBankIdResponse.class, initiBankIdRequest);
    }

    public ResultBankIdResponse resultBankId(String reference) {
        return client.request(Urls.POLL_BANKID + reference)
                .headers(NordeaSEConstants.NORDEA_CUSTOM_HEADERS)
                .header(Headers.SECURITY_TOKEN, sessionStorage.get(StorageKeys.SECURITY_TOKEN))
                .accept(MediaType.WILDCARD_TYPE)
                .get(ResultBankIdResponse.class);
    }

    public FetchTokenResponse fetchToken(FetchTokenRequest fetchTokenRequest) {
        return client.request(Urls.FETCH_TOKEN)
                .headers(NordeaSEConstants.NORDEA_CUSTOM_HEADERS)
                .header(Headers.SECURITY_TOKEN, sessionStorage.get(StorageKeys.SECURITY_TOKEN))
                .type(MediaType.APPLICATION_JSON_TYPE)
                .accept(MediaType.WILDCARD_TYPE)
                .post(FetchTokenResponse.class, fetchTokenRequest);
    }
}
