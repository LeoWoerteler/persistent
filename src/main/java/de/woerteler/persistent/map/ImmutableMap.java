package de.woerteler.persistent.map;

import java.util.*;
import java.util.Map.Entry;

/**
 * An immutable map.
 *
 * @author Leo Woerteler
 * @param <K> key type
 * @param <V> value type
 */
public final class ImmutableMap<K, V> {
  /** The empty map. */
  public static final ImmutableMap<?, ?> EMPTY =
      new ImmutableMap<Object, Object>(TrieNode.EMPTY);
  /** Number of bits per level, maximum is 5 because {@code 1 << 5 == 32}. */
  public static final int BITS = 5;

  /** Wrapped immutable map. */
  private final TrieNode root;

  /**
   * Constructor.
   * @param m map
   */
  private ImmutableMap(final TrieNode m) {
    root = m;
  }

  /**
   * The generic empty map.
   * @param <K> key type
   * @param <V> value type
   * @return empty map
   */
  @SuppressWarnings("unchecked")
  public static <K, V> ImmutableMap<K, V> empty() {
    return (ImmutableMap<K, V>) EMPTY;
  }

  /**
   * Creates a singleton map containing the given key-value pair.
   * @param <K> key type
   * @param <V> value type
   * @param key key of the singleton map
   * @param value value of the singleton map
   * @return empty map
   */
  public static <K, V> ImmutableMap<K, V> singleton(final K key, final V value) {
    return ImmutableMap.<K, V>empty().insert(key, value);
  }

  /**
   * Creates an {@link ImmutableMap} from a {@link Map}, effectively freezing it.
   * @param <K> key type
   * @param <V> value type
   * @param map map to freeze
   * @return resulting {@link ImmutableMap}
   */
  public static <K, V> ImmutableMap<K, V> from(final Map<? extends K, ? extends V> map) {
    return ImmutableMap.<K, V>empty().addAll(map);
  }

  /**
   * Deletes a key from this map.
   * @param key key to delete
   * @return updated map if changed, {@code this} otherwise
   */
  public ImmutableMap<K, V> delete(final K key) {
    final TrieNode del = root.delete(key.hashCode(), key, 0);
    return del == root ? this :
      del == null ? ImmutableMap.<K, V>empty() : new ImmutableMap<K, V>(del);
  }

  /**
   * Gets the value from this map.
   * @param key key to look for
   * @return bound value if found, the empty sequence {@code ()} otherwise
   */
  @SuppressWarnings("unchecked")
  public V get(final K key) {
    return (V) root.get(key.hashCode(), key, 0);
  }

  /**
   * Checks if the given key exists in the map.
   * @param key key to look for
   * @return {@code true()}, if the key exists, {@code false()} otherwise
   */
  public boolean contains(final K key) {
    return root.contains(key.hashCode(), key, 0);
  }

  /**
   * Adds all bindings from the given map into {@code this}.
   * @param other map to add
   * @return updated map if changed, {@code this} otherwise
   */
  public ImmutableMap<K, V> addAll(final ImmutableMap<K, V> other) {
    if(other == EMPTY) return this;
    final TrieNode upd = root.addAll(other.root, 0);
    return upd == other.root ? other : new ImmutableMap<K, V>(upd);
  }

  /**
   * Adds all bindings from the given map into {@code this}.
   * @param other map to add
   * @return updated map if changed, {@code this} otherwise
   */
  public ImmutableMap<K, V> addAll(final Map<? extends K, ? extends V> other) {
    ImmutableMap<K, V> map = this;
    for(final Entry<? extends K, ? extends V> e : other.entrySet())
      map = map.insert(e.getKey(), e.getValue());
    return map;
  }

  /**
   * Inserts the given value into this map.
   * @param key key to insert
   * @param value value to insert
   * @return updated map if changed, {@code this} otherwise
   */
  public ImmutableMap<K, V> insert(final K key, final V value) {
    return new ImmutableMap<K, V>(root.insert(key.hashCode(), key, value, 0));
  }

  /**
   * Number of values contained in this map.
   * @return size
   */
  public int size() {
    return root.size;
  }

  @Override
  public boolean equals(final Object obj) {
    if(!(obj instanceof ImmutableMap)) return false;
    final ImmutableMap<?, ?> other = (ImmutableMap<?, ?>) obj;
    return root.size == other.root.size && root.equals(other.root);
  }

  @Override
  public int hashCode() {
    return root.hashCode();
  }

  @Override
  public String toString() {
    return root.toString();
  }
}
