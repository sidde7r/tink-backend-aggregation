package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities;

import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountEntity {
    private String country;
    private ProductEntity product;
    private FormatsEntity formats;
    private String counterPart;
    private MarketerBankEntity marketerBank;
    private CurrentBalanceEntity currentBalance;
    private String dueDate;
    private AvailableBalanceLocalCurrencyEntity availableBalanceLocalCurrency;
    private BranchEntity branch;
    private AvailableBalanceEntity availableBalance;
    private BankEntity bank;
    private JoinTypeEntity joinType;
    private String sublevel;
    private CurrencyEntity currency;
    private String id;
    private UserCustomizationEntity userCustomization;
    private List<ParticipantEntity> participants;
    private CurrentBalanceLocalCurrencyEntity currentBalanceLocalCurrency;

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

    public CurrentBalanceEntity getCurrentBalance() {
        return currentBalance;
    }

    public String getDueDate() {
        return dueDate;
    }

    public AvailableBalanceLocalCurrencyEntity getAvailableBalanceLocalCurrency() {
        return availableBalanceLocalCurrency;
    }

    public BranchEntity getBranch() {
        return branch;
    }

    public AvailableBalanceEntity getAvailableBalance() {
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

    public CurrentBalanceLocalCurrencyEntity getCurrentBalanceLocalCurrency() {
        return currentBalanceLocalCurrency;
    }
}
