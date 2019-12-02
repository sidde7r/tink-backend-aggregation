package se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.fetcher.detail;

import java.util.Optional;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.pair.Pair;

public class AccountIdentifierProvider {
    public static Pair<String, AccountIdentifier.Type> getAccountIdentifierData(
            String internalAccountId, String iban) {
        return Optional.ofNullable(iban)
                .map(s -> Pair.of(iban, AccountIdentifier.Type.IBAN))
                .orElse(Pair.of(internalAccountId, AccountIdentifier.Type.COUNTRY_SPECIFIC));
    }

    public static AccountIdentifier getAccountIdentifier(String internalAccountId, String iban) {
        Pair<String, AccountIdentifier.Type> idDetails =
                getAccountIdentifierData(internalAccountId, iban);
        return AccountIdentifier.create(idDetails.second, idDetails.first);
    }
}
