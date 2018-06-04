package se.tink.backend.logging;

import se.tink.backend.logging.ImmutableDotNotationPrefixTrie;

import org.junit.Assert;
import org.junit.Test;

public class ImmutableDotNotationPrefixTrieTest {

    @Test
    public void testBasics() {
        ImmutableDotNotationPrefixTrie trie = ImmutableDotNotationPrefixTrie.builder().add("a.b").add("a.c.d").build();
        
        Assert.assertTrue(trie.anyStartsWith("a.b"));
        Assert.assertTrue(trie.anyStartsWith("a.b.c"));
        Assert.assertTrue(trie.anyStartsWith("a.c.d.hello"));
        
        Assert.assertFalse(trie.anyStartsWith("a"));
        Assert.assertFalse(trie.anyStartsWith("a.c"));
    }

    @Test
    public void testCheckingEmpty() {
        ImmutableDotNotationPrefixTrie trie = ImmutableDotNotationPrefixTrie.builder().add("a.b.c").build();

        // Not sure this should be true or false. Adding it for clarity of current implementation.
        Assert.assertFalse(trie.anyStartsWith(""));
    }

    @Test
    public void testAddingEmpty() {
        ImmutableDotNotationPrefixTrie trie = ImmutableDotNotationPrefixTrie.builder().add("").build();
        
        // Not sure this should be true or false. Adding it for clarity of current implementation.
        Assert.assertTrue(trie.anyStartsWith(""));
        Assert.assertTrue(trie.anyStartsWith("a.b.c"));
    }
    
    @Test
    public void testSharedPrefix() {
        ImmutableDotNotationPrefixTrie trie = ImmutableDotNotationPrefixTrie.builder().add("a.a.b").add("a.a.c")
                .build();

        Assert.assertTrue(trie.anyStartsWith("a.a.b"));
        Assert.assertTrue(trie.anyStartsWith("a.a.c"));

        Assert.assertFalse(trie.anyStartsWith("a.a"));
        Assert.assertFalse(trie.anyStartsWith("a.a.a"));
    }
    
}
