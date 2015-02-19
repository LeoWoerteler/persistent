package de.woerteler.persistent.array.object;

import java.util.*;

import de.woerteler.persistent.array.*;

/**
 * The empty array.
 *
 * @author Leo Woerteler
 * @param <E> element type
 */
final class EmptyObjectArray<E> extends ObjectArray<E> {
  /** The empty array. */
  static final EmptyObjectArray<?> INSTANCE = new EmptyObjectArray<>();

  /** Hidden constructor. */
  private EmptyObjectArray() {
  }

  @Override
  public ObjectArray<E> cons(final E elem) {
    return new SingletonObjectArray<E>(elem);
  }

  @Override
  public ObjectArray<E> snoc(final E elem) {
    return new SingletonObjectArray<E>(elem);
  }

  @Override
  public E get(final long index) {
    throw new IndexOutOfBoundsException();
  }

  @Override
  public long size() {
    return 0;
  }

  @Override
  public Array<E> concat(final Array<E> seq) {
    return seq;
  }

  @Override
  public E head() {
    throw new NoSuchElementException();
  }

  @Override
  public E last() {
    throw new NoSuchElementException();
  }

  @Override
  public ObjectArray<E> init() {
    throw new IllegalStateException();
  }

  @Override
  public ObjectArray<E> tail() {
    throw new IllegalStateException();
  }

  @Override
  public ObjectArray<E> subArray(final long pos, final long len) {
    if(pos < 0) throw new IndexOutOfBoundsException("first index < 0: " + pos);
    if(len < 0) throw new IndexOutOfBoundsException("length < 0: " + len);
    if(pos + len > 0)
      throw new IndexOutOfBoundsException("end out of bounds: " + (pos + len) + " > 0");
    return this;
  }

  @Override
  public boolean isEmpty() {
    return true;
  }

  @Override
  public ObjectArray<E> reverse() {
    return this;
  }

  @Override
  public ObjectArray<E> insertBefore(final long pos, final E val) {
    if(pos < 0) throw new IndexOutOfBoundsException("negative index: " + pos);
    if(pos > 0) throw new IndexOutOfBoundsException("position too big: " + pos);
    return new SingletonObjectArray<>(val);
  }

  @Override
  public ObjectArray<E> remove(final long pos) {
    if(pos < 0) throw new IndexOutOfBoundsException("negative index: " + pos);
    throw new IndexOutOfBoundsException("position too big: " + pos);
  }

  @Override
  public Iterator<E> iterator() {
    return Collections.emptyIterator();
  }

  @Override
  void checkInvariants() {
    // nothing can go wrong
  }

  @Override
  long[] sizes() {
    return new long[] { 0 };
  }
}
