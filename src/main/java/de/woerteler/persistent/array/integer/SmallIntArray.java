package de.woerteler.persistent.array.integer;

import java.util.*;

import de.woerteler.persistent.array.*;

/**
 * A singleton array.
 *
 * @author Leo Woerteler
 */
final class SmallIntArray extends IntArray {
  /** The elements. */
  final int[] elems;

  /**
   * Constructor.
   * @param elems elements
   */
  SmallIntArray(final int[] elems) {
    this.elems = elems;
    assert elems.length >= 1 && elems.length <= MAX_SMALL;
  }

  @Override
  public IntArray cons(final Integer head) {
    if(elems.length < MAX_SMALL) {
      final int[] newElems = slice(elems, -1, elems.length);
      newElems[0] = head.intValue();
      return new SmallIntArray(newElems);
    }

    final int mid = MIN_DIGIT - 1;
    final int[] left = slice(elems, -1, mid), right = slice(elems, mid, elems.length);
    left[0] = head;
    return new DeepIntArray(left, right);
  }

  @Override
  public IntArray snoc(final Integer last) {
    if(elems.length < MAX_SMALL) {
      final int[] newElems = slice(elems, 0, elems.length + 1);
      newElems[newElems.length - 1] = last;
      return new SmallIntArray(newElems);
    }

    final int[] left = slice(elems, 0, MIN_DIGIT),
        right = slice(elems, MIN_DIGIT, elems.length + 1);
    right[right.length - 1] = last;
    return new DeepIntArray(left, right);
  }

  @Override
  public Integer get(final long index) {
    // index to small?
    if(index < 0) throw new IndexOutOfBoundsException("Index < 0: " + index);

    // index too big?
    if(index >= elems.length) throw new IndexOutOfBoundsException(index + " >= " + elems.length);

    return elems[(int) index];
  }

  @Override
  public long size() {
    return elems.length;
  }

  @Override
  public Array<Integer> concat(final Array<Integer> seq) {
    if(seq.isEmpty()) return this;
    if(seq instanceof IntArray) ((IntArray) seq).consSmall(elems);
    Array<Integer> curr = seq;
    for(int i = elems.length; --i >= 0;) curr = curr.cons(elems[i]);
    return curr;
  }

  @Override
  public Integer head() {
    return elems[0];
  }

  @Override
  public Integer last() {
    return elems[elems.length - 1];
  }

  @Override
  public IntArray init() {
    if(elems.length == 1) return empty();
    return new SmallIntArray(slice(elems, 0, elems.length - 1));
  }

  @Override
  public IntArray tail() {
    if(elems.length == 1) return empty();
    return new SmallIntArray(slice(elems, 1, elems.length));
  }

  @Override
  public boolean isEmpty() {
    return false;
  }

  @Override
  public IntArray reverse() {
    final int n = elems.length;
    if(n == 1) return this;
    final int[] es = new int[n];
    for(int i = 0; i < n; i++) es[i] = elems[n - 1 - i];
    return new SmallIntArray(es);
  }

  @Override
  public IntArray insertBefore(final long pos, final Integer val) {
    if(pos < 0) throw new IndexOutOfBoundsException("negative index: " + pos);
    if(pos > elems.length) throw new IndexOutOfBoundsException("position too big: " + pos);

    final int p = (int) pos, n = elems.length, v = val;
    final int[] out = new int[n + 1];
    System.arraycopy(elems, 0, out, 0, p);
    out[p] = v;
    System.arraycopy(elems, p, out, p + 1, n - p);

    if(n < MAX_SMALL) return new SmallIntArray(out);
    return new DeepIntArray(slice(out, 0, MIN_DIGIT), slice(out, MIN_DIGIT, n + 1));
  }

  @Override
  public IntArray remove(final long pos) {
    if(pos < 0) throw new IndexOutOfBoundsException("negative index: " + pos);
    if(pos >= elems.length) throw new IndexOutOfBoundsException("position too big: " + pos);
    final int p = (int) pos, n = elems.length;
    if(n == 1) return empty();

    final int[] out = new int[n - 1];
    System.arraycopy(elems, 0, out, 0, p);
    System.arraycopy(elems, p + 1, out, p, n - 1 - p);
    return new SmallIntArray(out);
  }

  @Override
  public IntArray subArray(final long pos, final long len) {
    if(pos < 0) throw new IndexOutOfBoundsException("first index < 0: " + pos);
    if(len < 0) throw new IndexOutOfBoundsException("length < 0: " + len);
    if(pos + len > elems.length)
      throw new IndexOutOfBoundsException("end out of bounds: "
          + (pos + len) + " > " + elems.length);

    final int p = (int) pos, n = (int) len;
    return n == 0 ? IntArray.empty() : new SmallIntArray(slice(elems, p, p + n));
  }

  @Override
  public ListIterator<Integer> listIterator(final long start) {
    if(start < 0 || start > elems.length) throw new IndexOutOfBoundsException("" + start);
    return new ListIterator<Integer>() {
      private int index = (int) start;

      @Override
      public int nextIndex() {
        return index;
      }

      @Override
      public boolean hasNext() {
        return index < elems.length;
      }

      @Override
      public Integer next() {
        if(index >= elems.length) throw new NoSuchElementException();
        return elems[index++];
      }

      @Override
      public int previousIndex() {
        return index - 1;
      }

      @Override
      public boolean hasPrevious() {
        return index > 0;
      }

      @Override
      public Integer previous() {
        if(index <= 0) throw new NoSuchElementException();
        return elems[--index];
      }

      @Override
      public void set(final Integer e) {
        throw new UnsupportedOperationException();
      }

      @Override
      public void add(final Integer e) {
        throw new UnsupportedOperationException();
      }

      @Override
      public void remove() {
        throw new UnsupportedOperationException();
      }
    };
  }

  @Override
  void checkInvariants() {
    final int n = elems.length;
    if(n == 0) throw new AssertionError("Empty array in " + getClass().getSimpleName());
    if(n > MAX_SMALL) throw new AssertionError("Array too big: " + n);
  }

  @Override
  IntArray consSmall(final int[] left) {
    final int l = left.length, r = elems.length, n = l + r;
    if(Math.min(l, r) >= MIN_DIGIT) {
      // both arrays can be used as digits
      return new DeepIntArray(left, elems);
    }

    final int[] out = new int[n];
    System.arraycopy(left, 0, out, 0, l);
    System.arraycopy(elems, 0, out, l, r);
    if(n <= MAX_SMALL) return new SmallIntArray(out);

    final int mid = n / 2;
    return new DeepIntArray(slice(out, 0, mid), slice(out, mid, n));
  }
}
