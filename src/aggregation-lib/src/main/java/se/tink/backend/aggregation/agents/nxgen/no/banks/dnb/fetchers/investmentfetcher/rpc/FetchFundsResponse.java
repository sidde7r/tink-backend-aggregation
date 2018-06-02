package se.tink.backend.aggregation.agents.nxgen.no.banks.dnb.fetchers.investmentfetcher.rpc;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.no.banks.dnb.fetchers.investmentfetcher.entities.FundAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.no.banks.dnb.fetchers.investmentfetcher.entities.MyFundEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.InvestmentAccount;

@JsonObject
public class FetchFundsResponse {
    private List<FundAccountEntity> fundAccounts;
    private double unrealizedReturn;
    private List<MyFundEntity> myFunds;
    private double sumFund;
    private double fundPercentageIncrease;
    private double fundAmountIncrease;

    public List<FundAccountEntity> getFundAccounts() {
        return fundAccounts != null ? fundAccounts : Collections.emptyList();
    }

    public double getUnrealizedReturn() {
        return unrealizedReturn;
    }

    public List<MyFundEntity> getMyFunds() {
        return myFunds;
    }

    public double getSumFund() {
        return sumFund;
    }

    public double getFundPercentageIncrease() {
        return fundPercentageIncrease;
    }

    public double getFundAmountIncrease() {
        return fundAmountIncrease;
    }

    public List<InvestmentAccount> getInvestmentAccounts() {
        return getFundAccounts().stream()
                .map(FundAccountEntity::toInvestmentAccount)
                .distinct()
                .collect(Collectors.toList());
    }
}
