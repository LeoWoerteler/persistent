package de.woerteler.persistent.map;

import java.util.Map;

/**
 * An interface for an immutable map. All operations that would update a mutable
 * map return a new sequence reflecting the changes instead.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 * @param <K> The key type.
 * @param <V> The value type.
 */
public interface PersistentMap<K, V> {

  /**
   * Inserts the given value into this map.
   * 
   * @param key key to insert
   * @param value value to insert
   * @return updated map if changed, {@code this} otherwise
   */
  PersistentMap<K, V> insert(final K key, final V value);

  /**
   * Gets the value from this map.
   * 
   * @param key key to look for
   * @return bound value if found, the empty sequence {@code ()} otherwise
   */
  V get(K key);

  /**
   * Number of values contained in this map.
   * 
   * @return size
   */
  int size();

  /**
   * Checks if the given key exists in the map.
   * 
   * @param key key to look for
   * @return {@code true()}, if the key exists, {@code false()} otherwise
   */
  boolean contains(K key);

  /**
   * Deletes a key from this map.
   * 
   * @param key key to delete
   * @return updated map if changed, {@code this} otherwise
   */
  PersistentMap<K, V> delete(K key);

  /**
   * Adds all bindings from the given map into {@code this}.
   * 
   * @param other map to add
   * @return updated map if changed, {@code this} otherwise
   */
  PersistentMap<K, V> addAll(PersistentMap<K, V> other);

  /**
   * Adds all bindings from the given map into {@code this}.
   * 
   * @param other map to add
   * @return updated map if changed, {@code this} otherwise
   */
  PersistentMap<K, V> addAll(Map<? extends K, ? extends V> other);

}
