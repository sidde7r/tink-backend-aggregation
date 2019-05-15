package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.investment.rpc;

import com.google.common.base.Strings;
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
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.investment.entities.CustodyAccount;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.investment.entities.HandelsbankenSEPensionFund;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.investment.entities.HandelsbankenSEPensionSummary;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.rpc.BaseResponse;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class PensionDetailsResponse extends BaseResponse {
    private static final Logger LOGGER = LoggerFactory.getLogger(PensionDetailsResponse.class);

    private String pensionName;
    private HandelsbankenSEPensionSummary summary;
    private List<HandelsbankenSEPensionFund> funds;

    public InvestmentAccount toInvestmentAccount(
            HandelsbankenSEApiClient client, CustodyAccount custodyAccount) {

        return InvestmentAccount.builder(custodyAccount.getCustodyAccountNumber())
                .setAccountNumber(custodyAccount.getCustodyAccountNumber())
                .setName(pensionName)
                .setCashBalance(Amount.inSEK(0))
                .setPortfolios(Collections.singletonList(toPortfolio(client, custodyAccount)))
                .build();
    }

    private Portfolio toPortfolio(HandelsbankenSEApiClient client, CustodyAccount custodyAccount) {
        Portfolio portfolio = new Portfolio();

        portfolio.setUniqueIdentifier(custodyAccount.getCustodyAccountNumber());
        portfolio.setType(getPortfolioType());
        Amount totalValue = custodyAccount.getTinkAmount();
        portfolio.setTotalValue(totalValue.getValue());
        portfolio.setTotalProfit(getTotalProfit(totalValue));
        portfolio.setInstruments(getInstruments(client));
        return portfolio;
    }

    private Portfolio.Type getPortfolioType() {
        if (Strings.isNullOrEmpty(pensionName)) {
            LOGGER.warn("Handelsbanken pension name not present.");
            return Portfolio.Type.OTHER;
        }

        if (!pensionName
                .toLowerCase()
                .startsWith(HandelsbankenSEConstants.Investments.KF_TYPE_PREFIX)) {

            LOGGER.warn("Handelsbanken unknown kapital portfolio type: {}", pensionName);
            return Portfolio.Type.OTHER;
        }

        return Portfolio.Type.KF;
    }

    private double getTotalProfit(Amount totalValue) {
        return totalValue.getValue()
                - Optional.ofNullable(summary)
                        .flatMap(HandelsbankenSEPensionSummary::toPaymentsMade)
                        .orElse(0d);
    }

    private List<Instrument> getInstruments(HandelsbankenSEApiClient client) {
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

    private Optional<Instrument> parseInstrument(
            HandelsbankenSEApiClient client, HandelsbankenSEPensionFund fund) {
        return client.fundHoldingDetail(fund)
                .flatMap(HandelsbankenSEFundAccountHoldingDetail::toInstrument);
    }

    @Override
    public String toString() {
        return SerializationUtils.serializeToString(this);
    }
}
