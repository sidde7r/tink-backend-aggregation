package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.investment.rpc;

import com.google.common.base.Strings;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
        portfolio.setTotalProfit(
                totalValue.getValue()
                        - Optional.ofNullable(summary)
                                .flatMap(HandelsbankenSEPensionSummary::toPaymentsMade)
                                .orElse(0d));
        portfolio.setInstruments(
                Optional.ofNullable(funds)
                        .map(
                                funds ->
                                        funds.stream()
                                                .map(
                                                        fund ->
                                                                client.fundHoldingDetail(fund)
                                                                        .flatMap(
                                                                                HandelsbankenSEFundAccountHoldingDetail
                                                                                        ::toInstrument))
                                                .filter(Optional::isPresent)
                                                .map(Optional::get)
                                                .collect(Collectors.toList()))
                        .orElseGet(Collections::emptyList));
        return portfolio;
    }

    private Portfolio.Type getPortfolioType() {
        if (!Strings.isNullOrEmpty(pensionName)
                && pensionName
                        .toLowerCase()
                        .startsWith(HandelsbankenSEConstants.Fetcher.Investments.KF_TYPE_PREFIX)) {
            return Portfolio.Type.KF;
        }

        LOGGER.warn("Handelsbanken unknown kapital portfolio type: {}", pensionName);
        return Portfolio.Type.OTHER;
    }

    @Override
    public String toString() {
        return SerializationUtils.serializeToString(this);
    }
}

