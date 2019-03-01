package se.tink.libraries.customerinfo.countries;

import se.tink.libraries.customerinfo.CustomerInfo;

public class EsCustomerInfo extends CustomerInfo {

    private final String nieNumber;
    private final String nifNumber;
    private final String passportNumber;

    private EsCustomerInfo(Builder builder) {
        super(builder);
        nieNumber = builder.nieNumber;
        nifNumber = builder.nifNumber;
        passportNumber = builder.passportNumber;
    }

    public static EsCustomerInfoBuilder builder() {
        return new Builder();
    }

    public interface EsCustomerInfoBuilder extends CustomerInfo.InitialBuilderStep {
        EsCustomerInfoBuilder setNieNumber(String val);

        EsCustomerInfoBuilder setNifNumber(String val);

        EsCustomerInfoBuilder setPassportNumber(String val);
    }

    public static final class Builder extends CustomerInfo.Builder
            implements EsCustomerInfoBuilder {
        private String nieNumber;
        private String nifNumber;
        private String passportNumber;

        protected Builder() {}

        public EsCustomerInfoBuilder setNieNumber(String val) {
            nieNumber = val;
            return this;
        }

        public EsCustomerInfoBuilder setNifNumber(String val) {
            nifNumber = val;
            return this;
        }

        public EsCustomerInfoBuilder setPassportNumber(String val) {
            passportNumber = val;
            return this;
        }

        public EsCustomerInfo build() {
            return new EsCustomerInfo(this);
        }
    }

    public String getNieNumber() {
        return nieNumber;
    }

    public String getNifNumber() {
        return nifNumber;
    }

    public String getPassportNumber() {
        return passportNumber;
    }
}
