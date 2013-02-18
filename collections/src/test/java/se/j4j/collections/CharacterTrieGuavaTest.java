package se.j4j.collections;

import java.util.Map;
import java.util.Map.Entry;

import junit.framework.Test;

import com.google.common.collect.testing.MapTestSuiteBuilder;
import com.google.common.collect.testing.TestStringMapGenerator;
import com.google.common.collect.testing.features.CollectionFeature;
import com.google.common.collect.testing.features.CollectionSize;
import com.google.common.collect.testing.features.MapFeature;

public class CharacterTrieGuavaTest
{
	public static Test suite()
	{
		return MapTestSuiteBuilder.using(new TestStringMapGenerator(){
			@Override
			protected Map<String, String> create(Entry<String, String>[] entries)
			{
				CharacterTrie<String> trie = CharacterTrie.newTrie();
				for(Entry<String, String> entry : entries)
				{
					trie.put(entry.getKey(), entry.getValue());
				}
				return trie;
			}
		}).withFeatures(CollectionFeature.FAILS_FAST_ON_CONCURRENT_MODIFICATION, MapFeature.GENERAL_PURPOSE, CollectionSize.ANY)
				.named("CharacterTrie guava-test").createTestSuite();
	}
}
