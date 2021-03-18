package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.seb.fetcher.investment.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.seb.SebConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.instrument.InstrumentModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.portfolio.PortfolioModule;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;

@JsonObject
public class InvestmentEntity {

    //    private static final Logger log = LoggerFactory.getLogger(InvestmentEntity.class);
    private static final String OCCUPATIONAL_PENSION = "tjänstepension";
    private static final String ENDOWMENT_INSURANCE = "kapitalförsäkring";
    private static final String PENSION_SAVINGS_FUND = "pensionsspar fond";
    private static final String SAFE_PENSION_INSURANCE = "trygg privatpension";
    private static final String PENSION_INSURANCE = "pensionsförsäkring";

    @JsonProperty("ANDEL_VARDE_BEL")
    private double totalValue;

    @JsonProperty("DETAIL_URL")
    private String detailHandle;

    @JsonProperty("FORS_NR")
    private String bankIdentifier;

    @JsonProperty("TYP")
    private String type;

    public InvestmentAccount toTinkAccount(List<InstrumentModule> instruments) {
        PortfolioModule portfolios =
                PortfolioModule.builder()
                        .withType(getPortfolioType())
                        .withUniqueIdentifier(bankIdentifier)
                        .withCashValue(0d)
                        .withTotalProfit(0d)
                        .withTotalValue(totalValue)
                        .withInstruments(instruments)
                        .build();
        IdModule idModule =
                IdModule.builder()
                        .withUniqueIdentifier(bankIdentifier)
                        .withAccountNumber(bankIdentifier)
                        .withAccountName(type)
                        .addIdentifier(
                                AccountIdentifier.create(
                                        AccountIdentifierType.TINK, bankIdentifier))
                        .build();
        return InvestmentAccount.nxBuilder()
                .withPortfolios(portfolios)
                .withZeroCashBalance(SebConstants.DEFAULT_CURRENCY)
                .withId(idModule)
                .build();
    }

    @JsonIgnore
    private PortfolioModule.PortfolioType getPortfolioType() {
        if (Strings.isNullOrEmpty(type)) {
            return PortfolioModule.PortfolioType.OTHER;
        }

        switch (type.trim().toLowerCase()) {
            case ENDOWMENT_INSURANCE:
            case PENSION_INSURANCE:
            case PENSION_SAVINGS_FUND:
            case SAFE_PENSION_INSURANCE:
                return PortfolioModule.PortfolioType.KF;
            case OCCUPATIONAL_PENSION:
                return PortfolioModule.PortfolioType.PENSION;
            default:
                //                log.warn(String.format("Unknown investment account type: %s",
                // type));
                return PortfolioModule.PortfolioType.OTHER;
        }
    }

    public String getDetailHandle() {
        return detailHandle;
    }
}
