package se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.authenticator.dto;

import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.dto.HeaderResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.dto.TypeValuePair;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PersonalisationResponse extends HeaderResponse {
    private TypeValuePair userId;
    private TypeValuePair company;
    private TypeValuePair lang;
    private TypeValuePair name;
    private TypeValuePair firstName;
    private TypeValuePair lastName;
    private TypeValuePair pan;
    private TypeValuePair saveCredentials;
    private TypeValuePair logonMethod;
    private TransferLimits transferOtherLimits;
    private TransferLimits transferAtmLimits;
    private TypeValuePair modifyNewLimitAllowed;
    private TypeValuePair tacVersionNumber;
    private TypeValuePair agreementAppConditionsRequired;
    private TypeValuePair agreementBankConditionsRequired;
    private TypeValuePair agreementCommonReportStandardRequired;
    private TypeValuePair agreementDoccleRequired;

    public TypeValuePair getUserId() {
        return userId;
    }

    public TypeValuePair getCompany() {
        return company;
    }

    public TypeValuePair getLang() {
        return lang;
    }

    public TypeValuePair getName() {
        return name;
    }

    public TypeValuePair getFirstName() {
        return firstName;
    }

    public TypeValuePair getLastName() {
        return lastName;
    }

    public TypeValuePair getPan() {
        return pan;
    }

    public TypeValuePair getSaveCredentials() {
        return saveCredentials;
    }

    public TypeValuePair getLogonMethod() {
        return logonMethod;
    }

    public TransferLimits getTransferOtherLimits() {
        return transferOtherLimits;
    }

    public TransferLimits getTransferAtmLimits() {
        return transferAtmLimits;
    }

    public TypeValuePair getModifyNewLimitAllowed() {
        return modifyNewLimitAllowed;
    }

    public TypeValuePair getTacVersionNumber() {
        return tacVersionNumber;
    }

    public TypeValuePair getAgreementAppConditionsRequired() {
        return agreementAppConditionsRequired;
    }

    public TypeValuePair getAgreementBankConditionsRequired() {
        return agreementBankConditionsRequired;
    }

    public TypeValuePair getAgreementCommonReportStandardRequired() {
        return agreementCommonReportStandardRequired;
    }

    public TypeValuePair getAgreementDoccleRequired() {
        return agreementDoccleRequired;
    }
}
