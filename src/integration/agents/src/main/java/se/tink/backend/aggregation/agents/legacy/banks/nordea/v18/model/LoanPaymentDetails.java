package se.tink.backend.aggregation.agents.banks.nordea.v18.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import se.tink.backend.aggregation.agents.banks.nordea.NordeaHashMapDeserializer;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LoanPaymentDetails {

    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    private String date;

    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    private String interest;

    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    private String expenses;

    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    private String total;

    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    private String amortisation;

    private boolean pending;

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getInterest() {
        return interest;
    }

    public void setInterest(String interest) {
        this.interest = interest;
    }

    public String getExpenses() {
        return expenses;
    }

    public void setExpenses(String expenses) {
        this.expenses = expenses;
    }

    public String getTotal() {
        return total;
    }

    public void setTotal(String total) {
        this.total = total;
    }

    public String getAmortisation() {
        return amortisation;
    }

    public void setAmortisation(String amortisation) {
        this.amortisation = amortisation;
    }
}
