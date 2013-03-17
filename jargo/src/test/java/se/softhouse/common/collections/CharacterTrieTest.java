/* Copyright 2013 Jonatan Jönsson
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
*/
package se.softhouse.common.collections;

import static org.fest.assertions.Assertions.assertThat;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import org.junit.Test;

import se.softhouse.common.collections.CharacterTrie;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.testing.NullPointerTester;
import com.google.common.testing.NullPointerTester.Visibility;

public class CharacterTrieTest
{
	static final String foo = "foo";
	static final String bar = "bar";
	static final String zoo = "zoo";

	@Test
	public void testTrieTree()
	{

		String hello = "hello";
		String him = "him";
		String himmi = "himmi";
		String world = "world";
		CharacterTrie<String> tree = CharacterTrie.newTrie();

		assertThat(tree.toString()).isEqualTo("{}");
		assertThat(tree.keySet()).isEmpty();
		assertThat(tree.remove("nonexisting key")).isNull();

		// Insertion
		assertThat(tree.put("hello", hello)).as("Failed to insert hello").isNull();
		assertThat(tree.put("himmi", himmi)).as("Failed to insert himmi").isNull();
		assertThat(tree.put("him", him)).as("Failed to insert him").isNull();
		assertThat(tree.put("world", world)).as("Failed to insert world").isNull();

		assertThat(tree.put("world", world)).as("world should already exist in the tree").isEqualTo(world);

		assertThat(tree.size()).as("Wrong tree size, insertion in tree must have failed").isEqualTo(4);

		// Removal

		// Removing a node which have children
		assertThat(tree.remove("him")).as("Failed to delete 'him'").isEqualTo(him);
		assertThat(tree.remove("him")).as("Deleted 'him' from: " + tree + ", even though it's part of 'himmi' ").isNull();
		// Make sure the removal of 'him' left himmi intact
		assertThat(tree.containsKey("himmi")).as("'himmi' did not exist in tree" + tree).isTrue();

		// Clean up parents because they have no children
		assertThat(tree.remove("himmi")).as("Failed to delete 'himmi' from " + tree).isEqualTo(himmi);

		assertThat(tree.remove("Bye")).as("Deleted non-existing object 'Bye' from " + tree).isNull();

		assertThat(tree.containsKey("hello")).isTrue();
		assertThat(tree.containsKey("Bye")).isFalse();
		assertThat(tree.size()).as("Wrong tree size, deletion from tree must have failed").isEqualTo(2);

		// Retrieval

		assertThat(tree.get("hello")).isEqualTo(hello);
		assertThat(tree.get("world")).isEqualTo(world);

		assertThat(tree.get("Bye")).isNull();
		assertThat(tree.get("hell")).isNull();
		assertThat(tree.keySet()).containsOnly("hello", "world");
		assertThat(tree.values()).containsOnly(hello, world);
	}

	@Test
	public void testThatEmptyKeysAreSupported() throws Exception
	{
		CharacterTrie<String> trie = CharacterTrie.newTrie();

		trie.put("", foo);

		assertThat(trie.keySet()).contains("");

		assertThat(trie.get("")).isEqualTo(foo);
	}

	@Test
	public void testStartsWith()
	{
		CharacterTrie<Object> tree = CharacterTrie.newTrie();
		Object value = new Object();

		tree.put("name=", value);

		assertThat(tree.findLongestPrefix("name=value").getValue()).isEqualTo(value);
		assertThat(tree.findLongestPrefix("")).isNull();
	}

	@Test
	public void testToStringOnEntry()
	{
		CharacterTrie<String> trie = CharacterTrie.newTrie();
		trie.put("foo", "bar");

		assertThat(trie.findLongestPrefix("foo").toString()).isEqualTo("foo=bar");
	}

	@Test
	public void testThatParentsWithoutChildrenAreRemoved()
	{
		CharacterTrie<String> trie = CharacterTrie.newTrie();

		trie.put("foo", bar);
		trie.put("fooo", bar);
		trie.put("fooos", bar);

		// Removing fooo should leave fooos in the trie has it has fooos as a child
		assertThat(trie.remove("fooo")).isEqualTo(bar);

		// Removing fooos should remove the left-over fooo node as it no longer has any children
		assertThat(trie.remove("fooos")).isEqualTo(bar);
	}

