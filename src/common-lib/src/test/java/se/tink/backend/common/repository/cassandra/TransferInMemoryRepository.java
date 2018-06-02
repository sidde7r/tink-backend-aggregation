package se.tink.backend.common.repository.cassandra;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Maps;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.apache.commons.lang.NotImplementedException;
import org.springframework.data.cassandra.repository.MapId;
import se.tink.backend.core.transfer.Transfer;
import se.tink.libraries.uuid.UUIDUtils;

public class TransferInMemoryRepository extends InMemoryRepository<UUID, Transfer> implements TransferRepository {

    public TransferInMemoryRepository() {
        super();
    }

    public TransferInMemoryRepository(List<Transfer> initialState) {
        super(Maps.uniqueIndex(initialState, Transfer::getId));
    }

    @Override
    public void createTableIfNotExist() {
        throw new NotImplementedException();
    }

    @Override
    public <S extends Transfer> S save(S s) {
        db.put(s.getId(), s);
        return s;
    }

    @Override
    public <S extends Transfer> Iterable<S> save(Iterable<S> iterable) {
        for(Transfer t: iterable) {
            save(t);
        }
        return iterable;
    }

    @Override
    public void delete(MapId mapId) {
        throw new NotImplementedException("Please implement me if you need me :-)");
    }

    @Override
    public void delete(Transfer transfer) {
        if (db.containsKey(transfer.getId())) {
            db.remove(transfer.getId());
        }
    }

    @Override
    public void delete(Iterable<? extends Transfer> iterable) {
        for(Transfer t : iterable) {
            delete(t);
        }
    }

    @Override
    public void deleteByUserId(String userId) {
        throw new NotImplementedException("Please implement me if you need me :-)");
    }

    @Override
    public List<Transfer> findAllByUserId(String userId) {
        final UUID uuid = UUIDUtils.fromTinkUUID(userId);

        return FluentIterable.from(db.values())
                .filter(transfer -> Objects.equals(uuid, transfer.getUserId()))
                .toList();
    }

    @Override
    public List<Transfer> findAllByUserIdAndCredentialsId(String userId, String credentialsId) {
        final UUID uuidUser = UUIDUtils.fromTinkUUID(userId);
        final UUID uuidCredentials = UUIDUtils.fromTinkUUID(credentialsId);

        return FluentIterable.from(db.values())
                .filter(transfer -> Objects.equals(uuidUser, transfer.getUserId()))
                .filter(transfer -> Objects.equals(uuidCredentials, transfer.getCredentialsId()))
                .toList();
    }

    @Override
    public Transfer findOneByUserIdAndId(String userId, String id) {
        throw new NotImplementedException("Please implement me if you need me :-)");
    }

    @Override
    public Transfer findOneByUserIdAndId(UUID userId, UUID id) {
        throw new NotImplementedException("Please implement me if you need me :-)");
    }

    @Override
    public void deleteByUserIdAndCredentialsId(String userId, String credentialsId) {
        throw new NotImplementedException("Please implement me if you need me :-)");
    }
}
