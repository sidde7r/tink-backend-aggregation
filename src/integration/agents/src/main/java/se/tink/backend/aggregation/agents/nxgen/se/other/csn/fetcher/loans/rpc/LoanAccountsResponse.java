package se.tink.backend.aggregation.agents.nxgen.se.other.csn.fetcher.loans.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.List;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanDetails;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.loan.LoanModule;
import se.tink.libraries.amount.ExactCurrencyAmount;

@Getter
@JsonObject
public class LoanAccountsResponse {

    @JsonProperty("rantaAbslAbal")
    private BigDecimal rantaAbslAbal;

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

    private BigDecimal getInterestRate() {
        return rantaAbslAbal.divide(
                new BigDecimal(100)); // Need to adjust to a percentage, hence the division.
    }

    private ExactCurrencyAmount getBalance() {
        return ExactCurrencyAmount.inSEK(totalDebt);
    }

    private ExactCurrencyAmount getInitialBalance() {
        return ExactCurrencyAmount.inSEK(
                loanList.stream()
                        .findFirst()
                        .map(LoanEntity::getIncomingDebt)
                        .orElse(new BigDecimal(0))
                        .doubleValue());
    }

    public LoanAccount toTinkLoanAccount(UserInfoResponse userInfoResponse) {

        return LoanAccount.nxBuilder()
                .withLoanDetails(
                        LoanModule.builder()
                                .withType(LoanDetails.Type.STUDENT)
                                .withBalance(getBalance())
                                .withInterestRate(getInterestRate().doubleValue())
                                .setInitialBalance(getInitialBalance())
                                .build())
                .withId(userInfoResponse.getIdModule())
                .build();
    }
}
