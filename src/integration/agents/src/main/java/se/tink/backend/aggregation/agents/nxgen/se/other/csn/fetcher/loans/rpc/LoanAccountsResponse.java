package se.tink.backend.aggregation.agents.nxgen.se.other.csn.fetcher.loans.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;

@Getter
@JsonObject
public class LoanAccountsResponse {

    @JsonProperty("rantaAbslAbal")
    private BigDecimal interestRate;

    @JsonProperty("arsbearbetningPagar")
    private boolean isAnnualProcessing;

    @JsonProperty("skuldupprakningsdatum")
    private long debtSettlementDate;

    @JsonProperty("totalSkuld")
    private int totalDebt;

    @JsonProperty("lanLista")
    private List<LoanEntity> loanList;

    @JsonProperty("uppsagtLanFinns")
    private boolean isLoanTerminated;

    @JsonProperty("aterkravLista")
    private List<Object> repaymentList;

    @JsonProperty("skuldrattning")
    private boolean debtCorrection;

    @JsonProperty("rantaAterkrav")
    private BigDecimal interestRepayment;

    @JsonProperty("skuldsanering")
    private boolean debtRestructuring;

    public BigDecimal getInterestRate() {
        return interestRate.divide(BigDecimal.valueOf(100.0), 4, RoundingMode.HALF_UP);
    }

    public Collection<LoanAccount> toTinkAccounts(UserInfoResponse userInfoResponse) {
        return loanList.stream()
                .filter(LoanEntity::isLoanAccount)
                .map(loanEntity -> loanEntity.toTinkLoanAccount(userInfoResponse, this))
                .collect(Collectors.toList());
    }
}
