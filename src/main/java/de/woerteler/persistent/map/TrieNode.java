package de.woerteler.persistent.map;

/**
 * Abstract superclass of all trie nodes.
 *
 * @author Leo Woerteler
 */
public abstract class TrieNode {
  /** Number of children on each level. */
  static final int KIDS = 1 << ImmutableMap.BITS;
  /** Mask for the bits used on the current level. */
  private static final int MASK = KIDS - 1;

  /** The empty node. */
  public static final TrieNode EMPTY = new TrieNode(0) {
    @Override
    StringBuilder toString(final StringBuilder sb, final String ind) {
      return sb.append("map{}"); }
    @Override
    TrieNode delete(final int h, final Object k, final int l) {
      return this; }
    @Override
    Object get(final int h, final Object k, final int l) { return null; }
    @Override
    boolean contains(final int h, final Object k, final int l) {
      return false; }
    @Override
    TrieNode addAll(final TrieNode o, final int l) {
      return o; }
    @Override
    TrieNode add(final Leaf o, final int l) { return o; }
    @Override
    TrieNode add(final List o, final int l) { return o; }
    @Override
    TrieNode add(final Branch o, final int l) { return o; }
    @Override
    boolean verify() { return true; }
    @Override
    public TrieNode insert(final int h, final Object k, final Object v, final int l) {
      return new Leaf(h, k, v); }
    @Override
    StringBuilder toString(final StringBuilder sb) { return sb; }
    @Override
    public boolean equals(final Object obj) { return this == obj; }
    @Override
    public int hashCode() { return 0; }
  };

  /** Size of this node. */
  public final int size;
  /**
   * Constructor.
   * @param s size
   */
  TrieNode(final int s) {
    assert s != 0 || EMPTY == null;
    size = s;
  }

  /**
   * Inserts the given value into this map.
   * @param hash hash code used as key
   * @param key key to insert
   * @param val value to insert
   * @param lvl level
   * @return updated map if changed, {@code this} otherwise
   */
  abstract TrieNode insert(final int hash, final Object key, final Object val,
      final int lvl);

  /**
   * Deletes a key from this map.
   * @param hash hash code of the key
   * @param key key to delete
   * @param lvl level
   * @return updated map if changed, {@code null} if deleted,
   *         {@code this} otherwise
   */
  abstract TrieNode delete(int hash, Object key, int lvl);

  /**
   * Looks up the value associated with the given key.
   * @param hash hash code
   * @param key key to look up
   * @param lvl level
   * @return bound value if found, {@code null} otherwise
   */
  abstract Object get(int hash, Object key, int lvl);

  /**
   * Checks if the given key exists in the map.
   * @param hash hash code
   * @param key key to look for
   * @param lvl level
   * @return {@code true}, if the key exists, {@code false} otherwise
   */
  abstract boolean contains(int hash, Object key, int lvl);

  /**
   * <p> Inserts all bindings from the given node into this one.
   * <p> This method is part of the <i>double dispatch</i> pattern and
   *     should be implemented as {@code return o.add(this, lvl, ii);}.
   * @param o other node
   * @param lvl level
   * @return updated map if changed, {@code this} otherwise
   */
  abstract TrieNode addAll(final TrieNode o, final int lvl);

  /**
   * Add a leaf to this node, if the key isn't already used.
   * @param o leaf to insert
   * @param lvl level
   * @return updated map if changed, {@code this} otherwise
   */
  abstract TrieNode add(final Leaf o, final int lvl);

  /**
   * Add an overflow list to this node, if the key isn't already used.
   * @param o leaf to insert
   * @param lvl level
   * @return updated map if changed, {@code this} otherwise
   */
  abstract TrieNode add(final List o, final int lvl);

  /**
   * Add all bindings of the given branch to this node for which the key isn't
   * already used.
   * @param o leaf to insert
   * @param lvl level
   * @return updated map if changed, {@code this} otherwise
   */
  abstract TrieNode add(final Branch o, final int lvl);

  /**
   * Verifies the tree.
   * @return check result
   */
  abstract boolean verify();

  /**
   * Calculates the hash key for the given level.
   * @param hash hash value
   * @param lvl current level
   * @return hash key
   */
  static final int key(final int hash, final int lvl) {
    return hash >>> lvl * ImmutableMap.BITS & MASK;
  }

  /**
   * Creates a new array by appending the given item to the given array.
   * @param arr original array
   * @param item item to be appended
   * @return new array
   */
  static final Object[] append(final Object[] arr, final Object item) {
    final Object[] res = new Object[arr.length + 1];
    System.arraycopy(arr, 0, res, 0, arr.length);
    res[arr.length] = item;
    return res;
  }

  /**
   * Creates a new array by deleting the item at the given position from the given array.
   * @param arr original array
   * @param pos position of the item to be deleted
   * @return new array
   */
  static final Object[] delete(final Object[] arr, final int pos) {
    final Object[] res = new Object[arr.length - 1];
    System.arraycopy(arr, 0, res, 0, pos);
    System.arraycopy(arr, pos + 1, res, pos, res.length - pos);
    return res;
  }

  @Override
  public String toString() {
    return toString(new StringBuilder(), "").toString();
  }

  /**
   * Recursive {@link #toString()} helper.
   *
   * @param sb string builder
   * @param ind indentation string
   * @return string builder for convenience
   */
  abstract StringBuilder toString(final StringBuilder sb, final String ind);

  /**
   * Recursive helper for {@link ImmutableMap#toString()}.
   * @param sb string builder
   * @return reference to {@code sb}
   */
  abstract StringBuilder toString(final StringBuilder sb);

  @Override
  public abstract boolean equals(Object obj);

  @Override
  public abstract int hashCode();
}