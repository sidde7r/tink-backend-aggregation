package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities.loan;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.vavr.collection.List;
import java.util.Date;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities.BankEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities.BranchEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities.CurrencyEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities.FormatsEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities.JoinTypeEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities.MarketerBankEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities.ParticipantEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities.ProductEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities.UserCustomizationEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BaseLoanEntity {
    private String country;
    private ProductEntity product;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    private Date nextPaymentDate;

    private FormatsEntity formats;
    private AmountEntity nextFee;
    private String counterPart;
    private MarketerBankEntity marketerBank;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    private Date dueDate;

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

    public String getCountry() {
        return country;
    }

    public ProductEntity getProduct() {
        return product;
    }

    public Date getNextPaymentDate() {
        return nextPaymentDate;
    }

    public FormatsEntity getFormats() {
        return formats;
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

    public Date getDueDate() {
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
}
