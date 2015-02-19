package de.woerteler.persistent.array.object;

import java.util.*;

import de.woerteler.persistent.fingertree.node.*;

/**
 * A leaf node directly containing values.
 *
 * @author Leo Woerteler
 * @param <E> element type
 */
final class LeafNode<E> extends Node<E, E> {
  /** Elements stored in this leaf node. */
  final E[] values;

  /**
   * Constructor for binary nodes.
   * @param a first value
   * @param b second value
   */
  public LeafNode(final E a, final E b) {
    @SuppressWarnings("unchecked")
    final E[] arr = (E[]) new Object[] { a, b };
    values = arr;
  }

  /**
   * Constructor for ternary nodes.
   * @param a first value
   * @param b second value
   * @param c third value
   */
  public LeafNode(final E a, final E b, final E c) {
    @SuppressWarnings("unchecked")
    final E[] arr = (E[]) new Object[] { a, b, c };
    values = arr;
  }

  @Override
  public long size() {
    return values.length;
  }

  @Override
  public E getSub(final int pos) {
    return values[pos];
  }

  @Override
  public LeafNode<E> reverse() {
    return values.length == 2 ? new LeafNode<>(values[1], values[0]) :
      new LeafNode<>(values[2], values[1], values[0]);
  }

  @Override
  public boolean insert(final Node<E, E>[] siblings, final long pos, final E val) {
    if(values.length == 2) {
      // there is capacity
      siblings[1] = pos == 0 ? new LeafNode<>(val, values[0], values[1])
                  : pos == 1 ? new LeafNode<>(values[0], val, values[1])
                             : new LeafNode<>(values[0], values[1], val);
      return false;
    }

    if(siblings[0] != null && siblings[0].arity() == 2) {
      final LeafNode<E> left = (LeafNode<E>) siblings[0];
      // push an element to the left sibling
      siblings[0] = pos == 0 ? new LeafNode<>(left.values[0], left.values[1], val)
                             : new LeafNode<>(left.values[0], left.values[1], values[0]);
      siblings[1] = pos == 1 ? new LeafNode<>(val, values[1], values[2])
                  : pos == 2 ? new LeafNode<>(values[1], val, values[2])
                  : pos == 3 ? new LeafNode<>(values[1], values[2], val) : this;
      return false;
    }

    if(siblings[2] != null && siblings[2].arity() == 2) {
      final LeafNode<E> right = (LeafNode<E>) siblings[2];
      // push an element to the right sibling
      siblings[1] = pos == 0 ? new LeafNode<>(val, values[0], values[1])
                  : pos == 1 ? new LeafNode<>(values[0], val, values[1])
                  : pos == 2 ? new LeafNode<>(values[0], values[1], val) : this;
      siblings[2] = pos == 3 ? new LeafNode<>(val, right.values[0], right.values[1])
                             : new LeafNode<>(values[2], right.values[0], right.values[1]);
      return false;
    }

    // split the node
    siblings[3] = siblings[2];
    siblings[1] = pos == 0 ? new LeafNode<>(val, values[0])
                : pos == 1 ? new LeafNode<>(values[0], val)
                           : new LeafNode<>(values[0], values[1]);
    siblings[2] = pos == 2 ? new LeafNode<>(val, values[2])
                : pos == 3 ? new LeafNode<>(values[2], val)
                           : new LeafNode<>(values[1], values[2]);
    return true;
  }

  @Override
  public Node<E, E>[] remove(final Node<E, E> left, final Node<E, E> right, final long pos) {
    @SuppressWarnings("unchecked")
    final Node<E, E>[] out = new Node[] { left, null, right };
    if(values.length == 3) {
      // we do not have to split
      out[1] = new LeafNode<>(values[pos < 1 ? 1 : 0], values[pos < 2 ? 2 : 1]);
      return out;
    }

    final E rest = pos == 1 ? values[0] : values[1];

    if(left != null && left.arity() == 3) {
      // steal from the left neighbor
      final E[] vals = ((LeafNode<E>) left).values;
      out[0] = new LeafNode<>(vals[0], vals[1]);
      out[1] = new LeafNode<>(vals[2], rest);
      return out;
    }

    if(right != null && right.arity() == 3) {
      // steal from the right neighbor
      final E[] vals = ((LeafNode<E>) right).values;
      out[1] = new LeafNode<>(rest, vals[0]);
      out[2] = new LeafNode<>(vals[1], vals[2]);
      return out;
    }

    if(left != null) {
      // merge with left neighbor
      final E[] vals = ((LeafNode<E>) left).values;
      out[0] = new LeafNode<>(vals[0], vals[1], rest);
      out[1] = null;
    } else {
      // merge with right neighbor
      final E[] vals = ((LeafNode<E>) right).values;
      out[1] = null;
      out[2] = new LeafNode<>(rest, vals[0], vals[1]);
    }

    return out;
  }

