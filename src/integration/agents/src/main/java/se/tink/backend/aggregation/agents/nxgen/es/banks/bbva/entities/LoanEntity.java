package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanDetails;
import static se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.BbvaTypeMappers.LOAN_TYPE_MAPPER;

@JsonObject
public class LoanEntity {
    private String country;
    private AmountEntity awardedAmount;
    private ProductEntity product;
    private String nextPaymentDate;
    private AmountEntity pendingamount;
    private FormatsEntity formats;
    private LoanTypeEntity loanType;
    private AmountEntity nextFee;
    private String counterPart;
    private MarketerBankEntity marketerBank;
    private String dueDate;
    private BranchEntity branch;
    private AmountEntity redeemedBalance;
    private BankEntity bank;
    private AmountEntity finalFee;
    private JoinTypeEntity joinType;
    private String sublevel;
    private CurrencyEntity currency;
    private String id;
    private UserCustomizationEntity userCustomization;
    private List<ParticipantEntity> participants;
    private String digit;

    public String getCountry() {
        return country;
    }

    public AmountEntity getAwardedAmount() {
        return awardedAmount;
    }

    public ProductEntity getProduct() {
        return product;
    }

    public String getNextPaymentDate() {
        return nextPaymentDate;
    }

    public AmountEntity getPendingamount() {
        return pendingamount;
    }

    public FormatsEntity getFormats() {
        return formats;
    }

    public LoanTypeEntity getLoanType() {
        return loanType;
    }

    public AmountEntity getNextFee() {
        return nextFee;
    }

    public String getCounterPart() {
        return counterPart;
    }

    public MarketerBankEntity getMarketerBank() {
        return marketerBank;
    }

    public String getDueDate() {
        return dueDate;
    }

    public BranchEntity getBranch() {
        return branch;
    }

    public AmountEntity getRedeemedBalance() {
        return redeemedBalance;
    }

    public BankEntity getBank() {
        return bank;
    }

    public AmountEntity getFinalFee() {
        return finalFee;
    }

    public JoinTypeEntity getJoinType() {
        return joinType;
    }

    public String getSublevel() {
        return sublevel;
    }

    public CurrencyEntity getCurrency() {
        return currency;
    }

    public String getId() {
        return id;
    }

    public UserCustomizationEntity getUserCustomization() {
        return userCustomization;
    }

    public List<ParticipantEntity> getParticipants() {
        return participants;
    }

    public String getDigit() {
        return digit;
    }

    @JsonIgnore
    public LoanDetails.Type getTinkLoanType() {
        return Optional.ofNullable(loanType)
                .map(LoanTypeEntity::getId)
                .map(LOAN_TYPE_MAPPER::translate)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .orElse(LoanDetails.Type.OTHER);
    }

    @JsonIgnore
    private LoanAccount.Builder buildTinkLoanAccount() {
        return LoanAccount.builder(digit)
                .setBalance(pendingamount.toTinkAmount().negate())
                .setBankIdentifier(digit)
                .setAccountNumber(digit)
                .setName(product.getDescription());
    }

    @JsonIgnore
    public LoanAccount toTinkLoanAccount() {
        final LoanDetails loanDetails = LoanDetails.builder(getTinkLoanType())
                .setInitialBalance(awardedAmount.toTinkAmount())
                .setLoanNumber(digit)
                .setMonthlyAmortization(nextFee.toTinkAmount())
                .setAmortized(redeemedBalance.toTinkAmount())
                .build();

        return (LoanAccount) buildTinkLoanAccount()
                .setDetails(loanDetails)
                .build();
    }

    @JsonIgnore
    public LoanAccount toTinkLoanAccount(double interestRate, LoanDetails loanDetails) {
        return (LoanAccount)
                buildTinkLoanAccount()
                        .setInterestRate(interestRate)
                        .setDetails(loanDetails)
                        .build();
    }
}
