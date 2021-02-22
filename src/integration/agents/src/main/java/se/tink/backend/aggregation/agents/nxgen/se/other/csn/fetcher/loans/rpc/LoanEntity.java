package se.tink.backend.aggregation.agents.nxgen.se.other.csn.fetcher.loans.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.se.other.csn.CSNConstants;
import se.tink.backend.aggregation.nxgen.core.account.entity.Party;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanDetails;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.loan.LoanModule;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.AccountIdentifier.Type;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class LoanEntity {
    private static final Logger log = LoggerFactory.getLogger(LoanEntity.class);

    @JsonProperty("skuldspecifikation")
    private List<DebtDetailEntity> debtSpecification;

    @JsonProperty("laneTypKlartext")
    private String loanTypePlainText;

    @JsonProperty("lopnummer")
    private int serialNumber;

    @JsonProperty("senasteBerakningsdatum")
    private long latestCalculationDate;

    @JsonProperty("lanetyp")
    private String loanType;

    @JsonProperty("skuldbelopp")
    private BigDecimal debtAmount;

    @JsonProperty("skuldrattat")
    private String debtCorrected;

    @JsonProperty("klartext")
    private boolean isPlainText;

    public boolean isLoanAccount() {
        switch (loanType) {
            case CSNConstants.LoanTypes.ANNUTITY_LOAN:
            case CSNConstants.LoanTypes.STUDENT_LOAN:
            case CSNConstants.LoanTypes.STUDENT_AID:
                return true;
            default:
                log.info("loanType: " + loanType);
                return false;
        }
    }

    public LoanAccount toTinkLoanAccount(
            LoanTransactionsResponse loanTransactions,
            UserInfoResponse userInfo,
            LoanAccountsResponse loanAccount) {
        return LoanAccount.nxBuilder()
                .withLoanDetails(getLoanModule(loanTransactions, loanAccount))
                .withId(getIdModule(userInfo))
                .addParties(new Party(userInfo.getName(), Party.Role.HOLDER))
                .build();
    }

    private LoanModule getLoanModule(
            LoanTransactionsResponse loanTransactions, LoanAccountsResponse loanAccount) {
        return LoanModule.builder()
                .withType(LoanDetails.Type.STUDENT)
                .withBalance(getOutgoingDebt().negate())
                .withInterestRate(loanAccount.getInterestRate().doubleValue())
                .setLoanNumber(getAccountNumber())
                .setAmortized(
                        ExactCurrencyAmount.of(
                                loanTransactions.getTotalAmortization(serialNumber),
                                CSNConstants.CURRENCY))
                .build();
    }

    private IdModule getIdModule(UserInfoResponse userInfo) {
        return IdModule.builder()
                .withUniqueIdentifier(getUniqueIdenifier(userInfo.getSsn()))
                .withAccountNumber(getAccountNumber())
                .withAccountName(getAccountName())
                .addIdentifier(AccountIdentifier.create(Type.TINK, getAccountNumber()))
                .build();
    }

    private ExactCurrencyAmount getOutgoingDebt() {
        return ExactCurrencyAmount.of(
                debtSpecification.stream()
                        .filter(DebtDetailEntity::isOutgoingDebt)
                        .map(DebtDetailEntity::getAmount)
                        .findFirst()
                        .orElse(null),
                CSNConstants.CURRENCY);
    }

    private String getUniqueIdenifier(String ssn) {
        // From legacy agent, we are parsing the strings below as unique IDs.
        // We don't get those string in any request which is why we need to parse them hardcoded
        // from loan type.
        switch (loanType) {
            case CSNConstants.LoanTypes.ANNUTITY_LOAN:
                // Unique ID from legacy: "Lån efter den 30 juni 2001 (annuitetslån)"
                // Translated: "Loan after 30 june 2001 (annuity loan)"
                return ssn + "annuitetslan";
            case CSNConstants.LoanTypes.STUDENT_LOAN:
                // Unique ID from legacy: "Lån 1 januari 1989-30 juni 2001 (studielån)"
                // Translated: "Loan 1 january 1989-30 june 2001 (student loan)"
                return ssn + "studielan";
            case CSNConstants.LoanTypes.STUDENT_AID:
                // Unique ID from legacy: "Lån före 1 januari 1989 (studiemedel)"
                // Translated: "Loan before 1 january 1989 (student aid)"
                return ssn + "studiemedel";
            default:
                return loanType;
        }
    }

    private String getAccountNumber() {
        switch (loanType) {
            case CSNConstants.LoanTypes.ANNUTITY_LOAN:
                return "Lån efter den 30 juni 2001 (annuitetslån)";
            case CSNConstants.LoanTypes.STUDENT_LOAN:
                return "Lån 1 januari 1989-30 juni 2001 (studielån)";
            case CSNConstants.LoanTypes.STUDENT_AID:
                return "Lån före 1 januari 1989 (studiemedel)";
            default:
                log.info("loanType: " + loanType);
                return loanType;
        }
    }

    private String getAccountName() {
        switch (loanType) {
            case CSNConstants.LoanTypes.ANNUTITY_LOAN:
                return "Annuitetslån";
            case CSNConstants.LoanTypes.STUDENT_LOAN:
                return "Studielån";
            case CSNConstants.LoanTypes.STUDENT_AID:
                return "Studiemedel";
            default:
                return loanType;
        }
    }
}
