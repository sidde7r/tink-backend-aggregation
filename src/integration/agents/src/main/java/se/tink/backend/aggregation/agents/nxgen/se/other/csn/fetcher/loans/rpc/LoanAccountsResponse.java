package se.tink.backend.aggregation.agents.nxgen.se.other.csn.fetcher.loans.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
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
    private double rantaAbslAbal;

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
    private double interestRepayment;

    @JsonProperty("skuldsanering")
    private boolean debtRestructuring;

    private double getInterestRate() {
        return rantaAbslAbal / 100; // Need to adjust to a percentage, hence the division.
    }

    private ExactCurrencyAmount getBalance() {
        return ExactCurrencyAmount.inSEK(totalDebt);
    }

    private ExactCurrencyAmount getInitialBalance() {
        return ExactCurrencyAmount.inSEK(
                loanList.stream().findFirst().map(LoanEntity::getIncomingDebt).orElse(0.0));
    }

    public LoanAccount toTinkLoanAccount(UserInfoResponse userInfoResponse) {

        return LoanAccount.nxBuilder()
                .withLoanDetails(
                        LoanModule.builder()
                                .withType(LoanDetails.Type.STUDENT)
                                .withBalance(getBalance())
                                .withInterestRate(getInterestRate())
                                .setInitialBalance(getInitialBalance())
                                .build())
                .withId(userInfoResponse.getIdModule())
                .build();
    }
}
