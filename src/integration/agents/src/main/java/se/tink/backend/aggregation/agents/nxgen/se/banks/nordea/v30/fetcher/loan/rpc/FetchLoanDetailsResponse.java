package se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.fetcher.loan.rpc;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.NordeaSEConstants;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.fetcher.loan.entities.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.fetcher.loan.entities.CreditEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.fetcher.loan.entities.FollowingPaymentEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.fetcher.loan.entities.InterestEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.fetcher.loan.entities.OwnersEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.core.account.entity.HolderName;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanDetails;
import se.tink.libraries.amount.Amount;

@JsonObject
public class FetchLoanDetailsResponse {
    @JsonIgnore
    private static final AggregationLogger LOG =
            new AggregationLogger(FetchLoanDetailsResponse.class);

    @JsonProperty("loan_id")
    private String loanId;

    @JsonProperty("loan_formatted_id")
    private String loanFormattedId;

    @JsonProperty("product_code")
    private String productCode;

    @JsonProperty private String currency;
    @JsonProperty private String group;

    @JsonProperty("repayment_status")
    private String repaymentStatus;

    @JsonProperty private String nickname;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @JsonProperty("first_draw_down_date")
    private Date firstDrawDownDate;

    @JsonProperty private InterestEntity interest;
    @JsonProperty private AmountEntity amount;
    @JsonProperty private CreditEntity credit;

    @JsonProperty("following_payment")
    private FollowingPaymentEntity followingPayment;

    @JsonProperty private List<OwnersEntity> owners;

    @JsonIgnore
    public LoanAccount toTinkLoanAccount() {
        return LoanAccount.builder(maskAccountNumber(), getBalance().stripSign().negate())
                .setName(getNickname())
                .setAccountNumber(loanFormattedId)
                .setBankIdentifier(maskAccountNumber())
                .setInterestRate(interest.getRate())
                .setDetails(getLoanDetails())
                .setHolderName(getHolderName())
                .build();
    }

    @JsonIgnore
    private LoanDetails getLoanDetails() {
        return LoanDetails.builder(getLoanType())
                .setAmortized(getPaid())
                .setApplicants(getApplicants())
                .setCoApplicant(getApplicants().size() > 1)
                .setInitialBalance(getInitialBalance().stripSign().negate())
                .setInitialDate(firstDrawDownDate)
                .setLoanNumber(loanId)
                .setMonthlyAmortization(getInstalmentValue())
                .build();
    }

    @JsonIgnore
    public Amount getBalance() {
        return new Amount(NordeaSEConstants.CURRENCY, amount.getBalance());
    }

    @JsonSetter(nulls = Nulls.AS_EMPTY)
    public void setOwners(List<OwnersEntity> owners) {
        this.owners = owners;
    }

    public List<OwnersEntity> getOwners() {
        return owners;
    }

    @JsonIgnore
    private HolderName getHolderName() {
        if (getOwners().size() > 0) {
            return getOwners().get(0).getName();
        }

        return null;
    }

    @JsonIgnore
    private String getNickname() {
        return (nickname != null && nickname.isEmpty()) ? null : nickname;
    }

    @JsonIgnore
    public Amount getInitialBalance() {
        return new Amount(NordeaSEConstants.CURRENCY, credit.getLimit());
    }

    @JsonIgnore
    private Amount getPaid() {
        return new Amount(NordeaSEConstants.CURRENCY, amount.getPaid());
    }

    @JsonIgnore
    public List<String> getApplicants() {
        return getOwners().stream().map(Object::toString).collect(Collectors.toList());
    }

    @JsonIgnore
    private Amount getInstalmentValue() {
        return new Amount(NordeaSEConstants.CURRENCY, followingPayment.getInstalment());
    }

    // TODO: Map all the different Nordea loan type accounts
    @JsonIgnore
    private LoanDetails.Type getLoanType() {
        switch (group.toUpperCase()) {
            case "MORTGAGE":
                return LoanDetails.Type.MORTGAGE;
            default:
                LOG.info(
                        "Logging not mortgage loan accounts "
                                + NordeaSEConstants.LogTags.LOAN_ACCOUNT);
                return LoanDetails.Type.OTHER;
        }
    }

    // This method used for setting uniqueId is taken from the legacy Nordea agent.
    @JsonIgnore
    private String maskAccountNumber() {
        return "************" + loanId.substring(loanId.length() - 4);
    }
}
