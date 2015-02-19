package de.woerteler.persistent.array.object;

import de.woerteler.persistent.fingertree.*;
import de.woerteler.persistent.fingertree.node.*;

/**
 * A builder for creating an {@link ObjectArray} by prepending and appending elements.
 *
 * @author Leo Woerteler
 * @param <E> element type
 */
public final class ObjectArrayBuilder<E> {
  /** Ring buffer containing the root-level elements. */
  @SuppressWarnings("unchecked")
  private final E[] vals = (E[]) new Object[8];

  /** Number of elements in left digit. */
  private int l;
  /** Middle between left and right digit in the buffer. */
  private int m = 4;
  /** Number of elements in right digit. */
  private int r;

  /** Builder for the middle tree. */
  private final FingerTreeBuilder<E> tree = new FingerTreeBuilder<>();

  /**
   * Adds an element to the start of the array.
   * @param elem element to add
   */
  public void prepend(final E elem) {
    if(l < 4) {
      vals[(m - l + 7) & 7] = elem;
      l++;
    } else if(tree.isEmpty() && r < 4) {
      m = (m + 7) & 7;
      vals[(m - l + 8) & 7] = elem;
      r++;
    } else {
      final int l3 = (m + 7) & 7, l2 = (l3 + 7) & 7, l1 = (l2 + 7) & 7, l0 = (l1 + 7) & 7;
      final Node<E, E> node = new LeafNode<>(vals[l1], vals[l2], vals[l3]);
      tree.prepend(node);
      vals[l3] = vals[l0];
      vals[l2] = elem;
      vals[l1] = null;
      vals[l0] = null;
      l = 2;
    }
  }

  /**
   * Adds an element to the end of the array.
   * @param elem element to add
   */
  public void append(final E elem) {
    if(r < 4) {
      vals[(m + r) & 7] = elem;
      r++;
    } else if(tree.isEmpty() && l < 4) {
      m = (m + 1) & 7;
      vals[(m + r + 7) & 7] = elem;
      l++;
    } else {
      final int r0 = m, r1 = (r0 + 1) & 7, r2 = (r1 + 1) & 7, r3 = (r2 + 1) & 7;
      final Node<E, E> node = new LeafNode<>(vals[r0], vals[r1], vals[r2]);
      tree.append(node);
      vals[r0] = vals[r3];
      vals[r1] = elem;
      vals[r2] = null;
      vals[r3] = null;
      r = 2;
    }
  }

  /**
   * Creates an {@link ObjectArray} containing the elements of this builder.
   * @return resulting array
   */
  public ObjectArray<E> freeze() {
    final int n = l + r;
    if(n == 0) return ObjectArray.empty();
    if(n == 1) return new SingletonObjectArray<E>(vals[(m + r + 7) & 7]);

    final int a = tree.isEmpty() ? n / 2 : l, b = n - a;
    @SuppressWarnings("unchecked")
    final E[] left = (E[]) new Object[a], right = (E[]) new Object[b];
    final int lOff = (m - l + 8) & 7, rOff = lOff + a;
    for(int i = 0; i < a; i++) left[i] = vals[(lOff + i) & 7];
    for(int i = 0; i < b; i++) right[i] = vals[(rOff + i) & 7];
    return new DeepObjectArray<E>(left, tree.freeze(), right);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("ArrayBuilder[");
    boolean first = true;
    if(l > 0 || r > 0) {
      for(int i = 0; i < l; i++) {
        if(first) first = false;
        else sb.append(", ");
        sb.append(vals[(m - l + i + 8) & 7]);
      }
      tree.toString(sb);
      for(int i = 0; i < r; i++) {
        if(first) first = false;
        else sb.append(", ");
        sb.append(vals[(m + i) & 7]);
      }
    }
    return sb.append(']').toString();
  }
}
