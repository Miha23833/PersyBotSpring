package com.jerseybot.collections;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

public class RollIngFixedSizeMap<K, V> implements Map<K, V> {
    private final int capacity;

    private final Map<K, V> map;
    private final Deque<K> keys;

    public RollIngFixedSizeMap(int capacity) {
        if (capacity < 1) {
            throw new IllegalArgumentException("Capacity must be positive");
        }
        this.capacity = capacity;

        map = new HashMap<>();
        keys = new LinkedList<>();
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return map.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return map.containsValue(value);
    }

    @Override
    public V get(Object key) {
        return map.get(key);
    }

    @Nullable
    @Override
    public V put(K key, V value) {
        if (keys.size() > capacity) {
            map.remove(keys.removeLast());
        }
        keys.add(key);
        map.put(key, value);
        return null;
    }

    @Override
    public V remove(Object key) {
        if (map.containsKey(key)) {
            return map.remove(keys.remove(key));
        }
        return null;
    }

    @Override
    public void putAll(@NotNull Map<? extends K, ? extends V> m) {
        m.forEach(this::put);
    }

    @Override
    public void clear() {
        keys.clear();
        map.clear();
    }

    @NotNull
    @Override
    public Set<K> keySet() {
        return map.keySet();
    }

    @NotNull
    @Override
    public Collection<V> values() {
        return map.values();
    }

    @NotNull
    @Override
    public Set<Entry<K, V>> entrySet() {
        return map.entrySet();
    }
}
