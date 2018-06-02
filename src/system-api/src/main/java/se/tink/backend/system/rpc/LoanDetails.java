package se.tink.backend.system.rpc;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import java.util.List;
import java.util.UUID;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class LoanDetails {
    private UUID accountId;
    private Boolean coApplicant;
    private String applicants;
    private String loanSecurity;

    public LoanDetails() {

    }

    public UUID getAccountId() {
        return accountId;
    }

    public void setAccountId(UUID accountId) {
        this.accountId = accountId;
    }

    public List<String> getApplicants() {
        List<String> applicantsAsList = SerializationUtils.deserializeFromString(applicants, List.class);
        return applicantsAsList;
    }

    public void setApplicants(List<String> applicants) {
        String serializedApplicants = SerializationUtils.serializeToString(applicants);
        if (serializedApplicants != null) {
            this.applicants = serializedApplicants.toLowerCase();
        }

    }

    public String getLoanSecurity() {
        return loanSecurity;
    }

    public void setLoanSecurity(String loanSecurity) {
        this.loanSecurity = loanSecurity != null ? loanSecurity.toLowerCase() : null;
    }

    public Boolean isCoApplicant() {
        return coApplicant;
    }

    public void setCoApplicant(Boolean hasCoApplicant) {
        this.coApplicant = hasCoApplicant;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("accountId", accountId)
                .add("coApplicant", coApplicant)
                .add("applicants", applicants)
                .add("loanSecurity", loanSecurity)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        LoanDetails that = (LoanDetails) o;

        return Objects.equal(this.accountId, that.accountId) &&
                Objects.equal(this.coApplicant, that.coApplicant) &&
                Objects.equal(this.applicants, that.applicants) &&
                Objects.equal(this.loanSecurity, that.loanSecurity);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(accountId, coApplicant, applicants, loanSecurity);
    }

    public boolean hasUpdatedSince(LoanDetails previousLoanDetails) {
        Preconditions.checkNotNull(previousLoanDetails);
        Preconditions.checkArgument(accountId.equals(previousLoanDetails.accountId));

        if (coApplicant != null && previousLoanDetails.coApplicant == null) {
            return true;
        }
        if (coApplicant != null && !Objects.equal(coApplicant, previousLoanDetails.coApplicant)) {
            return true;
        }
        if (applicants != null && previousLoanDetails.applicants == null) {
            return true;
        }
        if (applicants != null && !Objects.equal(applicants, previousLoanDetails.applicants)) {
            return true;
        }
        if (loanSecurity != null && previousLoanDetails.loanSecurity == null) {
            return true;
        }
        if (loanSecurity != null && !Objects.equal(loanSecurity, previousLoanDetails.loanSecurity)) {
            return true;
        }

        return false;
    }

    public Boolean getCoApplicant() {
        return coApplicant;
    }
}
