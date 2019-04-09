package se.tink.backend.aggregation.utils;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.Test;

public class MapValueMaskerImplTest {
    @Test
    public void testMaskAllValues() {
        Map<String, String> map =
                ImmutableMap.<String, String>builder()
                        .put("key1", "value1")
                        .put("key2", "value2")
                        .build();
        MapValueMaskerImpl mapValueMasker = new MapValueMaskerImpl(Optional.empty());

        Map<String, String> maskedMap = mapValueMasker.copyAndMaskValues(map);

        assertThat(maskedMap.keySet()).containsExactly("key1", "key2");
        assertThat(maskedMap.values()).containsOnly("***MASKED***");
    }

    @Test
    public void testMaskAFewValues() {
        Map<String, String> map =
                ImmutableMap.<String, String>builder()
                        .put("key1", "value1")
                        .put("key2", "value2")
                        .put("key3", "value3")
                        .build();

        Set<String> whiteListedKeys = ImmutableSet.of("key2");

        MapValueMaskerImpl mapValueMasker = new MapValueMaskerImpl(Optional.of(whiteListedKeys));

        Map<String, String> maskedMap = mapValueMasker.copyAndMaskValues(map);

        assertThat(maskedMap.keySet()).containsExactly("key1", "key2", "key3");
        assertThat(maskedMap.values()).hasSize(3);
        assertThat(maskedMap.get("key1")).isEqualTo("***MASKED***");
        assertThat(maskedMap.get("key2")).isEqualTo("value2");
        assertThat(maskedMap.get("key3")).isEqualTo("***MASKED***");
    }

    @Test
    public void testMaskKeysNotExistingIsOk() {
        Map<String, String> map =
                ImmutableMap.<String, String>builder()
                        .put("key1", "value1")
                        .put("existingkey", "value1")
                        .build();

        Set<String> whiteListedKeys = ImmutableSet.of("notexistingkey", "existingkey");

        MapValueMaskerImpl mapValueMasker = new MapValueMaskerImpl(Optional.of(whiteListedKeys));

        Map<String, String> maskedMap = mapValueMasker.copyAndMaskValues(map);

        assertThat(maskedMap).hasSize(2);
        assertThat(maskedMap.get("key1")).isEqualTo("***MASKED***");
        assertThat(maskedMap.get("existingkey")).isEqualTo("value1");
    }

    @Test
    public void testMaskEmptyMapIsOk() {
        Map<String, String> map = ImmutableMap.<String, String>builder().build();

        MapValueMaskerImpl mapValueMasker = new MapValueMaskerImpl(Optional.empty());

        Map<String, String> maskedMap = mapValueMasker.copyAndMaskValues(map);

        assertThat(maskedMap).isNotNull();
        assertThat(maskedMap).hasSize(0);
    }

    @Test(expected = NullPointerException.class)
    public void testMaskNullShouldThrow() {
        MapValueMaskerImpl mapValueMasker = new MapValueMaskerImpl(Optional.empty());

        mapValueMasker.copyAndMaskValues(null);
    }

    @Test
    public void testMultivaluedMask() {
        Map<String, Collection<String>> map = Maps.newConcurrentMap();

        ImmutableList<String> value1 = ImmutableList.of("value1 a", "value1 b");
        ImmutableList<String> value2 = ImmutableList.of("value2 a", "value2 b");
        ImmutableList<String> value3 = ImmutableList.of("value3 a", "value3 b");

        map.put("key1", value1);
        map.put("key2", value2);
        map.put("key3", value3);

        ImmutableSet<String> whiteListedKeys = ImmutableSet.of("key2");

        MapValueMaskerImpl mapValueMasker =
                new MapValueMaskerImpl(Optional.<Set<String>>of(whiteListedKeys));

        Map<String, Collection<String>> maskedMap = mapValueMasker.copyAndMaskMultiValues(map);

        assertThat(maskedMap).isNotNull();
        assertThat(maskedMap).hasSize(3);
        assertThat(maskedMap.get("key1")).hasSize(2);
        assertThat(maskedMap.get("key1")).containsOnlyElementsOf(ImmutableList.of("***MASKED***"));
        assertThat(maskedMap.get("key2")).hasSize(2);
        assertThat(maskedMap.get("key2")).containsExactlyElementsOf(value2);
        assertThat(maskedMap.get("key3")).hasSize(2);
        assertThat(maskedMap.get("key3")).containsOnlyElementsOf(ImmutableList.of("***MASKED***"));
    }

    @Test
    public void testMultivaluedMaskEmptyLists() {
        Map<String, Collection<String>> map = Maps.newConcurrentMap();

        ImmutableList<String> value1 = ImmutableList.of();
        ImmutableList<String> value2 = ImmutableList.of();

        map.put("key1", value1);
        map.put("key2", value2);

        ImmutableSet<String> whiteListedKeys = ImmutableSet.of("key2");

        MapValueMaskerImpl mapValueMasker =
                new MapValueMaskerImpl(Optional.<Set<String>>of(whiteListedKeys));

        Map<String, Collection<String>> maskedMap = mapValueMasker.copyAndMaskMultiValues(map);

        assertThat(maskedMap).isNotNull();
        assertThat(maskedMap).hasSize(2);
        assertThat(maskedMap.get("key1")).hasSize(0);
        assertThat(maskedMap.get("key2")).hasSize(0);
    }

    @Test(expected = NullPointerException.class)
    public void testMultivaluedMaskNullShouldThrow() {
        MapValueMaskerImpl mapValueMasker = new MapValueMaskerImpl(Optional.empty());

        mapValueMasker.copyAndMaskMultiValues(null);
    }

    @Test
    public void testMultivaluedMaskShouldIgnoreCasing() {
        Map<String, Collection<String>> map = Maps.newConcurrentMap();

        ImmutableList<String> value1 = ImmutableList.of("value1 a", "value1 b");
        ImmutableList<String> value2 = ImmutableList.of("value2 a", "value2 b");

        map.put("SOMEWEIRD-cåäösing", value1);
        map.put("otherkey", value2);

        ImmutableSet<String> whiteListedKeys = ImmutableSet.of("someweird-CÅÄÖSING");

        MapValueMaskerImpl mapValueMasker =
                new MapValueMaskerImpl(Optional.<Set<String>>of(whiteListedKeys));

        Map<String, Collection<String>> maskedMap = mapValueMasker.copyAndMaskMultiValues(map);

        assertThat(maskedMap).isNotNull();
        assertThat(maskedMap).hasSize(2);
        assertThat(maskedMap.get("SOMEWEIRD-cåäösing")).hasSize(2);
        assertThat(maskedMap.get("SOMEWEIRD-cåäösing")).containsExactlyElementsOf(value1);
        assertThat(maskedMap.get("otherkey")).hasSize(2);
        assertThat(maskedMap.get("otherkey"))
                .containsOnlyElementsOf(ImmutableList.of("***MASKED***"));
    }
}
