package src.agent_sdk.compatibility_layers.aggregation_service.src.payments.beneficiary;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import se.tink.libraries.account.AccountIdentifier;

public class BeneficiariesCache {
    // This data structure consists of:
    // Key: "debtor" / source account identifier
    // Value: Set of beneficiary account identifiers for said "source" account identifier.
    private final Map<AccountIdentifier, Set<AccountIdentifier>> sourceAccountCache;

    public BeneficiariesCache() {
        this.sourceAccountCache = new HashMap<>();
    }

    /**
     * Check if the sourceAccount exists in the cache, i.e. `add(sourceAccount, ...)` has been
     * previously invoked. It does not verify if the cache for this specific sourceAccount contain
     * any beneficiaries though, it can be empty.
     */
    public boolean hasCached(AccountIdentifier sourceAccount) {
        return this.sourceAccountCache.containsKey(sourceAccount);
    }

    public boolean contains(AccountIdentifier sourceAccount, AccountIdentifier beneficiary) {
        return Optional.ofNullable(this.sourceAccountCache.getOrDefault(sourceAccount, null))
                .map(beneficiaryCache -> beneficiaryCache.contains(beneficiary))
                .orElse(false);
    }

    public void add(AccountIdentifier sourceAccount, Set<AccountIdentifier> beneficiaries) {
        Set<AccountIdentifier> beneficiaryCache =
                this.sourceAccountCache.getOrDefault(sourceAccount, new HashSet<>());
        beneficiaryCache.addAll(beneficiaries);
        this.sourceAccountCache.put(sourceAccount, beneficiaryCache);
    }

    public void add(AccountIdentifier sourceAccount, AccountIdentifier beneficiary) {
        Set<AccountIdentifier> beneficiaryCache =
                this.sourceAccountCache.getOrDefault(sourceAccount, new HashSet<>());
        beneficiaryCache.add(beneficiary);
        this.sourceAccountCache.put(sourceAccount, beneficiaryCache);
    }
}
