package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.vavr.collection.List;
import java.util.Date;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CreditCardEntity {
    private String country;
    private FormatsEntity formats;
    private LegacyProductEntity legacyProduct;
    private MarketerBankEntity marketerBank;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    private Date dueDate;

    private String migrationType;
    private TypeEntity type;
    private BranchEntity branch;
    private AmountEntity availableBalance;
    private BankEntity bank;
    private AmountEntity limit;
    private CurrencyEntity currency;
    private String id;
    private String pan;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    private Date stampDate;

    private List<ParticipantEntity> participants;
    private ProductEntity product;
    private String counterPart;
    private AmountEntity disposedAmount;
    private IndicatorsEntity indicators;
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

    public LegacyProductEntity getLegacyProduct() {
        return legacyProduct;
    }

    public MarketerBankEntity getMarketerBank() {
        return marketerBank;
    }

    public Date getDueDate() {
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

    public AmountEntity getAvailableBalance() {
        return availableBalance;
    }

    public BankEntity getBank() {
        return bank;
    }

    public AmountEntity getLimit() {
        return limit;
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

    public Date getStampDate() {
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

    public AmountEntity getDisposedAmount() {
        return disposedAmount;
    }

    public IndicatorsEntity getIndicators() {
        return indicators;
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
