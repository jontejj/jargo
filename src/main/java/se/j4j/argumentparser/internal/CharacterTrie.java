package se.j4j.argumentparser.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.NotThreadSafe;

/**
 * Stores {@link CharSequence}s in a <a href="http://en.wikipedia.org/wiki/Trie">trie</a>
 * TODO: Move CharacterTrie into it's own project
 * 
 * @param <E> the type of values stored in the tree
 */
@NotThreadSafe
public final class CharacterTrie<E>
{
	private int size;
	private final Entry<E> root;

	/**
	 * An entry represents a node in the tree.
	 */
	private static final class Entry<E> implements Map.Entry<CharSequence, E>
	{
		/**
		 * The char the parent node will use to reference this child with
		 */
		private final Character index;

		/**
		 * If true this node represents a value
		 */
		private boolean isValue;

		/**
		 * the value of this node
		 */
		private E value;

		/**
		 * The nodes that belongs to this Node
		 */
		private Map<Character, Entry<E>> children;

		/**
		 * Used to build strings from an index in the tree
		 */
		private final Entry<E> parent;

		private Entry(final Character index, final Entry<E> parent)
		{
			this.index = index;
			this.parent = parent;
		}

		@Override
		public CharSequence getKey()
		{
			StringBuilder sb = new StringBuilder();
			Entry<E> current = this;
			while(!current.isRoot())
			{
				sb.append(current.index);
				current = current.parent;
			}
			return sb.reverse().toString();
		}

		@Override
		public E getValue()
		{
			if(!isValue)
				throw new IllegalStateException(getKey() + " has no value associated with it");
			return value;
		}

		@Override
		public E setValue(final E value)
		{
			E oldValue = this.value;

			isValue = true;
			this.value = value;

			return oldValue;
		}

		/**
		 * @return all the keys that have the same prefix as this entry,
		 *         so for the root key all keys in the tree would be returned.
		 */
		public Set<CharSequence> keys()
		{
			Set<CharSequence> result = new HashSet<CharSequence>();
			if(isValue)
			{
				result.add(this.getKey());
			}
			if(hasChildren())
			{
				for(Entry<E> child : children.values())
				{
					result.addAll(child.keys());
				}
			}
			return result;
		}

		/**
		 * @return all the values in this node (recursively)
		 */
		public List<E> values()
		{
			List<E> result = new ArrayList<E>();
			if(isValue)
			{
				result.add(this.value);
			}
			if(hasChildren())
			{
				for(Entry<E> child : children.values())
				{
					result.addAll(child.values());
				}
			}
			return result;
		}

		@Override
		public String toString()
		{
			return getKey() + " -> " + getValue();
		}

		public Map.Entry<CharSequence, E> getLastMatch(final CharSequence key)
		{
			Entry<E> child = findLastChild(key);
			if(child.isValue)
				return child;
			return null;
		}

		/**
		 * Clear this entry from being a value
		 * 
		 * @return true if this call had any effect
		 */
		private boolean unset()
		{
			boolean wasValue = isValue;
			isValue = false;
			value = null;
			return wasValue;
		}

		private boolean isRoot()
		{
			return parent == null;
		}

		private boolean hasChildren()
		{
			return children != null ? children.size() > 0 : false;
		}

		private Entry<E> getChild(final Character c)
		{
			return children != null ? children.get(c) : null;
		}

		/**
		 * @param key the key to find the child/leaf for
		 * @return the leaf in the tree that is reached for the given key,
		 *         or null if no such leaf could be found
		 */
		private Entry<E> findChild(final CharSequence key)
		{
			// Start at the root and search the tree for the entry matching the
			// given key
			Entry<E> current = this;
			for(int i = 0, len = key.length(); i < len && current != null; i++)
			{
				Character c = key.charAt(i);
				current = current.getChild(c);
			}
			return current;
		}

		/**
		 * @param key the key to find the child/leaf for
		 * @return the leaf in the tree that is reached for the given key,
		 *         or null if no such leaf could be found
		 */
		private Entry<E> findLastChild(final CharSequence key)
		{
			// Start at the root and search the tree for an entry starting with
			// key, return the last possible match so that matches with more matching chars will be
			// prioritized
			Entry<E> current = this;
			for(int i = 0, len = key.length(); i < len; i++)
			{
				Character c = key.charAt(i);
				Entry<E> next = current.getChild(c);
				if(next == null)
					return current;
				current = next;
			}
			return current;
		}

		/**
		 * @param key the key to find the child/leaf for
		 * @return the value for the leaf in the tree that is reached for the
		 *         given key,
		 *         or null if no such value could be found
		 */
		private E get(final CharSequence key)
		{
			Entry<E> child = findChild(key);
			if(child == null)
				return null;
			if(child.isValue)
				return child.value;
			return null;
		}

