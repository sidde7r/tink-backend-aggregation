package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.investment.rpc;

public class ListSecuritiesRequest {
    private final String custodyAccount;

    private ListSecuritiesRequest(String custodyAccount) {
        this.custodyAccount = custodyAccount;
    }

    public static ListSecuritiesRequest createFromCustodyAccount(String custodyAccount) {
        return new ListSecuritiesRequest(custodyAccount);
    }

    public String getCustodyAccount() {
        return custodyAccount;
    }
}
