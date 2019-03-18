package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.vavr.collection.List;
import java.util.Date;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountEntity {
    private String country;
    private ProductEntity product;
    private FormatsEntity formats;
    private String counterPart;
    private MarketerBankEntity marketerBank;
    private AmountEntity currentBalance;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    private Date dueDate;

    private AmountEntity availableBalanceLocalCurrency;
    private BranchEntity branch;
    private AmountEntity availableBalance;
    private BankEntity bank;
    private JoinTypeEntity joinType;
    private String sublevel;
    private CurrencyEntity currency;
    private String id;
    private UserCustomizationEntity userCustomization;
    private List<ParticipantEntity> participants;
    private AmountEntity currentBalanceLocalCurrency;

    public String getCountry() {
        return country;
    }

    public ProductEntity getProduct() {
        return product;
    }

    public FormatsEntity getFormats() {
        return formats;
    }

    public String getCounterPart() {
        return counterPart;
    }

    public MarketerBankEntity getMarketerBank() {
        return marketerBank;
    }

    public AmountEntity getCurrentBalance() {
        return currentBalance;
    }

    public Date getDueDate() {
        return dueDate;
    }

    public AmountEntity getAvailableBalanceLocalCurrency() {
        return availableBalanceLocalCurrency;
    }

    public BranchEntity getBranch() {
        return branch;
    }

    public AmountEntity getAvailableBalance() {
        return availableBalance;
    }

    public BankEntity getBank() {
        return bank;
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

    public AmountEntity getCurrentBalanceLocalCurrency() {
        return currentBalanceLocalCurrency;
    }
}
