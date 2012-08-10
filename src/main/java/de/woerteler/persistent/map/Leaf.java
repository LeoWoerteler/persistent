package de.woerteler.persistent.map;

/**
 * A single binding of a {@link ImmutableMap}.
 *
 * @author Leo Woerteler
 */
final class Leaf extends TrieNode {
  /** Hash code of the key, stored for performance. */
  final int hash;
  /** Key of this binding. */
  final Object key;
  /** Value of this binding. */
  final Object value;

  /**
   * Constructor.
   * @param h hash code of the key
   * @param k key
   * @param v value
   */
  Leaf(final int h, final Object k, final Object v) {
    super(1);
    hash = h;
    key = k;
    value = v;
    assert verify();
  }

  @Override
  TrieNode insert(final int h, final Object k, final Object v, final int l) {
    // same hash, replace or merge
    if(h == hash) return ((key == null && k == null) || (k != null && k.equals(key))) ?
        new Leaf(h, k, v) : new List(hash, key, value, k, v);

        // different hash, branch
        final TrieNode[] ch = new TrieNode[KIDS];
        final int a = key(h, l), b = key(hash, l);
        final int used;
        if(a != b) {
          ch[a] = new Leaf(h, k, v);
          ch[b] = this;
          used = 1 << a | 1 << b;
        } else {
          ch[a] = insert(h, k, v, l + 1);
          used = 1 << a;
        }
        return new Branch(ch, used, 2);
  }

  @Override
  TrieNode delete(final int h, final Object k, final int l) {
    return h == hash && ((key == null && k == null) || (key != null && key.equals(k))) ? null
        : this;
  }

  @Override
  Object get(final int h, final Object k, final int l) {
    return h == hash && ((key == null && k == null) || (key != null && key.equals(k))) ? value
        : null;
  }

  @Override
  TrieNode getAt(final Pos pos) {
    return null;
  }

  @Override
  Object getKey(final int pos) {
    assert pos == 0;
    return key;
  }

  @Override
  Object getValue(final int pos) {
    assert pos == 0;
    return value;
  }

  @Override
  boolean contains(final int h, final Object k, final int l) {
    return h == hash && ((key == null && k == null) || (key != null && key.equals(k)));
  }

  @Override
  StringBuilder toString(final StringBuilder sb, final String ind) {
    return sb.append(ind).append("`-- ").append(key).append(
        " => ").append(value).append('\n');
  }

  @Override
  TrieNode addAll(final TrieNode o, final int l) {
    return o.add(this, l);
  }

  @Override
  TrieNode add(final Leaf o, final int l) {
    if(hash == o.hash) return key.equals(o.key) ?
        this : new List(hash, key, value, o.key, o.value);

    final TrieNode[] ch = new TrieNode[KIDS];
    final int k = key(hash, l), ok = key(o.hash, l);
    final int nu;

    // same key? add recursively
    if(k == ok) {
      ch[k] = add(o, l + 1);
      nu = 1 << k;
    } else {
      ch[k] = this;
      ch[ok] = o;
      nu = 1 << k | 1 << ok;
    }

    return new Branch(ch, nu, 2);
  }

  @Override
  TrieNode add(final List o, final int l) {

    // same hash? insert binding
    if(hash == o.hash) {
      for(int i = 0; i < o.size; i++) {
        if(key.equals(o.keys[i])) {
          final Object[] ks = o.keys.clone();
          final Object[] vs = o.values.clone();
          ks[i] = key;
          vs[i] = value;
          return new List(hash, ks, vs);
        }
      }
      return new List(hash, append(o.keys, key), append(o.values, value));
    }

    final TrieNode[] ch = new TrieNode[KIDS];
    final int k = key(hash, l), ok = key(o.hash, l);
    final int nu;

    // same key? add recursively
    if(k == ok) {
      ch[k] = add(o, l + 1);
      nu = 1 << k;
    } else {
      ch[k] = this;
      ch[ok] = o;
      nu = 1 << k | 1 << ok;
    }

    return new Branch(ch, nu, o.size + 1);
  }

  @Override
  TrieNode add(final Branch o, final int l) {
    final int k = key(hash, l);
    final TrieNode[] ch = o.copyKids();
    final TrieNode old = ch[k];
    ch[k] = old == null ? this : old.addAll(this, l + 1);
    return new Branch(ch, o.used | 1 << k,
        o.size + ch[k].size - (old != null ? old.size : 0));
  }

  @Override
  boolean verify() {
    return key.hashCode() == hash;
  }

  @Override
  StringBuilder toString(final StringBuilder sb) {
    return sb.append(key).append(":=").append(value).append(", ");
  }

  @Override
  public boolean equals(final Object obj) {
    if(obj instanceof Leaf) {
      final Leaf other = (Leaf) obj;
      return hash == other.hash
          && (((key == null && other.key == null) || (key != null && key.equals(other.key)))
          && (value == null ? other.value == null : value.equals(other.value)));
    }
    return false;
  }

  @Override
  public int hashCode() {
    return value == null ? hash : 31 * value.hashCode() + hash;
  }
}