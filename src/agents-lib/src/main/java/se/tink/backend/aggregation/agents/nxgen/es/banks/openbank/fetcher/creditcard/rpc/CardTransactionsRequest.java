package se.tink.backend.aggregation.agents.nxgen.es.banks.openbank.fetcher.creditcard.rpc;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CardTransactionsRequest {
    @JsonProperty("fechaHasta")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date toDate;

    @JsonProperty("fechaDesde")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date fromDate;

    @JsonProperty("numeroContrato")
    private String contractNumber;

    @JsonProperty("producto")
    private String productCode;

    @JsonProperty("pan")
    private String cardNumber;

    public Date getFromDate() {
        return fromDate;
    }

    public void setFromDate(Date fromDate) {
        this.fromDate = fromDate;
    }

    public String getContractNumber() {
        return contractNumber;
    }

    public void setContractNumber(String contractNumber) {
        this.contractNumber = contractNumber;
    }

    public Date getToDate() {
        return toDate;
    }

    public void setToDate(Date toDate) {
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
