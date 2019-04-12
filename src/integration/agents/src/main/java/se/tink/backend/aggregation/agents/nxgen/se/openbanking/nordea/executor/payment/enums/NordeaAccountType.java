package se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.executor.payment.enums;

import se.tink.libraries.account.AccountIdentifier;

import java.util.EnumMap;
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

    private static EnumMap<AccountIdentifier.Type, NordeaAccountType>
            tinkToNordeaAccountTypeMapper = new EnumMap<>(AccountIdentifier.Type.class);

    static {
        tinkToNordeaAccountTypeMapper.put(AccountIdentifier.Type.IBAN, IBAN);
        tinkToNordeaAccountTypeMapper.put(AccountIdentifier.Type.SE, BBAN_SE);
        tinkToNordeaAccountTypeMapper.put(AccountIdentifier.Type.SE_BG, BGNR);
        tinkToNordeaAccountTypeMapper.put(AccountIdentifier.Type.SE_PG, PGNR);
    }

    public static NordeaAccountType mapToNordeaAccountType(AccountIdentifier.Type tinkAccountType) {
        return Optional.ofNullable(tinkToNordeaAccountTypeMapper.get(tinkAccountType))
                .orElseThrow(
                        () ->
                                new IllegalArgumentException(
                                        "Cannot map Tink account type : "
                                                + tinkAccountType.toString()
                                                + " to a Nordea account type."));
    }
}
