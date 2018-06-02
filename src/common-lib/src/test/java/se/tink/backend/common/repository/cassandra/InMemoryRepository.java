package se.tink.backend.common.repository.cassandra;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang.NotImplementedException;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.MapId;

public abstract class InMemoryRepository<K extends Serializable, V> implements CassandraRepository<V> {

    protected final Map<K, V> db;

    protected InMemoryRepository() {
        db = Maps.newHashMap();
    }

    protected InMemoryRepository(Map<K, V> initialState) {
        db = Maps.newHashMap(initialState);
    }

    public boolean containsNothingButTheseIds(Set<K> keys) {
        if (db.size() != keys.size()) {
            return false;
        }

        for (K k : keys) {
            if (!db.containsKey(k)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public V findOne(MapId mapId) {
        throw new NotImplementedException("Please implement me if you need me :-)");
    }

    @Override
    public boolean exists(MapId mapId) {
        throw new NotImplementedException("Please implement me if you need me :-)");
    }

    @Override
    public List<V> findAll() {
        return Lists.newArrayList(db.values());
    }

    @Override
    public Iterable<V> findAll(Iterable<MapId> iterable) {
        throw new NotImplementedException("Please implement me if you need me :-)");
    }

    @Override
    public long count() {
        return db.size();
    }

    @Override
    public void delete(MapId mapId) {
        throw new NotImplementedException("Please implement me if you need me :-)");
    }

    @Override
    public void deleteAll() {
        throw new NotImplementedException("Please implement me if you need me :-)");
    }

}
