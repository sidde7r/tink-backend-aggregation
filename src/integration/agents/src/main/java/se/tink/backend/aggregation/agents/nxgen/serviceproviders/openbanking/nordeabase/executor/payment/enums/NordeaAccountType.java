package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.executor.payment.enums;

import com.google.common.collect.EnumHashBiMap;
import java.util.Arrays;
import java.util.Optional;
import se.tink.libraries.account.enums.AccountIdentifierType;

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

    private static final EnumHashBiMap<AccountIdentifierType, NordeaAccountType>
            tinkToNordeaAccountTypeBiMapper = EnumHashBiMap.create(AccountIdentifierType.class);

    static {
        tinkToNordeaAccountTypeBiMapper.put(AccountIdentifierType.IBAN, IBAN);
        tinkToNordeaAccountTypeBiMapper.put(AccountIdentifierType.SE, BBAN_SE);
        tinkToNordeaAccountTypeBiMapper.put(AccountIdentifierType.SE_BG, BGNR);
        tinkToNordeaAccountTypeBiMapper.put(AccountIdentifierType.SE_PG, PGNR);
        tinkToNordeaAccountTypeBiMapper.put(AccountIdentifierType.NO, BBAN_NO);
        tinkToNordeaAccountTypeBiMapper.put(AccountIdentifierType.DK, BBAN_DK);
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

    public static NordeaAccountType mapToNordeaAccountType(AccountIdentifierType tinkAccountType) {
        return Optional.ofNullable(tinkToNordeaAccountTypeBiMapper.get(tinkAccountType))
                .orElseThrow(
                        () ->
                                new IllegalArgumentException(
                                        "Cannot map Tink account type : "
                                                + tinkAccountType.toString()
                                                + " to a Nordea account type."));
    }

    public AccountIdentifierType mapToTinkAccountType() {
        return Optional.ofNullable(tinkToNordeaAccountTypeBiMapper.inverse().get(this))
                .orElseThrow(
                        () ->
                                new IllegalArgumentException(
                                        "Cannot map Nordea account type : "
                                                + this.toString()
                                                + " to a Tink account type."));
    }
}
