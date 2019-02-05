package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.fetcher.investment.rpc;

import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.EuroInformationConstants.RequestBodyValues.SECURITY_ACCOUNT;

public class InvestmentAccountOverviewRequest extends InvestmentAccountsListRequest {
    public InvestmentAccountOverviewRequest(int page, String accountNumber) {
        super(page);
        this.put(SECURITY_ACCOUNT, accountNumber);
    }
}
