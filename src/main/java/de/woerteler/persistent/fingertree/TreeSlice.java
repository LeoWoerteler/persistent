package de.woerteler.persistent.fingertree;

import de.woerteler.persistent.fingertree.node.*;

/**
 * A slice of a finger tree, used as internal representation
 * for {@link FingerTree#slice(long, long)}.
 *
 * @author Leo Woerteler
 * @param <N> node type
 * @param <E> element type
 */
public final class TreeSlice<N, E> {
  /** A full sub-tree. */
  private final FingerTree<N, E> tree;
  /** A partial node. */
  private final PartialNode<N, E> partial;

  /**
   * Constructor for whole trees.
   * @param tree the tree
   */
  TreeSlice(final FingerTree<N, E> tree) {
    this.tree = tree;
    this.partial = null;
  }

  /**
   * Constructor for partial nodes.
   * @param partial partial node
   */
  TreeSlice(final PartialNode<N, E> partial) {
    this.tree = null;
    this.partial = partial;
  }

  /**
   * Checks if this slice contains only a partial node.
   * @return {@code true} if this slice contains a partial node,
   *     {@code false} if it contains a full finger tree
   */
  public boolean isPartial() {
    return partial != null;
  }

  /**
   * Getter for a contained full tree, should only be called if {@link #isPartial()}
   * returns {@code false}.
   * @return the contained tree
   */
  public FingerTree<N, E> getTree() {
    return tree;
  }

  /**
   * Getter for a contained partial node, should only be called if {@link #isPartial()}
   * returns {@code true}.
   * @return the contained partial node
   */
  public PartialNode<N, E> getPartial() {
    return partial;
  }

  /**
   * Creates a slice from the given node buffer.
   * @param <N> node type
   * @param <E> element type
   * @param nodes node buffer
   * @param n number of nodes in the buffer
   * @param len number of elements in the buffer
   * @return constructed slice
   */
  static <N, E> TreeSlice<N, E> fromNodes(final NodeLike<N, E>[] nodes, final int n,
      final long len) {
    if(n == 1) {
      if(nodes[0] instanceof PartialNode) return new TreeSlice<>((PartialNode<N, E>) nodes[0]);
      final Node<N, E> node = (Node<N, E>) nodes[0];
      return new TreeSlice<>(new Single<>(node));
    }

    final int mid = n / 2;
    final Node<N, E>[] left = Deep.slice(nodes, 0, mid), right = Deep.slice(nodes, mid, n);
    return new TreeSlice<>(Deep.get(left, right, len));
  }
}
