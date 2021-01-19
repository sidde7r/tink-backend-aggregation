package se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.investment.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.investment.BankiaInvestmentUtils;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ValueAccountIdentifierEntity {

    @JsonProperty("entidad")
    private String entity;

    @JsonProperty("centro")
    private String center;

    @JsonProperty("digitosDeControl")
    private String controlDigits;

    @JsonProperty("numeroCuenta")
    private String accountNumber;

    private ValueAccountIdentifierEntity(
            String entity, String center, String controlDigits, String accountNumber) {
        this.entity = entity;
        this.center = center;
        this.controlDigits = controlDigits;
        this.accountNumber = accountNumber;
    }

    @JsonIgnore
    public static ValueAccountIdentifierEntity fromInternalProductCode(String productCode) {
        BankiaInvestmentUtils.checkValidInternalProductCode(productCode);

        String entity = productCode.substring(0, 4);
        String center = productCode.substring(4, 8);
        String controlDigits = productCode.substring(8, 10);
        String accountNumber = productCode.substring(10, 20);

        return new ValueAccountIdentifierEntity(entity, center, controlDigits, accountNumber);
    }

    public String getEntity() {
        return entity;
    }

    public String getCenter() {
        return center;
    }

    public String getControlDigits() {
        return controlDigits;
    }

    public String getAccountNumber() {
        return accountNumber;
    }
}
