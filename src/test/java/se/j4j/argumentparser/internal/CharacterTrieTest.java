package se.j4j.argumentparser.internal;

import static java.util.Arrays.asList;
import static org.fest.assertions.Assertions.assertThat;
import static se.j4j.argumentparser.internal.CharacterTrie.newTrie;

import org.junit.Test;

import com.google.common.collect.ImmutableSet;

public class CharacterTrieTest
{
	@Test
	public void testTrieTree()
	{

		Object hello = new Object();
		Object him = new Object();
		Object himmi = new Object();
		Object world = new Object();
		CharacterTrie<Object> tree = CharacterTrie.newTrie();

		assertThat(tree.toString()).isEqualTo("{}");
		assertThat(tree.keys()).isEmpty();
		assertThat(tree.remove("nonexisting key")).isFalse();

		// Insertion
		assertThat(tree.put("hello", hello)).as("Failed to insert hello").isNull();
		assertThat(tree.put("himmi", himmi)).as("Failed to insert himmi").isNull();
		assertThat(tree.put("him", him)).as("Failed to insert him").isNull();
		assertThat(tree.put("world", world)).as("Failed to insert world").isNull();

		assertThat(tree.put("world", world)).as("world should already exist in the tree").isEqualTo(world);

		assertThat(tree.size()).as("Wrong tree size, insertion in tree must have failed").isEqualTo(4);

		// Removal

		// Removing a node which have children
		assertThat(tree.remove("him")).as("Failed to delete 'him'").isTrue();
		assertThat(tree.remove("him")).as("Deleted 'him' from: " + tree + ", even though it's part of 'himmi' ").isFalse();
		// Make sure the removal of 'him' left himmi intact
		assertThat(tree.contains("himmi")).as("'himmi' did not exist in tree" + tree).isTrue();

		// Clean up parents because they have no children
		assertThat(tree.remove("himmi")).as("Failed to delete 'himmi' from " + tree).isTrue();

		assertThat(tree.remove("Bye")).as("Deleted non-existing object 'Bye' from " + tree).isFalse();

		assertThat(tree.contains("hello")).isTrue();
		assertThat(tree.contains("Bye")).isFalse();
		assertThat(tree.size()).as("Wrong tree size, deletion from tree must have failed").isEqualTo(2);

		// Retrieval

		assertThat(tree.get("hello")).isEqualTo(hello);
		assertThat(tree.get("world")).isEqualTo(world);

		assertThat(tree.get("Bye")).isNull();
		assertThat(tree.get("hell")).isNull();
		assertThat(tree.keys()).isEqualTo(ImmutableSet.of("hello", "world"));
		assertThat(tree.values()).isEqualTo(asList(world, hello));

		// TODO: what happens if an item is removed during a tree.keys()
		// operation?
	}

	@Test
	public void testStartsWith()
	{
		CharacterTrie<Object> tree = CharacterTrie.newTrie();
		Object value = new Object();

		tree.put("name=", value);

		assertThat(tree.getLastMatchingEntry("name=value").getValue()).isEqualTo(value);
		assertThat(tree.getLastMatchingEntry("")).isNull();
	}

	@Test
	public void testKeyConformity()
	{
		assertThat(CharacterTrie.validKey(null)).isFalse();
		assertThat(CharacterTrie.validKey("")).isTrue();
		assertThat(CharacterTrie.validKey("a")).isTrue();
	}

	@Test(expected = IllegalArgumentException.class)
	public void testNullKey()
	{
		newTrie().put(null, "");
	}
}
