package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.paypal.executor.payment.enums;

import com.google.common.collect.EnumHashBiMap;
import java.util.Arrays;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.paypal.PayPalConstants.ExceptionMessages;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.AccountIdentifier.Type;

public enum PayPalAccountType {
    // Gonna do mapping for email only.
    // PayPal account can also be identified by phone, account number or external identifier.
    EMAIL("EMAIL"),
    PHONE("PHONE"),
    ACCOUNT_NUMBER("ACCOUNT_NUMBER"),
    EXTERNAL_IDENTIFIER("EXTERNAL_IDENTIFIER");

    PayPalAccountType(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

    private String name;

    private static final EnumHashBiMap<Type, PayPalAccountType> tinkToPayPalAccountTypeBiMapper =
            EnumHashBiMap.create(AccountIdentifier.Type.class);

    static {
        tinkToPayPalAccountTypeBiMapper.put(Type.TINK, EMAIL);
    }

    public static PayPalAccountType fromString(String text) {
        return Arrays.stream(PayPalAccountType.values())
                .filter(s -> s.name.equalsIgnoreCase(text))
                .findFirst()
                .orElseThrow(
                        () ->
                                new IllegalArgumentException(
                                        String.format(
                                                ExceptionMessages.UNRECOGNIZED_PAYPAL_ACCOUNT,
                                                text)));
    }

    public static PayPalAccountType mapToPayPalAccountType(AccountIdentifier.Type tinkAccountType) {
        return Optional.ofNullable(tinkToPayPalAccountTypeBiMapper.get(tinkAccountType))
                .orElseThrow(
                        () ->
                                new IllegalArgumentException(
                                        String.format(
                                                ExceptionMessages.CANNOT_MAP_TINK_TO_PAYPAL,
                                                tinkAccountType.toString())));
    }

    public AccountIdentifier.Type mapToTinkAccountType() {
        return Optional.ofNullable(tinkToPayPalAccountTypeBiMapper.inverse().get(this))
                .orElseThrow(
                        () ->
                                new IllegalArgumentException(
                                        String.format(
                                                ExceptionMessages.CANNOT_MAP_PAYPAL_TO_TINK,
                                                name)));
    }
}
