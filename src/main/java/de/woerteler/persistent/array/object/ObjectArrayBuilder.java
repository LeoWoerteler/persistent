package de.woerteler.persistent.array.object;

import java.util.*;

import de.woerteler.persistent.fingertree.*;

/**
 * Builder for {@link ObjectArray}s.
 *
 * @author Leo Woerteler
 * @param <E> element type
 */
public final class ObjectArrayBuilder<E> implements Iterable<E> {
  /** Builder for the underlying finger tree. */
  private FingerTreeBuilder<E> builder = new FingerTreeBuilder<>();

  /**
   * Prepends a single element to the array.
   * @param elem element to prepend
   * @return this builder for convenience
   */
  public ObjectArrayBuilder<E> prepend(final E elem) {
    builder.prepend(new ObjectArray.Leaf<>(elem));
    return this;
  }

  /**
   * Appends a single element to the array.
   * @param elem element to append
   * @return this builder for convenience
   */
  public ObjectArrayBuilder<E> append(final E elem) {
    builder.append(new ObjectArray.Leaf<>(elem));
    return this;
  }

  /**
   * Appends another {@link ObjectArray} to the array.
   * @param other array to append
   * @return this builder for convenience
   */
  public ObjectArrayBuilder<E> append(final ObjectArray<E> other) {
    builder.append(other.root);
    return this;
  }

  /**
   * Returns an {@link ObjectArray} containing the elements in this builder.
   * @return object array
   */
  public ObjectArray<E> freeze() {
    return builder.isEmpty() ? ObjectArray.<E>empty() : new ObjectArray<>(builder.freeze());
  }

  @Override
  public Iterator<E> iterator() {
    return builder.iterator();
  }
}
