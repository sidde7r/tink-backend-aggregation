package se.tink.backend.aggregation.agents.banks.sbab.model.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Joiner;
import java.util.Optional;
import com.google.common.collect.Lists;
import java.util.List;
import se.tink.backend.aggregation.agents.exceptions.application.InvalidApplicationException;
import se.tink.libraries.application.GenericApplicationFieldGroup;
import se.tink.backend.aggregation.rpc.Credentials;
import se.tink.backend.core.enums.ApplicationFieldName;
import se.tink.libraries.application.ApplicationFieldOptionValues;
import se.tink.backend.aggregation.log.AggregationLogger;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MortgageApplicationRequest {

    private static final AggregationLogger log = new AggregationLogger(MortgageApplicationRequest.class);

    // Signature ID created prior to making the actual application (required).
    @JsonProperty("signaturId")
    private String signatureId;

    // External reference to be connected to the application at SBAB (required).
    @JsonProperty("externRef")
    private String externalReference;

    // The id of the office where the application was initiated (required).
    @JsonProperty("kontorsId")
    private String officeId;

    // Through which channel the mortgage commitment was received (required);
    @JsonProperty("kanal")
    private String channel = "PARTNER";

    // The id of the salesperson of the mortgage (required).
    @JsonProperty("saljarId")
    private String salespersonId = "TINK";

    @JsonProperty("amorteringsregelFran20180301")
    private boolean amortizationRule2018Applicable = false;

    // The id of the salesperson, if it is different from the one sending the application (not required).
    @JsonProperty("saljarIdOriginator")
    private String salespersonIdOriginator;

    // The SBAB id for a mortgage commitment (not required).
    @JsonProperty("sbabIdLanelofte")
    private Integer sbabIdMortgageCommitment;

    // Information about the mortgage in this application (required).
    @JsonProperty("lanespec")
    private MortgageSpecification mortgageSpecification;

    // Information about the user's household (required).
    @JsonProperty("hushall")
    private Household household;

    // Information about the user's property. Cannot be used together with 'bostadsratt' (not required).
    @JsonProperty("fastighet")
    private Property property;

    // Information about the user's condominium. Cannot be used together with 'fastighet' (not required).
    @JsonProperty("bostadsratt")
    private Condominium condominium;
    
    // Other customer information (optional).
    @JsonProperty("ovrigKundinformation")
    private String otherCustomerInformation;

    public String getSignatureId() {
        return signatureId;
    }

    public void setSignatureId(String signatureId) {
        this.signatureId = signatureId;
    }

    public String getExternalReference() {
        return externalReference;
    }

    public void setExternalReference(String externalReference) {
        this.externalReference = externalReference;
    }

    public String getOfficeId() {
        return officeId;
    }
    
    public void setOfficeId(String officeId) {
        this.officeId = officeId;
    }

    public String getChannel() {
        return channel;
    }

    public String getSalespersonId() {
        return salespersonId;
    }

    public String getSalespersonIdOriginator() {
        return salespersonIdOriginator;
    }

    public void setSalespersonIdOriginator(String salespersonIdOriginator) {
        this.salespersonIdOriginator = salespersonIdOriginator;
    }

    public Integer getSbabIdMortgageCommitment() {
        return sbabIdMortgageCommitment;
    }

    public void setSbabIdMortgageCommitment(Integer sbabIdMortgageCommitment) {
        this.sbabIdMortgageCommitment = sbabIdMortgageCommitment;
    }

    public MortgageSpecification getMortgageSpecification() {
        return mortgageSpecification;
    }

    public void setMortgageSpecification(MortgageSpecification mortgageSpecification) {
        this.mortgageSpecification = mortgageSpecification;
    }

    public Household getHousehold() {
        return household;
    }

    public void setHousehold(Household household) {
        this.household = household;
    }

    public Property getProperty() {
        return property;
    }

    public void setProperty(Property property) {
        this.property = property;
    }

    public Condominium getCondominium() {
        return condominium;
    }

    public void setCondominium(Condominium condominium) {
        this.condominium = condominium;
    }
    
    public String getOtherCustomerInformation() {
        return otherCustomerInformation;
    }

    public void setOtherCustomerInformation(String otherCustomerInformation) {
        this.otherCustomerInformation = otherCustomerInformation;
    }

    public static MortgageApplicationRequest createFromApplication(
            List<GenericApplicationFieldGroup> applicantGroups, GenericApplicationFieldGroup mortgageSecurityGroup,
            GenericApplicationFieldGroup householdGroup, GenericApplicationFieldGroup currentMortgageGroup,
            Credentials credentials)
            throws InvalidApplicationException {

        MortgageApplicationRequest mortgageApplication = new MortgageApplicationRequest();

        mortgageApplication.setExternalReference(credentials.getId());
        mortgageApplication.setOfficeId("68700");
        
        mortgageApplication.setMortgageSpecification(MortgageSpecification.createFromApplication(currentMortgageGroup));

        Household household = Household.createFromApplication(
                applicantGroups, householdGroup, currentMortgageGroup, mortgageSecurityGroup);

        mortgageApplication.setHousehold(household);

        String propertyType = mortgageSecurityGroup.getField(ApplicationFieldName.PROPERTY_TYPE);
        switch (propertyType) {
        case (ApplicationFieldOptionValues.APARTMENT):
            Condominium apartment = Condominium.createFromApplication(mortgageSecurityGroup);
            mortgageApplication.setCondominium(apartment);
            break;
        case (ApplicationFieldOptionValues.HOUSE):
        case (ApplicationFieldOptionValues.VACATION_HOUSE):
            Property house = Property.createFromApplication(mortgageSecurityGroup);
            mortgageApplication.setProperty(house);
            break;
        default:
            throw new IllegalStateException(String.format("Invalid property type: %s", propertyType));
        }

        List<String> otherCustomerInformation = Lists.newArrayList();

        List<ExistingLoan> existingLoans = mortgageApplication.getHousehold().getExistingLoans();
        if (existingLoans != null && !existingLoans.isEmpty()) {
            otherCustomerInformation.add("Lånedelar:");
            for (ExistingLoan existingLoan : existingLoans) {
                if (existingLoan.isConnectedToSamePropertyAsMortgageApplication()) {
                    otherCustomerInformation.add(existingLoan.getOtherInformation());
                }
            }
        }

        Optional<Integer> estimatedMarketValue = mortgageSecurityGroup
                .tryGetFieldAsInteger(ApplicationFieldName.ESTIMATED_MARKET_VALUE);

        if (estimatedMarketValue.isPresent()) {
            otherCustomerInformation.add(String.format("Uppskattat marknadsvärde: %d", estimatedMarketValue.get()));
        }

        Optional<Integer> purchasePrice = mortgageSecurityGroup
                .tryGetFieldAsInteger(ApplicationFieldName.PURCHASE_PRICE);

        if (purchasePrice.isPresent()) {
            otherCustomerInformation.add(String.format("Köpeskilling: %d", purchasePrice.get()));
        }

        String otherInformation = Joiner.on('\n').join(otherCustomerInformation);

        // The `otherCustomerInformation` field is delimited to 500 characters.
        if (otherInformation.length() > 500) {
            otherInformation = otherInformation.substring(0, 500);
        }

        mortgageApplication.setOtherCustomerInformation(otherInformation);

        return mortgageApplication;
    }

    public boolean isAmortizationRule2018Applicable() {
        return amortizationRule2018Applicable;
    }

    public void setAmortizationRule2018Applicable(boolean amortizationRule2018Applicable) {
        this.amortizationRule2018Applicable = amortizationRule2018Applicable;
    }
}