  @Override
  @SuppressWarnings("unchecked")
  protected NodeLike<E, E>[] concat(final NodeLike<E, E> other) {
    final NodeLike<E, E>[] arr = new NodeLike[] { this, null };
    if(other instanceof PartialLeaf) {
      final E last = ((PartialLeaf<E>) other).elem;
      insertChild(arr, 0, values.length, last);
    } else {
      arr[1] = other;
    }
    return arr;
  }

  @Override
  public int append(final NodeLike<E, E>[] nodes, final int pos) {
    if(pos == 0) {
      nodes[pos] = this;
      return 1;
    }

    final NodeLike<E, E> left = nodes[pos - 1];
    if(!(left instanceof PartialLeaf)) {
      nodes[pos] = this;
      return pos + 1;
    }

    final E elem = ((PartialLeaf<E>) left).elem;
    return insertChild(nodes, pos - 1, 0, elem) ? pos + 1 : pos;
  }

  @Override
  public NodeLike<E, E> slice(final long off, final long size) {
    final int p = (int) off;
    if(size == 1) return new PartialLeaf<E>(values[p]);
    return new LeafNode<>(values[p], values[p + 1]);
  }

  @Override
  public Node<E, E> replaceFirst(final E newFirst) {
    if(newFirst == values[0]) return this;
    return values.length == 2 ? new LeafNode<>(newFirst, values[1])
                              : new LeafNode<>(newFirst, values[1], values[2]);
  }

  @Override
  public Node<E, E> replaceLast(final E newLast) {
    if(newLast == values[values.length - 1]) return this;
    return values.length == 2 ? new LeafNode<>(values[0], newLast)
                              : new LeafNode<>(values[0], values[1], newLast);
  }

  @Override
  public void toString(final StringBuilder sb, final int indent) {
    for(int i = 0; i < indent; i++) sb.append("  ");
    sb.append("Node(").append(size()).append(")").append(Arrays.toString(values));
  }

  @Override
  public long checkInvariants() {
    if(values.length < 2 || values.length > 3)
      throw new AssertionError("Wrong ShallowNode size: " + values.length);
    return values.length;
  }

  @Override
  public int arity() {
    return values.length;
  }

  /**
   * Returns the contained value array for using it as a digit.
   * @return value array
   */
  E[] values() {
    return values;
  }



  /**
   * Inserts the given child at the given position into this node, writing the resulting node(s)
   * into the given array at position {@code i} and potentially {@code i + 1} if a split occurs.
   * @param arr array to write to
   * @param i position of this node in the array
   * @param pos insertion position in this node
   * @param sub the sub node to insert
   * @return {@code true} if a split occurred, {@code false} otherwise
   */
  boolean insertChild(final NodeLike<E, E>[] arr, final int i, final int pos, final E sub) {
    if(values.length == 2) {
      arr[i] = pos == 0 ? new LeafNode<>(sub, values[0], values[1]) :
        pos == 1 ? new LeafNode<>(values[0], sub, values[1]) :
          new LeafNode<>(values[0], values[1], sub);
      return false;
    }

    if(pos < 2) {
      arr[i] = pos == 0 ? new LeafNode<>(sub, values[0]) : new LeafNode<>(values[0], sub);
      arr[i + 1] = new LeafNode<>(values[1], values[2]);
    } else {
      arr[i] = new LeafNode<>(values[0], values[1]);
      arr[i + 1] = pos == 2 ? new LeafNode<>(sub, values[2]) : new LeafNode<>(values[2], sub);
    }

    return true;
  }
}
