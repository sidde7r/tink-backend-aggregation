package se.tink.backend.aggregation.agents.banks.sbab.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Optional;
import java.util.List;
import se.tink.libraries.application.GenericApplicationFieldGroup;

/**
 * Before a mortgage application can be made, a signature must be created which can be done by sending this to the bank.
 */
public class MortgageSignatureRequest {

    // The social security number/personal number on the format 'yyyymmdd-nnnn'.
    @JsonProperty("personnummer")
    private String socialSecurityNumber;

    // The number of applicants for the mortgage.
    @JsonProperty("antalSokande")
    private int numberOfApplicants;

    // The redirection url to redirect the user to when the signing is complete.
    @JsonProperty("returUrl")
    private String redirectionUrl;

    // Which type of signing client the user will use, for example Mobile BankID.
    @JsonProperty("klient")
    private String signClient;

    public String getSocialSecurityNumber() {
        return socialSecurityNumber;
    }

    public void setSocialSecurityNumber(String socialSecurityNumber) {
        this.socialSecurityNumber = socialSecurityNumber;
    }

    public int getNumberOfApplicants() {
        return numberOfApplicants;
    }

    public void setNumberOfApplicants(int numberOfApplicants) {
        this.numberOfApplicants = numberOfApplicants;
    }

    public String getRedirectionUrl() {
        return redirectionUrl;
    }

    public void setRedirectionUrl(String redirectionUrl) {
        this.redirectionUrl = redirectionUrl;
    }

    public String getSignClient() {
        return signClient;
    }

    public void setSignClient(String signClient) {
        this.signClient = signClient;
    }

    public static Optional<MortgageSignatureRequest> createFromApplication(
            List<GenericApplicationFieldGroup> applicantGroups, String ssn) {
        MortgageSignatureRequest signatureRequest = new MortgageSignatureRequest();
        signatureRequest.setNumberOfApplicants(applicantGroups.size());
        signatureRequest.setRedirectionUrl("https://www.sbab.se/");
        signatureRequest.setSocialSecurityNumber(ssn);
        signatureRequest.setSignClient("MOBIL");

        return Optional.of(signatureRequest);
    }
}
