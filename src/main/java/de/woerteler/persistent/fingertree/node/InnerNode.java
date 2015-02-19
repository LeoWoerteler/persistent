package de.woerteler.persistent.fingertree.node;

/**
 * An inner node containing nested sub-nodes.
 *
 * @author Leo Woerteler
 * @param <N> node type
 * @param <E> element type
 */
public abstract class InnerNode<N, E> extends Node<Node<N, E>, E> {
  /**
   * Removes the element at the given position in this node and returns the remaining sub-nodes.
   * @param pos position of the element to delete
   * @return remaining sub-nodes
   */
  public abstract Node<N, E>[] remove(long pos);

  /**
   * Returns a copy of this node's sub-nodes.
   * @return an array containing this node's children
   */
  public abstract Node<N, E>[] copyChildren();
}
