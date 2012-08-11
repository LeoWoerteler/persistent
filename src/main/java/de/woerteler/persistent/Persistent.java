package de.woerteler.persistent;

import java.util.Collection;

/**
 * Handles the creation of immutable sequences.
 *
 * @author Joschi <josua.krause@googlemail.com>
 *
 */
public final class Persistent {

  /** No constructor. */
  private Persistent() {
    // no constructor
  }

  /**
   * Returns the empty sequence.
   * 
   * @param <E> The type of the sequence.
   * @return The empty sequence.
   */
  public static <E> PersistentSequence<E> empty() {
    return TrieSequence.empty();
  }

  /**
   * Creates a persistent sequence from an array.
   * 
   * @param <E> The type of the sequence.
   * @param es The array.
   * @return The sequence.
   */
  public static <E> PersistentSequence<E> from(final E... es) {
    return ArraySequence.from(es);
  }

  /**
   * Creates a persistent sequence from an iterable.
   * 
   * @param <E> The type of the sequence.
   * @param it The iterable.
   * @return The sequence.
   */
  public static <E> PersistentSequence<E> from(final Iterable<E> it) {
    if(it instanceof Collection) return from((Collection<E>) it);
    return TrieSequence.from(it);
  }

  /**
   * Creates a persistent sequence from a collection.
   * 
   * @param <E> The type of sequence.
   * @param c The collection.
   * @return The sequence.
   */
  public static <E> PersistentSequence<E> from(final Collection<E> c) {
    return ArraySequence.from(c);
  }

}
