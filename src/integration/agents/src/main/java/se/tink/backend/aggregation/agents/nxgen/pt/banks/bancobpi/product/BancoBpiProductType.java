package se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.product;

import java.util.Arrays;

public enum BancoBpiProductType {
    CREDIT_CARD("P06"),
    MORTGAGE("P09"),
    LOAN("P07"),
    DEPOSIT("A01"),
    CURRENT_ACOUNT("A00");

    private String code;

    BancoBpiProductType(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static BancoBpiProductType getByCode(String code) {
        return Arrays.asList(values()).stream()
                .filter(v -> v.getCode().equals(code))
                .findAny()
                .orElseThrow(
                        () ->
                                new IllegalArgumentException(
                                        "Product with code " + code + " doesn't exist"));
    }
}
