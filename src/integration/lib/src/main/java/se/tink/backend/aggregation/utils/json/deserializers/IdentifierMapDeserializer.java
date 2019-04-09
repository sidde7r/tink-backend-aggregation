package se.tink.backend.aggregation.utils.json.deserializers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.ArrayNode;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

// @formatter:off
/**
 * Converts an array to map, using the given attribute as the key in the map.
 *
 * <p>Example using 'keyAttribute = name':
 *
 * <p>[ { "name: item_1, "content": "some_important_text" }, { "name": "item_2", "content":
 * "some_other_text" } ]
 *
 * <p>Will de-serialize to:
 *
 * <p>{ "item_1": { "name": "item_1", "content": "some_important_text" }, "item_2": { "name":
 * "item_2", "content": "some_other_text" } }
 */
// @formatter:on
public abstract class IdentifierMapDeserializer<K, T> extends StdDeserializer<Map<K, T>> {

    private final String keyAttribute;
    private final Class<K> keyType;
    private final Class<T> entityType;

    public IdentifierMapDeserializer(String keyAttribute, Class<K> keyType, Class<T> entityType) {
        super(entityType);
        this.keyAttribute = keyAttribute;
        this.keyType = keyType;
        this.entityType = entityType;
    }

    @Override
    public Map<K, T> deserialize(JsonParser parser, DeserializationContext ctx) throws IOException {

        ObjectCodec codec = parser.getCodec();
        TreeNode treeNode = codec.readTree(parser);
        ObjectMapper mapper = new ObjectMapper();

        Map<K, T> result = new HashMap<>();
        if (treeNode.isArray()) {
            for (JsonNode node : (ArrayNode) treeNode) {

                if (node.hasNonNull(keyAttribute)) {

                    try {
                        K key = mapper.convertValue(node.get(keyAttribute), keyType);
                        result.put(key, codec.treeToValue(node, entityType));
                    } catch (Exception e) {
                        throw new JsonMappingException(
                                parser,
                                String.format(
                                        "Attribute [%s] count not deserialize to [%s]",
                                        keyAttribute, keyType.toString()));
                    }
                } else {
                    throw new JsonMappingException(
                            parser,
                            String.format("Object does not have attribute %s", keyAttribute));
                }
            }
        } else {

            throw new JsonMappingException(parser, "Expected outer node to be of type array.");
        }
        return result;
    }
}
