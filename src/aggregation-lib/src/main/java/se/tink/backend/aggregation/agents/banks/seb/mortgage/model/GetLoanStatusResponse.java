package se.tink.backend.aggregation.agents.banks.seb.mortgage.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

public class GetLoanStatusResponse {

    private MortgageStatus status = null;
    private String description = null;

    @JsonProperty("status")
    public MortgageStatus getStatus() {
        return status;
    }

    public void setStatus(MortgageStatus status) {
        this.status = status;
    }

    @JsonProperty("description")
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        GetLoanStatusResponse that = (GetLoanStatusResponse) o;

        return Objects.equal(this.status, that.status) &&
                Objects.equal(this.description, that.description);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(status, description);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("status", status)
                .add("description", description)
                .toString();
    }

}

