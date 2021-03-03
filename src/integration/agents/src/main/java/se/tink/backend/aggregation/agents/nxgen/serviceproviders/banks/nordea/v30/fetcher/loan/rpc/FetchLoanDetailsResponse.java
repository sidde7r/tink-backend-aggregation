package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.fetcher.loan.rpc;

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
import se.tink.backend.aggregation.agents.AgentParsingUtils;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.NordeaBaseConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.fetcher.loan.entities.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.fetcher.loan.entities.CreditEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.fetcher.loan.entities.FollowingPaymentEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.fetcher.loan.entities.InterestEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.fetcher.loan.entities.OwnersEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.fetcher.loan.entities.RepaymentSchedule;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.fetcher.loan.entities.SubAgreementsItem;
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
    public Optional<LoanAccount> toTinkLoanAccount() {
        Optional<LoanModule> loanModule = getLoanModule();
        if (!loanModule.isPresent()) {
            return Optional.empty();
        }
        return Optional.of(
                LoanAccount.nxBuilder()
                        .withLoanDetails(loanModule.get())
                        .withId(
                                IdModule.builder()
                                        .withUniqueIdentifier(maskAccountNumber())
                                        .withAccountNumber(loanFormattedId)
                                        .withAccountName(getAccountName())
                                        .addIdentifier(new SwedishIdentifier(loanId))
                                        .setProductName(productCode)
                                        .build())
                        .addHolderName(getHolderName())
                        .build());
    }

    @JsonIgnore
    private Optional<LoanModule> getLoanModule() {
        Optional<InterestEntity> interestEntity = getInterest();
        if (!interestEntity.isPresent()) {
            return Optional.empty();
        }

        LoanModuleBuildStep builder =
                LoanModule.builder()
                        .withType(getTinkLoanType())
                        .withBalance(getBalance())
                        .withInterestRate(getInterestRate().orElse(BigDecimal.ZERO).doubleValue())
                        .setAmortized(getPaid())
                        .setInitialBalance(getInitialBalance())
                        .setApplicants(getApplicants())
                        .setCoApplicant(getApplicants().size() > 1)
                        .setInitialDate(convertDateToLocalDate(firstDrawDownDate))
                        .setLoanNumber(loanId);
        if (!Objects.isNull(getInterest().get().getDiscountedRateEndDate())) {
            builder.setNextDayOfTermsChange(
                    convertDateToLocalDate(getInterest().get().getDiscountedRateEndDate()));
        }
        return Optional.of(builder.build());
    }

    @JsonIgnore
    private LoanDetails.Type getTinkLoanType() {
        return NordeaBaseConstants.LOAN_TYPE_MAPPER.translate(group).orElse(LoanDetails.Type.OTHER);
    }

    @JsonIgnore
    private Optional<InterestEntity> getInterest() {
        if (!Objects.isNull(interest) && !Objects.isNull(interest.getRate())) {
            return Optional.of(interest);
        } else {
            return getInterestRateFromSubAgreements();
        }
    }

    @JsonIgnore
    private Optional<InterestEntity> getInterestRateFromSubAgreements() {
        List<InterestEntity> interests =
                subAgreements.stream()
                        .map(SubAgreementsItem::getInterest)
                        .collect(Collectors.toList());

        if (allRatesMatch(interests)) {
            return interests.stream().findFirst();
        }

        if (allRatesMatchWhenBaseRateMissingAreIgnored(interests)) {
            return interests.stream()
                    .filter(interest -> interest.getBaseRate() != null)
                    .findFirst();
        }
        return Optional.empty();
    }

    // When base rate is missing, the rate is the base rate, so we filter them out and check that
    // the remaining ones have the same rate.
    private boolean allRatesMatchWhenBaseRateMissingAreIgnored(List<InterestEntity> interests) {
        return interests.stream()
                        .filter(interest -> interest.getBaseRate() != null)
                        .map(InterestEntity::getRate)
                        .distinct()
                        .count()
                == 1;
    }

    private boolean allRatesMatch(List<InterestEntity> interests) {
        return interests.stream().map(InterestEntity::getRate).distinct().count() == 1;
    }

    @JsonIgnore
    private Optional<BigDecimal> getInterestRate() {
        Optional<InterestEntity> interest = getInterest();
        if (!interest.isPresent()) {
            return Optional.empty();
        }

        if (!Objects.isNull(interest.get().getRate())) {
            return Optional.of(
                    AgentParsingUtils.parsePercentageFormInterest(interest.get().getRate()));
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
        return new ExactCurrencyAmount(amount.getBalance(), NordeaBaseConstants.CURRENCY).negate();
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
        return new ExactCurrencyAmount(initialBalance, NordeaBaseConstants.CURRENCY).negate();
    }

    @JsonIgnore
    private ExactCurrencyAmount getPaid() {
        return new ExactCurrencyAmount(amount.getPaid(), NordeaBaseConstants.CURRENCY);
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
                                        NordeaBaseConstants.CURRENCY));
    }

    // This method used for setting uniqueId is taken from the legacy Nordea agent.
    @JsonIgnore
    private String maskAccountNumber() {
        return "************" + loanId.substring(loanId.length() - 4);
    }
}
