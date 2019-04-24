package se.tink.libraries.identitydata.countries;

import java.util.Map;
import se.tink.libraries.identitydata.IdentityData;

public class EsIdentityData extends IdentityData {

    private final String nieNumber;
    private final String nifNumber;
    private final String passportNumber;
    private final String otherDocumentNumber;

    private EsIdentityData(Builder builder) {
        super(builder);
        nieNumber = builder.nieNumber;
        nifNumber = builder.nifNumber;
        passportNumber = builder.passportNumber;
        otherDocumentNumber = builder.otherDocumentNumber;
    }

    public static EsIdentityDataBuilder builder() {
        return new Builder();
    }

    public interface EsIdentityDataBuilder extends IdentityData.InitialBuilderStep {
        EsIdentityDataBuilder setNieNumber(String val);

        EsIdentityDataBuilder setNifNumber(String val);

        EsIdentityDataBuilder setPassportNumber(String val);

        EsIdentityDataBuilder setDocumentNumber(String val);
    }

    public static final class Builder extends IdentityData.Builder
            implements EsIdentityDataBuilder {
        private String nieNumber;
        private String nifNumber;
        private String passportNumber;
        private String otherDocumentNumber;

        protected Builder() {}

        public EsIdentityDataBuilder setNieNumber(String val) {
            nieNumber = val;
            return this;
        }

        public EsIdentityDataBuilder setNifNumber(String val) {
            nifNumber = val;
            return this;
        }

        public EsIdentityDataBuilder setPassportNumber(String val) {
            passportNumber = val;
            return this;
        }

        @Override
        public EsIdentityDataBuilder setDocumentNumber(String val) {
            val = EsIdentityDocumentType.trimDni(val);

            switch (EsIdentityDocumentType.typeOf(val)) {
                case NIF:
                    nifNumber = val;
                    break;
                case NIE:
                    nieNumber = val;
                    break;
                case OTHER:
                    otherDocumentNumber = val;
                    break;
            }
            return this;
        }

        public EsIdentityData build() {
            return new EsIdentityData(this);
        }
    }

    @Override
    public Map<String, String> toMap() {
        Map<String, String> map = baseMap();
        if (nieNumber != null) {
            map.put("nieNumber", nieNumber);
        }
        if (nifNumber != null) {
            map.put("nifNumber", nifNumber);
        }
        if (passportNumber != null) {
            map.put("passportNumber", passportNumber);
        }
        if (otherDocumentNumber != null) {
            map.put("otherDocumentNumber", otherDocumentNumber);
        }

        return map;
    }

    @Override
    public String getSsn() {
        if (nieNumber != null) {
            return nieNumber;
        } else if (nifNumber != null) {
            return nifNumber;
        } else if (passportNumber != null) {
            return passportNumber;
        } else {
            return otherDocumentNumber;
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

    public String getOtherDocumentNumber() {
        return otherDocumentNumber;
    }
}
