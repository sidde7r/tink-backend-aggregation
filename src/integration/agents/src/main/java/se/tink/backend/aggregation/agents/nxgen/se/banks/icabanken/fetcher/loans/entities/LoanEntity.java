package se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.loans.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.api.client.util.Maps;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.IcaBankenConstants;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.loans.IcaBankenLoanParsingHelper;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanDetails;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.loan.LoanModule;
import se.tink.backend.aggregation.source_info.AccountSourceInfo;
import se.tink.libraries.account.identifiers.SwedishIdentifier;

@JsonObject
@Slf4j
public class LoanEntity {
    private String loanName;

    @JsonProperty("LoanDetails")
    private List<InterestRateMapEntity> loanDetails;

    @JsonProperty("LoanNumber")
    private String loanNumber;

    @JsonProperty("PresentDebt")
    private String presentDebt;

    @JsonProperty("Type")
    private String type;

    @JsonIgnore private Map<String, String> transformedLoanDetails;

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
                .sourceInfo(createAccountSourceInfo())
                .build();
    }

    @JsonIgnore
    private AccountSourceInfo createAccountSourceInfo() {
        return AccountSourceInfo.builder()
                .bankProductName(type) // ex. "ICA Privatlån anställd"
                .build();
    }

    @JsonIgnore
    private LoanModule buildLoanDetails(IcaBankenLoanParsingHelper loanParsingHelper) {
        return LoanModule.builder()
                .withType(getTinkLoanType())
                .withBalance(loanParsingHelper.getCurrentBalance())
                .withInterestRate(loanParsingHelper.getInterestRate())
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

    private LoanDetails.Type getTinkLoanType() {
        LoanDetails.Type loanType =
                IcaBankenConstants.AccountTypes.LOAN_TYPE_MAPPER
                        .translate(this.type)
                        .orElse(LoanDetails.Type.OTHER);
        if (LoanDetails.Type.OTHER == loanType) {
            logLoanType();
        }
        return loanType;
    }

    private void logLoanType() {
        log.info(
                "Unknown loan type: Name: {}, Type: {}",
                Optional.ofNullable(loanName).orElse("Not present"),
                Optional.ofNullable(type).orElse("Not present"));
    }
}
