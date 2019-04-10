package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v21.fetcher.entities.payments;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;
import org.apache.commons.lang.StringUtils;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.serializer.NordeaHashMapDeserializer;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PaymentEntity {
    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    private String paymentId;

    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    private String paymentType;

    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    private String paymentSubType;

    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    private String paymentSubTypeExtension;

    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    private String beneficiaryNickName;

    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    private String beneficiaryName;

    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    private String fromAccountId;

    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    private String currency;

    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    private String amount;

    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    private String dueDate;

    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    private String paymentDate;

    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    private String statusCode;

    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    private String toAccountId;

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public String getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(String paymentType) {
        this.paymentType = paymentType;
    }

    public String getPaymentSubType() {
        return paymentSubType;
    }

    public void setPaymentSubType(String paymentSubType) {
        this.paymentSubType = paymentSubType;
    }

    public String getPaymentSubTypeExtension() {
        return paymentSubTypeExtension;
    }

    public void setPaymentSubTypeExtension(String paymentSubTypeExtension) {
        this.paymentSubTypeExtension = paymentSubTypeExtension;
    }

    public String getBeneficiaryNickName() {
        return beneficiaryNickName;
    }

    public void setBeneficiaryNickName(String beneficiaryNickName) {
        this.beneficiaryNickName = beneficiaryNickName;
    }

    public String getBeneficiaryName() {
        return beneficiaryName;
    }

    public void setBeneficiaryName(String beneficiaryName) {
        this.beneficiaryName = beneficiaryName;
    }

    public String getFromAccountId() {
        return fromAccountId;
    }

    public void setFromAccountId(String fromAccountId) {
        this.fromAccountId = fromAccountId;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    public String getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(String paymentDate) {
        this.paymentDate = paymentDate;
    }

    public String getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }

    public String getToAccountId() {
        return toAccountId;
    }

    public void setToAccountId(String toAccountId) {
        this.toAccountId = toAccountId;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("paymentId", maskString(paymentId))
                .add("paymentType", paymentType)
                .add("paymentSubType", paymentSubType)
                .add("paymentSubTypeExtension", paymentSubTypeExtension)
                .add("beneficiaryNickName", beneficiaryNickName)
                .add("beneficiaryName", beneficiaryName)
                .add("fromAccountId", maskString(fromAccountId))
                .add("currency", currency)
                .add("amount", amount)
                .add("dueDate", dueDate)
                .add("paymentDate", paymentDate)
                .add("statusCode", statusCode)
                .add("toAccountId", maskString(toAccountId))
                .toString();
    }

    private String maskString(String str) {
        if (Strings.isNullOrEmpty(str)) {
            return str;
        }
        return StringUtils.rightPad(str.substring(0, str.length() / 4), str.length() / 4 * 3, '*')
                .concat(str.substring(str.length() / 4 * 3, str.length()));
    }
}
