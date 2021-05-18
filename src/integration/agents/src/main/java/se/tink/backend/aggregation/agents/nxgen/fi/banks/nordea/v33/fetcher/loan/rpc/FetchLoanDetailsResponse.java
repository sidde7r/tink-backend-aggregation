package se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v33.fetcher.loan.rpc;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
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
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class FetchLoanDetailsResponse {
    private String currency;
    private String group;
    private InterestEntity interest;
    private AmountEntity amount;
    private CreditEntity credit;
    private List<OwnersEntity> owners;
    private String nickname;
    private String loanId;
    private String loanFormattedId;
    private String productCode;
    private String repaymentStatus;
    private FollowingPaymentEntity followingPayment;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date firstDrawDownDate;

    @JsonIgnore
    public LoanAccount toTinkLoanAccount() {
        return LoanAccount.nxBuilder()
                .withLoanDetails(getLoanModule())
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(loanId)
                                .withAccountNumber(loanFormattedId)
                                .withAccountName(getAccountName())
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

    private String getAccountName() {
        return StringUtils.isNotBlank(nickname) ? nickname : loanFormattedId;
    }

    private ExactCurrencyAmount getBalance() {
        final BigDecimal balance =
                Optional.ofNullable(credit)
                        .map(CreditEntity::getAvailable)
                        .orElseGet(amount::getBalance);
        return new ExactCurrencyAmount(balance, currency);
    }

    private ExactCurrencyAmount getInitialBalance() {
        final BigDecimal initialBalance =
                Optional.ofNullable(credit)
                        .map(CreditEntity::getLimit)
                        .orElseGet(this::getGrantedValue);
        return new ExactCurrencyAmount(initialBalance, currency);
    }

    private ExactCurrencyAmount getPaid() {
        final BigDecimal paid =
                Optional.ofNullable(credit).map(CreditEntity::getSpent).orElseGet(amount::getPaid);
        return new ExactCurrencyAmount(paid, currency);
    }

    private List<String> getApplicants() {
        return owners.stream().map(OwnersEntity::getName).collect(Collectors.toList());
    }

    private LoanDetails.Type getLoanType() {
        return NordeaFIConstants.LOAN_TYPE_MAPPER.translate(group).orElse(LoanDetails.Type.OTHER);
    }

    private BigDecimal getGrantedValue() {
        return Optional.ofNullable(amount.getGranted())
                .map(BigDecimal::abs)
                .orElseGet(amount::getDrawn);
    }
}
