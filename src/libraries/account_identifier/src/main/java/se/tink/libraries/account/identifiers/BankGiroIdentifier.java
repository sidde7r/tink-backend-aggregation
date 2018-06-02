package se.tink.libraries.account.identifiers;

import java.util.regex.Pattern;

public class BankGiroIdentifier extends GiroIdentifier {

    private static final Pattern PATTERN = Pattern.compile("\\d{7,8}");

    public BankGiroIdentifier(String giroNumber) {
        super(giroNumber);
    }

    public BankGiroIdentifier(String giroNumber, String ocr) {
        super(giroNumber, ocr);
    }

    @Override
    protected Pattern getGiroNumberPattern() {
        return PATTERN;
    }

    @Override
    public Type getType() {
        return Type.SE_BG;
    }
}
