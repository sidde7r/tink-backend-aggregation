package se.tink.backend.aggregation.nxgen.storage;

import com.google.common.collect.Sets;
import io.reactivex.rxjava3.disposables.Disposable;
import java.util.HashSet;
import java.util.Set;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.nxgen.storage.configuration.NestedConfigurationLevel1;
import se.tink.backend.aggregation.nxgen.storage.configuration.OuterConfiguration;

public class PersistentStorageTest {
    public static final String KEY = "key";
    private PersistentStorage persistentStorage;

    @Before
    public void setup() throws Exception {
        this.persistentStorage = new PersistentStorage();
    }

    @Test
    public void testNotifySimpleSensitiveValues() {
        // given
        Set<String> sensitiveValuesTestSet = new HashSet<>();
        Disposable subscribe =
                persistentStorage
                        .getSensitiveValuesObservable()
                        .subscribe(sensitiveValuesTestSet::addAll);
        // when
        persistentStorage.put("mykey1", "myvalue1");
        persistentStorage.put("mykey2", 54);

        // then
        Assert.assertEquals(
                "sensitiveValues are not what was expected.",
                Sets.newHashSet("myvalue1", "54"),
                sensitiveValuesTestSet);
    }

    @Test
    public void testNotifyAgentPlatformSensitiveValues() {
        // given
        Set<String> sensitiveValuesTestSet = new HashSet<>();
        Disposable subscribe =
                persistentStorage
                        .getSensitiveValuesObservable()
                        .subscribe(sensitiveValuesTestSet::addAll);
        // when
        persistentStorage.put("AgentPlatformData", "{\"consentId\":\"consentId1\"}");

        // then
        Assert.assertEquals(
                "sensitiveValues are not what was expected.",
                Sets.newHashSet("consentId1"),
                sensitiveValuesTestSet);
    }

    @Test
    public void testNotifyConfigurationSensitiveValues() {
        // given
        Set<String> sensitiveValuesTestSet = new HashSet<>();
        Disposable subscribe =
                persistentStorage
                        .getSensitiveValuesObservable()
                        .subscribe(sensitiveValuesTestSet::addAll);
        NestedConfigurationLevel1 nestedConfigurationLevel1 =
                new NestedConfigurationLevel1("stringLevel2", 2, null);
        OuterConfiguration outerConfiguration =
                new OuterConfiguration("stringLevel1", 1, nestedConfigurationLevel1);

        // when
        persistentStorage.put("myOuterConfiguration1", outerConfiguration);

        // then
        Assert.assertEquals(
                "sensitiveValues are not what was expected.",
                Sets.newHashSet("stringLevel1", "1", "stringLevel2", "2"),
                sensitiveValuesTestSet);
    }
}
