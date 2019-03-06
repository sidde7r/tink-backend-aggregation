package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities;

import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanDetails;
import se.tink.libraries.amount.Amount;

@JsonObject
public class LoanEntity {
    private String country;
    private AwardedAmountEntity awardedAmount;
    private ProductEntity product;
    private String nextPaymentDate;
    private PendingamountEntity pendingamount;
    private FormatsEntity formats;
    private LoanTypeEntity loanType;
    private NextFeeEntity nextFee;
    private String counterPart;
    private MarketerBankEntity marketerBank;
    private String dueDate;
    private BranchEntity branch;
    private RedeemedBalanceEntity redeemedBalance;
    private BankEntity bank;
    private FinalFeeEntity finalFee;
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

    public AwardedAmountEntity getAwardedAmount() {
        return awardedAmount;
    }

    public ProductEntity getProduct() {
        return product;
    }

    public String getNextPaymentDate() {
        return nextPaymentDate;
    }

    public PendingamountEntity getPendingamount() {
        return pendingamount;
    }

    public FormatsEntity getFormats() {
        return formats;
    }

    public LoanTypeEntity getLoanType() {
        return loanType;
    }

    public NextFeeEntity getNextFee() {
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

    public RedeemedBalanceEntity getRedeemedBalance() {
        return redeemedBalance;
    }

    public BankEntity getBank() {
        return bank;
    }

    public FinalFeeEntity getFinalFee() {
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
}
