package se.tink.backend.aggregation.agents;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import se.tink.backend.aggregation.rpc.Account;
import se.tink.backend.aggregation.agents.models.TransferDestinationPattern;

public class TransferDestinationsResponse {
    private Map<Account, List<TransferDestinationPattern>> destinations;

    public TransferDestinationsResponse() {
        destinations = Maps.newHashMap();
    }

    public TransferDestinationsResponse(Map<Account, List<TransferDestinationPattern>> patterns) {
        this();
        mergeWithExistingDestinations(patterns);
    }

    public TransferDestinationsResponse(Account account, List<TransferDestinationPattern> destinations) {
        this(asMap(account, destinations));
    }

    public void addDestinations(Map<Account, List<TransferDestinationPattern>> destinations) {
        mergeWithExistingDestinations(destinations);
    }

    public void addDestinations(Account account, List<TransferDestinationPattern> destinations) {
        mergeWithExistingDestinations(asMap(account, destinations));
    }

    public void addDestination(Account account, TransferDestinationPattern destination) {
        addDestinations(account, ImmutableList.of(destination));
    }

    private void mergeWithExistingDestinations(Map<Account, List<TransferDestinationPattern>> destinations) {
        Map<Account, List<TransferDestinationPattern>> existingDestinations = this.destinations;
        this.destinations = Stream.of(existingDestinations, destinations)
                .map(Map::entrySet)
                .flatMap(Collection::stream)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) ->
                        Stream.of(a, b).flatMap(Collection::stream).collect(Collectors.toList()))
                );
        existingDestinations.clear();
    }

    public Map<Account, List<TransferDestinationPattern>> getDestinations() {
        return Optional.ofNullable(destinations).orElse(Collections.emptyMap());
    }

    private static Map<Account, List<TransferDestinationPattern>> asMap(Account account,
            List<TransferDestinationPattern> destinations) {
        return ImmutableMap.of(account, destinations);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Map<Account, List<TransferDestinationPattern>> destinations;

        public Builder addTransferDestination(Account account, TransferDestinationPattern destination) {
            Preconditions.checkNotNull(account, "You must provide a account.");
            Preconditions.checkNotNull(destination, "You must provider a transfer destination.");

            if (this.destinations == null) {
                this.destinations = new HashMap<>();
            }

            List<TransferDestinationPattern> accountDestinations = this.destinations.getOrDefault(
                    account, new ArrayList<>());
            accountDestinations.add(destination);
            
            this.destinations.putIfAbsent(account, accountDestinations);

            return this;
        }

        public Builder addTransferDestinations(Account account, List<TransferDestinationPattern> destinations) {
            destinations.forEach(destination -> addTransferDestination(account, destination));
            return this;
        }

        public Builder addTransferDestinations(Map<Account, List<TransferDestinationPattern>> destinationsMap) {
            destinationsMap.forEach((this::addTransferDestinations));
            return this;
        }

        public TransferDestinationsResponse build() {
            TransferDestinationsResponse transferDestinationsResponse = new TransferDestinationsResponse();
            if (this.destinations == null) {
                this.destinations = new HashMap<>();
            }

            transferDestinationsResponse.addDestinations(this.destinations);

            return transferDestinationsResponse;
        }
    }
}
