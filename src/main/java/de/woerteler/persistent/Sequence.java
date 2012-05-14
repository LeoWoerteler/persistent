package de.woerteler.persistent;

import java.lang.reflect.Array;
import java.util.*;

/**
 * An immutable sequence.
 *
 * @author Leo Woerteler
 * @param <T> type of the values in this collection
 */
public final class Sequence<T> implements Iterable<T>, RandomAccess {
  /** Root node. */
  private final Node root;

  /** Number of bits per step. */
  private static final int BITS = 6;
  /** Maximum size of nodes. */
  private static final int SIZE = 1 << BITS;
  /** Bit mask for the last {@code BITS} bits in an {@code int}. */
  private static final int LAST = SIZE - 1;

  /** the empty sequence. */
  public static final Sequence<?> EMPTY = new Sequence<Object>(null, new Object[0]);

  /** Insertion buffer. */
  final Object[] cache;

  /**
   * Private constructor.
   * @param r root node
   * @param ch cache
   */
  private Sequence(final Node r, final Object[] ch) {
    root = r;
    cache = ch;
  }

  /**
   * The generic empty sequence.
   * @param <T> type of the sequence's elements
   * @return {@link Sequence#EMPTY} with generic type
   */
  @SuppressWarnings("unchecked")
  public static <T> Sequence<T> empty() {
    return (Sequence<T>) EMPTY;
  }

  /**
   * Returns the size of this sequence.
   * @return number of items
   */
  public int length() {
    return (root == null ? 0 : root.size * SIZE) + cache.length;
  }

  /**
   * Adds the given item at the end of the sequence.
   * @param it item to add
   * @return copy of this sequence where the item is added
   */
  public Sequence<T> cons(final T it) {
    final int cl = cache.length;
    final Object[] newCache = new Object[cl + 1];
    if(cl > 0) System.arraycopy(cache, 0, newCache, 0, cl);
    newCache[cl] = it;

    // cache is flushed only when it's full
    if(cl < LAST) return new Sequence<T>(root, newCache);
    // insert the full cache into the tree
    final Node l = new Node(newCache, 1, 0);
    return new Sequence<T>(root == null ? l : root.insert(l), EMPTY.cache);
  }

  /**
   * Get the item at the given position.
   * @param pos position
   * @return item at that position
   */
  @SuppressWarnings("unchecked")
  public T get(final int pos) {
    if(root != null && pos < root.size << BITS) {
      Node nd = root;
      while(nd.level > 0) nd = (Node) nd.subs[(pos >>> (nd.level * BITS)) & LAST];
      return (T) nd.subs[pos & LAST];
    }
    return (T) cache[pos & LAST];
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("Sequence[");
    if(root != null) root.toString(sb);
    return sb.append("; ").append(Arrays.toString(cache)).append(']').toString();
  }

  /**
   * Iterates through the value chunks.
   * @return iterator
   */
  private Iterator<Object[]> chunkIterator() {
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
    return new Iterator<Object[]>() {
      /** Stack position. */
      int pos = pp;
      /** The cache has still to be served. */
      boolean ch = cache.length > 0;
      @Override
      public boolean hasNext() {
        return pos >= 0 || ch;
      }

      @Override
      public Object[] next() {
        // serve non-empty cache when finished
        if(pos < 0) {
          if(ch ^= true) throw new NoSuchElementException();
          return cache;
        }

        // next result is always on top
        final Object[] next = anc[pos].subs;
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

  /**
   * Writes the contents of this sequence to an array.
   * @return array containing all contents
   */
  public Object[] toArray() {
    return writeTo(new Object[length()]);
  }

  /**
   * Writes the contents of this sequence to an array.
   * @param arr array to be written to
   * @return array containing all contents
   */
  @SuppressWarnings("unchecked")
  public T[] toArray(final T[] arr) {
    return writeTo(arr.length >= length() ? arr :
      (T[]) Array.newInstance(arr.getClass().getComponentType(), length()));
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
     * Constructor.
     * @param sub children
     * @param s size
     * @param lvl level
     */
    Node(final Object[] sub, final int s, final int lvl) {
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
     * Recursive {@link Sequence#toString()} helper.
     * @param sb string builder for the result
     * @return string builder for convenience
     */
    public StringBuilder toString(final StringBuilder sb) {
      return sb.append("Node(").append(level).append(")").append(Arrays.toString(subs));
    }
  }
}
