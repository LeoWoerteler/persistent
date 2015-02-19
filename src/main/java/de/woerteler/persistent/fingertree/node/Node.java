package de.woerteler.persistent.fingertree.node;

import java.util.*;

/**
 * A node inside a digit.
 *
 * @author Leo Woerteler
 * @param <N> node type
 * @param <E> element type
 */
public abstract class Node<N, E> extends NodeLike<N, E> implements Iterable<E> {
  /**
   * Number of elements in this node.
   * @return number of elements
   */
  public abstract long size();

  /**
   * Number of children of this node.
   * @return number of children
   */
  public abstract int arity();

  /**
   * Returns the sub-node at the given position in this node.
   * @param pos index of the sub-node, must be between 0 and {@link #arity()} - 1
   * @return the sub-node
   */
  public abstract N getSub(final int pos);

  /**
   * Creates a reversed version of this node.
   * @return a node with the reverse order of contained elements
   */
  public abstract Node<N, E> reverse();

  /**
   * Inserts the given element at the given position in this node.
   * The array {@code siblings} is used for input as well as output. It must contain the left and
   * right sibling of this node (if existing) at positions 0 and 2 when calling the method.
   * After the method returns, there are two cases to consider:
   * <ul>
   *   <li>
   *     If the method returned {@code true} (i.e. this node was split), the array contains the
   *     left sibling at position 0, the split node at position 1 and 2 and the right sibling at 3.
   *   </li>
   *   <li>
   *     Otherwise the array contains (possibly modified versions of) left sibling at position 0,
   *     this node at position 1 and the right sibling at position 2.
   *   </li>
   * </ul>
   * @param siblings sibling array for input and output
   * @param pos insertion position
   * @param val value to insert
   * @return {@code true} if the node was split, {@code false} otherwise
   */
  public abstract boolean insert(Node<N, E>[] siblings, final long pos, final E val);

  /**
   * Removes the element at the given position in this node. Either the left or the right
   * neighbor must be given for balancing. If this node is merged with one of its neighbors, the
   * middle element of the result array is {@code null}.
   * @param l left neighbor, possibly {@code null}
   * @param r right neighbor, possibly {@code null}
   * @param pos position of the element to delete
   * @return three-element array with the new left neighbor, node and right neighbor
   */
  public abstract Node<N, E>[] remove(final Node<N, E> l, final Node<N, E> r, final long pos);

  /**
   * Extracts a sub-tree containing the elements at positions {@code off .. off + len - 1}
   * from the tree rooted at this node.
   * This method is only called if {@code len < this.size()} holds.
   * @param off offset of first element
   * @param len number of elements
   * @return the sub-tree, possibly under-full
   */
  public abstract NodeLike<N, E> slice(final long off, final long len);

  /**
   * Returns a version of this node where the first sub-node is the given one.
   * @param newFirst new first sub-node
   * @return resulting node
   */
  public abstract Node<N, E> replaceFirst(N newFirst);

  /**
   * Returns a version of this node where the last sub-node is the given one.
   * @param newLast new last sub-node
   * @return resulting node
   */
  public abstract Node<N, E> replaceLast(N newLast);

  /**
   * Checks that this node does not violate any invariants.
   * @return this node's size
   * @throws AssertionError if an invariant was violated
   */
  public abstract long checkInvariants();

  @Override
  public NodeIterator<E> iterator() {
    return new NodeIterator<>(this);
  }

  /**
   * An iterator through the elements in a node.
   *
   * @author Leo Woerteler
   * @param <E> element type
   */
  public static class NodeIterator<E> implements Iterator<E> {
    /** Node stack. */
    private Node<?, E>[] nodes;
    /** Position stack. */
    private int[] poss;
    /** Stack pointer. */
    private int sp;

    /**
     * Constructor.
     * @param start root node
     */
    @SuppressWarnings("unchecked")
    public NodeIterator(final Node<?, E> start) {
      nodes = new Node[8];
      poss = new int[8];
      init(start);
    }

    /**
     * Initializes this iterator with the given root node.
     * @param root root node
     */
    public void init(final Node<?, E> root) {
      sp = 0;
      Arrays.fill(nodes, null);
      Arrays.fill(poss, 0);
      nodes[sp] = root;
      while(nodes[sp] instanceof InnerNode) {
        @SuppressWarnings("unchecked")
        final Node<?, E> sub = ((InnerNode<?, E>) nodes[sp]).getSub(0);
        if(++sp == nodes.length) {
          nodes = Arrays.copyOf(nodes, 2 * nodes.length);
          poss = Arrays.copyOf(poss, 2 * poss.length);
        }
        nodes[sp] = sub;
      }
    }

    @Override
    public boolean hasNext() {
      return sp >= 0;
    }

    @Override
    @SuppressWarnings("unchecked")
    public E next() {
      final E out = (E) nodes[sp].getSub(poss[sp]);

      while(poss[sp] == nodes[sp].arity() - 1) {
        poss[sp] = 0;
        nodes[sp] = null;
        if(--sp < 0) return out;
      }

      poss[sp]++;
      while(nodes[sp] instanceof InnerNode) {
        final Node<?, E> curr = ((InnerNode<?, E>) nodes[sp]).getSub(poss[sp]);
        nodes[++sp] = curr;
      }

      return out;
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException();
    }
  }
}
