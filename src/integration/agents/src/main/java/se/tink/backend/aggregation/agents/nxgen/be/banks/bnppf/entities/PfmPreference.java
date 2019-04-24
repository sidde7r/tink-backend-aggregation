package se.tink.backend.aggregation.agents.nxgen.be.banks.bnppf.entities;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PfmPreference {
    private PfmUser pfmUser;
    private List<PfmAccount> pfmAccounts;

    public List<PfmAccount> getPfmAccounts() {
        return pfmAccounts;
    }

    public Optional<PfmAccount> getPfmAccountFor(String externalAccountId) {
        return Optional.ofNullable(pfmAccounts).orElseGet(Collections::emptyList).stream()
                .filter(
                        pfmAccount ->
                                externalAccountId.equalsIgnoreCase(pfmAccount.getExternalAccId()))
                .findFirst();
    }
}
