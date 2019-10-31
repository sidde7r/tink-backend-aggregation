package se.tink.backend.aggregation.nxgen.storage;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Sets;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.nxgen.storage.configuration.NestedConfigurationLevel1;
import se.tink.backend.aggregation.nxgen.storage.configuration.OuterConfiguration;

public class StorageTest {

    public static final String KEY = "key";
    private Storage sessionStorage;
    private Storage persistentStorage;

    @Before
    public void setup() throws Exception {
        this.sessionStorage = new SessionStorage();
        this.persistentStorage = new PersistentStorage();
    }

    @Test
    public void getGenericListFromSessionStorage() {
        this.sessionStorage.put(KEY, Collections.<String>emptyList());
        assertThat(
                Collections.EMPTY_LIST,
                equalTo(this.sessionStorage.get(KEY, new TypeReference<List<String>>() {}).get()));
        assertThat(
                Optional.empty(),
                equalTo(this.persistentStorage.get(KEY, new TypeReference<List<String>>() {})));
    }

    @Test
    public void getGenericMapFromSessionStorage() {
        Map<Integer, Long> map = new HashMap<>();
        map.put(1, 1l);
        this.sessionStorage.put(KEY, map);
        assertThat(
                map,
                equalTo(
                        this.sessionStorage
                                .get(KEY, new TypeReference<Map<Integer, Long>>() {})
                                .get()));
        assertThat(
                Optional.empty(),
                equalTo(
                        this.persistentStorage.get(
                                KEY, new TypeReference<Map<Integer, Long>>() {})));
    }

    @Test
    public void getGenericClassFromSessionStorage() {
        Double value = 1d;
        this.sessionStorage.put(KEY, value);
        assertThat(1d, equalTo(sessionStorage.get(KEY, new TypeReference<Double>() {}).get()));
        assertThat(
                Optional.empty(),
                equalTo(this.persistentStorage.get(KEY, new TypeReference<Double>() {})));
    }

    @Test
    public void getGenericListFromPersistentStorage() {
        this.persistentStorage.put(KEY, Collections.<String>emptyList());
        assertThat(
                Collections.EMPTY_LIST,
                equalTo(
                        this.persistentStorage
                                .get(KEY, new TypeReference<List<String>>() {})
                                .get()));
        assertThat(
                Optional.empty(),
                equalTo(this.sessionStorage.get(KEY, new TypeReference<List<String>>() {})));
    }

    @Test
    public void getGenericMapFromPersistentStorage() {
        Map<Integer, Long> map = new HashMap<>();
        map.put(1, 1l);
        this.persistentStorage.put(KEY, map);
        assertThat(
                map,
                equalTo(
                        this.persistentStorage
                                .get(KEY, new TypeReference<Map<Integer, Long>>() {})
                                .get()));
        assertThat(
                Optional.empty(),
                equalTo(this.sessionStorage.get(KEY, new TypeReference<Map<Integer, Long>>() {})));
    }

    @Test
    public void getGenericClassFromPersistentStorage() {
        Double value = 1d;
        this.persistentStorage.put(KEY, value);
        assertThat(
                1d, equalTo(this.persistentStorage.get(KEY, new TypeReference<Double>() {}).get()));
        assertThat(
                Optional.empty(),
                equalTo(this.sessionStorage.get(KEY, new TypeReference<Double>() {})));
    }

    @Test
    public void ensureOurGet_withOurPutString_doesNotRaise() {
        sessionStorage.put("mykey", "myvalue");
        final Optional<String> value = sessionStorage.get("mykey", String.class);
        assertThat(Optional.of("myvalue"), equalTo(value));
    }

    @Test
    public void testNotifySecretValues() {
        persistentStorage.put("mykey1", "myvalue1");
        persistentStorage.put("mykey2", 54);
        Set<String> sensitiveValuesTestSet = new HashSet<>();
        Disposable disposable =
                persistentStorage
                        .getSecretValuesObservable()
                        .subscribeOn(Schedulers.trampoline())
                        .subscribe(
                                newSecretValues -> sensitiveValuesTestSet.addAll(newSecretValues));

        Assert.assertEquals(
                "sensitiveValues are not what was expected.",
                Sets.newHashSet("myvalue1", "54"),
                sensitiveValuesTestSet);

        NestedConfigurationLevel1 nestedConfigurationLevel1 =
                new NestedConfigurationLevel1("stringLevel2", 2, null);
        OuterConfiguration outerConfiguration =
                new OuterConfiguration("stringLevel1", 1, nestedConfigurationLevel1);

        persistentStorage.put("myOuterConfiguration1", outerConfiguration);

        Assert.assertEquals(
                "sensitiveValues are not what was expected.",
                Sets.newHashSet("myvalue1", "54", "stringLevel1", "1", "stringLevel2", "2"),
                sensitiveValuesTestSet);
    }
}
