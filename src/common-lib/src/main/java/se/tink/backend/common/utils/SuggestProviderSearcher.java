package se.tink.backend.common.utils;

import com.google.common.collect.Sets;
import com.google.inject.Inject;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import se.tink.backend.core.Provider;
import se.tink.backend.core.ProviderStatuses;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.TransactionPayloadTypes;
import se.tink.backend.utils.guavaimpl.Predicates;

public class SuggestProviderSearcher {
    private final Map<String, Provider> providersByName;

    @Inject
    public SuggestProviderSearcher(Map<String, Provider> providersByName) {
        this.providersByName = providersByName;
    }

    public Set<Provider> suggest(Transaction transaction) {
        Optional<String> providerName = suggestName(transaction);
        return providerName.map(this::getProviderVariants).orElseGet(Collections::emptySet);
    }

    public Optional<String> suggestName(Transaction transaction) {
        String providerName = transaction.getPayloadValue(TransactionPayloadTypes.TRANSFER_PROVIDER);
        return Optional.ofNullable(providerName).filter(string -> !string.isEmpty());
    }

    /**
     * Get a set of provider name variants (even though they might not be valid). E.g. "seb" => ["seb", "seb-bankid"],
     * "americanexpress" => ["americanexpress", "americanexpress-bankid", "saseurobonusamericanexpress",
     * "saseurobonusamericanexpress-bankid"]
     *
     * @param providerName
     * @return
     */
    private Set<String> getProviderNameVariants(String providerName) {
        Set<String> providerNameVariants = Sets.newLinkedHashSet();
        providerNameVariants.add(providerName);

        // Add BankID variant (even though most providers don't have one). Could be done nicer.
        providerNameVariants.add(String.format("%s-bankid", providerName));

        // Add SAS EuroBonus variant for American Express
        if ("americanexpress".equals(providerName)) {
            providerNameVariants.addAll(getProviderNameVariants("saseurobonusamericanexpress"));
        }

        return providerNameVariants;
    }

    /**
     * Get a set of (enabled) providers that are variants of the supplied provider name. E.g. "seb" => ["seb-bankid"],
     * "americanexpress" => ["americanexpress", "saseurobonusamericanexpress"]
     *
     * @param providerName
     * @return
     */
    public Set<Provider> getProviderVariants(String providerName) {
        return getProviderNameVariants(providerName).stream()
                .map(providersByName::get)
                .filter(Objects::nonNull)
                .filter(Predicates.providersOfStatus(ProviderStatuses.DISABLED).negate())
                .collect(Collectors.toSet());
    }
}
