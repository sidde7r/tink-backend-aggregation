package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.executor.payment.enums;

import com.google.common.collect.EnumHashBiMap;
import java.util.Arrays;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.CrosskeyBaseConstants.ExceptionMessagePatterns;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.AccountIdentifier.Type;

public enum CrosskeyAccountType {
    IBAN("UK.OBIE.IBAN"),
    PAN("UK.OBIE.PAN"),
    PAYM("UK.OBIE.Paym"),
    SORT("UK.OBIE.SortCodeAccountNumber");

    private static final EnumHashBiMap<Type, CrosskeyAccountType>
            tinkToCrosskeyAccountTypeBiMapper = EnumHashBiMap.create(AccountIdentifier.Type.class);

    static {
        tinkToCrosskeyAccountTypeBiMapper.put(AccountIdentifier.Type.IBAN, IBAN);
    }

    private String statusText;

    CrosskeyAccountType(String statusText) {
        this.statusText = statusText;
    }

    public static CrosskeyAccountType fromString(String text) {
        return Arrays.stream(CrosskeyAccountType.values())
                .filter(s -> s.statusText.equalsIgnoreCase(text))
                .findFirst()
                .orElseThrow(
                        () ->
                                createException(
                                        ExceptionMessagePatterns.UNRECOGNIZED_ACCOUNT_TYPE, text));
    }

    private static IllegalArgumentException createException(String format, String param) {
        return new IllegalArgumentException(String.format(format, param));
    }

    public String toString() {
        return name();
    }

    public String getStatusText() {
        return statusText;
    }
}
