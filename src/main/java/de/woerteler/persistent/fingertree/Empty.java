package de.woerteler.persistent.fingertree;

import java.util.*;

import de.woerteler.persistent.fingertree.node.*;

/**
 * An empty finger tree.
 *
 * @author Leo Woerteler
 * @param <N> node type
 * @param <E> element type
 */
public final class Empty<N, E> extends FingerTree<N, E> {
  /** The empty finger tree. */
  static final Empty<?, ?> INSTANCE = new Empty<>();

  /**
   * Getter for the empty finger tree.
   * @param <N> node type
   * @param <E> element type
   * @return empty tree
   */
  @SuppressWarnings("unchecked")
  public static <N, E> Empty<N, E> getInstance() {
    return (Empty<N, E>) INSTANCE;
  }

  @Override
  public FingerTree<N, E> cons(final Node<N, E> fst) {
    return new Single<>(fst);
  }

  @Override
  public FingerTree<N, E> snoc(final Node<N, E> lst) {
    return new Single<>(lst);
  }

  @Override
  public Node<N, E> head() {
    throw new NoSuchElementException();
  }

  @Override
  public Node<N, E> last() {
    throw new NoSuchElementException();
  }

  @Override
  public FingerTree<N, E> init() {
    throw new IllegalStateException("Empty Tree");
  }

  @Override
  public FingerTree<N, E> tail() {
    throw new IllegalStateException("Empty Tree");
  }

  @Override
  public long size() {
    return 0;
  }

  @Override
  public FingerTree<N, E> concat(final Node<N, E>[] nodes, final FingerTree<N, E> other) {
    return other.addAll(nodes, true);
  }

  @Override
  public TreeSlice<N, E> slice(final long pos, final long len) {
    if(pos == 0 && len == 0) return new TreeSlice<>(this);
    throw new AssertionError("Empty sub-tree.");
  }

  @Override
  public FingerTree<N, E> reverse() {
    return this;
  }

  @Override
  public FingerTree<N, E> insert(final long pos, final E val) {
    throw new AssertionError("Empty sub-tree.");
  }

  @Override
  public Iterator<E> iterator() {
    return Collections.emptyIterator();
  }

  @Override
  public FingerTree<N, E> replaceHead(final Node<N, E> head) {
    throw new AssertionError("Empty sub-tree.");
  }

  @Override
  public FingerTree<N, E> replaceLast(final Node<N, E> last) {
    throw new AssertionError("Empty sub-tree.");
  }

  @Override
  void toString(final StringBuilder sb, final int indent) {
    for(int i = 0; i < indent; i++) sb.append("  ");
    sb.append("Empty[]");
  }

  @Override
  public long checkInvariants() {
    return 0;
  }

  @Override
  public long[] sizes(final int depth) {
    return new long[depth];
  }

  @Override
  FingerTree<N, E> addAll(final Node<N, E>[] nodes, final boolean left) {
    return buildTree(nodes, nodes.length, Deep.size(nodes));
  }
}
