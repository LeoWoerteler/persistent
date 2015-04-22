package de.woerteler.persistent.array.object;

import java.util.*;

import de.woerteler.persistent.array.*;
import de.woerteler.persistent.fingertree.*;

/**
 * An array storing arbitrary objects.
 *
 * @author Leo Woerteler
 * @param <E> element type
 */
public final class ObjectArray<E> extends Array<E> {
  /** Singleton instance of the empty array. */
  private static final ObjectArray<?> EMPTY = new ObjectArray<>(FingerTree.empty());

  /** Root node. */
  final FingerTree<E, E> root;

  /**
   * Constructor.
   * @param root root node
   */
  ObjectArray(final FingerTree<E, E> root) {
    this.root = root;
  }
  /**
   * The empty sequence.
   * Running time: <i>O(1)</i> and no allocation
   * @param <E> element type
   * @return (unique) instance of an empty sequence
   */
  @SuppressWarnings("unchecked")
  public static <E> ObjectArray<E> empty() {
    return (ObjectArray<E>) EMPTY;
  }

  /**
   * Creates a singleton array containing the given element.
   * @param <E> element type
   * @param elem the contained element
   * @return the singleton array
   */
  public static <E> ObjectArray<E> singleton(final E elem) {
    return new ObjectArray<>(FingerTree.singleton(new Leaf<>(elem)));
  }

  /**
   * Creates an array containing the given elements.
   * @param <E> element type
   * @param elems elements
   * @return the resulting array
   */
  @SafeVarargs
  public static <E> ObjectArray<E> from(final E... elems) {
    if(elems.length == 0) return empty();
    final FingerTreeBuilder<E> builder = new FingerTreeBuilder<>();
    for(final E e : elems) builder.append(new Leaf<>(e));
    return new ObjectArray<>(builder.freeze());
  }

  /**
   * Creates an array containing the elements returned by the given iterator.
   * @param <E> element type
   * @param iter element iterator
   * @return the resulting array
   */
  public static <E> ObjectArray<E> from(final Iterator<? extends E> iter) {
    if(!iter.hasNext()) return empty();
    final FingerTreeBuilder<E> builder = new FingerTreeBuilder<>();
    do {
      builder.append(new Leaf<E>(iter.next()));
    } while(iter.hasNext());
    return new ObjectArray<>(builder.freeze());
  }

  /**
   * Creates an array containing the elements returned by the given iterable.
   * @param <E> element type
   * @param iter element iterable
   * @return the resulting array
   */
  public static <E> ObjectArray<E> from(final Iterable<? extends E> iter) {
    return from(iter.iterator());
  }

  @Override
  public ObjectArray<E> cons(final E elem) {
    return new ObjectArray<>(root.cons(new Leaf<>(elem)));
  }

  @Override
  public ObjectArray<E> snoc(final E elem) {
    return new ObjectArray<>(root.snoc(new Leaf<>(elem)));
  }

  @Override
  @SuppressWarnings("unchecked")
  public Array<E> concat(final Array<E> other) {
    if(this.isEmpty()) return other;
    if(other.isEmpty()) return this;
    final ObjectArray<E> right =
        other instanceof ObjectArray ? (ObjectArray<E>) other : from(other);
    return new ObjectArray<>(root.concat(new Node[0], 0, right.root));
  }

  @Override
  public ObjectArray<E> init() {
    return size() == 1 ? ObjectArray.<E>empty() : new ObjectArray<>(root.init());
  }

  @Override
  public ObjectArray<E> tail() {
    return size() == 1 ? ObjectArray.<E>empty() : new ObjectArray<>(root.tail());
  }

  @Override
  public ObjectArray<E> subArray(final long pos, final long len) {
    if(pos < 0 || len < 0 || len > size() - pos) throw new IndexOutOfBoundsException();
    if(len == 0) return empty();
    if(len == size()) return this;
    return new ObjectArray<E>(root.slice(pos, len).getTree());
  }

