package se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v33.fetcher.loan.rpc;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v33.NordeaFIConstants;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v33.fetcher.loan.entities.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v33.fetcher.loan.entities.CreditEntity;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v33.fetcher.loan.entities.FollowingPaymentEntity;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v33.fetcher.loan.entities.InterestEntity;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v33.fetcher.loan.entities.OwnersEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanDetails;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.loan.LoanModule;
import se.tink.libraries.account.identifiers.FinnishIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class FetchLoanDetailsResponse {
    @JsonProperty private String currency;
    @JsonProperty private String group;
    @JsonProperty private InterestEntity interest;
    @JsonProperty private AmountEntity amount;
    @JsonProperty private CreditEntity credit;
    @JsonProperty private List<OwnersEntity> owners;
    @JsonProperty private String nickname = "Nordea loan";

    @JsonProperty("loan_id")
    private String loanId;

    @JsonProperty("loan_formatted_id")
    private String loanFormattedId;

    @JsonProperty("product_code")
    private String productCode;

    @JsonProperty("repayment_status")
    private String repaymentStatus;

    @JsonProperty("following_payment")
    private FollowingPaymentEntity followingPayment;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @JsonProperty("first_draw_down_date")
    private Date firstDrawDownDate;

    @JsonIgnore
    public LoanAccount toTinkLoanAccount() {
        return LoanAccount.nxBuilder()
                .withLoanDetails(getLoanModule())
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(loanId)
                                .withAccountNumber(loanFormattedId)
                                .withAccountName(nickname)
                                .addIdentifier(new FinnishIdentifier(loanId))
                                .setProductName(productCode)
                                .build())
                .build();
    }

    @JsonIgnore
    private LoanModule getLoanModule() {
        return LoanModule.builder()
                .withType(getLoanType())
                .withBalance(getBalance())
                .withInterestRate(interest.getRate())
                .setAmortized(getPaid())
                .setInitialBalance(getInitialBalance())
                .setApplicants(getApplicants())
                .setCoApplicant(getApplicants().size() > 1)
                .setLoanNumber(loanId)
                .build();
    }

    @JsonIgnore
    private ExactCurrencyAmount getBalance() {
        final BigDecimal balance =
                Optional.ofNullable(credit)
                        .map(CreditEntity::getAvailable)
                        .orElse(amount.getBalance());
        return new ExactCurrencyAmount(balance, NordeaFIConstants.CURRENCY);
    }

    @JsonIgnore
    private ExactCurrencyAmount getInitialBalance() {
        final BigDecimal initialBalance =
                Optional.ofNullable(credit).map(CreditEntity::getLimit).orElse(getGrantedValue());
        return new ExactCurrencyAmount(initialBalance, NordeaFIConstants.CURRENCY);
    }

    @JsonIgnore
    private ExactCurrencyAmount getPaid() {
        final BigDecimal paid =
                Optional.ofNullable(credit).map(CreditEntity::getSpent).orElse(amount.getPaid());
        return new ExactCurrencyAmount(paid, NordeaFIConstants.CURRENCY);
    }

    @JsonIgnore
    private List<String> getApplicants() {
        return owners.stream().map(Object::toString).collect(Collectors.toList());
    }

    @JsonIgnore
    private LoanDetails.Type getLoanType() {
        return NordeaFIConstants.LOAN_TYPE_MAPPER.translate(group).orElse(LoanDetails.Type.OTHER);
    }

    private BigDecimal getGrantedValue() {
        return Optional.ofNullable(amount.getGranted())
                .map(BigDecimal::abs)
                .orElse(amount.getDrawn());
    }
}
