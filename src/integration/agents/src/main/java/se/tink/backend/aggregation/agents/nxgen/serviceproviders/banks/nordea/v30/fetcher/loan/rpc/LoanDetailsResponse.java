package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.fetcher.loan.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.google.common.base.Strings;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.AgentParsingUtils;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.NordeaBaseConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.fetcher.loan.NordeaLoanParsingUtils;
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
import se.tink.backend.aggregation.utils.json.deserializers.LocalDateDeserializer;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class LoanDetailsResponse {

    private String loanId;
    private String loanFormattedId;
    private String productCode;
    private String currency;
    private String group;
    private String repaymentStatus;
    private String nickname;

    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate firstDrawDownDate;

    private InterestEntity interest;
    private AmountEntity amount;
    private CreditEntity credit;
    private FollowingPaymentEntity followingPayment;
    private List<OwnersEntity> owners;
    private List<SubAgreementsItem> subAgreements;
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
                                        .withUniqueIdentifier(
                                                NordeaLoanParsingUtils.loanIdToUniqueIdentifier(
                                                        loanId))
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
        Optional<InterestEntity> interestEntity = getInterestForLoanModule();
        if (!interestEntity.isPresent()) {
            return Optional.empty();
        }

        LoanModuleBuildStep builder =
                LoanModule.builder()
                        .withType(getTinkLoanType())
                        .withBalance(amount.getTinkBalance())
                        .withInterestRate(getInterestRate().orElse(BigDecimal.ZERO).doubleValue())
                        .setAmortized(amount.getTinkAmortized())
                        .setInitialBalance(getInitialBalance())
                        .setApplicants(getApplicants())
                        .setCoApplicant(owners.size() > 1)
                        .setInitialDate(firstDrawDownDate)
                        .setLoanNumber(loanId);
        if (!Objects.isNull(interestEntity.get().getDiscountedRateEndDate())) {
            builder.setNextDayOfTermsChange(interestEntity.get().getDiscountedRateEndDate());
        }
        return Optional.of(builder.build());
    }

    @JsonIgnore
    private LoanDetails.Type getTinkLoanType() {
        return NordeaBaseConstants.LOAN_TYPE_MAPPER.translate(group).orElse(LoanDetails.Type.OTHER);
    }

    @JsonIgnore
    private Optional<InterestEntity> getInterestForLoanModule() {
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
                    .filter(interestEntity -> interestEntity.getBaseRate() != null)
                    .findFirst();
        }
        return Optional.empty();
    }

    // When base rate is missing, the rate is the base rate, so we filter them out and check that
    // the remaining ones have the same rate.
    private boolean allRatesMatchWhenBaseRateMissingAreIgnored(List<InterestEntity> interests) {
        return interests.stream()
                        .filter(interestEntity -> interestEntity.getBaseRate() != null)
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
        Optional<InterestEntity> interestEntity = getInterestForLoanModule();
        if (!interestEntity.isPresent()) {
            return Optional.empty();
        }

        if (!Objects.isNull(interestEntity.get().getRate())) {
            return Optional.of(
                    AgentParsingUtils.parsePercentageFormInterest(interestEntity.get().getRate()));
        } else {
            throw new IllegalStateException("No interest rate found.");
        }
    }

    @JsonIgnore
    private String getHolderName() {
        if (owners.size() > 0) {
            return owners.get(0).getName();
        }
        return null;
    }

    @JsonIgnore
    private String getAccountName() {
        return Strings.isNullOrEmpty(nickname) ? loanFormattedId : nickname;
    }

    @JsonIgnore
    private ExactCurrencyAmount getInitialBalance() {
        BigDecimal initialBalance =
                Optional.ofNullable(credit).map(CreditEntity::getLimit).orElse(amount.getGranted());
        return new ExactCurrencyAmount(initialBalance, NordeaBaseConstants.CURRENCY).negate();
    }

    @JsonIgnore
    public List<String> getApplicants() {
        return owners.stream().map(Object::toString).collect(Collectors.toList());
    }
}
