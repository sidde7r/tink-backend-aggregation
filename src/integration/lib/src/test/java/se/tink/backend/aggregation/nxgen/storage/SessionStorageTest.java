package se.tink.backend.aggregation.nxgen.storage;

import com.google.common.collect.Sets;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import java.util.HashSet;
import java.util.Set;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.nxgen.storage.configuration.NestedConfigurationLevel1;
import se.tink.backend.aggregation.nxgen.storage.configuration.OuterConfiguration;

public class SessionStorageTest {
    public static final String KEY = "key";
    private SessionStorage sessionStorage;

    @Before
    public void setup() throws Exception {
        this.sessionStorage = new SessionStorage();
    }

    @Test
    public void testNotifySensitiveValues() {
        sessionStorage.put("mykey1", "myvalue1");
        sessionStorage.put("mykey2", 54);
        Set<String> sensitiveValuesTestSet = new HashSet<>();
        Disposable disposable =
                sessionStorage
                        .getSensitiveValuesObservable()
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

        sessionStorage.put("myOuterConfiguration1", outerConfiguration);

        Assert.assertEquals(
                "sensitiveValues are not what was expected.",
                Sets.newHashSet("myvalue1", "54", "stringLevel1", "1", "stringLevel2", "2"),
                sensitiveValuesTestSet);
    }
}
