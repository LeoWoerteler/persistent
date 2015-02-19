package de.woerteler.persistent.array.object;

import java.util.*;

import de.woerteler.persistent.array.*;
import de.woerteler.persistent.fingertree.*;
import de.woerteler.persistent.fingertree.node.*;

/**
 * An array containing at least two elements.
 *
 * @author Leo Woerteler
 * @param <E> element type
 */
final class DeepObjectArray<E> extends ObjectArray<E> {

  /** Left digit. */
  private final E[] left;
  /** Middle tree. */
  private final FingerTree<E, E> middle;
  /** Right digit. */
  private final E[] right;

  /**
   * Constructor.
   * @param left left digit
   * @param middle middle tree
   * @param right right digit
   */
  DeepObjectArray(final E[] left, final FingerTree<E, E> middle, final E[] right) {
    this.left = left;
    this.middle = middle;
    this.right = right;
  }

  @Override
  public boolean isEmpty() {
    return false;
  }

  @Override
  public long size() {
    // O(1) because the middle tree caches its size
    return left.length + middle.size() + right.length;
  }

  @Override
  public E head() {
    return left[0];
  }

  @Override
  public E last() {
    return right[right.length - 1];
  }

  @Override
  public ObjectArray<E> cons(final E elem) {
    if(left.length < 4) {
      final E[] newLeft = slice(left, -1, left.length);
      newLeft[0] = elem;
      return new DeepObjectArray<>(newLeft, middle, right);
    }

    final E[] newLeft = slice(left, -1, 1);
    newLeft[0] = elem;
    final Node<E, E> sub = new LeafNode<E>(left[1], left[2], left[3]);
    return new DeepObjectArray<>(newLeft, middle.cons(sub), right);
  }

  @Override
  public ObjectArray<E> snoc(final E elem) {
    if(right.length < 4) {
      final E[] newRight = slice(right, 0, right.length + 1);
      newRight[right.length] = elem;
      return new DeepObjectArray<>(left, middle, newRight);
    }

    final E[] newRight = slice(right, 3, 5);
    newRight[1] = elem;
    final Node<E, E> sub = new LeafNode<>(right[0], right[1], right[2]);
    return new DeepObjectArray<>(left, middle.snoc(sub), newRight);
  }

  @Override
  public ObjectArray<E> init() {
    if(right.length > 1) {
      // right digit is safe, just shrink it
      return new DeepObjectArray<>(left, middle, slice(right, 0, right.length - 1));
    }

    if(middle.isEmpty()) {
      // middle tree empty, make a tree from the left digit
      final int l = left.length;
      if(l == 1) return new SingletonObjectArray<>(left[0]);

      // split the left digit
      final int mid = l / 2;
      return new DeepObjectArray<E>(slice(left, 0, mid), Empty.<E, E>getInstance(),
          slice(left, mid, l));
    }

    // extract values for the right digit from the middle
    final LeafNode<E> last = (LeafNode<E>) middle.last();
    return new DeepObjectArray<>(left, middle.init(), last.values());
  }

  @Override
  public ObjectArray<E> tail() {
    if(left.length > 1) {
      // left digit is safe, just shrink it
      return new DeepObjectArray<>(slice(left, 1, left.length), middle, right);
    }

    if(middle.isEmpty()) {
      // middle tree empty, make a tree from the right list
      final int r = right.length;
      if(r == 1) return new SingletonObjectArray<>(right[0]);

      // split the right digit
      final int mid = r / 2;
      return new DeepObjectArray<>(slice(right, 0, mid), Empty.<E, E>getInstance(),
          slice(right, mid, r));
    }

    // extract values for the left digit from the middle
    final LeafNode<E> head = (LeafNode<E>) middle.head();
    return new DeepObjectArray<>(head.values(), middle.tail(), right);
  }

