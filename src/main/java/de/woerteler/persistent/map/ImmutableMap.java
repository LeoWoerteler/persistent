package de.woerteler.persistent.map;

import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;

import de.woerteler.persistent.FlatSequence;
import de.woerteler.persistent.Persistent;
import de.woerteler.persistent.PersistentSequence;
import de.woerteler.persistent.map.TrieNode.Pos;

/**
 * An immutable map.
 *
 * @author Leo Woerteler
 * @param <K> key type
 * @param <V> value type
 */
public final class ImmutableMap<K, V> extends AbstractPersistentMap<K, V> {
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
    return ImmutableMap.<K, V>empty().putAll(map);
  }

  @Override
  public ImmutableMap<K, V> remove(final K key) {
    final TrieNode del = root.delete(key == null ? 0 : key.hashCode(), key, 0);
    return del == root ? this :
      del == null ? ImmutableMap.<K, V>empty() : new ImmutableMap<K, V>(del);
  }

  @Override
  public V get(final K key) {
    return (V) root.get(key == null ? 0 : key.hashCode(), key, 0);
  }

  @Override
  public boolean containsKey(final K key) {
    return root.contains(key == null ? 0 : key.hashCode(), key, 0);
  }

  @Override
  public PersistentMap<K, V> putAll(final PersistentMap<K, V> other) {
    if(this == EMPTY) return other;
    if(!(other instanceof ImmutableMap)) return super.putAll(other);
    final ImmutableMap<K, V> o = (ImmutableMap<K, V>) other;
    final TrieNode upd = root.addAll(o.root, 0);
    return upd == root ? this : upd == o.root ? o : new ImmutableMap<K, V>(upd);
  }

  @Override
  public ImmutableMap<K, V> putAll(final Map<? extends K, ? extends V> other) {
    ImmutableMap<K, V> map = this;
    for(final Entry<? extends K, ? extends V> e : other.entrySet()) {
      map = map.put(e.getKey(), e.getValue());
    }
    return map;
  }

  @Override
  public ImmutableMap<K, V> put(final K key, final V value) {
    return new ImmutableMap<K, V>(root.insert(key == null ? 0 : key.hashCode(), key,
        value, 0));
  }

  @Override
  public int size() {
    return root.size;
  }

  /**
   * Returns the node at the given position in arbitrary order.
   * 
   * @param pos The position. It is modified to point to the position within the
   *          node.
   * @return The node.
   */
  protected TrieNode getNodeAt(final Pos pos) {
    if(pos.pos < 0 || pos.pos >= size()) throw new IndexOutOfBoundsException(
        "index: " + pos.pos + " size: " + size());
    TrieNode res = root;
    TrieNode child;
    while((child = res.getAt(pos)) != null) {
      res = child;
    }
    return res;
  }

  /**
   * An iterator over a persistent sequence originating from an
   * {@link ImmutableMap}.
   * 
   * @author Joschi <josua.krause@googlemail.com>
   * @param <T> The iteration type.
   */
  private abstract static class PersistentIterator<T> implements Iterator<T> {

    /** The node stack. */
    private final Deque<TrieNode> nodes = new LinkedList<TrieNode>();

    /** The position stack. */
    private final Deque<Pos> positions = new LinkedList<Pos>();

    /** The current position. */
    private int cur;

    /**
     * Creates a persistent iterator.
     * 
     * @param root The root node.
     */
    public PersistentIterator(final TrieNode root) {
      nodes.push(root);
      positions.push(new Pos(0));
      fetchNext();
    }

    /** Fetches the next item from the iterator. */
    private void fetchNext() {
      cur = next(nodes, positions);
    }

    /**
     * Advances the given iteration state.
     * 
     * @param nodes The stack of nodes.
     * @param positions The stack of positions.
     * @return The current position that can be used with the top of
     *         <code>nodes</code>.
     */
    private static int next(final Deque<TrieNode> nodes, final Deque<Pos> positions) {
      TrieNode node = nodes.peek();
      Pos pos = positions.peek();
      while(pos.pos >= node.size) {
        positions.pop();
        if(positions.isEmpty()) return -1;
        pos = positions.peek();
        pos.pos += node.size;
        nodes.pop();
        node = nodes.peek();
      }
      for(;;) {
        final Pos c = new Pos(pos);
        final TrieNode at = node.getAt(c);
        if(at == null) {
          ++pos.pos;
          return c.pos;
        }
        nodes.push(at);
        positions.push(c);
        node = at;
        pos = c;
      }
    }

    @Override
    public boolean hasNext() {
      return cur >= 0;
    }

    @Override
    public T next() {
      if(cur < 0) throw new NoSuchElementException();
      final T res = convertNode(nodes.peek(), cur);
      fetchNext();
      return res;
    }

    /**
     * Converts a node with a position to an iterated item.
     * 
     * @param node The node.
     * @param pos The position within the node.
     * @return The iterated item.
     */
    protected abstract T convertNode(TrieNode node, int pos);

    @Override
    public void remove() {
      throw new UnsupportedOperationException();
    }

  }

  @Override
  public PersistentSequence<K> keySequence() {
    final TrieNode r = root;
    final int size = r.size;
    if(size == 0) return Persistent.empty();
    return new FlatSequence<K>() {

      @Override
      public Iterator<K> iterator() {
        return new PersistentIterator<K>(r) {

          @Override
          protected K convertNode(final TrieNode node, final int pos) {
            return (K) node.getKey(pos);
          }

        };
      }

      @Override
      public K get(final int pos) {
        final Pos p = new Pos(pos);
        return (K) getNodeAt(p).getKey(p.pos);
      }

      @Override
      public int size() {
        return size;
      }

    };
  }

  @Override
  public PersistentSequence<V> valueSequence() {
    final TrieNode r = root;
    final int size = r.size;
    if(size == 0) return Persistent.empty();
    return new FlatSequence<V>() {

      @Override
      public Iterator<V> iterator() {
        return new PersistentIterator<V>(r) {

          @Override
          protected V convertNode(final TrieNode node, final int pos) {
            return (V) node.getValue(pos);
          }

        };
      }

      @Override
      public V get(final int pos) {
        final Pos p = new Pos(pos);
        return (V) getNodeAt(p).getValue(p.pos);
      }

      @Override
      public int size() {
        return size;
      }

    };
  }

  /**
   * A simple implementation of the {@link PersistentMap.PersistentEntry}
   * interface.
   * 
   * @author Joschi <josua.krause@googlemail.com>
   * @param <K> The key type.
   * @param <V> The value type.
   */
  private static class PEntry<K, V> implements PersistentEntry<K, V> {

    /** The key. */
    private final K key;
    /** The value. */
    private final V value;

    /**
     * An entry.
     * 
     * @param key The key.
     * @param value The value.
     */
    public PEntry(final K key, final V value) {
      this.key = key;
      this.value = value;
    }

    @Override
    public K getKey() {
      return key;
    }

    @Override
    public V getValue() {
      return value;
    }

    @Override
    public boolean equals(final Object obj) {
      if(obj == this) return true;
      if(!(obj instanceof PersistentEntry)) return false;
      final PersistentEntry<?, ?> e = (PersistentEntry<?, ?>) obj;
      return TrieNode.equal(key, e.getKey()) && TrieNode.equal(value, e.getValue());
    }

    @Override
    public int hashCode() {
      return (key == null ? 1 : key.hashCode()) * 31
          + (value == null ? 1 : value.hashCode());
    }

  }

  @Override
  public PersistentSequence<PersistentEntry<K, V>> entrySequence() {
    final TrieNode r = root;
    final int size = r.size;
    if(size == 0) return Persistent.empty();
    return new FlatSequence<PersistentEntry<K, V>>() {

      @Override
      public Iterator<PersistentEntry<K, V>> iterator() {
        return new PersistentIterator<PersistentEntry<K, V>>(r) {

          @Override
          protected PersistentEntry<K, V> convertNode(
              final TrieNode node, final int pos) {
            return new PEntry<K, V>((K) node.getKey(pos), (V) node.getValue(pos));
          }

        };
      }

      @Override
      public PEntry<K, V> get(final int pos) {
        final Pos p = new Pos(pos);
        final TrieNode at = getNodeAt(p);
        final int np = p.pos;
        return new PEntry<K, V>((K) at.getKey(np), (V) at.getValue(np));
      }

      @Override
      public int size() {
        return size;
      }

    };
  }

  @Override
  public boolean equals(final Object obj) {
    if(!(obj instanceof ImmutableMap)) return super.equals(obj);
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
