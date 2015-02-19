package de.woerteler.persistent.array;

import java.util.*;

/**
 * An efficient persistent sequence based on Hinze and Patterson's Finger Trees.
 * <p>
 * The running time of all operations is given in their JavaDoc, {@code n} is the size of the array
 * and a {@code *} behind the time means that the bound is amortized.
 * <p>
 * The elements are allowed to be {@code null}.
 *
 * @author Leo Woerteler
 * @param <E> element type
 */
public abstract class Array<E> implements Iterable<E> {
  /**
   * Prepends an element to the front of this array.
   * Running time: <i>O(1)*</i>
   * @param elem element to prepend
   * @return resulting array
   */
  public abstract Array<E> cons(final E elem);

  /**
   * Appends an element to the back of this array.
   * Running time: <i>O(1)*</i>
   * @param elem element to append
   * @return resulting array
   */
  public abstract Array<E> snoc(final E elem);

  /**
   * Gets the element at the given position in this array.
   * Running time: <i>O(log n)</i>
   * @param index index of the element to get
   * @return the corresponding element
   * @throws IndexOutOfBoundsException if the index is smaller that {@code 0}
   *             or {@code >=} the {@link #size()} of this array
   */
  public abstract E get(long index);

  /**
   * Returns the number of elements in this array.
   * Running time: <i>O(1)</i>
   * @return number of elements
   */
  public abstract long size();

  /**
   * Concatenates this array with another one.
   * Running time: <i>O(log (min { this.size(), other.size() }))</i>
   * @param other array to append to the end of this array
   * @return resulting array
   */
  public abstract Array<E> concat(Array<E> other);

  /**
   * First element of this array, equivalent to {@code array.get(0)}.
   * Running time: <i>O(1)</i>
   * @return the first element
   */
  public abstract E head();

  /**
   * Last element of this array, equivalent to {@code array.get(array.size() - 1)}.
   * Running time: <i>O(1)</i>
   * @return last element
   */
  public abstract E last();

  /**
   * Initial segment of this array, i.e. an array containing all elements of this array (in the
   * same order), except for the last one.
   * Running time: <i>O(1)*</i>
   * @return initial segment
   * @throws IllegalStateException if the array is empty
   */
  public abstract Array<E> init();

  /**
   * Tail segment of this array, i.e. an array containing all elements of this array (in the
   * same order), except for the first one.
   * Running time: <i>O(1)*</i>
   * @return tail segment
   * @throws IllegalStateException if the array is empty
   */
  public abstract Array<E> tail();

  /**
   * Extracts a contiguous part of this array.
   * @param pos position of first element
   * @param len number of elements
   * @return the sub-array
   * @throws IndexOutOfBoundsException if {@code pos < 0} or {@code pos + len > this.size()}
   */
  public abstract Array<E> subArray(final long pos, final long len);

  /**
   * Returns an array with the same elements as this one, but their order reversed.
   * Running time: <i>O(n)</i>
   * @return reversed version of this array
   */
  public abstract Array<E> reverse();

  /**
   * Checks if this array is empty.
   * Running time: <i>O(1)</i>
   * @return {@code true} if the array is empty, {@code false} otherwise
   */
  public abstract boolean isEmpty();

  /**
   * Inserts the given element at the given position into this array.
   * Running time: <i>O(log n)</i>
   * @param pos insertion position, must be between {@code 0} and {@code this.size()}
   * @param val element to insert
   * @return resulting array
   * @throws IndexOutOfBoundsException if {@code pos < 0 || pos > this.size()} holds
   */
  public abstract Array<E> insertBefore(final long pos, final E val);

  /**
   * Removes the element at the given position in this array.
   * Running time: <i>O(log n)</i>
   * @param pos deletion position, must be between {@code 0} and {@code this.size() - 1}
   * @return resulting array
   * @throws IndexOutOfBoundsException if {@code pos < 0 || pos >= this.size()} holds
   */
  public abstract Array<E> remove(final long pos);

  @Override
  public final boolean equals(final Object obj) {
    if(this == obj) return true;
    if(!(obj instanceof Array)) return false;

    final Array<?> other = (Array<?>) obj;
    if(size() != other.size()) return false;

    final Iterator<?> iter = other.iterator();
    for(final Object elem : this) {
      final Object elem2 = iter.next();
      if(elem == null ? elem2 != null : elem2 == null || !elem.equals(elem2)) {
        return false;
      }
    }

    return true;
  }

  @Override
  public final int hashCode() {
    int hash = 1;
    for(final E elem : this) hash = 31 * hash + (elem == null ? 0 : elem.hashCode());
    return hash;
  }

  @Override
  public final String toString() {
    final StringBuilder sb = new StringBuilder("Array[");
    final Iterator<E> iter = iterator();
    if(iter.hasNext()) {
      sb.append(iter.next());
      while(iter.hasNext()) sb.append(", ").append(iter.next());
    }
    return sb.append(']').toString();
  }
}
