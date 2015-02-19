package de.woerteler.persistent.array.integer;

import java.util.*;

import de.woerteler.persistent.fingertree.node.*;

/**
 * A leaf node directly containing primitive ints.
 *
 * @author Leo Woerteler
 */
final class IntLeaf extends Node<Integer, Integer> {
  /** Elements stored in this leaf node. */
  final int[] values;

  /**
   * Constructor.
   * @param values the values
   */
  public IntLeaf(final int[] values) {
    this.values = values;
    assert values.length >= IntArray.MIN_LEAF && values.length <= IntArray.MAX_LEAF;
  }

  @Override
  public long size() {
    return values.length;
  }

  @Override
  public IntLeaf reverse() {
    final int n = values.length;
    final int[] out = new int[n];
    for(int i = 0; i < n; i++) out[i] = values[n - 1 - i];
    return new IntLeaf(out);
  }

  @Override
  public boolean insert(final Node<Integer, Integer>[] siblings,
      final long pos, final Integer val) {
    final int p = (int) pos, n = values.length, v = val;
    final int[] vals = new int[n + 1];
    System.arraycopy(values, 0, vals, 0, p);
    vals[p] = v;
    System.arraycopy(values, p, vals, p + 1, n - p);

    if(n < IntArray.MAX_LEAF) {
      // there is capacity
      siblings[1] = new IntLeaf(vals);
      return false;
    }

    final IntLeaf left = (IntLeaf) siblings[0];
    if(left != null && left.values.length < IntArray.MAX_LEAF) {
      // push elements to the left sibling
      final int[] lvals = left.values;
      final int l = lvals.length, diff = IntArray.MAX_LEAF - l, move = (diff + 1) / 2;
      final int[] newLeft = new int[l + move], newRight = new int[n + 1 - move];
      System.arraycopy(lvals, 0, newLeft, 0, l);
      System.arraycopy(vals, 0, newLeft, l, move);
      System.arraycopy(vals, move, newRight, 0, newRight.length);
      siblings[0] = new IntLeaf(newLeft);
      siblings[1] = new IntLeaf(newRight);
      return false;
    }

    final IntLeaf right = (IntLeaf) siblings[2];
    if(right != null && right.values.length < IntArray.MAX_LEAF) {
      // push elements to the right sibling
      final int[] rvals = right.values;
      final int r = rvals.length, diff = IntArray.MAX_LEAF - r, move = (diff + 1) / 2,
          l = n + 1 - move;
      final int[] newLeft = new int[l], newRight = new int[r + move];
      System.arraycopy(vals, 0, newLeft, 0, l);
      System.arraycopy(vals, l, newRight, 0, move);
      System.arraycopy(rvals, 0, newRight, move, r);
      siblings[1] = new IntLeaf(newLeft);
      siblings[2] = new IntLeaf(newRight);
      return false;
    }

    // split the node
    final int l = vals.length / 2, r = vals.length - l;
    final int[] newLeft = new int[l], newRight = new int[r];
    System.arraycopy(vals, 0, newLeft, 0, l);
    System.arraycopy(vals, l, newRight, 0, r);
    siblings[3] = siblings[2];
    siblings[1] = new IntLeaf(newLeft);
    siblings[2] = new IntLeaf(newRight);
    return true;
  }

  @Override
  public Node<Integer, Integer>[] remove(final Node<Integer, Integer> left,
      final Node<Integer, Integer> right, final long pos) {
    final int p = (int) pos, n = values.length;
    @SuppressWarnings("unchecked")
    final Node<Integer, Integer>[] out = new Node[] { left, null, right };
    if(n > IntArray.MIN_LEAF) {
      // we do not have to split
      final int[] vals = new int[n - 1];
      System.arraycopy(values, 0, vals, 0, p);
      System.arraycopy(values, p + 1, vals, p, n - 1 - p);
      out[1] = new IntLeaf(vals);
      return out;
    }

    if(left != null && left.arity() > IntArray.MIN_LEAF) {
      // steal from the left neighbor
      final int[] lvals = ((IntLeaf) left).values;
      final int l = lvals.length, diff = l - IntArray.MIN_LEAF, move = (diff + 1) / 2;
      final int ll = l - move, rl = n - 1 + move;
      final int[] newLeft = new int[ll], newRight = new int[rl];

      System.arraycopy(lvals, 0, newLeft, 0, ll);
      System.arraycopy(lvals, ll, newRight, 0, move);
      System.arraycopy(values, 0, newRight, move, p);
      System.arraycopy(values, p + 1, newRight, move + p, n - 1 - p);
      out[0] = new IntLeaf(newLeft);
      out[1] = new IntLeaf(newRight);
      return out;
    }

    if(right != null && right.arity() > IntArray.MIN_LEAF) {
      // steal from the right neighbor
      final int[] rvals = ((IntLeaf) right).values;
      final int r = rvals.length, diff = r - IntArray.MIN_LEAF, move = (diff + 1) / 2;
      final int ll = n - 1 + move, rl = r - move;
      final int[] newLeft = new int[ll], newRight = new int[rl];

      System.arraycopy(values, 0, newLeft, 0, p);
      System.arraycopy(values, p + 1, newLeft, p, n - 1 - p);
      System.arraycopy(rvals, 0, newLeft, n - 1, move);
      System.arraycopy(rvals, move, newRight, 0, rl);
      out[1] = new IntLeaf(newLeft);
      out[2] = new IntLeaf(newRight);
      return out;
    }

    if(left != null) {
      // merge with left neighbor
      final int[] lvals = ((IntLeaf) left).values;
      final int l = lvals.length, r = values.length;
      final int[] vals = new int[l + r - 1];
      System.arraycopy(lvals, 0, vals, 0, l);
      System.arraycopy(values, 0, vals, l, p);
      System.arraycopy(values, p + 1, vals, l + p, r - 1 - p);
      out[0] = new IntLeaf(vals);
      out[1] = null;
    } else {
      // merge with right neighbor
      final int[] rvals = ((IntLeaf) right).values;
      final int l = values.length, r = rvals.length;
      final int[] vals = new int[l - 1 + r];
      System.arraycopy(values, 0, vals, 0, p);
      System.arraycopy(values, p + 1, vals, p, l - 1 - p);
      System.arraycopy(rvals, 0, vals, l - 1, r);
      out[1] = null;
      out[2] = new IntLeaf(vals);
    }

    return out;
  }