  @Override
  public ObjectArray<E> reverse() {
    return size() < 2 ? this : new ObjectArray<>(root.reverse());
  }

  @Override
  public ObjectArray<E> insertBefore(final long pos, final E val) {
    if(pos < 0 || pos > size()) throw new IndexOutOfBoundsException();
    if(isEmpty()) return singleton(val);
    return new ObjectArray<>(root.insert(pos, val));
  }

  @Override
  public ObjectArray<E> remove(final long pos) {
    if(pos < 0 || pos >= size()) throw new IndexOutOfBoundsException();
    final TreeSlice<E, E> slice = root.remove(pos);
    if(slice.isTree()) return new ObjectArray<>(slice.getTree());
    return empty();
  }

  @Override
  public E get(final long index) {
    if(0 <= index && index < size()) return root.get(index);
    throw new IndexOutOfBoundsException("" + index);
  }

  @Override
  public long size() {
    return root.size();
  }

  @Override
  public E head() {
    if(isEmpty()) throw new NoSuchElementException();
    return root.head().getSub(0);
  }

  @Override
  public E last() {
    return root.last().getSub(0);
  }

  @Override
  public boolean isEmpty() {
    return this == EMPTY;
  }

  @Override
  public ListIterator<E> listIterator(final long start) {
    return root.listIterator(start);
  }

  /**
   * Checks that this array's implementation does not violate any invariants.
   * @throws AssertionError if an invariant was violated
   */
  void checkInvariants() {
    root.checkInvariants();
  }

  /** Empty partial node. */
  private static final class Empty implements NodeLike<Object, Object> {
    /** Singleton instance. */
    static final Empty INSTANCE = new Empty();

    /** Hidden default constructor. */
    private Empty() {
    }

    @Override
    public int append(final NodeLike<Object, Object>[] nodes, final int pos) {
      return pos;
    }

    @Override
    public String toString() {
      return "Empty[]";
    }
  }

  /**
   * Leaf node containing a single element.
   * @author Leo Woerteler
   *
   * @param <E> element type
   */
  static class Leaf<E> implements Node<E, E> {
    /** The element. */
    private final E elem;

    /**
     * Constructor.
     * @param elem the element
     */
    Leaf(final E elem) {
      this.elem = elem;
    }

    @Override
    public int append(final NodeLike<E, E>[] nodes, final int pos) {
      final int ins = pos == 0 || nodes[pos - 1] instanceof Leaf ? pos : pos - 1;
      nodes[ins] = this;
      return ins + 1;
    }

    @Override
    public long size() {
      return 1;
    }

    @Override
    public int arity() {
      return 1;
    }

    @Override
    public E getSub(final int pos) {
      return elem;
    }

    @Override
    public Node<E, E> reverse() {
      return this;
    }

    @Override
    public boolean insert(final Node<E, E>[] siblings, final long pos, final E val) {
      siblings[3] = siblings[2];
      if(pos == 0) {
        siblings[1] = new Leaf<>(val);
        siblings[2] = this;
      } else {
        siblings[1] = this;
        siblings[2] = new Leaf<>(val);
      }
      return true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public NodeLike<E, E>[] remove(final Node<E, E> l, final Node<E, E> r, final long pos) {
      return new NodeLike[] { l, null, r };
    }

    @Override
    @SuppressWarnings("unchecked")
    public NodeLike<E, E> slice(final long off, final long len) {
      return len == 0 ? (NodeLike<E, E>) Empty.INSTANCE : this;
    }

    @Override
    public long checkInvariants() {
      return 1;
    }

    @Override
    public String toString() {
      final StringBuilder sb = new StringBuilder();
      sb.append("Leaf[");
      final String[] out = elem.toString().split("\n|\r\n?");
      if(out.length == 1) {
        sb.append(out[0]);
      } else {
        sb.append("\n");
        for(final String part : out) {
          sb.append("  ").append(part).append("\n");
        }
      }
      return sb.append(']').toString();
    }
  }
}
