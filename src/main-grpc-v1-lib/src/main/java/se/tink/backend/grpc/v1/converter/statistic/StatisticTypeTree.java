package se.tink.backend.grpc.v1.converter.statistic;

import com.google.common.collect.Maps;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class StatisticTypeTree implements Map<String, Map<String, StatisticNode>> {
    private Map<String, Map<String, StatisticNode>> statisticsByType;

    public StatisticTypeTree() {
        statisticsByType = Maps.newHashMap();
    }

    @Override
    public int size() {
        return statisticsByType.size();
    }

    @Override
    public boolean isEmpty() {
        return statisticsByType.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return statisticsByType.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return statisticsByType.containsValue(value);
    }

    @Override
    public Map<String, StatisticNode> get(Object key) {
        return statisticsByType.get(key);
    }

    @Override
    public Map<String, StatisticNode> put(String key, Map<String, StatisticNode> value) {
        return statisticsByType.put(key, value);
    }

    @Override
    public Map<String, StatisticNode> remove(Object key) {
        return statisticsByType.remove(key);
    }

    @Override
    public void putAll(Map<? extends String, ? extends Map<String, StatisticNode>> m) {
        statisticsByType.putAll(m);
    }

    @Override
    public void clear() {
        statisticsByType.clear();
    }

    @Override
    public Set<String> keySet() {
        return statisticsByType.keySet();
    }

    @Override
    public Collection<Map<String, StatisticNode>> values() {
        return statisticsByType.values();
    }

    @Override
    public Set<Entry<String, Map<String, StatisticNode>>> entrySet() {
        return statisticsByType.entrySet();
    }
}