  @Override
  public ObjectArray<E> concat(final Array<E> seq) {
    if(seq.size() < 2) return seq.isEmpty() ? this : this.snoc(seq.head());
    if(!(seq instanceof ObjectArray)) return concat(from(seq));

    final DeepObjectArray<E> other = (DeepObjectArray<E>) seq;

    // make nodes out of the digits facing each other
    final E[] ls = right, rs = other.left;
    final int l = ls.length, n = l + rs.length;
    final int k = (n + 2) / 3;
    @SuppressWarnings("unchecked")
    final Node<E, E>[] midNodes = new Node[k];
    int p = 0;
    for(int i = 0; i < k; i++) {
      final int rest = n - p;
      final E x = p < l ? ls[p] : rs[p - l];
      p++;
      final E y = p < l ? ls[p] : rs[p - l];
      p++;
      if(rest > 4 || rest == 3) {
        final E z = p < l ? ls[p] : rs[p - l];
        midNodes[i] = new LeafNode<>(x, y, z);
        p++;
      } else {
        midNodes[i] = new LeafNode<>(x, y);
      }
    }

    return new DeepObjectArray<>(left, middle.concat(midNodes, other.middle), other.right);
  }

  @Override
  public E get(final long index) {
    // index to small?
    if(index < 0) throw new IndexOutOfBoundsException("Index < 0: " + index);

    // index too big?
    final long midSize = left.length + middle.size(), size = midSize + right.length;
    if(index >= size) throw new IndexOutOfBoundsException(index + " >= " + size);

    // index in one of the digits?
    if(index < left.length) return left[(int) index];
    if(index >= midSize) return right[(int) (index - midSize)];

    // the element is in the middle tree
    return middle.get(index - left.length);
  }

  @Override
  public ObjectArray<E> reverse() {
    final int l = left.length, r = right.length;
    @SuppressWarnings("unchecked")
    final E[] newLeft = (E[]) new Object[r], newRight = (E[]) new Object[l];
    for(int i = 0; i < r; i++) newLeft[i] = right[r - 1 - i];
    for(int i = 0; i < l; i++) newRight[i] = left[l - 1 - i];
    return new DeepObjectArray<E>(newLeft, middle.reverse(), newRight);
  }

  @Override
  public ObjectArray<E> insertBefore(final long pos, final E val) {
    if(pos < 0) throw new IndexOutOfBoundsException("negative index: " + pos);
    if(pos > size()) throw new IndexOutOfBoundsException("position too big: " + pos);

    final int l = left.length;
    if(pos <= l) {
      final int p = (int) pos;
      final E[] temp = slice(left, 0, l + 1);
      System.arraycopy(temp, p, temp, p + 1, l - p);
      temp[p] = val;
      if(l < 4) return new DeepObjectArray<>(temp, middle, right);

      final FingerTree<E, E> newMid = middle.cons(new LeafNode<>(temp[2], temp[3], temp[4]));
      return new DeepObjectArray<>(slice(temp, 0, 2), newMid, right);
    }

    final long midSize = middle.size();
    if(pos - l < midSize) {
      final FingerTree<E, E> newMid = middle.insert(pos - l, val);
      return new DeepObjectArray<>(left, newMid, right);
    }

    final int r = right.length;
    final int p = (int) (pos - l - midSize);
    final E[] temp = slice(right, 0, r + 1);
    System.arraycopy(temp, p, temp, p + 1, r - p);
    temp[p] = val;
    if(r < 4) return new DeepObjectArray<>(left, middle, temp);

    final FingerTree<E, E> newMid = middle.snoc(new LeafNode<>(temp[0], temp[1], temp[2]));
    return new DeepObjectArray<>(left, newMid, slice(temp, 3, 5));
  }