  @Override
  protected NodeLike<Integer, Integer>[] concat(final NodeLike<Integer, Integer> other) {
    @SuppressWarnings("unchecked")
    final NodeLike<Integer, Integer>[] arr = new NodeLike[2];

    if(other instanceof PartialIntLeaf) {
      final int[] left = values, right = ((PartialIntLeaf) other).elems;
      final int l = left.length, r = right.length, n = l + r;
      if(n <= IntArray.MAX_LEAF) {
        // merge into one node
        final int[] vals = new int[n];
        System.arraycopy(left, 0, vals, 0, l);
        System.arraycopy(right, 0, vals, l, r);
        arr[0] = new IntLeaf(vals);
      } else {
        // split into two
        final int ll = n / 2, rl = n - ll, move = l - ll;
        final int[] newLeft = new int[ll], newRight = new int[rl];
        System.arraycopy(left, 0, newLeft, 0, ll);
        System.arraycopy(left, ll, newRight, 0, move);
        System.arraycopy(right, 0, newRight, move, r);
        arr[0] = new IntLeaf(newLeft);
        arr[1] = new IntLeaf(newRight);
      }
    } else {
      arr[0] = this;
      arr[1] = other;
    }
    return arr;
  }

  @Override
  public int append(final NodeLike<Integer, Integer>[] nodes, final int pos) {
    if(pos == 0) {
      nodes[pos] = this;
      return 1;
    }

    final NodeLike<Integer, Integer> left = nodes[pos - 1];
    if(!(left instanceof PartialIntLeaf)) {
      nodes[pos] = this;
      return pos + 1;
    }

    final int[] ls = ((PartialIntLeaf) left).elems, rs = values;
    final int l = ls.length, r = rs.length, n = l + r;
    if(n <= IntArray.MAX_LEAF) {
      // merge into one node
      final int[] vals = new int[n];
      System.arraycopy(ls, 0, vals, 0, l);
      System.arraycopy(rs, 0, vals, l, r);
      nodes[pos - 1] = new IntLeaf(vals);
      return pos;
    }

    // split into two
    final int ll = n / 2, rl = n - ll, move = r - rl;
    final int[] newLeft = new int[ll], newRight = new int[rl];
    System.arraycopy(ls, 0, newLeft, 0, l);
    System.arraycopy(rs, 0, newLeft, l, move);
    System.arraycopy(rs, move, newRight, 0, rl);
    nodes[pos - 1] = new IntLeaf(newLeft);
    nodes[pos] = new IntLeaf(newRight);
    return pos + 1;
  }

  @Override
  public NodeLike<Integer, Integer> slice(final long off, final long size) {
    final int p = (int) off, n = (int) size;
    final int[] out = new int[n];
    System.arraycopy(values, p, out, 0, n);
    return n < IntArray.MIN_LEAF ? new PartialIntLeaf(out) : new IntLeaf(out);
  }

  @Override
  public Node<Integer, Integer> replaceFirst(final Integer newFirst) {
    if(newFirst == values[0]) return this;
    final int[] vals = values.clone();
    vals[0] = newFirst;
    return new IntLeaf(vals);
  }

  @Override
  public Node<Integer, Integer> replaceLast(final Integer newLast) {
    if(newLast == values[values.length - 1]) return this;
    final int[] vals = values.clone();
    vals[vals.length - 1] = newLast;
    return new IntLeaf(vals);
  }

  @Override
  public void toString(final StringBuilder sb, final int indent) {
    for(int i = 0; i < indent; i++) sb.append("  ");
    sb.append("Node(").append(size()).append(")").append(Arrays.toString(values));
  }

  @Override
  public long checkInvariants() {
    if(values.length < IntArray.MIN_LEAF || values.length > IntArray.MAX_LEAF)
      throw new AssertionError("Wrong " + getClass().getSimpleName() + " size: " + values.length);
    return values.length;
  }

  @Override
  public int arity() {
    return values.length;
  }

  @Override
  public Integer getSub(final int index) {
    return values[index];
  }
}
