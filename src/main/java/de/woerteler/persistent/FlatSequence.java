package de.woerteler.persistent;

import java.lang.ref.WeakReference;
import java.util.Arrays;

/**
 * Provides implementations for almost all sequence methods. An implementing
 * class only needs to provide the actual data storage and may improve the
 * implementations with more specific ones.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 * @param <T> The content type.
 */
public abstract class FlatSequence<T> extends AbstractSequence<T> {

  /** A soft reference to the converted sequence. */
  // maybe use SoftReference here to ensure longer live-span
  private volatile WeakReference<TrieSequence<T>> converted;

  /**
   * Converts this sequence into a fast modifiable trie sequence. The resulting
   * sequence may be cached via a soft reference.
   * 
   * @return The converted sequence.
   */
  protected TrieSequence<T> convert() {
    TrieSequence<T> res = null;
    if(converted == null || (res = converted.get()) == null) {
      res = asTrieSequence();
      converted = new WeakReference<TrieSequence<T>>(res);
    }
    return res;
  }

  /**
   * Converts this sequence into a trie sequence. This method is called before
   * attempting to change the content of this sequence. The result may be cached
   * for successive calls to {@link #convert()}.
   * 
   * @return This sequence as trie sequence.
   */
  protected TrieSequence<T> asTrieSequence() {
    return TrieSequence.from(this);
  }

  @Override
  public PersistentSequence<T> add(final T item) {
    return convert().add(item);
  }

  @Override
  public PersistentSequence<T> append(final PersistentSequence<? extends T> seq) {
    return convert().append(seq);
  }

  @Override
  public Object[] toArray() {
    final Object[] res = new Object[size()];
    for(int i = 0; i < res.length; ++i) {
      res[i] = get(i);
    }
    return res;
  }

  @Override
  public T[] toArray(final T[] arr) {
    final int size = size();
    T[] a;
    if(arr.length != size) {
      a = Arrays.copyOf(arr, size);
    } else {
      a = arr;
    }
    for(int i = 0; i < size; ++i) {
      a[i] = get(i);
    }
    return a;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder(getClass().getSimpleName());
    return sb.append(Arrays.toString(toArray())).toString();
  }

}
