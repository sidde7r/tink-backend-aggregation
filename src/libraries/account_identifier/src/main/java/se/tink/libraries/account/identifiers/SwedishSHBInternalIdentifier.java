package se.tink.libraries.account.identifiers;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import se.tink.libraries.account.AccountIdentifier;

public class SwedishSHBInternalIdentifier extends AccountIdentifier {
    private static final Pattern PATTERN_ACCOUNT_NUMBER = Pattern.compile("([\\d \\-]{6,})");

    private final boolean isValid;

    private String accountNumber;

    public SwedishSHBInternalIdentifier(String id) {
        super();
        isValid = formatAccountNumber(id);
    }

    private boolean formatAccountNumber(String id) {
        Matcher matcher = PATTERN_ACCOUNT_NUMBER.matcher(id);

        if (!matcher.matches()) {
            return false;
        }
        accountNumber = matcher.group(1).replace(" ", "").replace("-", "");
        return true;
    }

    @Override
    public boolean isValid() {
        return isValid;
    }

    @Override
    public String getIdentifier() {
        return accountNumber;
    }

    @Override
    public Type getType() {
        return Type.SE_SHB_INTERNAL;
    }
}
