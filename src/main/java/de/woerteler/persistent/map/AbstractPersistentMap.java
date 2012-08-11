package de.woerteler.persistent.map;

import java.util.Map;
import java.util.Map.Entry;

/**
 * The abstract base class for persistent maps. It provides equality checks and
 * default implementations for interface methods. The default implementations
 * may be overridden with faster implementations.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 * @param <K> The key type.
 * @param <V> The value type.
 */
public abstract class AbstractPersistentMap<K, V> implements PersistentMap<K, V> {

  @Override
  public PersistentMap<K, V> putAll(final Map<? extends K, ? extends V> other) {
    PersistentMap<K, V> res = this;
    for(final Entry<? extends K, ? extends V> e : other.entrySet()) {
      res = res.put(e.getKey(), e.getValue());
    }
    return res;
  }

  @Override
  public PersistentMap<K, V> putAll(final PersistentMap<K, V> other) {
    PersistentMap<K, V> res = this;
    for(final PersistentEntry<K, V> e : other.entrySequence()) {
      res = res.put(e.getKey(), e.getValue());
    }
    return res;
  }

  @Override
  public boolean equals(final Object obj) {
    if(obj == this) return true;
    if(!(obj instanceof PersistentMap)) return false;
    @SuppressWarnings("unchecked")
    final PersistentMap<Object, Object> other = (PersistentMap<Object, Object>) obj;
    if(size() != other.size()) return false;
    for(final K key : keySequence()) {
      if(!other.containsKey(key)) return false;
      final V value = get(key);
      final Object o = other.get(key);
      if(value == null) {
        if(o != null) return false;
      } else {
        if(!value.equals(o)) return false;
      }
    }
    return true;
  }

  @Override
  public int hashCode() {
    throw new UnsupportedOperationException(
        "find hash code compatible with immutable map");
  }

}
