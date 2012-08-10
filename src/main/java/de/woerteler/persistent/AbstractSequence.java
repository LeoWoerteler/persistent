package de.woerteler.persistent;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * The abstract base class for persistent sequences. It provides equality checks
 * and default implementations for interface methods. The default
 * implementations may be overridden with faster implementations.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 * @param <E> The type of the sequence.
 */
public abstract class AbstractSequence<E> implements PersistentSequence<E> {

  @Override
  public PersistentSequence<E> append(final PersistentSequence<? extends E> sequence) {
    PersistentSequence<E> seq = this;
    for(final E elem : sequence) {
      seq = seq.add(elem);
    }
    return seq;
  }

  @Override
  public Iterator<E> iterator() {
    return new Iterator<E>() {

      private int pos;

      @Override
      public boolean hasNext() {
        return pos < size();
      }

      @Override
      public E next() {
        if(pos >= size()) throw new NoSuchElementException();
        return get(pos++);
      }

      @Override
      public void remove() {
        throw new UnsupportedOperationException();
      }

    };
  }

  @Override
  public int indexOf(final E elem) {
    int pos = 0;
    for(final E e : this) {
      if(e == null) {
        if(elem == null) return pos;
      } else {
        if(e.equals(elem)) return pos;
      }
      ++pos;
    }
    return -1;
  }

  @Override
  public boolean contains(final E elem) {
    return indexOf(elem) != -1;
  }

  @Override
  public boolean equals(final Object obj) {
    if(obj == this) return true;
    if(!(obj instanceof PersistentSequence)) return false;
    final PersistentSequence<?> other = (PersistentSequence<?>) obj;
    if(size() != other.size()) return false;

    final Iterator<?> mine = iterator(), theirs = other.iterator();
    while(mine.hasNext()) {
      final Object a = mine.next(), b = theirs.next();
      if(a == null ? b != null : !a.equals(b)) return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    // the hash code is not cached because to objects may change their hash code
    int hash = 1;
    for(final E val : this) {
      hash = 31 * hash + (val == null ? 0 : val.hashCode());
    }
    return hash;
  }

}
