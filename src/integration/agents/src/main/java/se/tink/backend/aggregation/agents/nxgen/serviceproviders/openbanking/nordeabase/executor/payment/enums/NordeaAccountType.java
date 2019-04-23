package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.executor.payment.enums;

import com.google.common.collect.EnumHashBiMap;
import se.tink.libraries.account.AccountIdentifier;

import java.util.Arrays;
import java.util.Optional;

public enum NordeaAccountType {
    IBAN,
    BBAN_SE,
    BBAN_DK,
    BBAN_NO,
    BGNR,
    PGNR,
    DK_01,
    DK_04,
    DK_15,
    DK_71,
    DK_73,
    DK_75;

    private static final EnumHashBiMap<AccountIdentifier.Type, NordeaAccountType>
            tinkToNordeaAccountTypeBiMapper = EnumHashBiMap.create(AccountIdentifier.Type.class);

    static {
        tinkToNordeaAccountTypeBiMapper.put(AccountIdentifier.Type.IBAN, IBAN);
        tinkToNordeaAccountTypeBiMapper.put(AccountIdentifier.Type.SE, BBAN_SE);
        tinkToNordeaAccountTypeBiMapper.put(AccountIdentifier.Type.SE_BG, BGNR);
        tinkToNordeaAccountTypeBiMapper.put(AccountIdentifier.Type.SE_PG, PGNR);
    }

    public String toString() {
        return name();
    }

    public static NordeaAccountType fromString(String text) {
        return Arrays.stream(NordeaAccountType.values())
                .filter(s -> s.name().equalsIgnoreCase(text))
                .findFirst()
                .orElseThrow(
                        () ->
                                new IllegalArgumentException(
                                        "Unrecognized Nordea account type : " + text));
    }

    public static NordeaAccountType mapToNordeaAccountType(AccountIdentifier.Type tinkAccountType) {
        return Optional.ofNullable(tinkToNordeaAccountTypeBiMapper.get(tinkAccountType))
                .orElseThrow(
                        () ->
                                new IllegalArgumentException(
                                        "Cannot map Tink account type : "
                                                + tinkAccountType.toString()
                                                + " to a Nordea account type."));
    }

    public AccountIdentifier.Type mapToTinkAccountType() {
        return Optional.ofNullable(tinkToNordeaAccountTypeBiMapper.inverse().get(this))
                .orElseThrow(
                        () ->
                                new IllegalArgumentException(
                                        "Cannot map Nordea account type : "
                                                + this.toString()
                                                + " to a Tink account type."));
    }
}
