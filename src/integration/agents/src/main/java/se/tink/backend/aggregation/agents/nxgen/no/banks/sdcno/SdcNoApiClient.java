package se.tink.backend.aggregation.agents.nxgen.no.banks.sdcno;

import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sdcno.config.SdcNoConfiguration;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sdcno.config.SdcNoConstants;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sdcno.config.SdcNoConstants.Headers;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.authenticator.entities.SdcAgreement;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.fetcher.rpc.FilterAccountsRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.fetcher.rpc.FilterAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.fetcher.rpc.SearchTransactionsRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.fetcher.rpc.SearchTransactionsResponse;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.url.URL;

class SdcNoApiClient {
    private final TinkHttpClient client;
    private final SdcNoConfiguration agentConfig;

    SdcNoApiClient(TinkHttpClient client, SdcNoConfiguration agentConfig) {
        this.client = client;
        this.agentConfig = agentConfig;
    }

    String initWebPage() {
        return createApiRequest(
                        new URL(agentConfig.getBasePageUrl() + SdcNoConstants.MINE_KONTOER_PATH))
                .get(String.class);
    }

    void postAccountNoToBank(final String accountId, final String accountNo) {
        String formToPost =
                "accountno="
                        + accountNo
                        + "&"
                        + "fromaccount=&"
                        + "fromaccountno=&"
                        + "componentkey=&"
                        + "startdate=&"
                        + "enddate=&"
                        + "sortstring=&"
                        + "sortdirection=&"
                        + "sort=&"
                        + "orderby=&"
                        + "selecteddate=&"
                        + "changeAgreementContext=true";

        createApiRequest(
                        new URL(
                                agentConfig.getBasePageUrl()
                                        + SdcNoConstants.KONTOBEVEGELSER_PATH
                                        + accountId))
                .overrideHeader("Content-Type", "application/x-www-form-urlencoded")
                .post(formToPost);
    }

    SdcAgreement fetchAgreement() {
        return createApiRequest(
                        new URL(agentConfig.getBaseApiUrl() + SdcNoConstants.USER_AGREEMENT_PATH),
                        Headers.API_VERSION_1)
                .get(SdcAgreement.class);
    }

    FilterAccountsResponse filterAccounts(FilterAccountsRequest filterRequest) {

        return createApiRequest(
                        new URL(agentConfig.getBaseApiUrl() + SdcNoConstants.ACCOUNTS_PATH),
                        SdcNoConstants.Headers.API_VERSION_2)
                .post(FilterAccountsResponse.class, filterRequest);
    }

    SearchTransactionsResponse filterTransactionsFor(SearchTransactionsRequest searchRequest) {

        return createApiRequest(
                        new URL(
                                agentConfig.getBaseApiUrl()
                                        + SdcNoConstants.ACCOUNTS_TRANSACTION_PATH),
                        Headers.API_VERSION_3)
                .post(SearchTransactionsResponse.class, searchRequest);
    }

    private RequestBuilder createApiRequest(final URL url) {
        return client.request(url)
                .header(Headers.USER_AGENT, Headers.USER_AGENT_VALUE)
                .accept(MediaType.WILDCARD)
                .type(MediaType.APPLICATION_JSON);
    }

    private RequestBuilder createApiRequest(final URL url, final String apiVersion) {
        return createApiRequest(url)
                .header(Headers.X_SDC_API_VERSION, apiVersion)
                .header(Headers.X_SDC_CLIENT_TYPE, Headers.CLIENT_TYPE)
                .header(Headers.X_SDC_LOCALE, Headers.LOCALE_EN);
    }
}
