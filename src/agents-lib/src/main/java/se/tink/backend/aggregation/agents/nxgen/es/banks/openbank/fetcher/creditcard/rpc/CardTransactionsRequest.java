package se.tink.backend.aggregation.agents.nxgen.es.banks.openbank.fetcher.creditcard.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CardTransactionsRequest {
    @JsonProperty("fechaHasta")
    private String fromDate;

    @JsonProperty("fechaDesde")
    private String toDate;

    @JsonProperty("numeroContrato")
    private String contractNumber;

    @JsonProperty("producto")
    private String productCode;

    @JsonProperty("pan")
    private String cardNumber;

    public String getFromDate() {
        return fromDate;
    }

    public void setFromDate(String fromDate) {
        this.fromDate = fromDate;
    }

    public String getContractNumber() {
        return contractNumber;
    }

    public void setContractNumber(String contractNumber) {
        this.contractNumber = contractNumber;
    }

    public String getToDate() {
        return toDate;
    }

    public void setToDate(String toDate) {
        this.toDate = toDate;
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }
}
