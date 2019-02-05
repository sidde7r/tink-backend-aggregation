package se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v30.fetcher.loan.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.LoanAccount;
import se.tink.backend.aggregation.nxgen.core.account.LoanDetails;
import se.tink.backend.aggregation.nxgen.core.account.entity.HolderName;
import se.tink.libraries.amount.Amount;

@JsonObject
public class LoanAccountEntity {

    // Loan Account
    @JsonProperty("loan_id")
    private String loanId;

    @JsonProperty("loan_formatted_id")
    private String loanFormattedId;

    private String nickname;

    private double balance;

    @JsonProperty("interest_rate")
    private double interestRate;

    // Loan Details
    private double paid;

    private int drawn;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @JsonProperty("first_draw_down_date")
    private Date firstDrawnDownDate;

    @JsonProperty("following_payment")
    private FollowingPaymentEntity followingPayment;

    // Shared
    private String currency;

    private List<OwnersEntity> owners;

    public LoanAccount toTinkLoanAccount() {

        return LoanAccount.builder(loanId, toAmount(balance))
                .setName(nickname)
                .setAccountNumber(loanFormattedId)
                .setBankIdentifier(loanId)
                .setInterestRate(interestRate)
                .setDetails(getLoanDetails())
                .setHolderName(getOwnerName())
                .build();
    }

    private LoanDetails getLoanDetails() {

        return LoanDetails.builder(LoanDetails.Type.DERIVE_FROM_NAME)
                .setAmortized(toAmount(paid))
                .setApplicants(getApplicants())
                .setCoApplicant(owners.size() > 1)
                .setInitialBalance(toAmount(-drawn))
                .setInitialDate(firstDrawnDownDate)
                .setLoanNumber(loanId)
                .setMonthlyAmortization(toAmount(followingPayment.getInstalmentValue()))
                .build();
    }

    private HolderName getOwnerName() {

        if (owners.size() > 0) {
            return owners.get(0).getName();
        }

        return null;
    }

    private List<String> getApplicants() {
        return owners.stream()
                .map(Object::toString)
                .collect(Collectors.toList());
    }

    private Amount toAmount(double value) {
        return new Amount(currency, value);
    }

}
