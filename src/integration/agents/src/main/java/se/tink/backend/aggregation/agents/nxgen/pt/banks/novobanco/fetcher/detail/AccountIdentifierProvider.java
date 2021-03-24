package se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.fetcher.detail;

import java.util.Optional;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.pair.Pair;

public class AccountIdentifierProvider {
    public static Pair<String, AccountIdentifierType> getAccountIdentifierData(
            String internalAccountId, String iban) {
        return Optional.ofNullable(iban)
                .map(s -> Pair.of(iban, AccountIdentifierType.IBAN))
                .orElse(Pair.of(internalAccountId, AccountIdentifierType.COUNTRY_SPECIFIC));
    }

    public static AccountIdentifier getAccountIdentifier(String internalAccountId, String iban) {
        Pair<String, AccountIdentifierType> idDetails =
                getAccountIdentifierData(internalAccountId, iban);
        return AccountIdentifier.create(idDetails.second, idDetails.first);
    }
}
