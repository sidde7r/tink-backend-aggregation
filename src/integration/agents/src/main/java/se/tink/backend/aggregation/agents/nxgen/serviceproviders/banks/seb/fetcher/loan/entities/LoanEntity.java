package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.seb.fetcher.loan.entities;

import com.amazonaws.util.StringUtils;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import se.tink.backend.aggregation.agents.AgentParsingUtils;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.seb.SebConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.compliance.account_capabilities.AccountCapabilities.Answer;
import se.tink.backend.aggregation.nxgen.core.account.entity.Party;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanDetails.Type;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.loan.LoanModule;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
public class LoanEntity {

    @JsonProperty("KONTRAKTNR")
    private String loanNumber;

    @JsonProperty("KTOSLAG_TXT")
    private String loanName;

    @JsonProperty("SKULD")
    private double currentDebt;

    @JsonProperty("RTE_SATS")
    private String interestRate;

    @JsonProperty("DATRTEJUST")
    private String nextDayOfTermsChange;

    @JsonProperty("AMORTERING")
    private Double monthlyAmortization;

    @JsonProperty("LANTAGARE1")
    private String applicant1;

    @JsonProperty("LANTAGARE2")
    private String applicant2;

    @JsonProperty("FLER_LANTAGARE_FL")
    private String multipleApplicants;

    @JsonProperty("OBJBETD1")
    private String objectInfo1;

    @JsonProperty("OBJBETD2")
    private String objectInfo2;

    @JsonProperty("OBJBETD3")
    private String objectInfo3;

    @JsonProperty("OBJBETD4")
    private String objectInfo4;

    public LoanAccount toTinkLoanAccount(Type loanType) {
        return LoanAccount.nxBuilder()
                .withLoanDetails(getLoanModule(loanType))
                .withId(getIdModule())
                .canWithdrawCash(Answer.NO)
                .canPlaceFunds(Answer.UNKNOWN)
                .canExecuteExternalTransfer(Answer.NO)
                .canReceiveExternalTransfer(Answer.NO)
                .addParties(getParties())
                .build();
    }

    private LoanModule getLoanModule(Type loanType) {
        return LoanModule.builder()
                .withType(loanType)
                .withBalance(getBalance())
                .withInterestRate(getInterestRate())
                .setApplicants(getApplicants())
                .setCoApplicant(hasCoApplicant())
                .setMonthlyAmortization(getMonthlyAmortization())
                .setLoanNumber(loanNumber)
                .setNextDayOfTermsChange(getNextDayOfTermsChange())
                .build();
    }

    private IdModule getIdModule() {
        return IdModule.builder()
                .withUniqueIdentifier(loanNumber)
                .withAccountNumber(loanNumber)
                .withAccountName(loanName)
                .addIdentifier(new SwedishIdentifier(loanNumber))
                .build();
    }

    private List<Party> getParties() {
        return Stream.of(applicant1, applicant2)
                .map(name -> new Party(name, Party.Role.HOLDER))
                .collect(Collectors.toList());
    }

    private ExactCurrencyAmount getBalance() {
        return new ExactCurrencyAmount(
                        BigDecimal.valueOf(currentDebt), SebConstants.DEFAULT_CURRENCY)
                .negate();
    }

    private ExactCurrencyAmount getMonthlyAmortization() {
        if (monthlyAmortization == null) {
            return null;
        }

        return new ExactCurrencyAmount(
                BigDecimal.valueOf(monthlyAmortization), SebConstants.DEFAULT_CURRENCY);
    }

    private LocalDate getNextDayOfTermsChange() {
        if (!StringUtils.isNullOrEmpty(nextDayOfTermsChange)) {
            return LocalDate.parse(nextDayOfTermsChange, DateTimeFormatter.ISO_LOCAL_DATE);
        }

        return null;
    }

    private Double getInterestRate() {
        return AgentParsingUtils.parsePercentageFormInterest(interestRate);
    }

    private List<String> getApplicants() {
        List<String> applicants = Lists.newArrayList(applicant1, applicant2);
        applicants.removeIf(Strings::isNullOrEmpty);

        return applicants;
    }

    private boolean hasCoApplicant() {
        return "J".equals(multipleApplicants);
    }
}
