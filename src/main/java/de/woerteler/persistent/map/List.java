package de.woerteler.persistent.map;

import java.util.*;

/**
 * Leaf that contains a collision list of keys with the same hash code.
 *
 * @author Leo Woerteler
 */
final class List extends TrieNode {
  /** Common hash value of all contained values. */
  final int hash;

  /** List of keys of this collision list. */
  final Object[] keys;
  /** List of values of this collision list. */
  final Object[] values;

  /**
   * Constructor.
   *
   * @param h hash value
   * @param ks key array
   * @param vs value array
   */
  List(final int h, final Object[] ks, final Object[] vs) {
    super(ks.length);
    keys = ks;
    values = vs;
    hash = h;
    assert verify();
  }

  /**
   * Constructor for creating a collision list from two bindings.
   * @param h hash value
   * @param k1 first key
   * @param v1 first value
   * @param k2 second key
   * @param v2 second value
   */
  List(final int h, final Object k1, final Object v1, final Object k2, final Object v2) {
    this(h, new Object[]{ k1, k2 }, new Object[]{ v1, v2 });
  }

  @Override
  TrieNode delete(final int h, final Object k, final int l) {

    if(h == hash) {
      for(int i = size; i-- > 0;) {
        if(k.equals(keys[i])) {
          // found entry
          if(size == 2) {
            // single leaf remains
            final int o = i ^ 1;
            return new Leaf(h, keys[o], values[o]);
          }
          // still collisions
          return new List(h, delete(keys, i), delete(values, i));
        }
      }
    }
    return this;
  }

  @Override
  TrieNode insert(final int h, final Object k, final Object v, final int l) {
    // same hash, replace or merge
    if(h == hash) {
      for(int i = keys.length; i-- > 0;) {
        if(k.equals(keys[i])) {
          // replace value
          final Object[] vs = values.clone();
          vs[i] = v;
          return new List(h, keys.clone(), vs);
        }
      }
      return new List(hash, append(keys, k), append(values, v));
    }

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
    // we definitely inserted one value
    return new Branch(ch, used, size + 1);
  }

  @Override
  Object get(final int h, final Object k, final int l) {
    if(h == hash)
      for(int i = keys.length; --i >= 0;)
        if(k.equals(keys[i])) return values[i];
    return null;
  }

  @Override
  boolean contains(final int h, final Object k, final int u) {
    if(h == hash) for(int i = keys.length; --i >= 0;)
      if(k.equals(keys[i])) return true;
    return false;
  }

  @Override
  StringBuilder toString(final StringBuilder sb, final String ind) {
    sb.append(ind).append("`-- Collision (").append(
        Integer.toHexString(hash)).append("):\n");
    for(int i = 0; i < keys.length; i++) {
      sb.append(ind).append("      ").append(keys[i]).append(" => ").append(
          values[i]).append('\n');
    }
    return sb;
  }

  @Override
  TrieNode addAll(final TrieNode o, final int l) {
    return o.add(this, l);
  }

  @Override
  TrieNode add(final Leaf o, final int l) {
    if(hash == o.hash) {
      for(final Object k : keys) if(k.equals(o.key)) return this;
      return new List(hash, append(keys, o.key), append(values, o.value));
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

    return new Branch(ch, nu, size + 1);
  }

  @Override
  TrieNode add(final List o, final int l) {
    if(hash == o.hash) {
      Object[] ks = keys;
      Object[] vs = values;

      outer: for(int i = 0; i < size; i++) {
        final Object ok = o.keys[i];
        // skip all entries that are overridden
        for(final Object k : keys) if(k.equals(ok)) continue outer;
        // key is not in this list, add it
        ks = append(ks, ok);
        vs = append(vs, o.values[i]);
      }
      return ks == keys ? this : new List(hash, ks, vs);
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

    return new Branch(ch, nu, size + o.size);
  }

  @Override
  TrieNode add(final Branch o, final int l) {
    final int k = key(hash, l);
    final TrieNode[] ch = o.copyKids();
    final TrieNode old = ch[k];
    ch[k] = old == null ? this : old.addAll(this, l + 1);
    return new Branch(ch,
        o.used | 1 << k, o.size + ch[k].size - (old != null ? old.size : 0));
  }

  @Override
  boolean verify() {
    for(int i = 1; i < size; i++) {
      for(int j = i; j-- > 0;) {
        if(keys[i].equals(keys[j])) return false;
      }
    }
    return true;
  }

  @Override
  StringBuilder toString(final StringBuilder sb) {
    for(int i = size; --i >= 0;)
      sb.append(keys[i]).append(":=").append(values[i]).append(", ");
    return sb;
  }

  @Override
  public boolean equals(final Object obj) {
    if(!(obj instanceof List)) return false;
    final List other = (List) obj;
    if(hash != other.hash || keys.length != other.keys.length) return false;

    final BitSet find = new BitSet(keys.length);
    find.set(0, keys.length);
    for(int i = 0; i < keys.length; i++) {
      final Object key = keys[i], value = values[i];
      boolean found = false;
      for (int j = find.nextSetBit(0); !found && j >= 0; j = find.nextSetBit(j + 1)) {
        final Object okey = other.keys[j], ovalue = other.values[j];
        if(key.equals(okey) && (value == null ? ovalue == null : value.equals(ovalue))) {
          find.clear(j);
          found = true;
        }
      }
      if(!found) return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    int h = 0;
    for(int i = 0; i < keys.length; i++)
      h ^= values[i] == null ? 0 : values[i].hashCode();
    return 31 * h + hash;
  }
}