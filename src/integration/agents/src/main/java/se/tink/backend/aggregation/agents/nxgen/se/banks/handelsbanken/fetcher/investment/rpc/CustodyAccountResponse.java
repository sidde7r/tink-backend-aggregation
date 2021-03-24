package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.investment.rpc;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.models.Portfolio;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.HandelsbankenSEApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.HandelsbankenSEConstants;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.HandelsbankenSEConstants.Currency;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.investment.entities.HandelsbankenPerformance;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.investment.entities.SecurityHoldingList;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.entities.HandelsbankenAmount;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.rpc.BaseResponse;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.instrument.InstrumentModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.portfolio.PortfolioModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.portfolio.PortfolioModule.PortfolioType;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;

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

        return InvestmentAccount.nxBuilder()
                .withPortfolios(Collections.singletonList(toPortfolioModule(client)))
                .withZeroCashBalance(Currency.SEK)
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(getAccountNumberBasedOnInvestmentType())
                                .withAccountNumber(getAccountNumberBasedOnInvestmentType())
                                .withAccountName(title)
                                .addIdentifier(
                                        AccountIdentifier.create(
                                                AccountIdentifierType.TINK,
                                                getAccountNumberBasedOnInvestmentType()))
                                .build())
                .build();
    }

    private double toMarketValue() {
        return marketValue == null ? 0d : marketValue.asDouble();
    }

    private String getAccountNumberBasedOnInvestmentType() {
        return Portfolio.Type.ISK.equals(toType()) ? iskAccountNumber : custodyAccountNumber;
    }

    private PortfolioModule toPortfolioModule(HandelsbankenSEApiClient client) {

        return PortfolioModule.builder()
                .withType(toType())
                .withUniqueIdentifier(getAccountNumberBasedOnInvestmentType())
                .withCashValue(mainDepositAccountBalance.getAmount())
                .withTotalProfit(
                        Optional.ofNullable(performance)
                                .flatMap(HandelsbankenPerformance::asDouble)
                                .orElse(0.0))
                .withTotalValue(toMarketValue())
                .withInstruments(toInstrumentModules(client))
                .setRawType(type)
                .build();
    }

    private List<InstrumentModule> toInstrumentModules(HandelsbankenSEApiClient client) {
        if (holdingLists == null) {
            return Collections.emptyList();
        }
        return holdingLists.stream()
                .flatMap(securityHoldingList -> securityHoldingList.toInstrumentModules(client))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private PortfolioType toType() {
        Portfolio.Type portfolioType = HandelsbankenSEConstants.PortfolioType.asType(this.type);

        if (portfolioType == Portfolio.Type.OTHER) {
            LOGGER.warn("Unknown portfolio type: {}", this.type);
        }

        switch (portfolioType) {
            case DEPOT:
                return PortfolioType.DEPOT;
            case ISK:
                return PortfolioType.ISK;
            default:
                return PortfolioType.OTHER;
        }
    }
}
