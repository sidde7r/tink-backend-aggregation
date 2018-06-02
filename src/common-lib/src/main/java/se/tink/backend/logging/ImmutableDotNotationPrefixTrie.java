package se.tink.backend.logging;

import com.google.common.base.MoreObjects;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

public class ImmutableDotNotationPrefixTrie {

    public static class Builder {
        private Node trie = new Node();

        private Builder() {
        }

        public Builder add(String prefix) {
            Iterator<String> pieces = SPLITTER.split(prefix).iterator();

            Node localTrie = this.trie;
            while (pieces.hasNext()) {
                String firstPiece = pieces.next();

                Node nextNode = localTrie.children.get(firstPiece);
                if (nextNode == null) {
                    nextNode = new Node();
                    localTrie.children.put(firstPiece, nextNode);
                }

                localTrie = nextNode;
            }

            localTrie.added = true;

            return this;
        }

        public ImmutableDotNotationPrefixTrie build() {
            return new ImmutableDotNotationPrefixTrie(trie);
        }
    }

    public static class Node {
        public Map<String, Node> children = Maps.newHashMap();
        public boolean added = false;

        public void freeze() {
            for (Node node : children.values()) {
                node.freeze();
            }
            children = ImmutableMap.copyOf(children);
        }

        /**
         * Mostly used for debugging.
         */
        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this).add("added", added).add("children", children).toString();
        }
    }

    private static final Splitter SPLITTER = Splitter.on(".").omitEmptyStrings().trimResults();

    public static Builder builder() {
        return new Builder();
    }

    public static ImmutableDotNotationPrefixTrie copyOf(Collection<String> strings) {
        Builder builder = builder();
        for (String string : strings) {
            builder.add(string);
        }
        return builder.build();
    }

    private final Node trie;

    public ImmutableDotNotationPrefixTrie(Node trie) {
        this.trie = trie;
    }

    public boolean anyStartsWith(String dotNotationString) {
        Iterator<String> pieces = SPLITTER.split(dotNotationString).iterator();

        if (trie.added) {
            return true;
        }

        if (!pieces.hasNext()) {
            return false;
        }

        Node localTrie = this.trie;
        while (pieces.hasNext()) {
            String piece = pieces.next();
            Node node = localTrie.children.get(piece);
            if (node == null) {
                return localTrie.added;
            }
            localTrie = node;
        }
        return localTrie.added;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("trie", this.trie).toString();
    }

}
