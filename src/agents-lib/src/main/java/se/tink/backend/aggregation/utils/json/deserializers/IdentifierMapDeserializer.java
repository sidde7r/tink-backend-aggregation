package se.tink.backend.aggregation.utils.json.deserializers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.common.base.Strings;
import java.io.IOException;
import java.util.HashMap;

// @formatter:off
/**
 * Converts an array to map, using the given attribute as the key in the map.
 *
 * Example using 'keyAttribute = name':
 *
 * [
 *    {
 *         "name: item_1,
 *         "content": "some_important_text"
 *     },
 *     {
 *         "name": "item_2",
 *         "content": "some_other_text"
 *     }
 * ]
 *
 * Will de-serialize to:
 *
 * {
 *     "item_1": {
 *         "name: item_1,
 *         "content": "some_important_text"
 *     },
 *     "item_2: {
 *         "name": "item_2",
 *         "content": "some_other_text"
 *     }
 * }
 */
// @formatter:on
public abstract class IdentifierMapDeserializer<T> extends StdDeserializer<HashMap<String, T>> {

    private final String keyAttribute;
    private final Class<T> entityType;

    public IdentifierMapDeserializer(String keyAttribute, Class<T> entityType) {
        super(entityType);
        this.keyAttribute = keyAttribute;
        this.entityType = entityType;
    }

    @Override
    public HashMap<String, T> deserialize(JsonParser parser, DeserializationContext ctx)
            throws IOException {

        ObjectCodec codec = parser.getCodec();
        TreeNode treeNode = codec.readTree(parser);

        HashMap<String, T> result = new HashMap<>();
        if (treeNode.isArray()) {
            for (JsonNode node : (ArrayNode) treeNode) {

                if (node.hasNonNull(keyAttribute)) {

                    String key = node.get(keyAttribute).asText();
                    if(Strings.isNullOrEmpty(key)) {
                        throw new IllegalStateException(String.format(
                                "%s cannot be represented as a String and therefor cannot be used as a key.",
                                keyAttribute));
                    }

                    result.put(key, codec.treeToValue(node, entityType));
                } else {

                    throw new IllegalStateException(String.format("Object does not have attribute %s",
                            keyAttribute));
                }
            }
        } else {

            throw new IllegalStateException("Expected outer node to be of type array.");
        }
        return result;
    }
}