	@Test
	public void testThatEntrySetIteratesOverAllElements() throws Exception
	{
		CharacterTrie<String> trie = CharacterTrie.newTrie();

		trie.put("foo", foo);
		trie.put("fooo", bar);
		trie.put("fooos", zoo);

		Set<String> actualEntries = valuesInSet(trie.entrySet());
		assertThat(actualEntries).containsOnly(foo, bar, zoo);
	}

	@Test
	public void testFindingAllEntriesWithPrefix() throws Exception
	{
		CharacterTrie<String> trie = CharacterTrie.newTrie();

		trie.put("foo", foo);
		trie.put("fooo", bar);
		trie.put("fooos", zoo);

		Set<String> actualEntries = valuesInSet(trie.getEntriesWithPrefix("fooo"));
		assertThat(actualEntries).containsOnly(bar, zoo);
	}

	@Test
	public void testThatFindingAllEntriesWithNonExistingPrefixReturnsEmptySet() throws Exception
	{
		CharacterTrie<String> trie = CharacterTrie.newTrie();

		trie.put("foo", foo);
		trie.put("fooo", bar);
		trie.put("fooos", zoo);

		assertThat(trie.getEntriesWithPrefix("zoo")).isEmpty();
	}

	@Test
	public void testThatFindingAllEntriesWithEmptyPrefixReturnsTheWholeTrie() throws Exception
	{
		CharacterTrie<String> trie = CharacterTrie.newTrie();

		trie.put("foo", foo);
		trie.put("fooo", bar);
		trie.put("fooos", zoo);

		// Make sure equals of Entry isn't fooling us
		CharacterTrie<String> copy = CharacterTrie.newTrie(trie);

		assertThat(trie.getEntriesWithPrefix("")).isEqualTo(copy.entrySet());
	}

	@Test
	public void testThatNullKeyOrValueIsNotContained() throws Exception
	{
		CharacterTrie<Object> trie = CharacterTrie.newTrie();
		assertThat(trie.containsKey(null)).isFalse();
		assertThat(trie.containsValue(null)).isFalse();
	}

	@Test
	public void testThatSizeForSubsetReturnsSizeOfSubset() throws Exception
	{
		CharacterTrie<String> trie = CharacterTrie.newTrie();

		trie.put("foo", foo);
		trie.put("fooo", bar);
		trie.put("fooos", zoo);

		assertThat(trie.getEntriesWithPrefix("fooo")).hasSize(2);
	}

	@Test
	public void testThatRemovalFromEntrySetIsMadeInOriginalStructure() throws Exception
	{
		CharacterTrie<String> trie = CharacterTrie.newTrie();

		trie.put("foo", foo);
		trie.put("fooo", bar);
		trie.put("fooos", zoo);

		Set<Entry<String, String>> entrySet = trie.entrySet();
		Entry<String, String> entryToRemove = Maps.immutableEntry("fooo", bar);
		entrySet.remove(entryToRemove);

		assertThat(trie.containsKey("fooo")).isFalse();

		Iterator<Entry<String, String>> iterator = entrySet.iterator();
		while(iterator.hasNext())
		{
			iterator.next();
			iterator.remove();
		}
		assertThat(trie).isEmpty();
	}

	@Test
	public void testThatEntrySetIsLexicographicallyOrdered() throws Exception
	{
		CharacterTrie<String> trie = CharacterTrie.newTrie();

		trie.put("one", "January");
		trie.put("two", "February");
		trie.put("three", "March");

		assertThat(trie.entrySet().toString()).isEqualTo("[one=January, three=March, two=February]");
	}

	@Test
	public void testThatPreCreatedEntryHasTheRightKey() throws Exception
	{
		CharacterTrie<String> trie = CharacterTrie.newTrie();
		trie.put("NS", foo);
		trie.put("N", bar);

		assertThat(trie.keySet()).containsOnly("NS", "N");
	}

	private Set<String> valuesInSet(Set<Entry<String, String>> entries)
	{
		Set<String> result = Sets.newHashSetWithExpectedSize(entries.size());
		for(Entry<String, String> entry : entries)
		{
			result.add(entry.getValue());
		}
		return result;
	}

	@Test
	public void testNullContracts()
	{
		NullPointerTester npeTester = new NullPointerTester();
		npeTester.testStaticMethods(CharacterTrie.class, Visibility.PACKAGE);
		npeTester.testInstanceMethods(CharacterTrie.newTrie(), Visibility.PACKAGE);
	}
}