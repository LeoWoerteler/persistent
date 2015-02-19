package de.woerteler.persistent;

import java.lang.reflect.Array;
import java.util.*;

/**
 * An immutable sequence.
 *
 * @author Leo Woerteler
 * @param <T> type of the values in this collection
 */
public final class TrieSequence<T> implements PersistentSequence<T>, RandomAccess {
  /** Root node. */
  private final Node root;

  /** Number of bits per step. */
  private static final int BITS = 5;
  /** Maximum size of nodes. */
  public static final int SIZE = 1 << BITS;
  /** Bit mask for the last {@code BITS} bits in an {@code int}. */
  private static final int LAST = SIZE - 1;

  /** the empty sequence. */
  public static final TrieSequence<?> EMPTY =
      new TrieSequence<Object>(null, new Object[0]);

  /** Insertion buffer. */
  final Object[] cache;

  /**
   * Private constructor.
   * @param r root node
   * @param ch cache
   */
  private TrieSequence(final Node r, final Object[] ch) {
    root = r;
    cache = ch;
  }

  /**
   * The generic empty sequence.
   * @param <T> type of the sequence's elements
   * @return {@link TrieSequence#EMPTY} with generic type
   */
  @SuppressWarnings("unchecked")
  public static <T> TrieSequence<T> empty() {
    return (TrieSequence<T>) EMPTY;
  }

  /**
   * Creates a singleton sequence containing the given element.
   * @param <T> element type
   * @param t element
   * @return singleton sequence
   */
  public static <T> TrieSequence<T> singleton(final T t) {
    return TrieSequence.<T>empty().add(t);
  }

  /**
   * Creates a sequence from an {@link Iterable}.
   *
   * @param <T> The type of the elements.
   * @param it The {@link Iterable}.
   * @return The sequence containing all elements from
   *  the {@link Iterable} in the given order.
   */
  public static <T> TrieSequence<T> from(final Iterable<T> it) {
    if(it instanceof TrieSequence) return (TrieSequence<T>) it;
    final Iterator<?> iter = it.iterator();
    if(!iter.hasNext()) return empty();

    Object[] cache = new Object[SIZE];
    Node root = null;
    int pos = 0;
    do {
      cache[pos++] = iter.next();
      if(pos == SIZE) {
        final Node nd = new Node(cache);
        root = root == null ? nd : root.insert(nd);
        pos = 0;
        cache = new Object[SIZE];
      }
    } while(iter.hasNext());
    return new TrieSequence<T>(root, Arrays.copyOf(cache, pos));
  }

  /**
   * Creates a sequence from an array.
   *
   * @param <T> The type of elements.
   * @param array The array.
   * @return The sequence containing all elements from the array in the same order.
   */
  @SuppressWarnings("unchecked")
  public static <T> TrieSequence<T> from(final T... array) {
    if(array.length == 0) return empty();
    Node root = null;
    int pos = 0;
    while(pos + SIZE <= array.length) {
      final Object[] leaf = new Object[SIZE];
      System.arraycopy(array, pos, leaf, 0, SIZE);
      final Node curr = new Node(leaf);
      root = root == null ? curr : root.insert(curr);
      pos += SIZE;
    }
    final Object[] cache = new Object[array.length - pos];
    if(cache.length > 0) System.arraycopy(array, pos, cache, 0, cache.length);
    return new TrieSequence<T>(root, cache);
  }

  @Override
  public int size() {
    return (root == null ? 0 : root.size * SIZE) + cache.length;
  }

  @Override
  public TrieSequence<T> add(final T it) {
    final int cl = cache.length;
    final Object[] newCache = new Object[cl + 1];
    if(cl > 0) System.arraycopy(cache, 0, newCache, 0, cl);
    newCache[cl] = it;

    // cache is flushed only when it's full
    if(cl < LAST) return new TrieSequence<T>(root, newCache);
    // insert the full cache into the tree
    final Node l = new Node(newCache);
    return new TrieSequence<T>(root == null ? l : root.insert(l), EMPTY.cache);
  }

  @Override
  @SuppressWarnings("unchecked")
  public T get(final int pos) {
    if(root != null && pos < root.size << BITS) {
      Node nd = root;
      while(nd.level > 0) nd = (Node) nd.subs[(pos >>> (nd.level * BITS)) & LAST];
      return (T) nd.subs[pos & LAST];
    }
    return (T) cache[pos & LAST];
  }

