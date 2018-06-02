package se.tink.backend.product.execution.unit.agents.sbab.mortgage.model.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BorrowerWithNewFields extends Borrower {
    @JsonProperty("yrkesroll")
    private String profession;

    @JsonProperty("anstallningstidpunkt")
    private String employmentDate;

    public String getProfession() {
        return profession;
    }

    public void setProfession(String profession) {
        this.profession = profession;
    }

    public String getEmploymentDate() {
        return employmentDate;
    }

    public void setEmploymentDate(String employmentDate) {
        this.employmentDate = employmentDate;
    }
}
