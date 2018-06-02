package se.tink.backend.system.usecases;

import com.google.common.collect.FluentIterable;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multiset;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Set;
import se.tink.backend.common.repository.cassandra.TransferRepository;
import se.tink.backend.core.enums.TransferType;
import se.tink.backend.core.transfer.Transfer;
import se.tink.backend.utils.guavaimpl.Predicates;

public class TransferUseCases {

    private final TransferRepository transferRepository;

    public TransferUseCases(TransferRepository transferRepository) {
        this.transferRepository = transferRepository;
    }

    public void syncTransfersWithDatabase(String userId, String credentialsId, List<Transfer> incomingTransfers) {
        Multiset<String> hashesInIncoming = FluentIterable.from(incomingTransfers)
                .transform(Transfer::getHashIgnoreSource)
                .toMultiset();

        List<Transfer> transfers = transferRepository.findAllByUserIdAndCredentialsId(userId, credentialsId);

        Multiset<String> hashesInDb = FluentIterable.from(transfers)
                .transform(Transfer::getHashIgnoreSource)
                .toMultiset();

        Set<String> hashesInCommon = findHashPairsWithSameValue(hashesInIncoming, hashesInDb);

        Iterable<Transfer> toRemove = FluentIterable.from(transfers)
                .filter(Predicates.transferIsOfType(TransferType.EINVOICE))
                .filter(Predicates.transferHashExcluded(Sets.difference(hashesInIncoming.elementSet(), hashesInCommon)))
                .toList();

        Iterable<Transfer> toAdd = FluentIterable.from(incomingTransfers)
                .filter(Predicates.transferIsOfType(TransferType.EINVOICE))
                .filter(Predicates.transferHashExcluded(Sets.difference(hashesInDb.elementSet(), hashesInCommon)))
                .toList();

        if (Iterables.size(toRemove) > 0) {
            transferRepository.delete(toRemove);
        }

        if (Iterables.size(toAdd) > 0) {
            transferRepository.save(toAdd);
        }
    }

    // For every hash the two sets have in common we add the hash to the list if the quantity of the hash
    // in each set is not the same.
    private Set<String> findHashPairsWithSameValue(Multiset<String> hashesInIncoming, Multiset<String> hashesInDb) {
        Set<String> hashesInCommon = Sets.newConcurrentHashSet();
        for (String mutualHash : Sets.intersection(hashesInIncoming.elementSet(), hashesInDb.elementSet())) {
            if (hashesInIncoming.count(mutualHash) != hashesInDb.count(mutualHash)) {
                hashesInCommon.add(mutualHash);
            }
        }
        return hashesInCommon;
    }

}
