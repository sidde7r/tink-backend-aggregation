package se.tink.backend.common.utils;

import java.util.Map;

import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;

public class GlobMatchTree<T> {
    private class GlobMatchNode {
        private Map<Character, GlobMatchNode> children = Maps.newHashMap();
        private T item;
    }

    private Function<T, String> function;

    // XXX: If we ever want to optimize the size and lookup speed of this data structure we should consider converting
    // this to a radix tree.
    private Map<Character, GlobMatchNode> roots = Maps.newHashMap();

    public GlobMatchTree(Iterable<T> items, Function<T, String> function) {
        this.function = function;

        for (T item : items) {
            String key = function.apply(item);
            add(roots, item, key);
        }
    }

    private void add(Map<Character, GlobMatchNode> nodes, T item, String key) {
        char letter = key.charAt(0);

        GlobMatchNode node = nodes.get(letter);

        if (node == null) {
            node = new GlobMatchNode();
            nodes.put(letter, node);
        }

        if (key.length() == 1) {
            node.item = item;
        } else {
            add(node.children, item, key.substring(1, key.length()));
        }
    }

    public T match(String text) {
        return match(text, roots, true);
    }

    private T match(String text, Map<Character, GlobMatchNode> currentNode, boolean isRoot) {
        if (Strings.isNullOrEmpty(text)) {
            GlobMatchNode wildcardChild = currentNode.get('*');

            if (wildcardChild == null) {
                return null;
            } else {
                return wildcardChild.item;
            }
        }

        char currentKey = text.charAt(0);

        boolean isWildcard = false;

        for (char key : currentNode.keySet()) {
            if (key == '*') {
                GlobMatchNode child = currentNode.get(key);
                if (child.children.isEmpty() || isRoot) {
                    isWildcard = true;
                } else if (!child.children.isEmpty()) {
                    if (key == currentKey) {
                        return match(text.substring(1, text.length()), child.children, false);
                    } else {
                        return null;
                    }
                }
                    
            } else if (key == currentKey) {
                GlobMatchNode child = currentNode.get(key);

                if (child.children.isEmpty() && text.length() == 1) {
                    return child.item;
                } else {
                    T match = match(text.substring(1, text.length()), child.children, false);
                    if (match != null) {
                        return match;
                    }
                }
            }
        }

        if (isWildcard) {
            GlobMatchNode wildcardChild = currentNode.get('*');

            if (wildcardChild.children.isEmpty()) {
                return wildcardChild.item;
            } else {
                int bestMatchLength = -1;
                T bestMatch = null;

                for (char key : wildcardChild.children.keySet()) {
                    int i = -1;

                    do {
                        i = text.indexOf(key, i + 1);

                        if (i == -1) {
                            continue;
                        }

                        T match = match(text.substring(i + 1, text.length()), wildcardChild.children.get(key).children, false);

                        if (match != null) {
                            int itemLength = function.apply(match).length();

                            if (itemLength <= bestMatchLength) {
                                continue;
                            }

                            bestMatchLength = itemLength;
                            bestMatch = match;
                        }
                    } while (i != -1);
                }

                if (bestMatch != null) {
                    return bestMatch;
                }
            }
        }

        return null;
    }
}
