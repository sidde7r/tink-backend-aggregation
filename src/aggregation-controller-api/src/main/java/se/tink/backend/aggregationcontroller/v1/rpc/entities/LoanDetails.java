package se.tink.backend.aggregationcontroller.v1.rpc.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import java.util.UUID;
import se.tink.libraries.serialization.utils.SerializationUtils;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LoanDetails {
    private UUID accountId;
    private Boolean coApplicant;
    private String applicants;
    private String loanSecurity;

    public UUID getAccountId() {
        return accountId;
    }

    public void setAccountId(UUID accountId) {
        this.accountId = accountId;
    }

    public Boolean getCoApplicant() {
        return coApplicant;
    }

    public void setCoApplicant(Boolean coApplicant) {
        this.coApplicant = coApplicant;
    }

    @SuppressWarnings("unchecked")
    public List<String> getApplicants() {
        return SerializationUtils.deserializeFromString(applicants, List.class);
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
        this.loanSecurity = loanSecurity;
    }
}
