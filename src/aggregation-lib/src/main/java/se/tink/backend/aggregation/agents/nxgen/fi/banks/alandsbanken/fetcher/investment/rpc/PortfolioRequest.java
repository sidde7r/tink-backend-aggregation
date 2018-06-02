package se.tink.backend.aggregation.agents.nxgen.fi.banks.alandsbanken.fetcher.investment.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PortfolioRequest {

    private String accountId;

    public String getAccountId() {
        return accountId;
    }

    public static PortfolioRequest withAccountId(String accountId) {
        PortfolioRequest request = new PortfolioRequest();
        request.accountId = accountId;
        return request;
    }
}
