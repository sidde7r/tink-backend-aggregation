package se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.loan.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.api.client.util.Lists;
import com.google.common.base.Strings;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.AgentParsingUtils;
import se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.LansforsakringarConstants.Accounts;
import se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.LansforsakringarConstants.LogTags;
import se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.loan.entities.BorrowersEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.loan.entities.SecuritiesEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanDetails.Type;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.loan.LoanModule;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class FetchLoanDetailsResponse {
    @JsonIgnore
    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private String loanName;
    private String loanNumber;
    private String originalDebt;
    private String currentDebt;
    private String currentInterestRate;
    private String rateBoundUntil;
    private String rateBindingPeriodLength;
    private List<BorrowersEntity> borrowers;
    private List<SecuritiesEntity> securities;
    private boolean fixedRate;
    private String modificationStatus;
    // `infoText` is null - cannot define it!
    private boolean nearExpiryDate;
    // `bindingPeriodInfoModel` is null - cannot define it!

    @JsonIgnore
    public LoanAccount toTinkLoanAccount() {
        return LoanAccount.nxBuilder()
                .withLoanDetails(getLoanModule())
                .withId(getIdModule())
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

    private LoanModule getLoanModule() {
        return LoanModule.builder()
                .withType(getLoanType())
                .withBalance(ExactCurrencyAmount.of(getDebt(), Accounts.CURRENCY))
                .withInterestRate(getCurrentInterestrate())
                .setLoanNumber(loanNumber)
                .setApplicants(getApplicants())
                .setCoApplicant(hasCoapplicant())
                .setSecurity(getSecurities())
                .build();
    }

    private String getSecurities() {
        if (securities == null) {
            return "";
        }
        return securities.stream()
                .map(SecuritiesEntity::toString)
                .reduce("", (s1, s2) -> s1 + "\n" + s2);
    }

    private boolean hasCoapplicant() {
        if (borrowers != null) {
            return borrowers.size() > 1;
        }
        return false;
    }

    private List<String> getApplicants() {
        if (borrowers == null) {
            return Lists.newArrayList();
        }
        return borrowers.stream()
                .map(BorrowersEntity::getName)
                .filter(applicant -> !Strings.isNullOrEmpty(applicant))
                .collect(Collectors.toList());
    }

    private Type getLoanType() {
        if (Strings.isNullOrEmpty(loanName)) {
            return Type.OTHER;
        } else if (loanName.toLowerCase().contains("bolån")) {
            return Type.MORTGAGE;
        } else {
            logger.info("tag={} Found new unknown entity", LogTags.UNKNOWN_LOAN_TYPE);
            return Type.OTHER;
        }
    }

    private BigDecimal getDebt() {
        if (!Strings.isNullOrEmpty(currentDebt)) {
            return BigDecimal.valueOf(-1 * Math.abs(AgentParsingUtils.parseAmount(currentDebt)));
        }
        return null;
    }

    private double getCurrentInterestrate() {
        return AgentParsingUtils.parsePercentageFormInterest(currentInterestRate);
    }
}
