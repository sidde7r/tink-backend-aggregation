package se.tink.backend.aggregation.agents.banks.seb.mortgage.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

public class GetLoanStatusSignResponse {

    private BankIdStatus status = null;
    private String errorsDescription = null;

    @JsonProperty("status")
    public BankIdStatus getStatus() {
        return status;
    }

    public void setStatus(BankIdStatus status) {
        this.status = status;
    }

    @JsonProperty("errors_description")
    public String getErrorsDescription() {
        return errorsDescription;
    }

    public void setErrorsDescription(String errorsDescription) {
        this.errorsDescription = errorsDescription;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        GetLoanStatusSignResponse that = (GetLoanStatusSignResponse) o;

        return Objects.equal(this.status, that.status)
                && Objects.equal(this.errorsDescription, that.errorsDescription);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(status, errorsDescription);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("status", status)
                .add("errorsDescription", errorsDescription)
                .toString();
    }

    public enum BankIdStatus {
        USER_SIGN,
        COMPLETE,
        NO_CLIENT,
        OUTSTANDING_TRANSACTION,
        USER_CANCEL,
        EXPIRED_TRANSACTION,
        STARTED,
        ERROR,
        USER_VALIDATION_ERROR // <- USER_VALIDATION_ERROR isn't sent from SEB, but we get 403 error
                              // status from them meaning that TryggVe validation failed
    }
}
