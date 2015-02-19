package de.woerteler.persistent.array.integer;

import java.util.*;

import de.woerteler.persistent.array.*;
import de.woerteler.persistent.fingertree.*;
import de.woerteler.persistent.fingertree.node.*;

/**
 * An array containing at least two elements.
 *
 * @author Leo Woerteler
 */
final class DeepIntArray extends IntArray {
  /** Left digit. */
  private final int[] left;
  /** Middle tree. */
  private final FingerTree<Integer, Integer> middle;
  /** Right digit. */
  private final int[] right;

  /**
   * Constructor.
   * @param left left digit
   * @param middle middle tree
   * @param right right digit
   */
  DeepIntArray(final int[] left, final FingerTree<Integer, Integer> middle, final int[] right) {
    this.left = left;
    this.middle = middle;
    this.right = right;
    assert left.length >= MIN_DIGIT && left.length <= MAX_DIGIT
        && right.length >= MIN_DIGIT && right.length <= MAX_DIGIT;
  }

  /**
   * Constructor for arrays with an empty middle tree.
   * @param left left digit
   * @param right right digit
   */
  DeepIntArray(final int[] left, final int[] right) {
    this.left = left;
    this.middle = Empty.getInstance();
    this.right = right;
    assert left.length >= MIN_DIGIT && left.length <= MAX_DIGIT
        && right.length >= MIN_DIGIT && right.length <= MAX_DIGIT;
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
  public Integer head() {
    return left[0];
  }

  @Override
  public Integer last() {
    return right[right.length - 1];
  }

  @Override
  public IntArray cons(final Integer elem) {
    if(left.length < MAX_DIGIT) {
      final int[] newLeft = slice(left, -1, left.length);
      newLeft[0] = elem;
      return new DeepIntArray(newLeft, middle, right);
    }

    final int mid = MAX_DIGIT / 2;
    final int[] newLeft = slice(left, -1, mid);
    newLeft[0] = elem;
    final Node<Integer, Integer> sub = new IntLeaf(slice(left, mid, left.length));
    return new DeepIntArray(newLeft, middle.cons(sub), right);
  }

  @Override
  public IntArray snoc(final Integer elem) {
    if(right.length < MAX_DIGIT) {
      final int[] newRight = slice(right, 0, right.length + 1);
      newRight[right.length] = elem;
      return new DeepIntArray(left, middle, newRight);
    }

    final int mid = (MAX_DIGIT + 1) / 2;
    final int[] newRight = slice(right, mid, right.length + 1);
    newRight[right.length - mid] = elem;
    final Node<Integer, Integer> sub = new IntLeaf(slice(right, 0, mid));
    return new DeepIntArray(left, middle.snoc(sub), newRight);
  }

  @Override
  public IntArray init() {
    if(right.length > MIN_DIGIT) {
      // right digit is safe, just shrink it
      return new DeepIntArray(left, middle, slice(right, 0, right.length - 1));
    }

    if(middle.isEmpty()) {
      // middle tree empty, make a tree from the left digit
      final int l = left.length, r = right.length, n = l + r - 1;
      if(n <= MAX_SMALL) {
        final int[] out = new int[n];
        System.arraycopy(left, 0, out, 0, l);
        System.arraycopy(right, 0, out, l, r - 1);
        return new SmallIntArray(out);
      }

      // balance left and right digit
      final int ll = n / 2, rl = n - ll, move = l - ll;
      final int [] newLeft = new int[ll], newRight = new int[rl];
      System.arraycopy(left, 0, newLeft, 0, ll);
      System.arraycopy(left, ll, newRight, 0, move);
      System.arraycopy(right, 0, newRight, move, r - 1);
      return new DeepIntArray(newLeft, newRight);
    }

    // merge right digit with last node
    final int[] ls = ((IntLeaf) middle.last()).values, rs = right;
    final int ll = ls.length, rl = rs.length, n = ll + rl - 1;
    final int[] newRight = new int[n];
    System.arraycopy(ls, 0, newRight, 0, ll);
    System.arraycopy(rs, 0, newRight, ll, rl - 1);
    return new DeepIntArray(left, middle.init(), newRight);
  }

  @Override
  public IntArray tail() {
    if(left.length > MIN_DIGIT) {
      // left digit is safe, just shrink it
      return new DeepIntArray(slice(left, 1, left.length), middle, right);
    }

    if(middle.isEmpty()) {
      // middle tree empty, make a tree from the right list
      final int l = left.length, r = right.length, n = l - 1 + r;
      if(n <= MAX_SMALL) {
        final int[] out = new int[n];
        System.arraycopy(left, 1, out, 0, l - 1);
        System.arraycopy(right, 0, out, l - 1, r);
        return new SmallIntArray(out);
      }

      // balance left and right digit
      final int ll = n / 2, rl = n - ll;
      final int [] newLeft = new int[ll], newRight = new int[rl];
      System.arraycopy(left, 1, newLeft, 0, l - 1);
      System.arraycopy(right, 0, newLeft, l - 1, r - rl);
      System.arraycopy(right, r - rl, newRight, 0, rl);
      return new DeepIntArray(newLeft, newRight);
    }

    // merge left digit with first node
    final int[] ls = left, rs = ((IntLeaf) middle.head()).values;
    final int ll = ls.length, rl = rs.length, n = ll - 1 + rl;
    final int[] newLeft = new int[n];
    System.arraycopy(ls, 1, newLeft, 0, ll - 1);
    System.arraycopy(rs, 0, newLeft, ll - 1, rl);
    return new DeepIntArray(newLeft, middle.tail(), right);
  }

  @Override
  public IntArray concat(final Array<Integer> seq) {
    // empty array
    if(seq.isEmpty()) return this;

    // other sorts of arrays
    if(!(seq instanceof IntArray)) return concat(from(seq));

    if(seq instanceof SmallIntArray) {
      // merge with right digit
      final int[] newRight = concat(right, ((SmallIntArray) seq).elems);
      final int r = newRight.length;
      if(r <= MAX_DIGIT) return new DeepIntArray(left, middle, newRight);
      final int mid = r / 2;
      final int[] leaf = slice(newRight, 0, mid);
      final FingerTree<Integer, Integer> newMid = middle.snoc(new IntLeaf(leaf));
      return new DeepIntArray(left, newMid, slice(newRight, mid, r));
    }

    final DeepIntArray other = (DeepIntArray) seq;

    // make nodes out of the digits facing each other
    final int[] ls = right, rs = other.left;
    final int l = ls.length, n = l + rs.length;
    final int k = (n + MAX_LEAF - 1) / MAX_LEAF, s = (n + k - 1) / k;
    @SuppressWarnings("unchecked")
    final Node<Integer, Integer>[] midNodes = new Node[k];
    int p = 0;
    for(int i = 0; i < k; i++) {
      final int curr = Math.min(n - p, s);
      final int[] arr = new int[curr];
      for(int j = 0; j < curr; j++, p++) arr[j] = p < l ? ls[p] : rs[p - l];
      midNodes[i] = new IntLeaf(arr);
    }

    return new DeepIntArray(left, middle.concat(midNodes, other.middle), other.right);
  }

  @Override
  public Integer get(final long index) {
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
  public IntArray reverse() {
    final int l = left.length, r = right.length;
    final int[] newLeft = new int[r], newRight = new int[l];
    for(int i = 0; i < r; i++) newLeft[i] = right[r - 1 - i];
    for(int i = 0; i < l; i++) newRight[i] = left[l - 1 - i];
    return new DeepIntArray(newLeft, middle.reverse(), newRight);
  }

  @Override
  public IntArray insertBefore(final long pos, final Integer val) {
    if(pos < 0) throw new IndexOutOfBoundsException("negative index: " + pos);
    if(pos > size()) throw new IndexOutOfBoundsException("position too big: " + pos);

    final int l = left.length;
    if(pos <= l) {
      final int p = (int) pos;
      final int[] temp = slice(left, 0, l + 1);
      System.arraycopy(temp, p, temp, p + 1, l - p);
      temp[p] = val;
      if(l < MAX_DIGIT) return new DeepIntArray(temp, middle, right);

      final int m = (l + 1) / 2;
      return new DeepIntArray(slice(temp, 0, m),
          middle.cons(new IntLeaf(slice(temp, m, l + 1))), right);
    }

    final long midSize = middle.size();
    if(pos - l < midSize) return new DeepIntArray(left, middle.insert(pos - l, val), right);

    final int r = right.length;
    final int p = (int) (pos - l - midSize);
    final int[] temp = slice(right, 0, r + 1);
    System.arraycopy(temp, p, temp, p + 1, r - p);
    temp[p] = val;
    if(r < MAX_DIGIT) return new DeepIntArray(left, middle, temp);

    final int m = (r + 1) / 2;
    return new DeepIntArray(left, middle.snoc(new IntLeaf(slice(temp, 0, m))),
        slice(temp, m, r + 1));
  }

  @Override
  public IntArray remove(final long pos) {
    if(pos < 0) throw new IndexOutOfBoundsException("negative index: " + pos);
    if(pos >= size()) throw new IndexOutOfBoundsException("position too big: " + pos);

    if(pos < left.length) {
      // delete from left digit
      final int p = (int) pos, l = left.length;
      if(l > MIN_DIGIT) {
        // there is enough space, just delete the element
        final int[] newLeft = new int[l - 1];
        System.arraycopy(left, 0, newLeft, 0, p);
        System.arraycopy(left, p + 1, newLeft, p, newLeft.length - p);
        return new DeepIntArray(newLeft, middle, right);
      }

      if(middle.isEmpty()) {
        // merge left and right digit
        final int r = right.length, n = l - 1 + r;
        final int[] vals = new int[n];
        System.arraycopy(left, 0, vals, 0, p);
        System.arraycopy(left, p + 1, vals, p, l - 1 - p);
        System.arraycopy(right, 0, vals, l - 1, r);
        return fromMerged(vals);
      }

      // extract a new left digit from the middle
      final int[] head = ((IntLeaf) middle.head()).values;
      final int r = head.length, n = l - 1 + r;

      if(r > MIN_LEAF) {
        // refill from neighbor
        final int move = (r - MIN_LEAF + 1) / 2;
        final int[] newLeft = new int[l - 1 + move];
        System.arraycopy(left, 0, newLeft, 0, p);
        System.arraycopy(left, p + 1, newLeft, p, l - 1 - p);
        System.arraycopy(head, 0, newLeft, l - 1, move);
        final int[] newHead = slice(head, move, r);
        return new DeepIntArray(newLeft, middle.replaceHead(new IntLeaf(newHead)), right);
      }

      // merge digit and head node
      final int[] newLeft = new int[n];
      System.arraycopy(left, 0, newLeft, 0, p);
      System.arraycopy(left, p + 1, newLeft, p, l - 1 - p);
      System.arraycopy(head, 0, newLeft, l - 1, r);
      return new DeepIntArray(newLeft, middle.tail(), right);
    }

    final long midSize = middle.size(), rightOffset = left.length + midSize;
    if(pos >= rightOffset) {
      // delete from right digit
      final int p = (int) (pos - rightOffset), r = right.length;
      if(r > MIN_DIGIT) {
        // there is enough space, just delete the element
        final int[] newRight = new int[r - 1];
        System.arraycopy(right, 0, newRight, 0, p);
        System.arraycopy(right, p + 1, newRight, p, r - 1 - p);
        return new DeepIntArray(left, middle, newRight);
      }

      if(middle.isEmpty()) {
        // merge left and right digit
        final int l = left.length, n = l + r - 1;
        final int[] vals = new int[n];
        System.arraycopy(left, 0, vals, 0, l);
        System.arraycopy(right, 0, vals, l, p);
        System.arraycopy(right, p + 1, vals, l + p, r - 1 - p);
        return fromMerged(vals);
      }

      // extract a new right digit from the middle
      final int[] last = ((IntLeaf) middle.last()).values;
      final int l = last.length, n = l + r - 1;

      if(l > MIN_LEAF) {
        // refill from neighbor
        final int move = (l - MIN_LEAF + 1) / 2;
        final int[] newLast = slice(last, 0, l - move);
        final int[] newRight = new int[r - 1 + move];
        System.arraycopy(last, l - move, newRight, 0, move);
        System.arraycopy(right, 0, newRight, move, p);
        System.arraycopy(right, p + 1, newRight, move + p, r - 1 - p);
        return new DeepIntArray(left, middle.replaceLast(new IntLeaf(newLast)), newRight);
      }

      // merge last node and digit
      final int[] newRight = new int[n];
      System.arraycopy(last, 0, newRight, 0, l);
      System.arraycopy(right, 0, newRight, l, p);
      System.arraycopy(right, p + 1, newRight, l + p, r - 1 - p);
      return new DeepIntArray(left, middle.init(), newRight);
    }

    // delete in middle tree
    final long off = pos - left.length;

    if(middle instanceof Deep) {
      // middle tree cannot underflow
      return new DeepIntArray(left, ((Deep<Integer, Integer>) middle).remove(off), right);
    }

    // tree height might change
    final int[] mid = ((IntLeaf) middle.head()).values;
    final int p = (int) off, m = mid.length;
    if(m > IntArray.MIN_LEAF) {
      // no underflow
      final int[] newMid = new int[m - 1];
      System.arraycopy(mid, 0, newMid, 0, p);
      System.arraycopy(mid, p + 1, newMid, p, m - 1 - p);
      return new DeepIntArray(left, new Single<>(new IntLeaf(newMid)), right);
    }

    // node will underflow, balance with digits
    final int l = left.length, r = right.length;

    if(l > r) {
      // steal from the bigger digit, in this case left (cannot be minimal)
      final int move = (l - MIN_DIGIT + 1) / 2;
      final int[] newLeft = slice(left, 0, l - move);
      final int[] newMid = slice(left, l - move, l + m - 1);
      System.arraycopy(mid, 0, newMid, move, p);
      System.arraycopy(mid, p + 1, newMid, move + p, m - 1 - p);
      return new DeepIntArray(newLeft, new Single<>(new IntLeaf(newMid)), right);
    }

    if(r > MIN_DIGIT) {
      // steal from right digit
      final int move = (r - MIN_DIGIT + 1) / 2;
      final int[] newMid = new int[m - 1 + move];
      System.arraycopy(mid, 0, newMid, 0, p);
      System.arraycopy(mid, p + 1, newMid, p, m - 1 - p);
      System.arraycopy(right, 0, newMid, m - 1, move);
      final int[] newRight = slice(right, move, r);
      return new DeepIntArray(left, new Single<>(new IntLeaf(newMid)), newRight);
    }

    // divide onto left and right digit
    final int ml = (m - 1) / 2, mr = (m - 1) - ml;
    final int[] newLeft = slice(left, 0, l + ml);
    final int[] newRight = slice(right, -mr, r);
    if(p < ml) {
      System.arraycopy(mid, 0, newLeft, l, p);
      System.arraycopy(mid, p + 1, newLeft, l + p, ml - p);
      System.arraycopy(mid, ml + 1, newRight, 0, mr);
    } else {
      System.arraycopy(mid, 0, newLeft, l, ml);
      System.arraycopy(mid, ml, newRight, 0, p - ml);
      System.arraycopy(mid, p + 1, newRight, p - ml, m - 1 - p);
    }
    return new DeepIntArray(newLeft, newRight);
  }

  @Override
  public IntArray subArray(final long pos, final long len) {
    if(pos < 0) throw new IndexOutOfBoundsException("first index < 0: " + pos);
    if(len < 0) throw new IndexOutOfBoundsException("length < 0: " + len);
    final long midSize = middle.size(), size = left.length + midSize + right.length;
    if(len > size - pos)
      throw new IndexOutOfBoundsException("end out of bounds: " + (pos + len) + " > " + size);

    // the easy cases
    if(len == 0) return IntArray.empty();
    if(len == size) return this;

    final long end = pos + len;
    if(end <= left.length) {
      // completely in left digit
      final int p = (int) pos, n = (int) len;
      if(len <= MAX_SMALL) return new SmallIntArray(slice(left, p, p + n));
      final int mid = p + n / 2;
      return new DeepIntArray(slice(left, p, mid), slice(left, mid, p + n));
    }

    final long rightOffset = left.length + midSize;
    if(pos >= rightOffset) {
      // completely in right digit
      final int p = (int) (pos - rightOffset), n = (int) len;
      if(len <= MAX_SMALL) return new SmallIntArray(slice(right, p, p + n));
      final int mid = p + n / 2;
      return new DeepIntArray(slice(right, p, mid), slice(right, mid, p + n));
    }

    final int inLeft = pos < left.length ? (int) (left.length - pos) : 0,
        inRight = end > rightOffset ? (int) (end - rightOffset) : 0;
    if(inLeft >= MIN_DIGIT && inRight >= MIN_DIGIT) {
      // digits are still long enough
      final int[] newLeft = inLeft == left.length ? left : slice(left, (int) pos, left.length);
      final int[] newRight = inRight == right.length ? right : slice(right, 0, inRight);
      return new DeepIntArray(newLeft, middle, newRight);
    }

    if(middle.isEmpty()) {
      // merge left and right partial digits
      final int[] out;
      if(inLeft == 0) {
        out = inRight == right.length ? right : slice(right, 0, inRight);
      } else if(inRight == 0) {
        out = inLeft == left.length ? left : slice(left, left.length - inLeft, left.length);
      } else {
        out = slice(left, left.length - inLeft, left.length + inRight);
        System.arraycopy(right, 0, out, inLeft, inRight);
      }
      return fromMerged(out);
    }

    final long inMiddle = len - inLeft - inRight;
    final FingerTree<Integer, Integer> mid;
    if(inMiddle == midSize) {
      mid = middle;
    } else {
      // the middle tree must be split
      final long off = pos < left.length ? 0 : pos - left.length;
      final TreeSlice<Integer, Integer> slice = middle.slice(off, inMiddle);
      // only a partial leaf, merge with digits
      if(slice.isPartial()) {
        final int[] single = ((PartialIntLeaf) slice.getPartial()).elems;
        if(inLeft > 0) {
          final int[] out = slice(left, (int) pos, left.length + single.length);
          System.arraycopy(single, 0, out, inLeft, single.length);
          return fromMerged(out);
        }
        if(inRight > 0) {
          final int[] out = slice(single, 0, single.length + inRight);
          System.arraycopy(right, 0, out, single.length, inRight);
          return fromMerged(out);
        }
        return new SmallIntArray(single);
      }

      mid = slice.getTree();
    }

    // `mid` is non-empty

    // create a left digit
    final int off = left.length - inLeft;
    final int[] newLeft;
    final FingerTree<Integer, Integer> mid1;
    if(inLeft >= MIN_DIGIT) {
      newLeft = inLeft == left.length ? left : slice(left, off, left.length);
      mid1 = mid;
    } else {
      final int[] head = ((IntLeaf) mid.head()).values;
      if(inLeft == 0) {
        newLeft = head;
      } else {
        newLeft = slice(head, -inLeft, head.length);
        System.arraycopy(left, off, newLeft, 0, inLeft);
      }
      mid1 = mid.tail();
    }

    // create a right digit
    final int[] newRight;
    final FingerTree<Integer, Integer> newMiddle;
    if(inRight >= MIN_DIGIT) {
      newMiddle = mid1;
      newRight = inRight == right.length ? right : slice(right, 0, inRight);
    } else if(!mid1.isEmpty()) {
      final int[] last = ((IntLeaf) mid1.last()).values;
      newMiddle = mid1.init();
      if(inRight == 0) {
        newRight = last;
      } else {
        newRight = slice(last, 0, last.length + inRight);
        System.arraycopy(right, 0, newRight, last.length, inRight);
      }
    } else {
      // not enough elements for a right digit
      if(inRight == 0) return fromMerged(newLeft);
      final int n = newLeft.length + inRight;
      final int[] out = slice(newLeft, 0, n);
      System.arraycopy(right, 0, out, newLeft.length, inRight);
      return fromMerged(out);
    }

    return new DeepIntArray(newLeft, newMiddle, newRight);
  }

  /**
   * Creates an int array from two merged, possibly partial digits.
   * This method requires that the input array's length is not longer than {@code 2 * MAX_DIGIT}.
   * @param merged the merged digits
   * @return the array
   */
  private IntArray fromMerged(final int[] merged) {
    if(merged.length <= MAX_SMALL) return new SmallIntArray(merged);
    final int mid = merged.length / 2;
    return new DeepIntArray(slice(merged, 0, mid), slice(merged, mid, merged.length));
  }

  @Override
  public Iterator<Integer> iterator() {
    final int[] l = left, r = right;
    final FingerTree<Integer, Integer> mid = middle;
    return new Iterator<Integer>() {
      int[] lft = l;
      Iterator<Integer> sub;
      int[] rght;
      int pos;

      @Override
      public boolean hasNext() {
        return lft != null || sub != null || rght != null;
      }

      @Override
      public Integer next() {
        final Integer out;
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
    final int l = left.length, r = right.length;
    if(l < MIN_DIGIT || l > MAX_DIGIT) throw new AssertionError("Left digit: " + l);
    if(r < MIN_DIGIT || r > MAX_DIGIT) throw new AssertionError("Right digit: " + r);
    middle.checkInvariants();
  }

  @Override
  long[] sizes() {
    final long[] sizes = middle.sizes(1);
    sizes[0] = left.length + right.length;
    return sizes;
  }

  @Override
  IntArray consSmall(final int[] vals) {
    final int a = vals.length, b = left.length, n = a + b;
    if(n <= MAX_DIGIT) {
      // no need to change the middle tree
      return new DeepIntArray(concat(vals, left), middle, right);
    }

    if(a >= MIN_DIGIT && MIN_LEAF <= b && b <= MAX_LEAF) {
      // reuse the arrays
      return new DeepIntArray(vals, middle.cons(new IntLeaf(left)), right);
    }

    // left digit is too big
    final int mid = n / 2, move = mid - a;
    final int[] newLeft = slice(vals, 0, mid);
    System.arraycopy(left, 0, newLeft, a, move);
    final IntLeaf leaf = new IntLeaf(slice(left, move, b));
    return new DeepIntArray(newLeft, middle.cons(leaf), right);
  }
}
