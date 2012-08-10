package de.woerteler.persistent.map;

import java.util.Map;
import java.util.Map.Entry;

/**
 * An immutable map.
 *
 * @author Leo Woerteler
 * @param <K> key type
 * @param <V> value type
 */
public final class ImmutableMap<K, V> implements PersistentMap<K, V> {
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
    return new ImmutableMap<K, V>(TrieNode.EMPTY.insert(key.hashCode(), key, value, 0));
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

  @Override
  public ImmutableMap<K, V> delete(final K key) {
    final TrieNode del = root.delete(key.hashCode(), key, 0);
    return del == root ? this :
      del == null ? ImmutableMap.<K, V>empty() : new ImmutableMap<K, V>(del);
  }

  @Override
  public V get(final K key) {
    return (V) root.get(key.hashCode(), key, 0);
  }

  @Override
  public boolean contains(final K key) {
    return root.contains(key.hashCode(), key, 0);
  }

  @Override
  public ImmutableMap<K, V> addAll(final PersistentMap<K, V> other) {
    if(!(other instanceof ImmutableMap)) throw new UnsupportedOperationException(
        "not yet implemented");
    final ImmutableMap<K, V> o = (ImmutableMap<K, V>) other;
    final TrieNode upd = root.addAll(o.root, 0);
    return upd == root ? this : upd == o.root ? o : new ImmutableMap<K, V>(upd);
  }

  @Override
  public ImmutableMap<K, V> addAll(final Map<? extends K, ? extends V> other) {
    ImmutableMap<K, V> map = this;
    for(final Entry<? extends K, ? extends V> e : other.entrySet()) {
      map = map.insert(e.getKey(), e.getValue());
    }
    return map;
  }

  @Override
  public ImmutableMap<K, V> insert(final K key, final V value) {
    return new ImmutableMap<K, V>(root.insert(key.hashCode(), key, value, 0));
  }

  @Override
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
