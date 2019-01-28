package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.fetcher.investment.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.fetcher.investment.entities.FundInvestmentEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.InvestmentAccount;
import se.tink.libraries.amount.Amount;
import se.tink.backend.system.rpc.Instrument;
import se.tink.backend.system.rpc.Portfolio;

@JsonObject
public class FetchFundInvestmentsResponse extends ArrayList<FundInvestmentEntity> {
    @JsonIgnore
    public Collection<InvestmentAccount> getTinkInvestmentAccounts() {
        HashMap<String, TmpAccount> tmpMap = new HashMap<>();
        for (FundInvestmentEntity investmentEntity : this) {
            TmpAccount tmpAccount = tmpMap
                    .getOrDefault(investmentEntity.getKontonummer(), new TmpAccount(investmentEntity));
            if (!tmpMap.containsKey(investmentEntity.getKontonummer())) {
                tmpMap.put(investmentEntity.getKontonummer(), tmpAccount);
            }
            tmpAccount.accountBalance += investmentEntity.getVerdi();
            tmpAccount.totalProfit += investmentEntity.getGevinst();
            tmpAccount.totalValue += investmentEntity.getVerdi();
            tmpAccount.instruments.add(investmentEntity.toTinkInstrument());
        }

        return tmpMap.values().stream()
                .map(investmentItem -> {
                    Portfolio portfolio = createPortfolio(investmentItem);
                    return createAccount(investmentItem, portfolio);
                })
                .collect(Collectors.toList());
    }

    private InvestmentAccount createAccount(TmpAccount investmentItem,
            Portfolio portfolio) {
        return InvestmentAccount.builder(investmentItem.accountNumber)
                .setAccountNumber(investmentItem.accountNumber)
                .setName(investmentItem.accountName)
                .setBankIdentifier(investmentItem.accountNumber)
                .setCashBalance(Amount.inNOK(0))
                .setPortfolios(Collections.singletonList(portfolio))
                .build();
    }

    private Portfolio createPortfolio(TmpAccount investmentItem) {
        Portfolio portfolio = new Portfolio();
        portfolio.setInstruments(investmentItem.instruments);
        portfolio.setRawType(investmentItem.portfolioRawType);
        portfolio.setTotalProfit(investmentItem.totalProfit);
        portfolio.setTotalValue(investmentItem.totalValue);
        portfolio.setType(investmentItem.portfolioType);
        portfolio.setUniqueIdentifier(investmentItem.accountNumber);
        return portfolio;
    }

    private static class TmpAccount {
        String portfolioRawType;
        Portfolio.Type portfolioType;
        String accountName;
        String accountNumber;
        String uniqueIdentifier;
        double totalValue;
        double totalProfit;
        double accountBalance;
        List<Instrument> instruments = new ArrayList<>();

        private TmpAccount(FundInvestmentEntity investmentEntity) {
            this.portfolioRawType = investmentEntity.getType();
            this.portfolioType = investmentEntity.getTinkPortfolioType();
            this.accountName = investmentEntity.getPortfolioName();
            this.accountNumber = investmentEntity.getKontonummer();
            this.uniqueIdentifier = investmentEntity.getKontonummer();
        }
    }
}
