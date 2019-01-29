package se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v33.fetcher.loan.rpc;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v33.NordeaFIConstants;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v33.fetcher.loan.entities.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v33.fetcher.loan.entities.CreditEntity;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v33.fetcher.loan.entities.FollowingPaymentEntity;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v33.fetcher.loan.entities.InterestEntity;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v33.fetcher.loan.entities.LoansEntity;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v33.fetcher.loan.entities.OwnersEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.LoanAccount;
import se.tink.backend.aggregation.nxgen.core.account.LoanDetails;
import se.tink.backend.aggregation.nxgen.core.account.entity.HolderName;
import se.tink.libraries.amount.Amount;

@JsonObject
public class FetchLoanDetailsResponse {
    @JsonProperty("loan_id")
    private String loanId;
    @JsonProperty("loan_formatted_id")
    private String loanFormattedId;
    @JsonProperty("product_code")
    private String productCode;
    @JsonProperty
    private String currency;
    @JsonProperty
    private String group;
    @JsonProperty("repayment_status")
    private String repaymentStatus;
    @JsonProperty
    private String nickname;
    @JsonFormat(pattern = "yyyy-MM-dd")
    @JsonProperty("first_draw_down_date")
    private Date firstDrawDownDate;
    @JsonProperty
    private InterestEntity interest;
    @JsonProperty
    private AmountEntity amount;
    @JsonProperty
    private CreditEntity credit;
    @JsonProperty("following_payment")
    private FollowingPaymentEntity followingPayment;
    @JsonProperty
    private List<OwnersEntity> owners;

    public LoanAccount toTinkLoanAccount(LoansEntity loansEntity) {

        return LoanAccount.builder(loanId, getBalance().stripSign().negate())
                .setName(nickname)
                .setAccountNumber(loanFormattedId)
                .setBankIdentifier(loanId)
                .setInterestRate(interest.getRate())
                .setDetails(getLoanDetails())
                .setHolderName(getHolderName())
                .build();
    }

    private LoanDetails getLoanDetails() {

        return LoanDetails.builder(LoanDetails.Type.DERIVE_FROM_NAME)
                .setAmortized(getPaid())
                .setApplicants(getApplicants())
                .setCoApplicant(getApplicants().size() > 1)
                .setInitialBalance(getInitialBalance().stripSign().negate())
                .setInitialDate(firstDrawDownDate)
                .setLoanNumber(loanId)
                .setMonthlyAmortization(getInstalmentValue())
                .build();
    }

    public Amount getBalance() {
        return new Amount(NordeaFIConstants.CURRENCY, amount.getBalance());
    }

    private HolderName getHolderName() {
        if (owners.size() > 0) {
            return owners.get(0).getName();
        }

        return null;
    }

    public Amount getInitialBalance() {
        return new Amount(NordeaFIConstants.CURRENCY, credit.getLimit());
    }

    public Amount getPaid() {
        return new Amount(NordeaFIConstants.CURRENCY, amount.getPaid());
    }

    public List<String> getApplicants() {
        return owners.stream()
                .map(Object::toString)
                .collect(Collectors.toList());
    }

    public Amount getInstalmentValue() {
        return new Amount(NordeaFIConstants.CURRENCY, followingPayment.getInstalment());
    }
}
