package se.tink.libraries.account.identifiers;

import java.util.regex.Pattern;

public class PlusGiroIdentifier extends GiroIdentifier {

    private static final Pattern PATTERN = Pattern.compile("\\d{2,8}");

    public PlusGiroIdentifier(String giroNumber) {
        super(giroNumber);
    }

    public PlusGiroIdentifier(String giroNumber, String ocr) {
        super(giroNumber, ocr);
    }

    @Override
    protected Pattern getGiroNumberPattern() {
        return PATTERN;
    }

    @Override
    public Type getType() {
        return Type.SE_PG;
    }
}
