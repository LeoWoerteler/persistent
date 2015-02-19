package de.woerteler.persistent.array.object;

import de.woerteler.persistent.fingertree.node.*;

/**
 * A partial shallow node containing one element.
 *
 * @author Leo Woerteler
 * @param <E> element type
 */
final class PartialLeaf<E> extends PartialNode<E, E> {
  /** The single element. */
  public final E elem;

  /**
   * Constructor.
   * @param elem the single element
   */
  PartialLeaf(final E elem) {
    this.elem = elem;
  }

  @Override
  protected NodeLike<E, E>[] concat(final NodeLike<E, E> other) {
    @SuppressWarnings("unchecked")
    final NodeLike<E, E>[] out = new NodeLike[2];
    if(other instanceof PartialLeaf) {
      out[0] = new LeafNode<>(elem, ((PartialLeaf<E>) other).elem);
    } else {
      final LeafNode<E> leaf = (LeafNode<E>) other;
      leaf.insertChild(out, 0, 0, elem);
    }
    return out;
  }

  @Override
  public int append(final NodeLike<E, E>[] nodes, final int pos) {
    if(pos == 0) {
      nodes[0] = this;
      return 1;
    }

    final NodeLike<E, E> left = nodes[pos - 1];
    if(left instanceof LeafNode) {
      final LeafNode<E> leaf = (LeafNode<E>) left;
      return leaf.insertChild(nodes, pos - 1, leaf.arity(), elem) ? pos + 1 : pos;
    }

    // partial node
    nodes[pos - 1] = new LeafNode<>(((PartialLeaf<E>) left).elem, elem);
    return pos;
  }

  @Override
  public void toString(final StringBuilder sb, final int indent) {
    for(int i = 0; i < indent; i++) sb.append("  ");
    sb.append("OneElement[").append(elem).append(']');
  }
}
