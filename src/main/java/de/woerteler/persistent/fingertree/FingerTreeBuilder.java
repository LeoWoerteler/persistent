package de.woerteler.persistent.fingertree;

import de.woerteler.persistent.fingertree.node.*;

/**
 * A builder for {@link FingerTree}s from leaf nodes.
 *
 * @author Leo Woerteler
 * @param <E> element type
 */
public final class FingerTreeBuilder<E> {
  /** The root node, {@code null} if the tree is empty. */
  private BufferNode<E, E> root;

  /**
   * Checks if this builder is empty, i.e. if no leaf nodes were added to it.
   * @return {@code true} if the builder is empty, {@code false} otherwise
   */
  public boolean isEmpty() {
    return root == null;
  }

  /**
   * Adds a leaf node to the front of the tree.
   * @param leaf the leaf node to add
   */
  public void prepend(final Node<E, E> leaf) {
    if(root == null) root = new BufferNode<>();
    root.prepend(leaf);
  }

  /**
   * Adds a leaf node to the back of the tree.
   * @param leaf the leaf node to add
   */
  public void append(final Node<E, E> leaf) {
    if(root == null) root = new BufferNode<>();
    root.append(leaf);
  }

  /**
   * Builds a finger tree from the current state of this builder.
   * @return the resulting finger tree
   */
  public FingerTree<E, E> freeze() {
    return root == null ? Empty.<E, E>getInstance() : root.freeze();
  }

  /**
   * Writes the elements contained in this builder onto the given string builder.
   * @param sb string builder
   */
  public void toString(final StringBuilder sb) {
    if(root != null) root.toString(sb);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder(getClass().getSimpleName()).append('[');
    toString(sb);
    return sb.append(']').toString();
  }

  /**
   * Node of the middle tree.
   *
   * @param <N> node type
   * @param <E> element type
   */
  private static class BufferNode<N, E> {
    /** Ring buffer for nodes in the digits. */
    @SuppressWarnings("unchecked")
    final Node<N, E>[] nodes = new Node[8];
    /** Number of elements in left digit. */
    int l;
    /** Position of middle between left and right digit in buffer. */
    int m = 4;
    /** Number of elements in right digit. */
    int r;
    /** Root node of middle tree. */
    BufferNode<Node<N, E>, E> sub;

    /** Default constructor. */
    BufferNode() {
      // just for package access
    }

    /**
     * Adds a node to the front of this tree.
     * @param node the node to add
     */
    void prepend(final Node<N, E> node) {
      if(l < 4) {
        nodes[(m - l + 7) & 7] = node;
        l++;
      } else if(sub == null && r < 4) {
        m = (m + 7) & 7;
        nodes[(m - l + 8) & 7] = node;
        r++;
      } else {
        final int l3 = (m + 7) & 7, l2 = (l3 + 7) & 7, l1 = (l2 + 7) & 7, l0 = (l1 + 7) & 7;
        final Node<Node<N, E>, E> next = new InnerNode3<>(nodes[l1], nodes[l2], nodes[l3]);
        nodes[l3] = nodes[l0];
        nodes[l2] = node;
        nodes[l1] = null;
        nodes[l0] = null;
        l = 2;
        if(sub == null) sub = new BufferNode<>();
        sub.prepend(next);
      }
    }

    /**
     * Adds a node to the back of this tree.
     * @param node the node to add
     */
    void append(final Node<N, E> node) {
      if(r < 4) {
        nodes[(m + r) & 7] = node;
        r++;
      } else if(sub == null && l < 4) {
        m = (m + 1) & 7;
        nodes[(m + r - 1) & 7] = node;
        l++;
      } else {
        final int r0 = m, r1 = (r0 + 1) & 7, r2 = (r1 + 1) & 7, r3 = (r2 + 1) & 7;
        final Node<Node<N, E>, E> next = new InnerNode3<>(nodes[r0], nodes[r1], nodes[r2]);
        nodes[r0] = nodes[r3];
        nodes[r1] = node;
        nodes[r2] = null;
        nodes[r3] = null;
        r = 2;
        if(sub == null) sub = new BufferNode<>();
        sub.append(next);
      }
    }

    /**
     * Creates an {@link FingerTree} containing the elements of this builder.
     * @return the finger tree
     */
    FingerTree<N, E> freeze() {
      final int n = l + r;
      if(n == 1) return new Single<>(nodes[(m + r + 7) & 7]);
      final int a = sub == null ? n / 2 : l, b = n - a;
      @SuppressWarnings("unchecked")
      final Node<N, E>[] left = new Node[a], right = new Node[b];
      final int lOff = m - l + 8, rOff = lOff + a;
      for(int i = 0; i < a; i++) left[i] = nodes[(lOff + i) & 7];
      for(int i = 0; i < b; i++) right[i] = nodes[(rOff + i) & 7];
      return sub == null ? Deep.get(left, right) : Deep.get(left, sub.freeze(), right);
    }

    /**
     * Writes the elements contained in this node onto the given string builder.
     * @param sb string builder
     */
    void toString(final StringBuilder sb) {
      boolean first = true;
      for(int i = 0; i < l; i++) {
        final Node<N, E> node = nodes[(m - l + i + 8) & 7];
        for(final E elem : node) {
          if(first) first = false;
          else sb.append(", ");
          sb.append(elem);
        }
      }
      if(sub != null) {
        if(first) first = false;
        else sb.append(", ");
        sub.toString(sb);
      }
      for(int i = 0; i < r; i++) {
        final Node<N, E> node = nodes[(m + i) & 7];
        for(final E elem : node) {
          if(first) first = false;
          else sb.append(", ");
          sb.append(elem);
        }
      }
    }
  }
}
