package de.woerteler.persistent;

import java.lang.ref.SoftReference;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

/**
 * A persistent sequence that is fast upon first creation. The addition of
 * elements is slow for the first element but may be faster for later elements
 * due to caching.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 * @param <E> The type of this sequence.
 */
public final class ArraySequence<E> implements PersistentSequence<E> {

  /**
   * Creates a persistent sequence out of an array.
   * 
   * @param <E> The type of the sequence.
   * @param array The array.
   * @return The sequence.
   */
  public static <E> PersistentSequence<E> from(final E[] array) {
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
  public Iterator<E> iterator() {
    return new Iterator<E>() {

      private int pos;

      @Override
      public boolean hasNext() {
        return pos < array.length;
      }

      @Override
      public E next() {
        return array[pos++];
      }

      @Override
      public void remove() {
        throw new UnsupportedOperationException();
      }

    };
  }

  /** A soft reference to the converted sequence. */
  private volatile SoftReference<TrieSequence<E>> converted;

  /**
   * Converts this sequence into a fast modifiable trie sequence. The resulting
   * sequence may be cached via a soft reference.
   * 
   * @return The converted sequence.
   */
  private TrieSequence<E> convert() {
    TrieSequence<E> res = null;
    if(converted == null || (res = converted.get()) == null) {
      res = TrieSequence.from(array);
      converted = new SoftReference<TrieSequence<E>>(res);
    }
    return res;
  }

  @Override
  public PersistentSequence<E> add(final E item) {
    return convert().add(item);
  }

  @Override
  public PersistentSequence<E> append(final PersistentSequence<? extends E> seq) {
    if(!(seq instanceof ArraySequence)) return convert().append(seq);
    @SuppressWarnings("unchecked")
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

}
