package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Optional;
import se.tink.backend.aggregation.agents.models.Portfolio;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.SdcApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.SdcConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.fetcher.rpc.CustodyContentResponse;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;
import se.tink.libraries.amount.Amount;

@JsonObject
public class SdcCustodyDetailsModel {

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
        return InvestmentAccount.builder(this.id)
                .setCashBalance(getCashBalance())
                .setAccountNumber(this.id)
                .setName(this.name)
                .setBankIdentifier(
                        Optional.ofNullable(this.yieldAccount)
                                .map(SdcCustodyDetailsAccount::getId)
                                .orElse(null))
                .setPortfolios(bankClient.fetchCustodyContent(this).toPortfolios())
                .build();
    }

    @JsonIgnore
    private Amount getCashBalance() {
        double depositAmount = 0.0;
        if (depositValue != null) {
            depositAmount = depositValue.toExactCurrencyAmount().getDoubleValue();
        }

        return availableBalance.toTinkAmount().subtract(depositAmount);
    }

    public Portfolio toPortfolio(CustodyContentResponse custodyContent) {
        Portfolio portfolio = new Portfolio();

        portfolio.setType(parsePortfolioType());
        portfolio.setRawType(this.type);
        portfolio.setTotalValue(
                this.depositValue == null
                        ? null
                        : this.depositValue.toExactCurrencyAmount().getDoubleValue());
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
