package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.investment.rpc;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.models.Instrument;
import se.tink.backend.aggregation.agents.models.Portfolio;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.HandelsbankenSEApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.HandelsbankenSEConstants;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.investment.entities.HandelsbankenPerformance;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.investment.entities.SecurityHoldingList;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.entities.HandelsbankenAmount;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.rpc.BaseResponse;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;
import se.tink.libraries.amount.Amount;

public class CustodyAccountResponse extends BaseResponse {
    private static final Logger LOGGER = LoggerFactory.getLogger(CustodyAccountResponse.class);

    private HandelsbankenPerformance performance;
    private HandelsbankenAmount marketValue;
    private HandelsbankenAmount mainDepositAccountBalance;
    private String accountNumberFormatted;
    private String custodyAccountNumber;
    private String iskAccountNumber;
    private String title;
    private String type;
    private List<SecurityHoldingList> holdingLists;

    public InvestmentAccount toInvestmentAccount(HandelsbankenSEApiClient client) {
        return InvestmentAccount.builder(custodyAccountNumber)
                .setAccountNumber(getAccountNumberBasedOnInvestmentType())
                .setName(title)
                .setCashBalance(Amount.inSEK(0))
                .setPortfolios(Collections.singletonList(toPortfolio(client)))
                .build();
    }

    private double toMarketValue() {
        return marketValue == null ? 0d : marketValue.asDouble();
    }

    private String getAccountNumberBasedOnInvestmentType() {
        return Portfolio.Type.ISK.equals(toType()) ? iskAccountNumber : custodyAccountNumber;
    }

    private Portfolio toPortfolio(HandelsbankenSEApiClient client) {
        Portfolio portfolio = new Portfolio();

        portfolio.setRawType(type);
        portfolio.setType(toType());
        portfolio.setTotalProfit(
                Optional.ofNullable(performance)
                        .flatMap(HandelsbankenPerformance::asDouble)
                        .orElse(null));
        portfolio.setTotalValue(toMarketValue());
        portfolio.setUniqueIdentifier(getAccountNumberBasedOnInvestmentType());
        portfolio.setInstruments(toInstruments(client));

        return portfolio;
    }

    private List<Instrument> toInstruments(HandelsbankenSEApiClient client) {
        if (holdingLists == null) {
            return Collections.emptyList();
        }
        return holdingLists.stream()
                .flatMap(securityHoldingList -> securityHoldingList.toInstruments(client))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private Portfolio.Type toType() {
        Portfolio.Type portfolioType =
                HandelsbankenSEConstants.PortfolioType.asType(this.type);

        if (portfolioType == Portfolio.Type.OTHER) {
            LOGGER.warn("Unknown portfolio type: {}", this.type);
        }

        return portfolioType;
    }
}
