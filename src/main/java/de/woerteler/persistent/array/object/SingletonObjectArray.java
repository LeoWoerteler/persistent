package de.woerteler.persistent.array.object;

import java.util.*;

import de.woerteler.persistent.array.*;
import de.woerteler.persistent.fingertree.*;

/**
 * A singleton array.
 *
 * @author Leo Woerteler
 * @param <E> element type
 */
final class SingletonObjectArray<E> extends ObjectArray<E> {
  /** The element. */
  private final E elem;

  /**
   * Constructor.
   * @param elem element
   */
  SingletonObjectArray(final E elem) {
    this.elem = elem;
  }

  @Override
  public ObjectArray<E> cons(final E head) {
    @SuppressWarnings("unchecked")
    final E[] left = (E[]) new Object[] { head }, right = (E[]) new Object[] { elem };
    return new DeepObjectArray<E>(left, Empty.<E, E>getInstance(), right);
  }

  @Override
  public ObjectArray<E> snoc(final E last) {
    @SuppressWarnings("unchecked")
    final E[] left = (E[]) new Object[] { elem }, right = (E[]) new Object[] { last };
    return new DeepObjectArray<>(left, Empty.<E, E>getInstance(), right);
  }

  @Override
  public E get(final long index) {
    if(index == 0) return elem;
    throw new IndexOutOfBoundsException("index: " + index + ", length: 1");
  }

  @Override
  public long size() {
    return 1;
  }

  @Override
  public Array<E> concat(final Array<E> seq) {
    return seq.cons(elem);
  }

  @Override
  public E head() {
    return elem;
  }

  @Override
  public E last() {
    return elem;
  }

  @Override
  public ObjectArray<E> init() {
    return empty();
  }

  @Override
  public ObjectArray<E> tail() {
    return empty();
  }

  @Override
  public boolean isEmpty() {
    return false;
  }

  @Override
  public ObjectArray<E> reverse() {
    return this;
  }

  @Override
  public ObjectArray<E> insertBefore(final long pos, final E val) {
    if(pos < 0) throw new IndexOutOfBoundsException("negative index: " + pos);
    if(pos > 1) throw new IndexOutOfBoundsException("position too big: " + pos);

    @SuppressWarnings("unchecked")
    final E[] oldV = (E[]) new Object[] { elem }, newV = (E[]) new Object[] { val };
    final FingerTree<E, E> middle = Empty.getInstance();
    return pos == 0 ? new DeepObjectArray<>(newV, middle, oldV)
                    : new DeepObjectArray<>(oldV, middle, newV);
  }

  @Override
  public ObjectArray<E> remove(final long pos) {
    if(pos < 0) throw new IndexOutOfBoundsException("negative index: " + pos);
    if(pos > 0) throw new IndexOutOfBoundsException("position too big: " + pos);
    return empty();
  }

  @Override
  public ObjectArray<E> subArray(final long pos, final long len) {
    if(pos < 0) throw new IndexOutOfBoundsException("first index < 0: " + pos);
    if(len < 0) throw new IndexOutOfBoundsException("length < 0: " + len);
    if(pos + len > 1)
      throw new IndexOutOfBoundsException("end out of bounds: " + (pos + len) + " > 1");
    return len == 1 ? this : ObjectArray.<E>empty();
  }

  @Override
  public Iterator<E> iterator() {
    final E elm = elem;
    return new Iterator<E>() {
      private boolean first = true;

      @Override
      public boolean hasNext() {
        return first;
      }

      @Override
      public E next() {
        if(!first) throw new NoSuchElementException();
        first = false;
        return elm;
      }

      @Override
      public void remove() {
        throw new UnsupportedOperationException();
      }
    };
  }

  @Override
  void checkInvariants() {
    // nothing can go wrong
  }

  @Override
  long[] sizes() {
    return new long[] { 1 };
  }
}
