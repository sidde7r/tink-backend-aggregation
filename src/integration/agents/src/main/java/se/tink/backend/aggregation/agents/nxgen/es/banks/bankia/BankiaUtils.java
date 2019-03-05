package se.tink.backend.aggregation.agents.nxgen.es.banks.bankia;

import java.util.regex.Pattern;

public class BankiaUtils {
    public static final Pattern INTERNAL_PRODUCT_CODE_PATTERN = Pattern.compile("^[0-9]{20}$");
}
