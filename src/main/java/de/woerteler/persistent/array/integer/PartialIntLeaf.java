package de.woerteler.persistent.array.integer;

import de.woerteler.persistent.fingertree.*;

/**
 * A partial shallow node containing fewer elements than required in a node.
 *
 * @author Leo Woerteler
 */
final class PartialIntLeaf implements NodeLike<Integer, Integer> {
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
}
