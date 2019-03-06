package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities;

import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PensionPlanEntity {
    private AmountEntity liquidValue;
    private String country;
    private ProductEntity product;
    private FormatsEntity formats;
    private String counterPart;
    private MarketerBankEntity marketerBank;
    private String dueDate;
    private BranchEntity branch;
    private double shares;
    private BankEntity bank;
    private AmountEntity balance;
    private JoinTypeEntity joinType;
    private String sublevel;
    private CurrencyEntity currency;
    private String id;
    private UserCustomizationEntity userCustomization;
    private List<ParticipantEntity> participants;

    public AmountEntity getLiquidValue() {
        return liquidValue;
    }

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

    public double getShares() {
        return shares;
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
