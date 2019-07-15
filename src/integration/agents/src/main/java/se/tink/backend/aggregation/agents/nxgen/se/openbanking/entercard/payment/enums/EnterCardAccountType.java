package se.tink.backend.aggregation.agents.nxgen.se.openbanking.entercard.payment.enums;

import com.google.common.collect.EnumHashBiMap;
import java.util.Arrays;
import java.util.Optional;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.AccountIdentifier.Type;

public enum EnterCardAccountType {
    BANK_ACCOUNT,
    NEM_CPR,
    NEM_CVR,
    NEM_SE,
    NEM_CVPU,
    NEM_CVSE,
    PLUS_GIRO,
    BANK_GIRO;

    private static final EnumHashBiMap<AccountIdentifier.Type, EnterCardAccountType>
            tinkToEnterCardAccountTypeBiMapper = EnumHashBiMap.create(AccountIdentifier.Type.class);

    // TODO:: Map all the different AccountTypes above to standard ones.
    static {
        tinkToEnterCardAccountTypeBiMapper.put(AccountIdentifier.Type.SE_BG, BANK_GIRO);
        tinkToEnterCardAccountTypeBiMapper.put(AccountIdentifier.Type.SE_PG, PLUS_GIRO);
        tinkToEnterCardAccountTypeBiMapper.put(Type.SE, BANK_ACCOUNT);
    }

    public String toString() {
        return name();
    }

    public static EnterCardAccountType fromString(String text) {
        return Arrays.stream(EnterCardAccountType.values())
                .filter(s -> s.name().equalsIgnoreCase(text))
                .findFirst()
                .orElseThrow(
                        () ->
                                new IllegalArgumentException(
                                        "Unrecognized EnterCard account type : " + text));
    }

    public static EnterCardAccountType mapToEnterCardAccountType(
            AccountIdentifier.Type tinkAccountType) {
        return Optional.ofNullable(tinkToEnterCardAccountTypeBiMapper.get(tinkAccountType))
                .orElseThrow(
                        () ->
                                new IllegalArgumentException(
                                        "Cannot map Tink account type : "
                                                + tinkAccountType.toString()
                                                + " to a EnterCard account type."));
    }

    public AccountIdentifier.Type mapToTinkAccountType() {
        return Optional.ofNullable(tinkToEnterCardAccountTypeBiMapper.inverse().get(this))
                .orElseThrow(
                        () ->
                                new IllegalArgumentException(
                                        "Cannot map EnterCard account type : "
                                                + this.toString()
                                                + " to a Tink account type."));
    }
}
