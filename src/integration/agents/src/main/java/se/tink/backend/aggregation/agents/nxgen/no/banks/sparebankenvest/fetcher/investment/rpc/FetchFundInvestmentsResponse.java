package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.fetcher.investment.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.fetcher.investment.entities.FundInvestmentEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.instrument.InstrumentModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.portfolio.PortfolioModule;
import se.tink.libraries.account.identifiers.OtherIdentifier;

@JsonObject
public class FetchFundInvestmentsResponse extends ArrayList<FundInvestmentEntity> {

    @JsonIgnore
    public Collection<InvestmentAccount> getTinkInvestmentAccounts() {
        Map<String, TmpAccount> tmpMap = new HashMap<>();
        for (FundInvestmentEntity investmentEntity : this) {
            if (!tmpMap.containsKey(investmentEntity.getAccountNumber())) {
                TmpAccount tmpAccount = new TmpAccount(investmentEntity);
                tmpAccount.instruments.add(investmentEntity.toTinkInstrument());
                tmpMap.put(investmentEntity.getAccountNumber(), tmpAccount);
            }
        }

        return tmpMap.values().stream()
                .map(
                        investmentItem -> {
                            PortfolioModule portfolio = createPortfolio(investmentItem);
                            return createAccount(investmentItem, portfolio);
                        })
                .collect(Collectors.toList());
    }

    private InvestmentAccount createAccount(TmpAccount investmentItem, PortfolioModule portfolio) {
        return InvestmentAccount.nxBuilder()
                .withPortfolios(portfolio)
                .withZeroCashBalance("NOK")
                .withId(prepareIdModule(investmentItem))
                .build();
    }

    private IdModule prepareIdModule(TmpAccount investmentItem) {
        return IdModule.builder()
                .withUniqueIdentifier(investmentItem.accountNumber)
                .withAccountNumber(investmentItem.accountNumber)
                .withAccountName(investmentItem.accountName)
                .addIdentifier(new OtherIdentifier(investmentItem.accountNumber))
                .build();
    }

    private PortfolioModule createPortfolio(TmpAccount investmentItem) {
        return PortfolioModule.builder()
                .withType(investmentItem.portfolioType)
                .withUniqueIdentifier(investmentItem.accountNumber)
                .withCashValue(investmentItem.accountBalance.doubleValue())
                .withTotalProfit(investmentItem.totalProfit.doubleValue())
                .withTotalValue(investmentItem.totalValue.doubleValue())
                .withInstruments(investmentItem.instruments)
                .setRawType(investmentItem.portfolioRawType)
                .build();
    }

    private static class TmpAccount {
        private final String portfolioRawType;
        private final PortfolioModule.PortfolioType portfolioType;
        private final String accountName;
        private final String accountNumber;
        private final BigDecimal totalValue;
        private final BigDecimal totalProfit;
        private final BigDecimal accountBalance;
        private final List<InstrumentModule> instruments = new ArrayList<>();

        private TmpAccount(FundInvestmentEntity investmentEntity) {
            this.portfolioRawType = investmentEntity.getType();
            this.portfolioType = investmentEntity.getTinkPortfolioType();
            this.accountName = investmentEntity.getPortfolioName();
            this.accountNumber = investmentEntity.getAccountNumber();
            this.totalValue = BigDecimal.valueOf(investmentEntity.getValue());
            this.accountBalance = BigDecimal.valueOf(investmentEntity.getValue());
            this.totalProfit =
                    investmentEntity.getProfit() != null
                            ? BigDecimal.valueOf(investmentEntity.getProfit())
                            : new BigDecimal(0);
        }
    }
}
