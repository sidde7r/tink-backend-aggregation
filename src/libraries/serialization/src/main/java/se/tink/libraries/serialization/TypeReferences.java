package se.tink.libraries.serialization;

import com.fasterxml.jackson.core.type.TypeReference;
import java.awt.Point;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class TypeReferences {

    public static final TypeReference<List<String>> LIST_OF_STRINGS =
            new TypeReference<List<String>>() {};

    public static final TypeReference<List<List<Point>>> LIST_OF_LIST_OF_POINTS =
            new TypeReference<List<List<Point>>>() {};

    public static final TypeReference<HashMap<String, String>> MAP_OF_STRING_STRING =
            new TypeReference<HashMap<String, String>>() {};

    public static final TypeReference<HashMap<String, List<String>>> MAP_OF_STRING_LIST_STRING =
            new TypeReference<HashMap<String, List<String>>>() {};

    public static final TypeReference<HashMap<String, Object>> MAP_OF_STRING_OBJECT =
            new TypeReference<HashMap<String, Object>>() {};

    public static final TypeReference<HashSet<String>> SET_OF_STRINGS =
            new TypeReference<HashSet<String>>() {};
}
