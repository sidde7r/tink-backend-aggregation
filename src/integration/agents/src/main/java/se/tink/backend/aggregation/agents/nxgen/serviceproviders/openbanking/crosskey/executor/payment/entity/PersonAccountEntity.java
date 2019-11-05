package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.executor.payment.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.PropertyNamingStrategy.UpperCamelCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.executor.payment.enums.CrosskeyAccountType;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;

@JsonObject
@JsonInclude(Include.NON_NULL)
@JsonNaming(UpperCamelCaseStrategy.class)
public class PersonAccountEntity {

    private String identification;
    private String name;
    private String schemeName;

    public PersonAccountEntity() {}

    @JsonIgnore
    private PersonAccountEntity(Builder builder) {
        this.identification = builder.identification;
        this.name = builder.name;
        this.schemeName = builder.schemeName;
    }

    public PersonAccountEntity(String identification, String name, String schemeName) {
        this.identification = identification;
        this.name = name;
        this.schemeName = schemeName;
    }

    @JsonIgnore
    public static PersonAccountEntity creditorOf(PaymentRequest paymentRequest) {
        String identification = paymentRequest.getPayment().getCreditor().getAccountNumber();
        String name = paymentRequest.getPayment().getCreditor().getName();
        return new Builder()
                .withIdentification(identification)
                .withName(name)
                .withSchemaName(CrosskeyAccountType.IBAN.getStatusText())
                .build();
    }

    @JsonIgnore
    public static PersonAccountEntity debtorOf(PaymentRequest paymentRequest) {
        String identification = paymentRequest.getPayment().getDebtor().getAccountNumber();
        return new PersonAccountEntity.Builder()
                .withIdentification(identification)
                .withSchemaName(CrosskeyAccountType.IBAN.getStatusText())
                .build();
    }

    public String getIdentification() {
        return identification;
    }

    public String getName() {
        return name;
    }

    public String getSchemeName() {
        return schemeName;
    }

    public static class Builder {
        private String identification;
        private String name;
        private String schemeName;

        public Builder withIdentification(String identification) {
            this.identification = identification;
            return this;
        }

        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        public Builder withSchemaName(String schemaName) {
            this.schemeName = schemaName;
            return this;
        }

        public PersonAccountEntity build() {
            return new PersonAccountEntity(this);
        }
    }
}
