package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.investment.rpc;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.models.Portfolio;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.HandelsbankenSEApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.investment.entities.CustodyAccount;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.investment.entities.HandelsbankenSEPensionFund;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.investment.entities.HandelsbankenSEPensionSummary;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.rpc.BaseResponse;
import se.tink.backend.aggregation.agents.utils.log.LogTag;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class PensionDetailsResponse extends BaseResponse {
    private static final AggregationLogger log =
            new AggregationLogger(PensionDetailsResponse.class);

    private String pensionName;
    private HandelsbankenSEPensionSummary summary;
    private List<HandelsbankenSEPensionFund> funds;

    public InvestmentAccount toInvestmentAccount(
            HandelsbankenSEApiClient client, CustodyAccount custodyAccount) {

        // Temporary logging to investigate the correct parsing of KF portfolios
        log.infoExtraLong(this.toString(), LogTag.from("handelsbanken_pension_details"));

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
        portfolio.setType(Portfolio.Type.PENSION);
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

    @Override
    public String toString() {
        return SerializationUtils.serializeToString(this);
    }
}
