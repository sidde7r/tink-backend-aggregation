package se.tink.backend.aggregation.agents.banks.nordea.v20.model.payments;

import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import se.tink.libraries.account.AccountIdentifier;

public class Payment {
    public enum SubType {
        EINVOICE("eInvoice"), NORMAL("Normal"), NOTIFIED_PAYMENT("NotifiedPayment"), THIRDPARTY("ThirdParty");

        private final String serializedValue;

        SubType(String serializedValue) {
            this.serializedValue = serializedValue;
        }

        public String getSerializedValue() {
            return serializedValue;
        }

        public Predicate<PaymentEntity> predicateForType() {
            final String serializedValue = getSerializedValue();
            return paymentEntity -> paymentEntity != null &&
                    Objects.equal(paymentEntity.getPaymentSubType(), serializedValue);
        }

        public static SubType fromSerializedValue(String serializedValue) {
            for (SubType subType : SubType.values()) {
                if (Objects.equal(subType.getSerializedValue(), serializedValue)) {
                    return subType;
                }
            }

            return null;
        }
    }

    public enum SubTypeExtension {
        SE_BG("BGType", AccountIdentifier.Type.SE_BG), SE_PG("PGType", AccountIdentifier.Type.SE_PG);

        private final String serializedValue;
        private final AccountIdentifier.Type type;

        SubTypeExtension(String serializedValue, AccountIdentifier.Type type) {
            this.serializedValue = serializedValue;
            this.type = type;
        }

        public String getSerializedValue() {
            return serializedValue;
        }

        public Predicate<PaymentEntity> predicateForType() {
            final String serializedValue = getSerializedValue();
            return paymentEntity -> paymentEntity != null &&
                    Objects.equal(paymentEntity.getPaymentSubTypeExtension(), serializedValue);
        }

        public static SubTypeExtension fromSerializedValue(String serializedValue) {
            for (SubTypeExtension subTypeExtension : SubTypeExtension.values()) {
                if (Objects.equal(subTypeExtension.getSerializedValue(), serializedValue)) {
                    return subTypeExtension;
                }
            }

            return null;
        }

        AccountIdentifier.Type getType() {
            return type;
        }
    }

    public enum StatusCode {
        UNCONFIRMED("Unconfirmed"), CONFIRMED("Confirmed");

        private String serializedValue;

        StatusCode(String serializedValue) {
            this.serializedValue = serializedValue;
        }

        public String getSerializedValue() {
            return serializedValue;
        }

        public Predicate<PaymentEntity> predicateForType() {
            final String serializedValue = getSerializedValue();
            return paymentEntity -> paymentEntity != null &&
                    Objects.equal(paymentEntity.getStatusCode(), serializedValue);
        }

        public static StatusCode fromSerializedValue(String serializedValue) {
            for (StatusCode statusCode : StatusCode.values()) {
                if (Objects.equal(statusCode.getSerializedValue(), serializedValue)) {
                    return statusCode;
                }
            }

            return null;
        }
    }
}
