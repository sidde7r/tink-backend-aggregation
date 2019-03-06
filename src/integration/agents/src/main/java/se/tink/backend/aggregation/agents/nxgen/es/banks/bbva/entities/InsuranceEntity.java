package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities;

import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class InsuranceEntity {
    private String country;
    private ProductEntity product;
    private String nextPaymentDate;
    private FormatsEntity formats;
    private String counterPart;
    private MarketerBankEntity marketerBank;
    private String dueDate;
    private LastReceiptTypeEntity lastReceiptType;
    private BranchEntity branch;
    private BankEntity bank;
    private List<RelatedContractEntity> relatedContracts;
    private PremiumEntity premium;
    private JoinTypeEntity joinType;
    private String technicalProduct;
    private String sublevel;
    private CurrencyEntity currency;
    private String comertialProduct;
    private String id;
    private UserCustomizationEntity userCustomization;
    private List<ParticipantEntity> participants;
    private StatusEntity status;

    public String getCountry() {
        return country;
    }

    public ProductEntity getProduct() {
        return product;
    }

    public String getNextPaymentDate() {
        return nextPaymentDate;
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

    public LastReceiptTypeEntity getLastReceiptType() {
        return lastReceiptType;
    }

    public BranchEntity getBranch() {
        return branch;
    }

    public BankEntity getBank() {
        return bank;
    }

    public List<RelatedContractEntity> getRelatedContracts() {
        return relatedContracts;
    }

    public PremiumEntity getPremium() {
        return premium;
    }

    public JoinTypeEntity getJoinType() {
        return joinType;
    }

    public String getTechnicalProduct() {
        return technicalProduct;
    }

    public String getSublevel() {
        return sublevel;
    }

    public CurrencyEntity getCurrency() {
        return currency;
    }

    public String getComertialProduct() {
        return comertialProduct;
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

    public StatusEntity getStatus() {
        return status;
    }
}
