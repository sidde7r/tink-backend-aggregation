package se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.investment;

import com.google.common.base.Preconditions;
import java.util.regex.Pattern;

public class BankiaInvestmentUtils {
    private static final Pattern INTERNAL_PRODUCT_CODE_PATTERN = Pattern.compile("^[0-9]{20}$");

    public static void checkValidInternalProductCode(String productCode) {
        Preconditions.checkArgument(
                INTERNAL_PRODUCT_CODE_PATTERN.matcher(productCode).matches(),
                "Internal product code from bankia doesn't match expected format: " + productCode);
    }
}