  /**
   * Appends the given sequence to this one.
   * @param sequence sequence to append
   * @return copy of this sequence where <code>seq</code> is appended
   */
  @Override
  @SuppressWarnings("unchecked")
  public PersistentSequence<T> append(final PersistentSequence<? extends T> sequence) {
    if(sequence.size() == 0) return this;
    if(this == EMPTY) return (PersistentSequence<T>) sequence;

    if(!(sequence instanceof TrieSequence)) {
      TrieSequence<T> seq = this;
      for(final T elem : sequence) seq = seq.add(elem);
      return seq;
    }

    final TrieSequence<? extends T> seq = (TrieSequence<? extends T>) sequence;
    if(cache.length == 0) return fastAppend(seq);

    // cache is non-empty, so all chunks have to be shifted
    Node node = root;
    final Iterator<Node> iter = seq.nodeIterator();
    final int len = cache.length, rest = SIZE - len;
    Object[] curr = Arrays.copyOf(cache, SIZE);
    while(iter.hasNext()) {
      final Object[] chunk = iter.next().subs;
      System.arraycopy(chunk, 0, curr, len, rest);
      node = node == null ? new Node(curr) : node.insert(new Node(curr));
      curr = Arrays.copyOfRange(chunk, rest, rest + SIZE);
    }

    final int clen = seq.cache.length;
    if(clen == 0) return new TrieSequence<T>(node, Arrays.copyOf(curr, cache.length));

    if(clen < rest) {
      // cache fits into current chunk
      final Object[] newCache = Arrays.copyOf(curr, len + clen);
      System.arraycopy(seq.cache, 0, newCache, len, clen);
      return new TrieSequence<T>(node, newCache);
    }

    // insert additional chunk first
    System.arraycopy(seq.cache, 0, curr, len, rest);
    final Object[] newCache = Arrays.copyOfRange(seq.cache, rest, clen);
    node = node == null ? new Node(curr) : node.insert(new Node(curr));
    return new TrieSequence<T>(node, newCache);
  }

  /**
   * Faster version of {@link #append(PersistentSequence)} if {@link #cache} is empty.
   * This allows for all leaf nodes to be shared. Sharing would be possible for inner
   * nodes up to height {@code Integer.numberOfTrailingZeros(seq.size())}, but isn't
   * implemented.
   * @param seq Sequence to append
   * @return new sequence
   */
  private TrieSequence<T> fastAppend(final TrieSequence<? extends T> seq) {
    Node node = root;
    final Iterator<Node> iter = seq.nodeIterator();
    while(iter.hasNext()) node = node.insert(iter.next());
    return new TrieSequence<T>(node, seq.cache);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("Sequence[");
    if(root != null) root.toString(sb);
    return sb.append("; ").append(Arrays.toString(cache)).append(']').toString();
  }

  /**
   * Iterates through the leaf nodes.
   * @return iterator
   */
  private Iterator<Node> nodeIterator() {
    final Node[] anc = new Node[root == null ? 0 : root.level + 1];
    final int[] poss = new int[anc.length];

    int p = -1;
    if(root != null) {
      anc[++p] = root;
      while(anc[p].level > 0) {
        anc[p + 1] = (Node) anc[p].subs[0];
        p++;
      }
    }

    final int pp = p;
    return new Iterator<Node>() {
      /** Stack position. */
      int pos = pp;
      @Override
      public boolean hasNext() {
        return pos >= 0;
      }

      @Override
      public Node next() {
        // next result is always on top
        final Node next = anc[pos];
        // leave all exhausted nodes
        do if(--pos < 0) return next;
        while(poss[pos] == anc[pos].subs.length - 1);
        // go to next sibling
        poss[pos]++;
        do {
          // take the first child node until a leaf is reached
          anc[pos + 1] = (Node) anc[pos].subs[poss[pos]];
          poss[++pos] = 0;
        } while(anc[pos].level > 0);
        return next;
      }

      @Override
      public void remove() {
        throw new UnsupportedOperationException();
      }
    };
  }

  /**
   * Iterates through the value chunks.
   * @return iterator
   */
  private Iterator<Object[]> chunkIterator() {
    final Iterator<Node> iter = nodeIterator();
    return new Iterator<Object[]>() {
      /** Node iterator, {@code null} if drained. */
      Iterator<Node> nodes = iter.hasNext() ? iter : null;
      /** Cache position. */
      boolean serveCache = cache.length > 0;
      @Override
      public boolean hasNext() {
        return nodes != null || serveCache;
      }

      @Override
      public Object[] next() {
        if(nodes != null) {
          final Node next = nodes.next();
          if(!nodes.hasNext()) nodes = null;
          return next.subs;
        }
        if(!serveCache) throw new NoSuchElementException();
        serveCache = false;
        return cache;
      }

      @Override
      public void remove() {
        throw new UnsupportedOperationException();
      }
    };
  }

