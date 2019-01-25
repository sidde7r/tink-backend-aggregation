package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.SdcApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.SdcConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.fetcher.rpc.CustodyContentResponse;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.core.account.InvestmentAccount;
import se.tink.backend.core.Amount;
import se.tink.backend.system.rpc.Portfolio;
import se.tink.libraries.serialization.utils.SerializationUtils;

@JsonObject
public class SdcCustodyDetailsModel {
    private static final AggregationLogger LOGGER = new AggregationLogger(SdcCustodyDetailsModel.class);

    private String displayId;
    private String id;
    private String name;
    private String type;
    private SdcAmount availableBalance;
    private SdcAmount depositValue;
    private SdcCustodyDetailsAccount yieldAccount;

//    Other possible fields of interest:
//    private SdcCustodyProperties properties;
//    private SdcChange intradayChange;

    public InvestmentAccount toInvestmentAccount(SdcApiClient bankClient) {
        // log values to see if this is cash balance
        LOGGER.infoExtraLong(SerializationUtils.serializeToString(this),
                SdcConstants.Fetcher.Investment.CASH_BALANCE);

        return InvestmentAccount.builder(this.id)
                .setCashBalance(getCashBalance())
                .setAccountNumber(this.id)
                .setName(this.name)
                .setBankIdentifier(Optional.ofNullable(this.yieldAccount)
                        .map(SdcCustodyDetailsAccount::getId)
                        .orElse(null)
                )
                .setPortfolios(bankClient.fetchCustodyContent(this).toPortfolios())
                .build();
    }

    @JsonIgnore
    private Amount getCashBalance() {
        double depositAmount = 0.0;
        if (depositValue != null) {
            depositAmount = depositValue.toTinkAmount().getValue();
        }

        return availableBalance.toTinkAmount().subtract(depositAmount);
    }

    public Portfolio toPortfolio(CustodyContentResponse custodyContent) {
        Portfolio portfolio = new Portfolio();

        portfolio.setType(parsePortfolioType());
        portfolio.setRawType(this.type);
        portfolio.setTotalValue(this.depositValue == null ? null : this.depositValue.toTinkAmount().getValue());
        portfolio.setUniqueIdentifier(this.id);
        portfolio.setInstruments(custodyContent.toInstruments());
        return portfolio;
    }

    private Portfolio.Type parsePortfolioType() {
        return SdcConstants.Fetcher.Investment.PortfolioTypes.parse(this.type)
                .orElse(Portfolio.Type.OTHER);
    }

    public String getId() {
        return this.id;
    }
}
