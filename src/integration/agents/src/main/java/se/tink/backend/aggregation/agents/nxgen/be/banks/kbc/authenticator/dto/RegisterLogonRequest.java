package se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.authenticator.dto;

import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.dto.TypeValuePair;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class RegisterLogonRequest {
    private final TypeValuePair captcha;
    private final TypeValuePair response;
    private final TypeValuePair saveCardNumber;
    private final TypeValuePair applicationId;
    private final TypeValuePair language;
    private final TypeValuePair company;
    private final TypeValuePair username;
    private final TypeValuePair ucrType;

    private RegisterLogonRequest(
            TypeValuePair captcha,
            TypeValuePair response,
            TypeValuePair saveCardNumber,
            TypeValuePair applicationId,
            TypeValuePair language,
            TypeValuePair company,
            TypeValuePair username,
            TypeValuePair ucrType) {
        this.captcha = captcha;
        this.response = response;
        this.saveCardNumber = saveCardNumber;
        this.applicationId = applicationId;
        this.language = language;
        this.company = company;
        this.username = username;
        this.ucrType = ucrType;
    }

    public static Builder builder() {
        return new Builder();
    }

    public TypeValuePair getCaptcha() {
        return captcha;
    }

    public TypeValuePair getResponse() {
        return response;
    }

    public TypeValuePair getSaveCardNumber() {
        return saveCardNumber;
    }

    public TypeValuePair getApplicationId() {
        return applicationId;
    }

    public TypeValuePair getLanguage() {
        return language;
    }

    public TypeValuePair getCompany() {
        return company;
    }

    public TypeValuePair getUsername() {
        return username;
    }

    public TypeValuePair getUcrType() {
        return ucrType;
    }

    public static class Builder {
        private TypeValuePair captcha;
        private TypeValuePair response;
        private TypeValuePair saveCardNumber;
        private TypeValuePair applicationId;
        private TypeValuePair language;
        private TypeValuePair company;
        private TypeValuePair username;
        private TypeValuePair ucrType;

        private Builder() {}

        public Builder captcha(String captcha) {
            this.captcha = TypeValuePair.createText(captcha);
            return this;
        }

        public Builder response(String response) {
            this.response = TypeValuePair.createText(response);
            ;
            return this;
        }

        public Builder saveCardNumber(boolean saveCardNumber) {
            this.saveCardNumber = TypeValuePair.createBoolean(saveCardNumber);
            return this;
        }

        public Builder applicationId(String applicationId) {
            this.applicationId = TypeValuePair.createText(applicationId);
            ;
            return this;
        }

        public Builder language(String language) {
            this.language = TypeValuePair.createText(language);
            ;
            return this;
        }

        public Builder company(String company) {
            this.company = TypeValuePair.createText(company);
            ;
            return this;
        }

        public Builder username(String username) {
            this.username = TypeValuePair.createText(username);
            ;
            return this;
        }

        public Builder ucrType(String ucrType) {
            this.ucrType = TypeValuePair.createText(ucrType);
            ;
            return this;
        }

        public RegisterLogonRequest build() {
            return new RegisterLogonRequest(
                    captcha,
                    response,
                    saveCardNumber,
                    applicationId,
                    language,
                    company,
                    username,
                    ucrType);
        }
    }
}
