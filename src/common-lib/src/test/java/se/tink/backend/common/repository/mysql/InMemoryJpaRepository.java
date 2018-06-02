package se.tink.backend.common.repository.mysql;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang.NotImplementedException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.PagingAndSortingRepository;

public abstract class InMemoryJpaRepository<K extends Serializable, V> implements PagingAndSortingRepository<V, K> {

    protected final HashMap<K, V> db;

    protected InMemoryJpaRepository() {
        db = Maps.newHashMap();
    }

    protected InMemoryJpaRepository(Map<K, V> initialState) {
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
    public V findOne(K k) {
        return db.get(k);
    }

    @Override
    public boolean exists(K k) {
        return db.containsKey(k);
    }

    @Override
    public List<V> findAll() {
        return Lists.newArrayList(db.values());
    }

    @Override
    public List<V> findAll(Sort sort) {
        throw new NotImplementedException("Please implement me if you need me :-)");
    }

    @Override
    public Page<V> findAll(Pageable pageable) {
        throw new NotImplementedException("Please implement me if you need me :-)");
    }

    @Override
    public List<V> findAll(Iterable<K> iterable) {
        throw new NotImplementedException("Please implement me if you need me :-)");
    }

    @Override
    public <S extends V> List<S> save(Iterable<S> iterable) {
        throw new NotImplementedException("Please implement me if you need me :-)");
    }

    @Override
    public long count() {
        return db.size();
    }

    @Override
    public void delete(K id) {
        db.remove(id);
    }

    @Override
    public void delete(V v) {
        throw new NotImplementedException("Please implement me if you need me :-)");
    }

    @Override
    public void delete(Iterable<? extends V> iterable) {
        throw new NotImplementedException("Please implement me if you need me :-)");
    }

    @Override
    public void deleteAll() {
        throw new NotImplementedException("Please implement me if you need me :-)");
    }

}
