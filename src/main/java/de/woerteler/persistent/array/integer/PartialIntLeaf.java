package de.woerteler.persistent.array.integer;

import java.util.*;

import de.woerteler.persistent.fingertree.node.*;

/**
 * A partial shallow node containing fewer elements than required in a node.
 *
 * @author Leo Woerteler
 */
final class PartialIntLeaf extends PartialNode<Integer, Integer> {
  /** The single element. */
  final int[] elems;

  /**
   * Constructor.
   * @param elems the elements
   */
  PartialIntLeaf(final int[] elems) {
    this.elems = elems;
  }

  @Override
  protected NodeLike<Integer, Integer>[] concat(final NodeLike<Integer, Integer> other) {
    @SuppressWarnings("unchecked")
    final NodeLike<Integer, Integer>[] out = new NodeLike[2];
    if(other instanceof IntLeaf) {
      final int[] ls = elems, rs = ((IntLeaf) other).values;
      final int l = ls.length, r = rs.length, n = l + r;
      if(n <= IntArray.MAX_LEAF) {
        // merge into one node
        final int[] vals = new int[n];
        System.arraycopy(ls, 0, vals, 0, l);
        System.arraycopy(rs, 0, vals, l, r);
        out[0] = new IntLeaf(vals);
      } else {
        // split into two
        final int ll = n / 2, rl = n - ll, move = r - rl;
        final int[] newLeft = new int[ll], newRight = new int[rl];
        System.arraycopy(ls, 0, newLeft, 0, l);
        System.arraycopy(rs, 0, newLeft, l, move);
        System.arraycopy(rs, move, newRight, 0, rl);
        out[0] = new IntLeaf(newLeft);
        out[1] = new IntLeaf(newRight);
      }
    } else {
      final int[] elems2 = ((PartialIntLeaf) other).elems;
      final int l = elems.length, r = elems2.length, n = l + r;
      final int[] vals = new int[n];
      System.arraycopy(elems, 0, vals, 0, l);
      System.arraycopy(elems2, 0, vals, l, r);
      out[0] = n < IntArray.MIN_LEAF ? new PartialIntLeaf(vals) : new IntLeaf(vals);
    }
    return out;
  }

  @Override
  public int append(final NodeLike<Integer, Integer>[] nodes, final int pos) {
    if(pos == 0) {
      nodes[0] = this;
      return 1;
    }

    final NodeLike<Integer, Integer> left = nodes[pos - 1];
    if(left instanceof PartialIntLeaf) {
      final int[] ls = ((PartialIntLeaf) left).elems, rs = elems;
      final int l = ls.length, r = rs.length, n = l + r;
      final int[] vals = new int[n];
      System.arraycopy(ls, 0, vals, 0, l);
      System.arraycopy(rs, 0, vals, l, r);
      nodes[pos - 1] = n < IntArray.MIN_LEAF ? new PartialIntLeaf(vals) : new IntLeaf(vals);
      return pos;
    }

    final int[] ls = ((IntLeaf) left).values, rs = elems;
    final int l = ls.length, r = rs.length, n = l + r;
    if(n <= IntArray.MAX_LEAF) {
      final int[] vals = new int[n];
      System.arraycopy(ls, 0, vals, 0, l);
      System.arraycopy(rs, 0, vals, l, r);
      nodes[pos - 1] = new IntLeaf(vals);
      return pos;
    }

    final int ll = n / 2, rl = n - ll, move = l - ll;
    final int[] newLeft = new int[ll], newRight = new int[rl];
    System.arraycopy(ls, 0, newLeft, 0, ll);
    System.arraycopy(ls, ll, newRight, 0, move);
    System.arraycopy(rs, 0, newRight, move, r);
    nodes[pos - 1] = new IntLeaf(newLeft);
    nodes[pos] = new IntLeaf(newRight);
    return pos + 1;
  }

  @Override
  public void toString(final StringBuilder sb, final int indent) {
    for(int i = 0; i < indent; i++) sb.append("  ");
    sb.append(getClass().getSimpleName()).append(Arrays.toString(elems));
  }
}
