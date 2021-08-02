package se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.product;

import java.util.Arrays;
import java.util.Optional;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanDetails.Type;

public enum BancoBpiProductType {
    CREDIT_CARD("P06"),
    MORTGAGE("P09", Type.MORTGAGE),
    LOAN("P07", Type.CREDIT),
    LOAN_VEHICLE("P08", Type.VEHICLE),
    DEPOSIT("A01"),
    CURRENT_ACOUNT("A00");

    private String code;
    private Type domainLoanType;

    BancoBpiProductType(String code) {
        this.code = code;
    }

    BancoBpiProductType(String code, Type domainLoanType) {
        this(code);
        this.domainLoanType = domainLoanType;
    }

    public String getCode() {
        return code;
    }

    public static BancoBpiProductType getByCode(String code) {
        return Arrays.stream(values())
                .filter(v -> v.getCode().equals(code))
                .findAny()
                .orElseThrow(
                        () ->
                                new IllegalArgumentException(
                                        "Product with code " + code + " doesn't exist"));
    }

    public boolean isLoan() {
        return domainLoanType != null;
    }

    public Type getDomainLoanType() {
        return Optional.ofNullable(domainLoanType)
                .orElseThrow(
                        () ->
                                new IllegalStateException(
                                        "Product with code "
                                                + getCode()
                                                + " is not a loan product"));
    }

    public static BancoBpiProductType[] getLoanProductTypes() {
        return Arrays.stream(values()).filter(t -> t.isLoan()).toArray(BancoBpiProductType[]::new);
    }
}