  @Override
  public ObjectArray<E> remove(final long pos) {
    if(pos < 0) throw new IndexOutOfBoundsException("negative index: " + pos);
    if(pos >= size()) throw new IndexOutOfBoundsException("position too big: " + pos);

    if(pos < left.length) {
      // delete from left digit
      final int p = (int) pos;
      if(left.length > 1) {
        // there is enough space, delete it
        @SuppressWarnings("unchecked")
        final E[] newLeft = (E[]) new Object[left.length - 1];
        System.arraycopy(left, 0, newLeft, 0, p);
        System.arraycopy(left, p + 1, newLeft, p, newLeft.length - p);
        return new DeepObjectArray<>(newLeft, middle, right);
      }

      if(!middle.isEmpty()) {
        // extract a new left digit from the middle
        final LeafNode<E> head = (LeafNode<E>) middle.head();
        return new DeepObjectArray<>(head.values(), middle.tail(), right);
      }

      if(right.length == 1) {
        // only one element left
        return new SingletonObjectArray<>(right[0]);
      }

      final int mid = right.length / 2;
      return new DeepObjectArray<>(slice(right, 0, mid), middle, slice(right, mid, right.length));
    }

    final long midSize = middle.size();
    if(pos - left.length < midSize) {
      // delete in middle tree
      final long off = pos - left.length;

      if(middle instanceof Single) {
        // tree height might change
        final E[] values = ((LeafNode<E>) middle.head()).values();
        if(values.length > 2) {
          // no underflow
          final E a = values[off < 1 ? 1 : 0], b = values[off < 2 ? 2 : 1];
          return new DeepObjectArray<>(left, new Single<>(new LeafNode<E>(a, b)), right);
        }

        // node will underflow, balance with digits
        final E elem = values[off == 0 ? 1 : 0];

        if(left.length < right.length) {
          // merge into left digit
          final E[] newLeft = slice(left, 0, left.length + 1);
          newLeft[left.length] = elem;
          return new DeepObjectArray<>(newLeft, Empty.<E, E>getInstance(), right);
        }

        if(right.length < 4) {
          // merge into right digit
          final E[] newRight = slice(right, -1, right.length);
          newRight[0] = elem;
          return new DeepObjectArray<>(left, Empty.<E, E>getInstance(), newRight);
        }

        // refill from left and right digit
        return new DeepObjectArray<E>(slice(left, 0, 3),
            new Single<>(new LeafNode<>(left[3], elem, right[0])), slice(right, 1, 4));
      }

      // middle tree cannot underflow
      final Deep<E, E> deep = (Deep<E, E>) middle;
      return new DeepObjectArray<>(left, deep.remove(off), right);
    }

    // delete from right digit
    final int off = (int) (pos - left.length - midSize);
    if(right.length > 1) {
      // no rebalancing necessary
      @SuppressWarnings("unchecked")
      final E[] newRight = (E[]) new Object[right.length - 1];
      System.arraycopy(right, 0, newRight, 0, off);
      System.arraycopy(right, off + 1, newRight, off, newRight.length - off);
      return new DeepObjectArray<>(left, middle, newRight);
    }

    if(!middle.isEmpty()) {
      // refill the right digit from the middle
      final LeafNode<E> last = (LeafNode<E>) middle.last();
      return new DeepObjectArray<>(left, middle.init(), last.values());
    }

    // rebalance from left digit
    if(left.length == 1) return new SingletonObjectArray<>(left[0]);

    // split the left digit
    final int mid = left.length / 2;
    return new DeepObjectArray<>(slice(left, 0, mid), middle, slice(left, mid, left.length));
  }

