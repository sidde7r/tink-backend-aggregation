package se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.fetcher.entity;

import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public final class IngElement {

    private String uuid;
    private String productNumber;
    private Long sequence;
    private Double balance;
    private String tranCode;
    private String comment;
    private String description;
    private Double amount;
    private Integer categoryId;
    private Boolean hasUncertainCategorization;
    private String effectiveDate;
    private IngStatus status;
    private String cardNumber;
    private String operationId;
    private String crossingCode;
    private List<IngDetectedCategory> detectedCategories;

    public String getUuid() {
        return uuid;
    }

    public String getProductNumber() {
        return productNumber;
    }

    public Long getSequence() {
        return sequence;
    }

    public Double getBalance() {
        return balance;
    }

    public String getTranCode() {
        return tranCode;
    }

    public String getComment() {
        return comment;
    }

    public String getDescription() {
        return description;
    }

    public Double getAmount() {
        return amount;
    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public Boolean getHasUncertainCategorization() {
        return hasUncertainCategorization;
    }

    public String getEffectiveDate() {
        return effectiveDate;
    }

    public IngStatus getStatus() {
        return status;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public String getOperationId() {
        return operationId;
    }

    public String getCrossingCode() {
        return crossingCode;
    }

    public List<IngDetectedCategory> getDetectedCategories() {
        return detectedCategories;
    }
}
