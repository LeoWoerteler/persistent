package de.woerteler.persistent.array.object;

import java.util.*;

import de.woerteler.persistent.array.*;
import de.woerteler.persistent.fingertree.*;
import de.woerteler.persistent.fingertree.node.*;

/**
 * An array storing arbitrary objects.
 *
 * @author Leo Woerteler
 * @param <E> element type
 */
public abstract class ObjectArray<E> extends Array<E> {
  /**
   * The empty sequence.
   * Running time: <i>O(1)</i> and no allocation
   * @param <E> element type
   * @return (unique) instance of an empty sequence
   */
  @SuppressWarnings("unchecked")
  public static <E> ObjectArray<E> empty() {
    return (ObjectArray<E>) EmptyObjectArray.INSTANCE;
  }

  /**
   * Creates a singleton array containing the given element.
   * @param <E> element type
   * @param elem the contained element
   * @return the singleton array
   */
  public static <E> ObjectArray<E> singleton(final E elem) {
    return new SingletonObjectArray<>(elem);
  }

  /**
   * Creates an array containing the given elements.
   * @param <E> element type
   * @param elems elements
   * @return the resulting array
   */
  @SafeVarargs
  public static <E> ObjectArray<E> from(final E... elems) {
    final int n = elems.length;
    if(n == 0) return ObjectArray.empty();
    if(n == 1) return new SingletonObjectArray<E>(elems[0]);

    if(n < 7) {
      final int mid = n / 2;
      @SuppressWarnings("unchecked")
      final E[] left = (E[]) new Object[mid], right = (E[]) new Object[n - mid];
      System.arraycopy(elems, 0, left, 0, mid);
      System.arraycopy(elems, mid, right, 0, n - mid);
      return new DeepObjectArray<>(left, Empty.<E, E>getInstance(), right);
    }

    final int k = n == 7 ? 2 : 3;
    @SuppressWarnings("unchecked")
    final E[] left = (E[]) new Object[k], right = (E[]) new Object[k];
    System.arraycopy(elems, 0, left, 0, k);
    System.arraycopy(elems, n - k, right, 0, k);

    int remaining = n - 2 * k;
    @SuppressWarnings("unchecked")
    final Node<E, E>[] nodes = new Node[(remaining + 2) / 3];
    int i = k, j = 0;
    while(remaining > 4 || remaining == 3) {
      nodes[j++] = new LeafNode<E>(elems[i++], elems[i++], elems[i++]);
      remaining -= 3;
    }

    while(remaining > 0) {
      nodes[j++] = new LeafNode<E>(elems[i++], elems[i++]);
      remaining -= 2;
    }

    final FingerTree<E, E> root = FingerTree.buildTree(nodes, j, n - 2 * k);
    return new DeepObjectArray<>(left, root, right);
  }

  /**
   * Creates an array containing the elements returned by the given iterator.
   * @param <E> element type
   * @param iter element iterator
   * @return the resulting array
   */
  public static <E> ObjectArray<E> from(final Iterator<? extends E> iter) {
    final ObjectArrayBuilder<E> builder = new ObjectArrayBuilder<>();
    while(iter.hasNext()) builder.append(iter.next());
    return builder.freeze();
  }

  /**
   * Creates an array containing the elements returned by the given iterable.
   * @param <E> element type
   * @param iter element iterable
   * @return the resulting array
   */
  public static <E> ObjectArray<E> from(final Iterable<? extends E> iter) {
    final ObjectArrayBuilder<E> builder = new ObjectArrayBuilder<>();
    for(final E e : iter) builder.append(e);
    return builder.freeze();
  }

  @Override
  public abstract ObjectArray<E> cons(E elem);

  @Override
  public abstract ObjectArray<E> snoc(E elem);

  @Override
  public abstract Array<E> concat(Array<E> other);

  @Override
  public abstract ObjectArray<E> init();

  @Override
  public abstract ObjectArray<E> tail();

  @Override
  public abstract ObjectArray<E> subArray(long pos, long len);

  @Override
  public abstract ObjectArray<E> reverse();

  @Override
  public abstract ObjectArray<E> insertBefore(long pos, E val);

  @Override
  public abstract ObjectArray<E> remove(long pos);

  /**
   * Checks that this array's implementation does not violate any invariants.
   * @throws AssertionError if an invariant was violated
   */
  abstract void checkInvariants();

  /**
   * Returns an array containing the number of elements stored at each level of the tree.
   * @return array of sizes
   */
  abstract long[] sizes();
}
