package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.entities;

import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.HandelsbankenSEApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.rpc.FundHoldingsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.entities.Link;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.fetcher.entities.HandelsbankenAmount;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.rpc.BaseResponse;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.core.account.InvestmentAccount;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.rpc.Credentials;
import se.tink.backend.core.Amount;

public class CustodyAccount extends BaseResponse {
    private static final AggregationLogger log = new AggregationLogger(CustodyAccount.class);

    private String type;
    private String title;
    private String custodyAccountNumber;
    private HandelsbankenAmount marketValue;

    public Optional<InvestmentAccount> toInvestmentAccount(HandelsbankenSEApiClient client,
            Credentials credentials) {
        if (type != null) {
            switch (type.toLowerCase()) {
            case "fund_summary":
                return client.fundHoldings(this)
                        .map(FundHoldingsResponse::getUserFundHoldings)
                        .map(fundHoldings -> fundHoldings.toAccount(this));
            case "isk":
                // Intentional fall through
            case "normal":
                return client.custodyAccount(this).map(custodyAccount -> custodyAccount.toInvestmentAccount(client));
            case "kapital":
                return client.pensionDetails(this)
                        .map(pensionDetails -> pensionDetails.toInvestmentAccount(client, this));
            default:
                // Intentional fall through
            }
        }
        log.info(String.format("Not yet implemented custody account - type: %s, relative links: %s",
                        type, getLinks().stream().map(Link::getRel).collect(Collectors.toList())));
        return Optional.empty();
    }

    public Optional<URL> toFundHoldings() {
        return searchLink(HandelsbankenConstants.URLS.Links.FUND_HOLDINGS);
    }

    public Optional<URL> toCustodyAccount() {
        return searchLink(HandelsbankenConstants.URLS.Links.CUSTODY_ACCOUNT);
    }

    public Optional<URL> toPensionDetails() {
        return searchLink(HandelsbankenConstants.URLS.Links.PENSION_DETAILS);
    }

    public String getCustodyAccountNumber() {
        return custodyAccountNumber;
    }

    public String getTitle() {
        return title;
    }

    public String getType() {
        return type;
    }

    public Amount getTinkAmount() {
        return Optional.ofNullable(marketValue).map(HandelsbankenAmount::asAmount).orElse(Amount.inSEK(0.0));
    }
}
