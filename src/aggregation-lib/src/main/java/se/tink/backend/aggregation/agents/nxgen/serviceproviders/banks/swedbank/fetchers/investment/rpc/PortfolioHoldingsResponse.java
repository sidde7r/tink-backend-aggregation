package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.fetchers.investment.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.SwedbankBaseConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc.AbstractAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc.AmountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PortfolioHoldingsResponse {
    private List<FundAccountEntity> fundAccounts;
    private List<EndowmentInsuranceEntity> endowmentInsurances;
    private List<EquityTraderEntity> equityTraders;
    private List<SavingsAccountEntity> savingsAccounts;
    private List<InvestmentSavingsAccountEntity> investmentSavings;
    private String serverTime;
    private AmountEntity totalValue;

    public List<FundAccountEntity> getFundAccounts() {
        return fundAccounts;
    }

    public List<EndowmentInsuranceEntity> getEndowmentInsurances() {
        return endowmentInsurances;
    }

    public List<EquityTraderEntity> getEquityTraders() {
        return equityTraders;
    }

    public List<SavingsAccountEntity> getSavingsAccounts() {
        return savingsAccounts;
    }

    public List<InvestmentSavingsAccountEntity> getInvestmentSavings() {
        return investmentSavings;
    }

    public String getServerTime() {
        return serverTime;
    }

    public AmountEntity getTotalValue() {
        return totalValue;
    }

    @JsonIgnore
    // extract all account numbers for the investment accounts, BUT SAVINGSACCOUNT
    // let savings account be fetch by transactional account fetcher
    public List<String> getInvestmentAccountNumbers() {
        return Stream.of(Optional.ofNullable(fundAccounts),
                Optional.ofNullable(endowmentInsurances),
                Optional.ofNullable(equityTraders),
                Optional.ofNullable(savingsAccounts),
                Optional.ofNullable(investmentSavings))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .flatMap(List::stream)
                .filter(account -> SwedbankBaseConstants.InvestmentAccountType
                                        .fromAccountType(((AbstractInvestmentAccountEntity) account).getType()) !=
                                SwedbankBaseConstants.InvestmentAccountType.SAVINGSACCOUNT)
                .map(AbstractAccountEntity::getFullyFormattedNumber)
                .collect(Collectors.toList());
    }
}
