package de.woerteler.persistent;

import java.util.Arrays;
import java.util.Collection;

/**
 * A persistent sequence that is fast upon first creation. The addition of
 * elements is slow for the first element but may be faster for later elements
 * due to caching.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 * @param <E> The type of this sequence.
 */
public final class ArraySequence<E> extends FlatSequence<E> {

  /**
   * Creates a persistent sequence out of an array.
   * 
   * @param <E> The type of the sequence.
   * @param array The array.
   * @return The sequence.
   */
  public static <E> PersistentSequence<E> from(final E... array) {
    if(array.length == 0) return TrieSequence.empty();
    return new ArraySequence<E>(array.clone());
  }

  /**
   * Creates a persistent sequence out of a collection.
   * 
   * @param <E> The type of the sequence.
   * @param c The collection.
   * @return The sequence.
   */
  @SuppressWarnings("unchecked")
  public static <E> PersistentSequence<E> from(final Collection<E> c) {
    final Object[] arr = c.toArray();
    if(arr.length == 0) return TrieSequence.empty();
    return new ArraySequence<E>((E[]) arr);
  }

  /** The internal array. */
  protected final E[] array;

  /**
   * Creates a sequence out of an array.
   * 
   * @param array The array that is not copied.
   */
  private ArraySequence(final E[] array) {
    this.array = array;
  }

  @Override
  protected TrieSequence<E> asTrieSequence() {
    return TrieSequence.from(array);
  }

  @Override
  @SuppressWarnings("unchecked")
  public PersistentSequence<E> append(final PersistentSequence<? extends E> seq) {
    if(!(seq instanceof ArraySequence)) return super.append(seq);

    final ArraySequence<E> other = (ArraySequence<E>) seq;
    final int newLength = array.length + other.array.length;
    final E[] newArray = Arrays.copyOf(array, newLength);
    System.arraycopy(other.array, 0, newArray, array.length, other.array.length);
    return new ArraySequence<E>(newArray);
  }

  @Override
  public E get(final int pos) {
    return array[pos];
  }

  @Override
  public int size() {
    return array.length;
  }

  @Override
  public Object[] toArray() {
    return array.clone();
  }

  @Override
  public E[] toArray(final E[] arr) {
    E[] a;
    if(arr.length != array.length) {
      a = Arrays.copyOf(arr, array.length);
    } else {
      a = arr;
    }
    System.arraycopy(array, 0, a, 0, array.length);
    return a;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder(getClass().getSimpleName());
    return sb.append(Arrays.toString(array)).toString();
  }

}
