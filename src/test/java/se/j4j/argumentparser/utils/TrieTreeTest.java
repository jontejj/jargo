package se.j4j.argumentparser.utils;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.junit.Test;

import se.j4j.argumentparser.internal.TrieTree;

public class TrieTreeTest
{
	private <T> List<T> asList(final T ... values)
	{
		List<T> list = new ArrayList<T>();
		for(T value : values)
		{
			list.add(value);
		}
		return list;
	}

	@Test
	public void testTrieTree()
	{

		Object hello = new Object();
		Object him = new Object();
		Object himmi = new Object();
		Object world = new Object();
		TrieTree<Object> tree = TrieTree.newTree();

		assertEquals("{}", tree.toString());
		assertEquals(new HashSet<Object>(), tree.keys());
		assertFalse(tree.delete("nonexisting key"));

		assertNull("Failed to insert hello", tree.set("hello", hello));
		assertNull("Failed to insert himmi", tree.set("himmi", himmi));
		assertNull("Failed to insert him", tree.set("him", him));
		assertNull("Failed to insert world", tree.set("world", world));

		assertNotNull("world should already exist in the tree", tree.set("world", world));

		assertEquals("Wrong tree size, insertion in tree must have failed", 4, tree.size());

		// Removing a node which have children
		assertTrue("Failed to delete \"him\" from " + tree, tree.delete("him"));
		assertFalse("Deleted \"him\" from: " + tree + ", even though it's part of \"himmi\" ", tree.delete("him"));
		// Make sure the children was left intact
		assertTrue("\"himmi\" did not exist in tree" + tree, tree.contains("himmi"));

		// Clean up parents because they haven't children
		assertTrue("Failed to delete \"himmi\" from " + tree, tree.delete("himmi"));

		assertFalse("Deleted non-existing object \"Bye\" from " + tree, tree.delete("Bye"));

		assertTrue("hello didn't exist in the tree", tree.contains("hello"));
		assertFalse("Bye did exist in the tree", tree.contains("Bye"));
		assertEquals("Wrong tree size, deletion from tree must have failed", 2, tree.size());

		assertEquals(hello, tree.get("hello"));
		assertEquals(world, tree.get("world"));
		assertNull(tree.get("Bye"));
		assertNull(tree.get("hell"));
		assertEquals(new HashSet<String>(asList("hello", "world")), tree.keys());

		// TODO: what happens if an item is removed during a tree.keys()
		// operation?
	}

	@Test
	public void testStartsWith()
	{
		TrieTree<Object> tree = TrieTree.newTree();
		Object value = new Object();

		tree.set("name=", value);

		assertEquals(value, tree.getLastMatch("name=value"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testInvalidKeys()
	{
		assertFalse(TrieTree.validKey(""));
		assertFalse(TrieTree.validKey(null));
		TrieTree<Object> tree = TrieTree.newTree();
		tree.set("", null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testNullKey()
	{
		TrieTree<Object> tree = TrieTree.newTree();
		tree.set(null, null);
	}

	@Test
	public void testValidKeys()
	{
		assertTrue(TrieTree.validKey("a"));
	}
}
