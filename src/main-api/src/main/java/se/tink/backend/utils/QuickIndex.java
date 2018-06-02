package se.tink.backend.utils;

import java.util.Map;

import com.google.common.collect.Maps;

/**
 * 
 * Assign incremental values to arbitrary objects, while still having O(1) lookups.
 *
 */
public class QuickIndex {
    private Map<Object, Integer> map;
    private int maxSize = Integer.MAX_VALUE;

    public QuickIndex() {
        this.map = Maps.newHashMap();
    }

    public QuickIndex(int maxSize) {
        this.maxSize = maxSize;
        this.map = Maps.newHashMapWithExpectedSize(maxSize);
    }

    public int add(Object key) {
        if (!map.containsKey(key)) {
            if (size() >= maxSize) {
                throw new IndexOutOfBoundsException();
            }
            map.put(key, size());
        }
        return map.get(key);
    }

    public boolean contains(Object key) {
        return map.containsKey(key);
    }

    // For serialization purpose only
    public Map<Object, Integer> getMap() {
        return map;
    }

    public int getMaxSize() {
        return maxSize;
    }

    public int indexOf(Object key) {
        if (map.containsKey(key)) {
            return map.get(key);
        } else {
            return -1;
        }
    }

    // For serialization purpose only
    public void setMap(Map<Object, Integer> map) { 
        this.map = map;
    }

    public void setMaxSize(int maxSize) {
        this.maxSize = maxSize;
    }

    public int size() {
        return map.size();
    }
}
