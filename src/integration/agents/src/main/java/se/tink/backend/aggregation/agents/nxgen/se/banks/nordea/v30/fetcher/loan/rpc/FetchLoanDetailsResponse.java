package se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.fetcher.loan.rpc;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import com.google.common.base.Strings;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.AgentParsingUtils;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.NordeaSEConstants;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.fetcher.loan.entities.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.fetcher.loan.entities.CreditEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.fetcher.loan.entities.FollowingPaymentEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.fetcher.loan.entities.InterestEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.fetcher.loan.entities.OwnersEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.fetcher.loan.entities.RepaymentSchedule;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.fetcher.loan.entities.SubAgreementsItem;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanDetails;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.loan.LoanModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.loan.builder.LoanModuleBuildStep;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class FetchLoanDetailsResponse {
    private final Logger LOG = LoggerFactory.getLogger(FetchLoanDetailsResponse.class);

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

    @JsonProperty("sub_agreements")
    private List<SubAgreementsItem> subAgreements;

    @JsonProperty("repayment_schedule")
    private RepaymentSchedule repaymentSchedule;

    @JsonIgnore
    public LoanAccount toTinkLoanAccount() {
        return LoanAccount.nxBuilder()
                .withLoanDetails(getLoanModule())
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(maskAccountNumber())
                                .withAccountNumber(loanFormattedId)
                                .withAccountName(getAccountName())
                                .addIdentifier(new SwedishIdentifier(loanId))
                                .setProductName(productCode)
                                .build())
                .addHolderName(getHolderName())
                .build();
    }

    @JsonIgnore
    private LoanModule getLoanModule() {
        LoanModuleBuildStep builder =
                LoanModule.builder()
                        .withType(getLoanType())
                        .withBalance(getBalance())
                        .withInterestRate(getInterestRate().doubleValue())
                        .setAmortized(getPaid())
                        .setInitialBalance(getInitialBalance())
                        .setApplicants(getApplicants())
                        .setCoApplicant(getApplicants().size() > 1)
                        .setInitialDate(convertDateToLocalDate(firstDrawDownDate))
                        .setLoanNumber(loanId);
        if (!Objects.isNull(getInterest().getDiscountedRateEndDate())) {
            builder.setNextDayOfTermsChange(
                    convertDateToLocalDate(getInterest().getDiscountedRateEndDate()));
        }
        return builder.build();
    }

    @JsonIgnore
    private InterestEntity getInterest() {
        if (!Objects.isNull(interest) && !Objects.isNull(interest.getRate())) {
            return interest;
        } else {
            return getInterestRateFromSubAgreements();
        }
    }

    @JsonIgnore
    private InterestEntity getInterestRateFromSubAgreements() {
        List<InterestEntity> interests =
                subAgreements.stream()
                        .filter(
                                subAgreements ->
                                        Objects.nonNull(subAgreements.getInterest().getBaseRate()))
                        .map(SubAgreementsItem::getInterest)
                        .collect(Collectors.toList());
        if (interests.stream().map(InterestEntity::getRate).distinct().count() > 1) {
            throw new IllegalStateException("Interest rates in sub parts does not match.");
        }
        return interests.stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No interest rate found."));
    }

    @JsonIgnore
    private BigDecimal getInterestRate() {
        InterestEntity interest = getInterest();
        if (!Objects.isNull(interest.getRate())) {
            return AgentParsingUtils.parsePercentageFormInterest(interest.getRate());
        } else {
            throw new IllegalStateException("No interest rate found.");
        }
    }

    @JsonIgnore
    private boolean isSubAgreementsInterestRatesSame() {
        return subAgreements.stream()
                        .map(SubAgreementsItem::getInterest)
                        .map(InterestEntity::getRate)
                        .distinct()
                        .count()
                == 1;
    }

    @JsonIgnore
    public ExactCurrencyAmount getBalance() {
        return new ExactCurrencyAmount(amount.getBalance(), NordeaSEConstants.CURRENCY).negate();
    }

    @JsonSetter(nulls = Nulls.AS_EMPTY)
    public void setOwners(List<OwnersEntity> owners) {
        this.owners = owners;
    }

    public List<OwnersEntity> getOwners() {
        return owners;
    }

    @JsonIgnore
    private String getHolderName() {
        if (getOwners().size() > 0) {
            return getOwners().get(0).getName();
        }
        return null;
    }

    @JsonIgnore
    private String getAccountName() {
        return Strings.isNullOrEmpty(nickname) ? loanFormattedId : nickname;
    }

    private LocalDate convertDateToLocalDate(Date dateToConvert) {
        if (Objects.isNull(dateToConvert)) {
            return null;
        }
        return dateToConvert.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    @JsonIgnore
    private ExactCurrencyAmount getInitialBalance() {
        BigDecimal initialBalance =
                Optional.ofNullable(credit).map(CreditEntity::getLimit).orElse(amount.getGranted());
        return new ExactCurrencyAmount(initialBalance, NordeaSEConstants.CURRENCY).negate();
    }

    @JsonIgnore
    private ExactCurrencyAmount getPaid() {
        return new ExactCurrencyAmount(amount.getPaid(), NordeaSEConstants.CURRENCY);
    }

    @JsonIgnore
    public List<String> getApplicants() {
        return getOwners().stream().map(Object::toString).collect(Collectors.toList());
    }

    @JsonIgnore
    private Optional<ExactCurrencyAmount> getInstalmentValue() {
        return Optional.ofNullable(followingPayment)
                .map(
                        payment ->
                                new ExactCurrencyAmount(
                                        BigDecimal.valueOf(payment.getInstalment()),
                                        NordeaSEConstants.CURRENCY));
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
