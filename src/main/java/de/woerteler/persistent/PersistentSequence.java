package de.woerteler.persistent;

import java.util.NoSuchElementException;
import java.util.RandomAccess;

/**
 * An interface for immutable sequences.
 * All operations that would update a mutable sequence return a new sequence reflecting
 * the changes instead.
 *
 * @author Leo Woerteler
 * @param <E> element type
 */
public interface PersistentSequence<E> extends Iterable<E>, RandomAccess {
  /**
   * Adds an item at the end of the sequence.
   * @param item item to be added
   * @return a sequence which contains {@code item} in addition to the items of this
   *   sequence
   */
  PersistentSequence<E> add(final E item);

  /**
   * Appends the elements of the given sequence to this sequence.
   * @param sequence sequence to be appended
   * @return a sequence where the items of {@code sequence} are appended to this sequence
   */
  PersistentSequence<E> append(final PersistentSequence<? extends E> sequence);

  /**
   * Returns the first index of the given element if it exists in the sequence.
   * 
   * @param elem The element.
   * @return The first index of the element or <code>-1</code> if the element is
   *         not contained.
   */
  int indexOf(E elem);

  /**
   * Whether the sequence contains the given element.
   * 
   * @param elem The element.
   * @return Whether the sequence contains it.
   */
  boolean contains(E elem);

  /**
   * Gets the element at the given position in the sequence.
   * @param pos position of the element
   * @return element at the given position
   * @throws NoSuchElementException is the given position is not in the range of indices
   *   of this sequence
   */
  E get(final int pos);

  /**
   * Number of elements of this sequence.
   * @return number of elements
   */
  int size();

  /**
   * Returns an array containing all of the elements in this sequence in proper
   * order (from first to last element).
   * @return array containing all elements
   */
  Object[] toArray();

  /**
   * Returns an array containing all of the elements in this sequence in
   * proper order (from first to last element); the runtime type of the returned array is
   * that of the specified array. If the sequence fits in the specified array, it is
   * returned therein.  Otherwise, a new array is allocated with the runtime type of the
   * specified array and the size of this sequence.
   * @param array array to copy the elements in or to determine the type of the output
   * @return array containing the elements of this sequence
   */
  E[] toArray(final E[] array);
}
