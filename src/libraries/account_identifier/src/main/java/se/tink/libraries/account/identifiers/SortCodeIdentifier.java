package se.tink.libraries.account.identifiers;

import se.tink.libraries.account.AccountIdentifier;

public class SortCodeIdentifier extends AccountIdentifier {

    private static final int SORT_CODE_LENGTH = 6;
    private static final int ACCOUNT_NUMBER_LENGTH = 8;

    private final String numericSortCode;
    private final String numericAccountNumber;

    public SortCodeIdentifier(String sortCodeAccountNumber) {
        this(null, sortCodeAccountNumber);
    }

    public SortCodeIdentifier(String name, String sortCode, String accountNumber) {
        this(name, sortCode + accountNumber);
    }

    public SortCodeIdentifier(String name, String sortCodeAccountNumber) {
        String numericSortCodeAccountNumber = getNumericNumber(sortCodeAccountNumber);

        this.numericSortCode = numericSortCodeAccountNumber.substring(0, SORT_CODE_LENGTH);
        this.numericAccountNumber = numericSortCodeAccountNumber.substring(SORT_CODE_LENGTH);
        this.setName(name);
    }

    private String getNumericNumber(String s) {
        return s.replaceAll("[^0-9]", "");
    }

    @Override
    public String getIdentifier() {
        return numericSortCode + numericAccountNumber;
    }

    @Override
    public boolean isValid() {
        return numericSortCode.length() == SORT_CODE_LENGTH &&
                numericAccountNumber.length() == ACCOUNT_NUMBER_LENGTH;
    }

    @Override
    public Type getType() {
        return Type.SORT_CODE;
    }
}
