package se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.authenticator.dto;

import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.dto.TypeValuePair;

public class EnrollDeviceRoundOneRequest {
    private TypeValuePair agreementIsPoliticalProminentPersonChecked;
    private TypeValuePair applicationTypeCode;
    private TypeValuePair agreementCommonReportStandardChecked;
    private TypeValuePair agreementAppConditionsChecked;
    private TypeValuePair agreementBankConditionsChecked;
    private TypeValuePair agreementDoccleChecked;

    private EnrollDeviceRoundOneRequest(TypeValuePair agreementIsPoliticalProminentPersonChecked,
            TypeValuePair applicationTypeCode, TypeValuePair agreementCommonReportStandardChecked,
            TypeValuePair agreementAppConditionsChecked, TypeValuePair agreementBankConditionsChecked,
            TypeValuePair agreementDoccleChecked) {
        this.agreementIsPoliticalProminentPersonChecked = agreementIsPoliticalProminentPersonChecked;
        this.applicationTypeCode = applicationTypeCode;
        this.agreementCommonReportStandardChecked = agreementCommonReportStandardChecked;
        this.agreementAppConditionsChecked = agreementAppConditionsChecked;
        this.agreementBankConditionsChecked = agreementBankConditionsChecked;
        this.agreementDoccleChecked = agreementDoccleChecked;
    }

    public static EnrollDeviceRoundOneRequest create(TypeValuePair agreementIsPoliticalProminentPersonChecked,
            TypeValuePair applicationTypeCode, TypeValuePair agreementCommonReportStandardChecked,
            TypeValuePair agreementAppConditionsChecked, TypeValuePair agreementBankConditionsChecked,
            TypeValuePair agreementDoccleChecked) {
        return new EnrollDeviceRoundOneRequest(agreementIsPoliticalProminentPersonChecked, applicationTypeCode,
                agreementCommonReportStandardChecked, agreementAppConditionsChecked, agreementBankConditionsChecked,
                agreementDoccleChecked);
    }

    public static EnrollDeviceRoundOneRequest createWithStandardTypes(boolean agreementIsPoliticalProminentPersonChecked,
            String applicationTypeCode, boolean agreementCommonReportStandardChecked,
            boolean agreementAppConditionsChecked, boolean agreementBankConditionsChecked,
            boolean agreementDoccleChecked) {
        return create(
                TypeValuePair.createBoolean(agreementIsPoliticalProminentPersonChecked),
                TypeValuePair.createText(applicationTypeCode),
                TypeValuePair.createBoolean(agreementCommonReportStandardChecked),
                TypeValuePair.createBoolean(agreementAppConditionsChecked),
                TypeValuePair.createBoolean(agreementBankConditionsChecked),
                TypeValuePair.createBoolean(agreementDoccleChecked));
    }

    public TypeValuePair getAgreementIsPoliticalProminentPersonChecked() {
        return agreementIsPoliticalProminentPersonChecked;
    }

    public TypeValuePair getApplicationTypeCode() {
        return applicationTypeCode;
    }

    public TypeValuePair getAgreementCommonReportStandardChecked() {
        return agreementCommonReportStandardChecked;
    }

    public TypeValuePair getAgreementAppConditionsChecked() {
        return agreementAppConditionsChecked;
    }

    public TypeValuePair getAgreementBankConditionsChecked() {
        return agreementBankConditionsChecked;
    }

    public TypeValuePair getAgreementDoccleChecked() {
        return agreementDoccleChecked;
    }
}
