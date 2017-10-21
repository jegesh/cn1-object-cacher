
package net.gesher.cn1.caching;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * source: https://stackoverflow.com/a/40986470/1857802
 * @author yaakov
 * @param <K>
 * @param <V>
 */
public class ValueTreeMap<K,V> implements Iterable<V>, Map<K,V> {
    private final TreeSet<V> tree;
    private final HashMap<K, V> map = new HashMap<>();

    public ValueTreeMap(Comparator<V> comparator){
         tree = new TreeSet<>(comparator);
    }
    
    public V put(K key, V value){
        V oldValue = map.get(key);
        if(oldValue != null){
            tree.remove(oldValue);
            map.put(key, value);
            return oldValue;
        }
        tree.add(value);
        map.put(key, value);
        return null;
    }

    @Override
    public V get(Object key){
        return map.get(key);
    }

    @Override
    public Iterator<V> iterator() {
        return tree.iterator();
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
        return tree.contains((V)value);
    }

    @Override
    public V remove(Object key) {
        V val = map.get((K)key);
        tree.remove(val);
        return map.remove(key);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
    }

    @Override
    public void clear() {
        tree.clear();
        map.clear();
    }

    @Override
    public Set<K> keySet() {
        return map.keySet();
    }

    @Override
    public Collection<V> values() {
        return tree;
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return map.entrySet();
    }

}
