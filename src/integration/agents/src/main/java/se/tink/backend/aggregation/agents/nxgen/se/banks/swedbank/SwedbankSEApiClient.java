package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank;

import com.google.common.collect.ImmutableMap;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.fetchers.einvoice.rpc.EInvoiceDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.fetchers.einvoice.rpc.EInvoiceEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.fetchers.einvoice.rpc.IncomingEinvoicesResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.fetchers.investment.rpc.DetailedPensionResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.fetchers.investment.rpc.DetailedPortfolioResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.fetchers.investment.rpc.FundMarketInfoResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.fetchers.investment.rpc.PensionPortfoliosResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.fetchers.investment.rpc.PortfolioHoldingsResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.fetchers.loan.rpc.DetailedLoanResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.fetchers.loan.rpc.LoanDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.fetchers.loan.rpc.LoanOverviewResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.filter.SwedbankSeHttpFilter;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.SwedbankBaseConstants;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.SwedbankDefaultApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.SwedbankStorage;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.configuration.SwedbankConfiguration;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.LinkEntity;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;

public class SwedbankSEApiClient extends SwedbankDefaultApiClient {
    public SwedbankSEApiClient(
            TinkHttpClient client,
            SwedbankConfiguration configuration,
            SwedbankStorage swedbankStorage,
            AgentComponentProvider componentProvider) {
        super(client, configuration, swedbankStorage, componentProvider);
        this.client.addFilter(new SwedbankSeHttpFilter(configuration.getUserAgent()));
    }

    public LoanOverviewResponse loanOverview() {
        return makeMenuItemRequest(
                SwedbankBaseConstants.MenuItemKey.LOANS, LoanOverviewResponse.class);
    }

    public String loanOverviewAsString() {
        return makeMenuItemRequest(SwedbankBaseConstants.MenuItemKey.LOANS, String.class);
    }

    public String optionalRequest(LinkEntity linkEntity) {
        return makeRequest(linkEntity, String.class, true);
    }

    public LoanDetailsResponse loanDetails(LinkEntity linkEntity) {
        return makeRequest(linkEntity, LoanDetailsResponse.class, true);
    }

    public DetailedLoanResponse loadDetailsEntity(LinkEntity linkEntity) {
        return makeRequest(linkEntity, DetailedLoanResponse.class, true);
    }

    public PortfolioHoldingsResponse portfolioHoldings() {
        return makeMenuItemRequest(
                SwedbankBaseConstants.MenuItemKey.PORTFOLIOS, PortfolioHoldingsResponse.class);
    }

    public PensionPortfoliosResponse getPensionPortfolios() {
        return makeMenuItemRequest(
                SwedbankBaseConstants.MenuItemKey.PENSION_PORTFOLIOS,
                PensionPortfoliosResponse.class);
    }

    public DetailedPensionResponse detailedPensionInfo(LinkEntity linkEntity) {
        return makeRequest(linkEntity, DetailedPensionResponse.class, true);
    }

    public DetailedPortfolioResponse detailedPortfolioInfo(LinkEntity linkEntity) {
        return makeRequest(linkEntity, DetailedPortfolioResponse.class, true);
    }

    public FundMarketInfoResponse fundMarketInfo(LinkEntity linkEntity) {
        return makeRequest(linkEntity, FundMarketInfoResponse.class, true);
    }

    public FundMarketInfoResponse fundMarketInfo(String fundCode) {
        return makeMenuItemRequest(
                SwedbankBaseConstants.MenuItemKey.FUND_MARKET_INFO,
                null,
                FundMarketInfoResponse.class,
                ImmutableMap.of(SwedbankBaseConstants.ParameterKey.FUND_CODE, fundCode));
    }

    public List<EInvoiceEntity> incomingEInvoices() {
        IncomingEinvoicesResponse incomingEinvoicesResponse =
                makeMenuItemRequest(
                        SwedbankBaseConstants.MenuItemKey.EINVOICES,
                        IncomingEinvoicesResponse.class);

        return incomingEinvoicesResponse.getEinvoices();
    }

    public EInvoiceDetailsResponse eInvoiceDetails(LinkEntity linkEntity) {
        return makeRequest(linkEntity, EInvoiceDetailsResponse.class, false);
    }
}
