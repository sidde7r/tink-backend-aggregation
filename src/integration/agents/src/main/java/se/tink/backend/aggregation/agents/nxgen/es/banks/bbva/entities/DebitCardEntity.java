package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities;

import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class DebitCardEntity {
    private String country;
    private ProductEntity product;
    private FormatsEntity formats;
    private String counterPart;
    private LegacyProductEntity legacyProduct;
    private MarketerBankEntity marketerBank;
    private String dueDate;
    private String migrationType;
    private TypeEntity type;
    private IndicatorsEntity indicators;
    private BranchEntity branch;
    private AmountEntity availableBalance;
    private BankEntity bank;
    private JoinTypeEntity joinType;
    private String sublevel;
    private PaymentMethodEntity paymentMethod;
    private CurrencyEntity currency;
    private String id;
    private UserCustomizationEntity userCustomization;
    private String pan;
    private String stampDate;
    private List<ParticipantEntity> participants;
    private StatusEntity status;

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

    public LegacyProductEntity getLegacyProduct() {
        return legacyProduct;
    }

    public MarketerBankEntity getMarketerBank() {
        return marketerBank;
    }

    public String getDueDate() {
        return dueDate;
    }

    public String getMigrationType() {
        return migrationType;
    }

    public TypeEntity getType() {
        return type;
    }

    public IndicatorsEntity getIndicators() {
        return indicators;
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

    public PaymentMethodEntity getPaymentMethod() {
        return paymentMethod;
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

    public String getPan() {
        return pan;
    }

    public String getStampDate() {
        return stampDate;
    }

    public List<ParticipantEntity> getParticipants() {
        return participants;
    }

    public StatusEntity getStatus() {
        return status;
    }
}
