package de.woerteler.persistent.map;

/**
 * Inner node of a {@link ImmutableMap}.
 *
 * @author Leo Woerteler
 */
final class Branch extends TrieNode {
  /** Child array. */
  private final TrieNode[] kids;
  /** Bit array with a bit set for every used slot. */
  final int used;

  /**
   * Constructor taking children array and the size of this map.
   * @param ch children
   * @param u bit array
   * @param s size of this node
   */
  Branch(final TrieNode[] ch, final int u, final int s) {
    super(s);
    kids = ch;
    used = u;
    assert verify();
  }

  /**
   * Copies the children array.
   * This is faster than {@code kids.clone()} according to
   * <a href="http://www.javaspecialists.eu/archive/Issue124.html">Heinz M. Kabutz</a>.
   * @return copy of the child array
   */
  TrieNode[] copyKids() {
    final TrieNode[] copy = new TrieNode[KIDS];
    System.arraycopy(kids, 0, copy, 0, KIDS);
    return copy;
  }

  @Override
  TrieNode insert(final int h, final Object k, final Object v, final int l) {
    final int key = key(h, l);
    final TrieNode sub = kids[key], nsub;
    final int bs, rem;
    if(sub != null) {
      nsub = sub.insert(h, k, v, l + 1);
      bs = used;
      rem = sub.size;
    } else {
      nsub = new Leaf(h, k, v);
      bs = used | 1 << key;
      rem = 0;
    }
    final TrieNode[] ks = copyKids();
    ks[key] = nsub;
    return new Branch(ks, bs, size - rem + nsub.size);
  }

  @Override
  TrieNode delete(final int h, final Object k, final int l) {
    final int key = key(h, l);
    final TrieNode sub = kids[key];
    if(sub == null) return this;
    final TrieNode nsub = sub.delete(h, k, l + 1);
    if(nsub == sub) return this;

    final int nu;
    if(nsub == null) {
      nu = used ^ 1 << key;
      if(Integer.bitCount(nu) == 1) {
        final TrieNode single = kids[Integer.numberOfTrailingZeros(nu)];
        // check whether the child depends on the right offset
        if(!(single instanceof Branch)) return single;
      }
    } else {
      nu = used;
    }

    final TrieNode[] ks = copyKids();
    ks[key] = nsub;
    return new Branch(ks, nu, size - 1);
  }

  @Override
  Object get(final int h, final Object k, final int l) {
    final int key = key(h, l);
    final TrieNode sub = kids[key];
    return sub == null ? null : sub.get(h, k, l + 1);
  }

  @Override
  TrieNode getAt(final Pos pos) {
    final int s = Integer.bitCount(used);
    for(int i = 0, j = 0; i < s; i++, j++) {
      while((used & 1 << j) == 0) {
        j++;
      }
      final TrieNode sub = kids[j];
      if(pos.pos < sub.size) return sub;
      pos.pos -= sub.size;
    }
    throw new InternalError();
  }

  @Override
  Object getKey(final int pos) {
    throw new InternalError();
  }

  @Override
  Object getValue(final int pos) {
    throw new InternalError();
  }

  @Override
  boolean contains(final int h, final Object k, final int l) {
    final TrieNode sub = kids[key(h, l)];
    return sub != null && sub.contains(h, k, l + 1);
  }

  /** End strings. */
  private static final String[] ENDS = { "|-- ", "|   ", "`-- ", "    " };

  @Override
  StringBuilder toString(final StringBuilder sb, final String ind) {
    final int s = Integer.bitCount(used);
    for(int i = 0, j = 0; i < s; i++, j++) {
      while((used & 1 << j) == 0) {
        j++;
      }
      final int e = i == s - 1 ? 2 : 0;
      sb.append(ind).append(ENDS[e]).append(
          String.format("%x", j)).append('\n');
      kids[j].toString(sb, ind + ENDS[e + 1]);
    }
    return sb;
  }

  @Override
  TrieNode addAll(final TrieNode o, final int l) {
    return o.add(this, l);
  }

  @Override
  TrieNode add(final Leaf o, final int l) {
    final int k = key(o.hash, l);
    final TrieNode ch = kids[k], nw;
    if(ch != null) {
      final TrieNode ins = ch.add(o, l + 1);
      if(ins == ch) return this;
      nw = ins;
    } else {
      nw = o;
    }

    final TrieNode[] ks = copyKids();
    ks[k] = nw;

    // we don't replace here, so the size must increase
    return new Branch(ks, used | 1 << k, size + 1);
  }

  @Override
  TrieNode add(final List o, final int l) {
    final int k = key(o.hash, l);
    final TrieNode ch = kids[k], nw;
    int n = o.size;
    if(ch != null) {
      final TrieNode ins = ch.add(o, l + 1);
      if(ins == ch) return this;
      n = ins.size - ch.size;
      nw = ins;
    } else {
      nw = o;
    }

    final TrieNode[] ks = copyKids();
    ks[k] = nw;

    // we don't replace here, so the size must increase
    return new Branch(ks, used | 1 << k, size + n);
  }

  @Override
  TrieNode add(final Branch o, final int l) {
    TrieNode[] ch = null;
    int nu = used, ns = size;
    for(int i = 0; i < kids.length; i++) {
      final TrieNode k = kids[i], ok = o.kids[i];
      if(ok != null) {
        final TrieNode nw = k == null ? ok : ok.addAll(k, l + 1);
        if(nw != k) {
          if(ch == null) {
            ch = copyKids();
          }
          ch[i] = nw;
          nu |= 1 << i;
          ns += nw.size - (k == null ? 0 : k.size);
        }
      }
    }
    return ch == null ? this : new Branch(ch, nu, ns);
  }

  @Override
  boolean verify() {
    int c = 0;
    for(int i = 0; i < KIDS; i++) {
      final boolean bit = (used & 1 << i) != 0, act = kids[i] != null;
      if(bit ^ act) return false;
      if(act) {
        c += kids[i].size;
      }
    }
    return c == size;
  }

  @Override
  StringBuilder toString(final StringBuilder sb) {
    for(int i = 0; i < KIDS; i++) if(kids[i] != null) {
      kids[i].toString(sb);
    }
    return sb;
  }

  @Override
  public boolean equals(final Object obj) {
    if(!(obj instanceof Branch)) return false;
    final Branch other = (Branch) obj;
    if(used != other.used) return false;
    for(int i = 0; i < kids.length; i++)
      if(kids[i] != null && !kids[i].equals(other.kids[i])) return false;
    return true;
  }

  @Override
  public int hashCode() {
    int h = 1;
    for(int i = 0; i < kids.length; i++) {
      h = 31 * h + (kids[i] == null ? 0 : kids[i].hashCode());
    }
    return h;
  }

}
