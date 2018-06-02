package se.tink.backend.product.execution.unit.agents.sbab.mortgage.model.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class InterestRateEntity {

    @JsonProperty("listRantesats")
    private double listRate;

    @JsonProperty("prisdiffRantesats")
    private double customerRate;

    @JsonProperty("bindningstidManader")
    private double contractDurationInMonths;

    public double getListRate() {
        return listRate;
    }

    public void setListRate(double listRate) {
        this.listRate = listRate;
    }

    public double getCustomerRate() {
        return customerRate;
    }

    public void setCustomerRate(double customerRate) {
        this.customerRate = customerRate;
    }

    public double getContractDurationInMonths() {
        return contractDurationInMonths;
    }

    public void setContractDurationInMonths(double contractDurationInMonths) {
        this.contractDurationInMonths = contractDurationInMonths;
    }
}