  @Override
  public ObjectArray<E> subArray(final long pos, final long len) {
    if(pos < 0) throw new IndexOutOfBoundsException("first index < 0: " + pos);
    if(len < 0) throw new IndexOutOfBoundsException("length < 0: " + len);
    if(len > size() - pos)
      throw new IndexOutOfBoundsException("end out of bounds: " + (pos + len) + " > " + size());

    // the easy cases
    if(len < 2) return len == 0 ? ObjectArray.<E>empty() : new SingletonObjectArray<>(get(pos));

    // at least two elements
    final long end = pos + len;
    if(end <= left.length) {
      // completely in left digit
      final int from = (int) pos, to = (int) end, mid = (from + to) >>> 1;
      return new DeepObjectArray<>(slice(left, from, mid), Empty.<E, E>getInstance(),
          slice(left, mid, to));
    }

    final long midSize = middle.size(), rightOffset = left.length + midSize;
    if(pos >= rightOffset) {
      // completely in right digit
      final int from = (int) (pos - rightOffset), to = from + (int) len, mid = (from + to) >>> 1;
      return new DeepObjectArray<>(slice(right, from, mid), Empty.<E, E>getInstance(),
          slice(right, mid, to));
    }

    final int inLeft = pos < left.length ? (int) (left.length - pos) : 0,
        inRight = end > rightOffset ? (int) (end - rightOffset) : 0;

    if(inLeft > 0 && inRight > 0) {
      // middle tree stays untouched
      return new DeepObjectArray<>(slice(left, (int) pos, left.length), middle,
          slice(right, 0, inRight));
    }

    // the middle tree must be split
    final long off = pos < left.length ? 0 : pos - left.length;
    final TreeSlice<E, E> slice = middle.slice(off, len - inLeft - inRight);
    if(slice.isPartial()) {
      final E mid = ((PartialLeaf<E>) slice.getPartial()).elem;
      @SuppressWarnings("unchecked")
      final E[] single = (E[]) new Object[] { mid };
      final Empty<E, E> empty = Empty.getInstance();
      return inLeft > 0 ? new DeepObjectArray<>(slice(left, (int) pos, left.length), empty, single)
                        : new DeepObjectArray<>(single, empty, slice(right, 0, inRight));
    }

    final FingerTree<E, E> newMid = slice.getTree();
    if(inLeft != 0) {
      final int start = (int) pos;
      final E[] newLeft = slice(left, start, left.length);
      if(inRight != 0) {
        // reuse right digit
        return new DeepObjectArray<>(newLeft, newMid, slice(right, 0, inRight));
      }

      // middle tree cannot be empty
      final LeafNode<E> last = (LeafNode<E>) newMid.last();
      return new DeepObjectArray<>(newLeft, newMid.init(), last.values());
    }

    final E[] head = ((LeafNode<E>) newMid.head()).values();
    final FingerTree<E, E> tail = newMid.tail();
    if(inRight != 0) return new DeepObjectArray<>(head, tail, slice(right, 0, inRight));
    if(!tail.isEmpty()) {
      final LeafNode<E> last = (LeafNode<E>) tail.last();
      return new DeepObjectArray<>(head, tail.init(), last.values());
    }
    final int mid = head.length / 2;
    return new DeepObjectArray<>(slice(head, 0, mid), tail, slice(head, mid, head.length));
  }

  @Override
  public Iterator<E> iterator() {
    final E[] l = left, r = right;
    final FingerTree<E, E> mid = middle;
    return new Iterator<E>() {
      E[] lft = l;
      Iterator<E> sub;
      E[] rght;
      int pos;

      @Override
      public boolean hasNext() {
        return lft != null || sub != null || rght != null;
      }

      @Override
      public E next() {
        final E out;
        if(lft != null) {
          out = lft[pos++];
          if(pos == lft.length) {
            lft = null;
            sub = mid.iterator();
            if(!sub.hasNext()) {
              sub = null;
              rght = r;
              pos = 0;
            }
          }
        } else if(sub != null) {
          out = sub.next();
          if(!sub.hasNext()) {
            sub = null;
            rght = r;
            pos = 0;
          }
        } else if(rght != null) {
          out = rght[pos++];
          if(pos == rght.length) {
            rght = null;
          }
        } else {
          throw new NoSuchElementException();
        }

        return out;
      }

      @Override
      public void remove() {
        throw new UnsupportedOperationException();
      }
    };
  }

  @Override
  void checkInvariants() {
    middle.checkInvariants();
  }

  @Override
  long[] sizes() {
    final long[] sizes = middle.sizes(1);
    sizes[0] = left.length + right.length;
    return sizes;
  }

  /**
   * Returns an array containing the values at the indices {@code from} to {@code to - 1} in
   * the given array. Its length is always {@code to - from}. If {@code from} is smaller than zero,
   * the first {@code -from} entries in the resulting array are {@code null}.
   * If {@code to > arr.length} then the last {@code to - arr.length} entries are {@code null}.
   * If {@code from == 0 && to == arr.length}, the original array is returned.
   * @param <E> element type of the array
   * @param arr input array
   * @param from first index, inclusive (may be negative)
   * @param to last index, exclusive (may be greater than {@code arr.length})
   * @return resulting array
   */
  private static <E> E[] slice(final E[] arr, final int from, final int to) {
    if(from == 0 && to == arr.length) return arr;
    @SuppressWarnings("unchecked")
    final E[] out = (E[]) new Object[to - from];
    final int in0 = Math.max(0, from), in1 = Math.min(to, arr.length);
    final int out0 = Math.max(-from, 0);
    System.arraycopy(arr, in0, out, out0, in1 - in0);
    return out;
  }
}
