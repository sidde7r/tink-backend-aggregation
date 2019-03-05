package se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.investment.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.BankiaUtils;
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

    public static ValueAccountIdentifierEntity fromInternalProductCode(String productCode) {
        Preconditions.checkArgument(
                BankiaUtils.INTERNAL_PRODUCT_CODE_PATTERN.matcher(productCode).matches(),
                "Internal product code from bankia doesn't match expected format");

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
