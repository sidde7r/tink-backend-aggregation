package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.investment.entities;

import java.util.AbstractMap.SimpleEntry;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.HandelsbankenSEConstants.AccountPayloadKeys;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.instrument.InstrumentModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.portfolio.PortfolioModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.portfolio.PortfolioModule.PortfolioType;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.serialization.utils.SerializationUtils;

@JsonObject
public class FundHoldingsUser {

    private FundHoldingPart part;
    private FundHoldingSummary fundHoldingSummary;
    private List<FundHolding> fundHoldingList;

    public String getIdentifier() {
        return part != null ? part.getIdentifier() : null;
    }

    private double toSummaryMarketValue() {
        return fundHoldingSummary != null ? fundHoldingSummary.getMarketValue() : 0;
    }

    private double toSummaryPurchaseValue() {
        return fundHoldingSummary != null ? fundHoldingSummary.getPurchaseValue() : 0;
    }

    private List<InstrumentModule> toInstrumentModules() {
        if (Objects.isNull(fundHoldingList)) {
            return Collections.emptyList();
        }

        return fundHoldingList.stream()
                .map(FundHolding::toInstrumentModule)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private Map<String, String> getFundAccountMapping() {
        if (Objects.isNull(fundHoldingList)) {
            return Collections.emptyMap();
        }

        final Map<String, String> fundAccountMap =
                fundHoldingList.stream()
                        .filter(holding -> holding.getFundAccount().isPresent())
                        .map(
                                holding ->
                                        new SimpleEntry(
                                                holding.getIsin(), holding.getFundAccount().get()))
                        .collect(
                                Collectors.toMap(
                                        SimpleEntry<String, String>::getKey,
                                        SimpleEntry<String, String>::getValue,
                                        ((oldVal, newVal) -> oldVal)));

        /* TODO: Currently there is an issue with payloads greater than 256 characters exceeding the maximum limit of the field in the database.
        This has caused credentials with large payloads not being able to succesfully refresh since January 2020
        Temporarily disabling the usage of the payload field for those users until we figure out a solution.*/
        // 256 - 25 because that will be the length of the json value of fundAccountMap.
        if (SerializationUtils.serializeToString(fundAccountMap).length() > 256 - 25) {
            return Collections.emptyMap();
        }
        return fundAccountMap;
    }

    public InvestmentAccount toAccount(CustodyAccount custodyAccount) {
        return InvestmentAccount.nxBuilder()
                .withPortfolios(toPortfolioModule(custodyAccount))
                .withZeroCashBalance(getCurrency(custodyAccount))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(getIdentifier())
                                .withAccountNumber(getIdentifier())
                                .withAccountName(custodyAccount.getTitle())
                                .addIdentifier(
                                        AccountIdentifier.create(
                                                AccountIdentifierType.TINK, getIdentifier()))
                                .build())
                .putPayload(
                        AccountPayloadKeys.FUND_ACCOUNT_NUMBER,
                        SerializationUtils.serializeToString(getFundAccountMapping()))
                .build();
    }

    private String getCurrency(CustodyAccount custodyAccount) {
        return Optional.ofNullable(custodyAccount.getTinkAmount().getCurrencyCode()).orElse("");
    }

    private PortfolioModule toPortfolioModule(CustodyAccount custodyAccount) {
        double summaryMarketValue = toSummaryMarketValue();
        return PortfolioModule.builder()
                .withType(PortfolioType.DEPOT)
                .withUniqueIdentifier(getIdentifier())
                .withCashValue(0.0)
                .withTotalProfit(summaryMarketValue - toSummaryPurchaseValue())
                .withTotalValue(summaryMarketValue)
                .withInstruments(toInstrumentModules())
                .setRawType(custodyAccount.getType())
                .build();
    }
}
