package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities;

import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PrepaidCardEntity {
    private String country;
    private FormatsEntity formats;
    private ConsolidatedBalanceEntity consolidatedBalance;
    private LegacyProductEntity legacyProduct;
    private MarketerBankEntity marketerBank;
    private String dueDate;
    private String migrationType;
    private TypeEntity type;
    private BranchEntity branch;
    private AvailableBalanceEntity availableBalance;
    private BankEntity bank;
    private CurrencyEntity currency;
    private String id;
    private String pan;
    private String stampDate;
    private List<ParticipantEntity> participants;
    private ProductEntity product;
    private String counterPart;
    private IndicatorsEntity indicators;
    private OperativeLimitEntity operativeLimit;
    private JoinTypeEntity joinType;
    private String sublevel;
    private PaymentMethodEntity paymentMethod;
    private UserCustomizationEntity userCustomization;
    private StatusEntity status;

    public String getCountry() {
        return country;
    }

    public FormatsEntity getFormats() {
        return formats;
    }

    public ConsolidatedBalanceEntity getConsolidatedBalance() {
        return consolidatedBalance;
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

    public BranchEntity getBranch() {
        return branch;
    }

    public AvailableBalanceEntity getAvailableBalance() {
        return availableBalance;
    }

    public BankEntity getBank() {
        return bank;
    }

    public CurrencyEntity getCurrency() {
        return currency;
    }

    public String getId() {
        return id;
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

    public ProductEntity getProduct() {
        return product;
    }

    public String getCounterPart() {
        return counterPart;
    }

    public IndicatorsEntity getIndicators() {
        return indicators;
    }

    public OperativeLimitEntity getOperativeLimit() {
        return operativeLimit;
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

    public UserCustomizationEntity getUserCustomization() {
        return userCustomization;
    }

    public StatusEntity getStatus() {
        return status;
    }
}
