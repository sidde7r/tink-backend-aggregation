package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.investment.rpc;

import java.util.AbstractMap.SimpleEntry;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.investment.InvestmentBuildStep;
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

        Map<String, String> map = getFundAccountMapping(client);

        InvestmentBuildStep builder =
                InvestmentAccount.nxBuilder()
                        .withPortfolios(Collections.singletonList(toPortfolioModule(client)))
                        .withZeroCashBalance(Currency.SEK)
                        .withId(
                                IdModule.builder()
                                        .withUniqueIdentifier(
                                                getAccountNumberBasedOnInvestmentType())
                                        .withAccountNumber(getAccountNumberBasedOnInvestmentType())
                                        .withAccountName(title)
                                        .addIdentifier(
                                                AccountIdentifier.create(
                                                        Type.TINK,
                                                        getAccountNumberBasedOnInvestmentType()))
                                        .build());

        /* TODO : Due to overload of payload that caused alerts, below we split payload into smaller size - CATS-379 */

        int i = 0;
        Map<String, String> tempMap = new HashMap<>();
        for (Entry<String, String> entry : map.entrySet()) {
            tempMap.put(entry.getKey(), entry.getValue());
            if ((i + 1) % 5 == 0 || i == (map.size() - 1)) {
                String key = AccountPayloadKeys.FUND_ACCOUNT_NUMBER + "_part_" + i + 1;
                final String instrumentsMap = SerializationUtils.serializeToString(tempMap);
                LOGGER.info(
                        "# of instruments: "
                                + tempMap.size()
                                + " /Serialized length: "
                                + instrumentsMap.length());
                builder.putPayload(key, instrumentsMap);
                tempMap.clear();
            }
            i++;
        }

        return builder.build();
    }

    private Map<String, String> getFundAccountMapping(HandelsbankenSEApiClient client) {
        final List<InstrumentModule> instruments = toInstrumentModules(client);

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
