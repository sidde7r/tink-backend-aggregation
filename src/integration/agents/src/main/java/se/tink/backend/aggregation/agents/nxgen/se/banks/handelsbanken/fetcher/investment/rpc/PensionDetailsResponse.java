package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.investment.rpc;

import com.google.common.base.Strings;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.HandelsbankenSEApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.HandelsbankenSEConstants;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.HandelsbankenSEConstants.Currency;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.investment.entities.CustodyAccount;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.investment.entities.HandelsbankenSEPensionFund;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.investment.entities.HandelsbankenSEPensionInfo;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.investment.entities.HandelsbankenSEPensionSummary;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.investment.entities.HandelsbankenSEProperty;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.rpc.BaseResponse;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.instrument.InstrumentModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.portfolio.PortfolioModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.portfolio.PortfolioModule.PortfolioType;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class PensionDetailsResponse extends BaseResponse {
    private static final Logger LOGGER = LoggerFactory.getLogger(PensionDetailsResponse.class);

    private String pensionName;
    private HandelsbankenSEPensionSummary summary;
    private List<HandelsbankenSEPensionFund> funds;

    public InvestmentAccount toInvestmentAccount(
            HandelsbankenSEApiClient client, HandelsbankenSEPensionInfo pensionInfo) {
        final String identifier = getIdentifier();
        final BigDecimal value = pensionInfo.getValue().getAmount();
        return InvestmentAccount.nxBuilder()
                .withPortfolios(toPortfolioModule(client, value.doubleValue()))
                .withCashBalance(ExactCurrencyAmount.zero(Currency.SEK))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(identifier)
                                .withAccountNumber(identifier)
                                .withAccountName(pensionName)
                                .addIdentifier(
                                        AccountIdentifier.create(
                                                AccountIdentifierType.TINK, identifier))
                                .build())
                .build();
    }

    public InvestmentAccount toInvestmentAccount(
            HandelsbankenSEApiClient client, CustodyAccount custodyAccount) {
        final String identifer = custodyAccount.getCustodyAccountNumber();
        return InvestmentAccount.nxBuilder()
                .withPortfolios(
                        Collections.singletonList(toPortfolioModule(client, custodyAccount)))
                .withCashBalance(ExactCurrencyAmount.zero(Currency.SEK))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(identifer)
                                .withAccountNumber(identifer)
                                .withAccountName(pensionName)
                                .addIdentifier(
                                        AccountIdentifier.create(
                                                AccountIdentifierType.TINK, identifer))
                                .build())
                .build();
    }

    private PortfolioModule toPortfolioModule(HandelsbankenSEApiClient client, Double value) {
        final String identifier = getIdentifier();
        return PortfolioModule.builder()
                .withType(getPortfolioType())
                .withUniqueIdentifier(identifier)
                .withCashValue(0)
                .withTotalProfit(0)
                .withTotalValue(value)
                .withInstruments(getInstrumentModules(client))
                .build();
    }

    private PortfolioModule toPortfolioModule(
            HandelsbankenSEApiClient client, CustodyAccount custodyAccount) {

        final String identifer = custodyAccount.getCustodyAccountNumber();
        final Double totalValue = custodyAccount.getMarketValue().getAmount();

        return PortfolioModule.builder()
                .withType(getPortfolioType())
                .withUniqueIdentifier(identifer)
                .withCashValue(0)
                .withTotalProfit(getTotalProfit(totalValue))
                .withTotalValue(totalValue)
                .withInstruments(getInstrumentModules(client))
                .build();
    }

    private PortfolioType getPortfolioType() {
        if (Strings.isNullOrEmpty(pensionName)) {
            LOGGER.warn("Handelsbanken pension name not present.");
            return PortfolioType.OTHER;
        }

        if (pensionName.contains(HandelsbankenSEConstants.Investments.PENSION)) {
            return PortfolioType.PENSION;
        }

        if (!pensionName
                .toLowerCase()
                .startsWith(HandelsbankenSEConstants.Investments.KF_TYPE_PREFIX)) {

            LOGGER.warn("Handelsbanken unknown kapital portfolio type: {}", pensionName);
            return PortfolioType.OTHER;
        }

        return PortfolioType.KF;
    }

    private double getTotalProfit(Double totalValue) {
        return totalValue
                - Optional.ofNullable(summary)
                        .flatMap(HandelsbankenSEPensionSummary::toPaymentsMade)
                        .orElse(0d);
    }

    private List<InstrumentModule> getInstrumentModules(HandelsbankenSEApiClient client) {
        return Optional.ofNullable(funds)
                .map(
                        funds ->
                                funds.stream()
                                        .map(fund -> parseInstrument(client, fund))
                                        .filter(Optional::isPresent)
                                        .map(Optional::get)
                                        .collect(Collectors.toList()))
                .orElseGet(Collections::emptyList);
    }

    private Optional<InstrumentModule> parseInstrument(
            HandelsbankenSEApiClient client, HandelsbankenSEPensionFund fund) {
        return client.fundHoldingDetail(fund)
                .flatMap(HandelsbankenSEFundAccountHoldingDetail::toInstrumentModule);
    }

    @Override
    public String toString() {
        return SerializationUtils.serializeToString(this);
    }

    private String getIdentifier() {
        return summary.getItems().stream()
                .filter(HandelsbankenSEProperty::isIdentifer)
                .map(HandelsbankenSEProperty::getValue)
                .collect(Collectors.joining());
    }
}
