package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.investment.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.lang.invoke.MethodHandles;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.HandelsbankenSEApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.HandelsbankenSEConstants;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.investment.rpc.FundHoldingsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.entities.HandelsbankenAmount;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.rpc.BaseResponse;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.libraries.amount.ExactCurrencyAmount;

@Slf4j
public class CustodyAccount extends BaseResponse {
    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private String type;
    private String title;
    private String custodyAccountNumber;
    private String accountNumberFormatted;
    private HandelsbankenAmount marketValue;

    public Optional<InvestmentAccount> toInvestmentAccount(HandelsbankenSEApiClient client) {
        if (type != null) {
            switch (type.toLowerCase()) {
                case HandelsbankenSEConstants.Investments.FUND:
                    return client.fundHoldings(this)
                            .map(FundHoldingsResponse::getUserFundHoldings)
                            .map(fundHoldings -> fundHoldings.toAccount(this));
                case HandelsbankenSEConstants.Investments.ISK:
                    // Intentional fall through
                case HandelsbankenSEConstants.Investments.NORMAL:
                    return client.custodyAccount(this)
                            .map(custodyAccount -> custodyAccount.toInvestmentAccount(client));
                case HandelsbankenSEConstants.Investments.KF_AND_PENSION:
                    return client.pensionDetails(this)
                            .map(
                                    pensionDetails ->
                                            pensionDetails.toInvestmentAccount(client, this));
                default:
                    // Intentional fall through
            }
        }
        logger.info(
                String.format(
                        "Not yet implemented custody account - type: %s, relative links: %s",
                        type, getLinks().keySet()));
        return Optional.empty();
    }

    public Optional<URL> getFundHoldingsUrl() {
        return searchLink(HandelsbankenConstants.URLS.Links.FUND_HOLDINGS);
    }

    public Optional<URL> getCustodyAccountUrl() {
        return searchLink(HandelsbankenConstants.URLS.Links.CUSTODY_ACCOUNT);
    }

    public Optional<URL> getPensionDetailsUrl() {
        return searchLink(HandelsbankenConstants.URLS.Links.PENSION_DETAILS);
    }

    public String getCustodyAccountNumber() {
        if (custodyAccountNumber != null) {
            log.info("Handelsbanken returned an old version of KF investment in CustodyAccounts.");
            return custodyAccountNumber;
        } else {
            log.info("Handelsbanken returned new version of KF investment in CustodyAccounts.");
            return getAccountNumberFormatted();
        }
    }

    public String getTitle() {
        return title;
    }

    public String getType() {
        return type;
    }

    public HandelsbankenAmount getMarketValue() {
        return marketValue;
    }

    @JsonIgnore
    public ExactCurrencyAmount getTinkAmount() {
        return Optional.ofNullable(marketValue)
                .map(HandelsbankenAmount::toExactCurrencyAmount)
                .orElse(ExactCurrencyAmount.zero("SEK"));
    }

    public String getAccountNumberFormatted() {
        return StringUtils.deleteWhitespace(accountNumberFormatted);
    }
}
