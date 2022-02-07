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
