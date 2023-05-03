package com.jerseybot.utils.collections;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class FreshLimitedQueue<T> {

    private final Queue<T> fresh;
    private final Queue<T> old;
    private final int maxFreshCount;

    public FreshLimitedQueue(int maxFreshCount) {
        this.fresh = new ConcurrentLinkedQueue<>();
        this.old = new ConcurrentLinkedQueue<>();
        this.maxFreshCount = maxFreshCount;
    }

    public List<T> getFresh() {
        return List.copyOf(fresh);
    }

    public List<T> getOld() {
        return List.copyOf(old);
    }

    public List<T> clearFresh() {
        List<T> result = List.copyOf(this.fresh);
        this.fresh.clear();
        return result;
    }

    public List<T> clearOld() {
        List<T> result = List.copyOf(this.old);
        this.old.clear();
        return result;
    }

    public boolean isFreshEmpty() {
        return this.fresh.isEmpty();
    }

    public boolean isOldEmpty() {
        return this.old.isEmpty();
    }

    public boolean contains(T obj) {
        return this.fresh.contains(obj) || this.old.contains(obj);
    }

    public boolean freshContains(T obj) {
        return this.fresh.contains(obj);
    }

    public boolean oldContains(T obj) {
        return this.old.contains(obj);
    }

    public void clear() {
        this.clearFresh();
        this.clearOld();
    }

    public List<T> removeAll() {
        List<T> removed = new ArrayList<>();
        removed.addAll(fresh);
        removed.addAll(old);

        this.clear();

        return removed;
    }

    public void add(T obj) {
        if (fresh.size() == maxFreshCount) {
            old.add(fresh.remove());
        }
        fresh.add(obj);
    }

    public void remove(T obj) {
        this.fresh.remove(obj);
        this.old.remove(obj);
    }
}
