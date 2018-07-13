package se.tink.backend.aggregation.agents.banks.handelsbanken.v6.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EInvoicesGroupedEntity {
    private String type;
    private String name;
    private String number;
    @JsonProperty("eInvoiceDTO")
    private List<PaymentEntity> eInvoices;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public List<PaymentEntity> geteInvoices() {
        return eInvoices;
    }

    public void seteInvoices(List<PaymentEntity> eInvoices) {
        this.eInvoices = eInvoices;
    }
}