  @Override
  public Iterator<T> iterator() {
    final Iterator<Object[]> iter = chunkIterator();
    return new Iterator<T>() {
      /** Current chunk, {@code null} if finished. */
      Object[] curr = iter.hasNext() ? iter.next() : null;
      /** Position inside the current chunk. */
      int pos;

      @Override
      public boolean hasNext() {
        return curr != null;
      }

      @Override
      public T next() {
        if(curr == null) throw new NoSuchElementException();
        @SuppressWarnings("unchecked")
        final T res = (T) curr[pos++];
        if(pos == curr.length) {
          curr = iter.hasNext() ? iter.next() : null;
          pos = 0;
        }
        return res;
      }

      @Override
      public void remove() {
        throw new UnsupportedOperationException();
      }
    };
  }

  @Override
  public Object[] toArray() {
    return writeTo(new Object[size()]);
  }

  @Override
  @SuppressWarnings("unchecked")
  public T[] toArray(final T[] arr) {
    return writeTo(arr.length >= size() ? arr :
      (T[]) Array.newInstance(arr.getClass().getComponentType(), size()));
  }

  /**
   * Writes the contents to the given array.
   * @param <O> array element type
   * @param arr array
   * @return {@code arr} for convenience
   */
  private <O> O[] writeTo(final O[] arr) {
    int pos = 0;
    final Iterator<Object[]> chunks = chunkIterator();
    while(chunks.hasNext()) {
      final Object[] chunk = chunks.next();
      System.arraycopy(chunk, 0, arr, pos, chunk.length);
    }
    return arr;
  }

  @Override
  public boolean equals(final Object obj) {
    if(!(obj instanceof TrieSequence)) return false;
    final TrieSequence<?> other = (TrieSequence<?>) obj;
    if(size() != other.size()) return false;

    final Iterator<?> mine = iterator(), theirs = other.iterator();
    while(mine.hasNext()) {
      final Object a = mine.next(), b = theirs.next();
      if(a == null ? b != null : !a.equals(b)) return false;
    }
    return true;
  }


  @Override
  public int hashCode() {
    int hash = 1;
    for(final T val : this)  hash = 31 * hash + (val == null ? 0 : val.hashCode());
    return hash;
  }

  /**
   * Node of the sequence's tree.
   * @author Leo Woerteler
   */
  private static final class Node {
    /** Number of blocks in this node. */
    final int size;
    /** Bits to be compared. */
    final int level;
    /** Child nodes. */
    final Object[] subs;

    /**
     * Constructor for leaf nodes.
     * @param sub elements
     */
    Node(final Object[] sub) {
      this(sub, 1, 0);
    }

    /**
     * Private constructor for inner nodes.
     * @param sub children
     * @param s size
     * @param lvl level
     */
    private Node(final Object[] sub, final int s, final int lvl) {
      subs = sub;
      size = s;
      level = lvl;
    }

    /**
     * Inserts a new leaf into this node.
     * @param l leaf to insert
     * @return copy of this node where the leaf is inserted
     */
    public Node insert(final Node l) {
      final int nextFree = size >>> ((level - 1) * BITS);
      if(level == 0 || nextFree == SIZE)
        return new Node(new Node[] { this, l }, size + 1, level + 1);

      final Node[] newSubs;
      final int sl = subs.length;
      if(nextFree == sl) {
        // add a new entry
        newSubs = new Node[sl + 1];
        System.arraycopy(subs, 0, newSubs, 0, sl);
        newSubs[sl] = l;
      } else {
        // recursively insert
        newSubs = new Node[sl];
        System.arraycopy(subs, 0, newSubs, 0, sl);
        newSubs[nextFree] = ((Node) subs[nextFree]).insert(l);
      }

      return new Node(newSubs, size + 1, level);
    }

    /**
     * Recursive {@link TrieSequence#toString()} helper.
     * @param sb string builder for the result
     * @return string builder for convenience
     */
    public StringBuilder toString(final StringBuilder sb) {
      if(level == 0) sb.append("Leaf[");
      else sb.append("Node(").append(level).append(")[");
      for(int i = 0; i < subs.length; i++) {
        if(i > 0) sb.append(", ");
        if(level == 0) sb.append(subs[i]);
        else ((Node) subs[i]).toString(sb);
      }
      return sb.append(']');
    }
  }
}
