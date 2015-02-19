package de.woerteler.persistent.fingertree;

/**
 * A possibly partial node of a finger tree.
 *
 * @author Leo Woerteler
 *
 * @param <N> node type
 * @param <E> element type
 */
public interface NodeLike<N, E> {
  /**
   * Appends this possibly partial node to the given buffer.
   * @param nodes the buffer
   * @param pos number of nodes in the buffer
   * @return new number of nodes
   */
  int append(final NodeLike<N, E>[] nodes, final int pos);
}
