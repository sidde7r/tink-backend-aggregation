package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities;

import io.vavr.collection.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SecuritiesPortfolioEntity {
    private String country;
    private ProductEntity product;
    private FormatsEntity formats;
    private String counterPart;
    private MarketerBankEntity marketerBank;
    private String dueDate;
    private BranchEntity branch;
    private List<SecurityEntity> securities;
    private BankEntity bank;
    private AmountEntity balance;
    private JoinTypeEntity joinType;
    private String sublevel;
    private CurrencyEntity currency;
    private String id;
    private UserCustomizationEntity userCustomization;
    private List<ParticipantEntity> participants;

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

    public String getDueDate() {
        return dueDate;
    }

    public BranchEntity getBranch() {
        return branch;
    }

    public List<SecurityEntity> getSecurities() {
        return securities;
    }

    public BankEntity getBank() {
        return bank;
    }

    public AmountEntity getBalance() {
        return balance;
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
}
