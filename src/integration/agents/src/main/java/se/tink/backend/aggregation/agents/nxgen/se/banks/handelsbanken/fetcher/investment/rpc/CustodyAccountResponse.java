package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.investment.rpc;

import java.util.AbstractMap.SimpleEntry;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.models.Portfolio;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.HandelsbankenSEApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.HandelsbankenSEConstants;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.HandelsbankenSEConstants.AccountPayloadKeys;
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
import se.tink.libraries.account.AccountIdentifier.Type;
import se.tink.libraries.serialization.utils.SerializationUtils;

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

        final String instrumentsMap =
                SerializationUtils.serializeToString(getFundAccountMapping(client));
        /* TODO : Logging below for debugging purpose - CATS-379 */
        LOGGER.info("Instrument serialized map size: " + instrumentsMap.length());

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
                                                Type.TINK, getAccountNumberBasedOnInvestmentType()))
                                .build())
                .putPayload(AccountPayloadKeys.FUND_ACCOUNT_NUMBER, instrumentsMap)
                .build();
    }

    private Map<String, String> getFundAccountMapping(HandelsbankenSEApiClient client) {
        final List<InstrumentModule> instruments = toInstrumentModules(client);
        /* TODO : Logging below for debugging purpose - CATS-379 */
        LOGGER.info("Number of instruments: " + instruments.size());
        return instruments.stream()
                .map(InstrumentModule::getInstrumentIdModule)
                .map(
                        instrumentIdModule ->
                                new SimpleEntry(
                                        instrumentIdModule.getIsin(),
                                        instrumentIdModule.getUniqueIdentifier()))
                .collect(
                        Collectors.toMap(
                                SimpleEntry<String, String>::getKey,
                                SimpleEntry<String, String>::getValue,
                                ((oldVal, newVal) -> oldVal)));
    }

    private double toMarketValue() {
        return marketValue == null ? 0d : marketValue.asDouble();
    }

    private String getAccountNumberBasedOnInvestmentType() {
        return Portfolio.Type.ISK.equals(toType()) ? iskAccountNumber : custodyAccountNumber;
    }

    private PortfolioModule toPortfolioModule(HandelsbankenSEApiClient client) {

        return PortfolioModule.builder()
                .withType(PortfolioType.ISK) // TODO
                .withUniqueIdentifier(getAccountNumberBasedOnInvestmentType())
                .withCashValue(toMarketValue())
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

    private Portfolio.Type toType() {
        Portfolio.Type portfolioType = HandelsbankenSEConstants.PortfolioType.asType(this.type);

        if (portfolioType == Portfolio.Type.OTHER) {
            LOGGER.warn("Unknown portfolio type: {}", this.type);
        }

        return portfolioType;
    }
}
