package se.tink.backend.aggregation.agents.nxgen.se.banks.minpension;

import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.nxgen.se.banks.minpension.MinPensionConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.se.banks.minpension.MinPensionConstants.HeaderValues;
import se.tink.backend.aggregation.agents.nxgen.se.banks.minpension.MinPensionConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.se.banks.minpension.authenticator.rpc.BankIdRequest;
import se.tink.backend.aggregation.agents.nxgen.se.banks.minpension.authenticator.rpc.InitBankIdResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.minpension.authenticator.rpc.PollBankIdResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.minpension.authenticator.rpc.UserTOCResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.minpension.fetcher.pension.rpc.PensionAccountsResponse;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class MinPensionApiClient {
    private final TinkHttpClient client;
    private final SessionStorage sessionStorage;

    public MinPensionApiClient(TinkHttpClient client, SessionStorage sessionStorage) {
        this.client = client;
        this.sessionStorage = sessionStorage;
    }

    public InitBankIdResponse initBankid() {
        return client.request(Urls.INIT_BANKID)
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON)
                .header(HeaderKeys.USER_AGENT, HeaderValues.USER_AGENT)
                .post(InitBankIdResponse.class, new BankIdRequest());
    }

    public PollBankIdResponse pollBankId(String orderRef) {
        return client.request(Urls.POLL_BANKID)
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON)
                .header(HeaderKeys.USER_AGENT, HeaderValues.USER_AGENT)
                .post(PollBankIdResponse.class, new BankIdRequest(orderRef));
    }

    public UserTOCResponse fetchUserTOCStatus() {
        return client.request(Urls.FETCH_USER)
                .accept(MediaType.APPLICATION_JSON)
                .header(HeaderKeys.USER_AGENT, HeaderValues.USER_AGENT)
                .options(UserTOCResponse.class);
    }

    public String fetchSsn() {
        return client.request(Urls.FETCH_SSN)
                .accept(MediaType.TEXT_PLAIN)
                .header(HeaderKeys.USER_AGENT, HeaderValues.USER_AGENT)
                .get(String.class);
    }

    public PensionAccountsResponse fetchPensionAccounts() {
        return client.request(Urls.FETCH_PENSION_ACCOUNTS)
                .accept(MediaType.APPLICATION_JSON)
                .header(HeaderKeys.USER_AGENT, HeaderValues.USER_AGENT)
                .get(PensionAccountsResponse.class);
    }
}
