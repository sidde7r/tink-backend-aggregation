package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.vavr.collection.List;
import java.util.Date;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class InsuranceEntity extends AbstractContractDetailsEntity {

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    private Date nextPaymentDate;

    private LastReceiptTypeEntity lastReceiptType;
    private List<RelatedContractEntity> relatedContracts;
    private AmountEntity premium;
    private String technicalProduct;
    private String comertialProduct;
    private StatusEntity status;

    public Date getNextPaymentDate() {
        return nextPaymentDate;
    }

    public LastReceiptTypeEntity getLastReceiptType() {
        return lastReceiptType;
    }

    public List<RelatedContractEntity> getRelatedContracts() {
        return relatedContracts;
    }

    public AmountEntity getPremium() {
        return premium;
    }

    public String getTechnicalProduct() {
        return technicalProduct;
    }

    public String getComertialProduct() {
        return comertialProduct;
    }

    public StatusEntity getStatus() {
        return status;
    }

    @Override
    protected String getAccountNumber() {
        return null;
    }
}