		/**
		 * @param c the Character index to remove
		 * @throws NullPointerException if this Entry doesn't have had any
		 *             children before
		 */
		private void deleteChild(final Character c)
		{
			children.remove(c);
		}

		/**
		 * Makes sure that a child that represents the given {@link childChar} is found in this
		 * entry.
		 * 
		 * @param childChar the character to create/get a child for
		 * @return either the already existing child or a newly created one
		 */
		private Entry<E> ensureChild(final Character childChar)
		{
			if(children == null)
			{
				children = new HashMap<Character, Entry<E>>();
				Entry<E> child = new Entry<E>(childChar, this);
				children.put(childChar, child);
				return child;
			}
			Entry<E> existing = children.get(childChar);
			if(existing != null)
				return existing;
			Entry<E> child = new Entry<E>(childChar, this);
			children.put(childChar, child);
			return child;
		}
	}

	@CheckReturnValue
	public static <E> CharacterTrie<E> newTrie()
	{
		return new CharacterTrie<E>();
	}

	private CharacterTrie()
	{
		root = createRoot();
	}

	/**
	 * @param key
	 * @return true if the given key can work as a key in a CharacterTrie
	 */
	@CheckReturnValue
	public static boolean validKey(@Nonnull final CharSequence key)
	{
		return key != null;
	}

	/**
	 * @param key the key
	 * @param value the value
	 * @return the old value associated with {@code key}, or null if no
	 *         such association existed before
	 * @throws NullPointerException if {@code key} is null
	 */
	public E put(@Nonnull final CharSequence key, final E value)
	{
		if(!validKey(key))
			throw new IllegalArgumentException("As Null keys are errorprone they aren't supported in a CharacterTrie");

		// Start at the root and search the tree for the entry to insert the
		// final character into
		Entry<E> current = root;
		for(int i = 0, len = key.length(); i < len; i++)
		{
			Character c = key.charAt(i);
			// Traverses the tree down to the end where we put in our child
			current = current.ensureChild(c);
		}
		E oldValue = current.setValue(value);
		// TODO: what if null was actually inserted?
		if(oldValue == null)
		{
			size++;
		}
		return oldValue;
	}

	@CheckReturnValue
	public int size()
	{
		return size;
	}

	/**
	 * @param key the key to delete from this tree
	 * @return true if the key previously had a value in this tree
	 */
	public boolean remove(@Nonnull final CharSequence key)
	{
		// Start at the root and search the tree for the entry to delete
		Entry<E> current = root;
		for(int i = 0, len = key.length(); i < len; i++)
		{
			Character c = key.charAt(i);
			current = current.getChild(c);
			if(current == null)
				return false;
		}
		if(current.unset())
		{
			size--;
			if(current.hasChildren())
				// We have children so we are important and can't be removed
				return true;

			Entry<E> parent = current.parent;

			// Remove ourselves from the parent
			parent.deleteChild(current.index);

			// Clean up unused entries
			while(!parent.hasChildren() && !parent.isValue)
			{
				Entry<E> grandParent = parent.parent;
				// Ask the grandParent to remove our parent
				grandParent.deleteChild(parent.index);

				// Walk up the tree and remove entries without children
				parent = grandParent;
			}
			return true;
		}
		return false;
	}

	/**
	 * @param key
	 * @return true if the given key exists in the tree
	 */
	@CheckReturnValue
	public boolean contains(@Nonnull final CharSequence key)
	{
		return root.get(key) != null;
	}

	/**
	 * @param key
	 * @return the value stored for the given key, or null if no such value was
	 *         found
	 */
	@CheckReturnValue
	public E get(@Nonnull final CharSequence key)
	{
		return root.get(key);
	}

	/**
	 * @param key
	 * @return the entry that starts with the same characters as {@code key}
	 */
	@CheckReturnValue
	public Map.Entry<CharSequence, E> getLastMatchingEntry(@Nonnull final CharSequence key)
	{
		return root.getLastMatch(key);
	}

	/**
	 * Create a simple Entry which parent is null.
	 * 
	 * @return
	 */
	private Entry<E> createRoot()
	{
		return new Entry<E>('r', null);
	}

	/**
	 * @return all the keys in this tree
	 */
	@CheckReturnValue
	@Nonnull
	public Set<CharSequence> keys()
	{
		return root.keys();
	}

	/**
	 * @return all the values in this tree
	 */
	@CheckReturnValue
	@Nonnull
	public Collection<E> values()
	{
		return root.values();
	}

	@Override
	public String toString()
	{
		if(root.children == null)
			return "{}";
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		Set<CharSequence> keys = keys();
		// Collections.sort(keys);
		Iterator<CharSequence> keysIter = keys.iterator();
		while(keysIter.hasNext())
		{
			CharSequence key = keysIter.next();
			sb.append(key);
			sb.append(" -> ");
			sb.append(get(key));
			if(keysIter.hasNext())
			{
				sb.append(", ");
			}
		}
		sb.append("}");
		return sb.toString();
	}
}
