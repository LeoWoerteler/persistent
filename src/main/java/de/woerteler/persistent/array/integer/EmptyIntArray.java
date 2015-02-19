package de.woerteler.persistent.array.integer;

import java.util.*;

import de.woerteler.persistent.array.*;

/**
 * The empty array.
 *
 * @author Leo Woerteler
 * @param <E> element type
 */
final class EmptyIntArray<E> extends IntArray {
  /** The empty array. */
  static final EmptyIntArray<?> INSTANCE = new EmptyIntArray<>();

  /** Hidden constructor. */
  private EmptyIntArray() {
  }

  @Override
  public IntArray cons(final Integer elem) {
    return new SmallIntArray(new int[] { elem });
  }

  @Override
  public IntArray snoc(final Integer elem) {
    return new SmallIntArray(new int[] { elem });
  }

  @Override
  public Integer get(final long index) {
    throw new IndexOutOfBoundsException();
  }

  @Override
  public long size() {
    return 0;
  }

  @Override
  public Array<Integer> concat(final Array<Integer> seq) {
    return seq;
  }

  @Override
  public Integer head() {
    throw new NoSuchElementException();
  }

  @Override
  public Integer last() {
    throw new NoSuchElementException();
  }

  @Override
  public IntArray init() {
    throw new IllegalStateException();
  }

  @Override
  public IntArray tail() {
    throw new IllegalStateException();
  }

  @Override
  public IntArray subArray(final long pos, final long len) {
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
  public IntArray reverse() {
    return this;
  }

  @Override
  public IntArray insertBefore(final long pos, final Integer val) {
    if(pos < 0) throw new IndexOutOfBoundsException("negative index: " + pos);
    if(pos > 0) throw new IndexOutOfBoundsException("position too big: " + pos);
    return new SmallIntArray(new int[] { val.intValue() });
  }

  @Override
  public IntArray remove(final long pos) {
    if(pos < 0) throw new IndexOutOfBoundsException("negative index: " + pos);
    throw new IndexOutOfBoundsException("position too big: " + pos);
  }

  @Override
  public Iterator<Integer> iterator() {
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

  @Override
  IntArray consSmall(final int[] vals) {
    return new SmallIntArray(vals);
  }
}
