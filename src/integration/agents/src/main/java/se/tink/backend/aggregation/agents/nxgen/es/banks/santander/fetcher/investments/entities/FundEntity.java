package se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.investments.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import java.util.Collections;
import java.util.List;
import javax.xml.bind.annotation.XmlRootElement;
import se.tink.backend.aggregation.agents.models.Instrument;
import se.tink.backend.aggregation.agents.models.Portfolio;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.entities.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.entities.CustomerData;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.entities.GeneralInfoEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.investments.rpc.FundDetailsResponse;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.entity.HolderName;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
@XmlRootElement
public class FundEntity {

    @JsonProperty("comunes")
    private GeneralInfoEntity generalInfo;

    @JsonProperty("titular")
    private CustomerData customerData;

    @JsonProperty("descSituacionContrato")
    private String contractDescription;

    @JsonProperty("impValoracion")
    private AmountEntity totalValue;

    @JsonProperty("impValoracionContravalor")
    private AmountEntity counterTotalValue;

    @JsonProperty("numParticipac")
    private double quantity;

    @JsonIgnore
    public InvestmentAccount toInvestmentAccount(FundDetailsResponse fundDetailsResponse) {
        return InvestmentAccount.builder(generalInfo.getContractDescription())
                .setName(generalInfo.getAlias())
                .setAccountNumber(generalInfo.getAlias())
                .setHolderName(getHolderName(fundDetailsResponse))
                .setPortfolios(getPortfolios(fundDetailsResponse))
                .setCashBalance(
                        ExactCurrencyAmount.zero(totalValue.getTinkAmount().getCurrencyCode()))
                .build();
    }

    @JsonIgnore
    private List<Portfolio> getPortfolios(FundDetailsResponse fundDetailsResponse) {

        List<Instrument> instruments = fundDetailsResponse.toTinkInstruments();
        Portfolio portfolio = new Portfolio();
        portfolio.setCashValue(0.0);
        portfolio.setInstruments(instruments);
        portfolio.setTotalValue(totalValue.getAmountAsDouble());
        portfolio.setType(Portfolio.Type.DEPOT);
        portfolio.setUniqueIdentifier(getPortfolioUniqueId());

        return Collections.singletonList(portfolio);
    }

    private String getPortfolioUniqueId() {
        return generalInfo.getContractDescription().replaceAll("[^\\dA-Za-z]", "");
    }

    @JsonIgnore
    private HolderName getHolderName(FundDetailsResponse fundDetailsResponse) {
        String holder = fundDetailsResponse.getHolder();
        if (!Strings.isNullOrEmpty(holder)) {
            return new HolderName(holder);
        }

        return null;
    }

    public GeneralInfoEntity getGeneralInfo() {
        return generalInfo;
    }
}
