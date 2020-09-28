package se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.loans.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.api.client.util.Maps;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.loans.IcaBankenLoanParsingHelper;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanDetails;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.loan.LoanModule;
import se.tink.libraries.account.identifiers.SwedishIdentifier;

@JsonObject
public class LoanEntity {
    @JsonIgnore private static final Logger log = LoggerFactory.getLogger(LoanEntity.class);

    private String loanName;
    private double initialDebt;
    private long initialDate;
    private String applicants;

    @JsonProperty("InterestRatesDetails")
    private List<InterestRateMapEntity> interestRatesDetails;

    @JsonProperty("LoanDetails")
    private List<InterestRateMapEntity> loanDetails;

    @JsonProperty("LoanNumber")
    private String loanNumber;

    @JsonProperty("PresentDebt")
    private String presentDebt;

    @JsonProperty("Type")
    private String type;

    @JsonIgnore private Map<String, String> transformedLoanDetails;

    private Double parseInterestRate(InterestRateMapEntity interestRatesDetails) {
        return Double.parseDouble(interestRatesDetails.getValue().split(",")[0].replace(",", "."));
    }

    public Double getInterestRate() {
        if (interestRatesDetails.isEmpty()) {
            log.info("interestRatesDetails has no elements: {}", interestRatesDetails);
        }
        return interestRatesDetails.stream()
                .filter(i -> LocalDate.now().toString().compareTo(i.getKey()) > 0)
                .map(this::parseInterestRate)
                .findAny()
                .orElseThrow(IllegalStateException::new);
    }

    public void setLoanDetails(List<InterestRateMapEntity> loanDetails) {
        this.loanDetails = loanDetails;
        this.transformedLoanDetails = buildLoanDetailsMap();
    }

    @JsonIgnore
    private Map<String, String> buildLoanDetailsMap() {
        Map<String, String> map = Maps.newHashMap();

        for (InterestRateMapEntity keyValuePair : loanDetails) {
            map.put(keyValuePair.getKey().toLowerCase().trim(), keyValuePair.getValue());
        }

        return map;
    }

    @JsonIgnore
    public LoanAccount toTinkLoan() {
        IcaBankenLoanParsingHelper loanParsingHelper =
                new IcaBankenLoanParsingHelper(transformedLoanDetails);

        return LoanAccount.nxBuilder()
                .withLoanDetails(buildLoanDetails(loanParsingHelper))
                .withId(getIdModule(loanParsingHelper))
                .build();
    }

    @JsonIgnore
    private LoanModule buildLoanDetails(IcaBankenLoanParsingHelper loanParsingHelper) {
        logLoanType();

        return LoanModule.builder()
                .withType(LoanDetails.Type.BLANCO)
                .withBalance(loanParsingHelper.getCurrentBalance())
                .withInterestRate(getInterestRate())
                .setAmortized(loanParsingHelper.getAmortized(presentDebt))
                .setApplicants(loanParsingHelper.getApplicantsList())
                .setInitialBalance(loanParsingHelper.getInitialBalance())
                .setInitialDate(
                        loanParsingHelper
                                .getInitialDate()
                                .toInstant()
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate())
                .setCoApplicant(loanParsingHelper.hasCoApplicant())
                .build();
    }

    private IdModule getIdModule(IcaBankenLoanParsingHelper loanParsingHelper) {
        return IdModule.builder()
                .withUniqueIdentifier(loanNumber)
                .withAccountNumber(loanNumber)
                .withAccountName(loanParsingHelper.getLoanName())
                .addIdentifier(new SwedishIdentifier(loanNumber))
                .build();
    }

    private void logLoanType() {
        log.info(
                "Unknown loan type: Name: {}, Type: {}",
                Optional.ofNullable(loanName).orElse("Not present"),
                Optional.ofNullable(type).orElse("Not present"));
    }
}
