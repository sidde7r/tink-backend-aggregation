package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.unittest;

import com.google.common.base.Preconditions;
import java.io.File;
import java.nio.file.Paths;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class CreditCalculation {
    private List<AccountBalanceV11TestEntity> testCasesV11;
    private List<AccountBalanceV20TestEntity> testCasesV20;
    private List<AccountBalanceV30TestEntity> testCasesV30;

    @Before
    public void setup() {

        final String testFilePath = "data/test/agents/ukob/balanceEntityTests.json";
        final File testFile = Paths.get(testFilePath).toFile();

        Preconditions.checkState(testFile.canRead(),
                String.format("Could not load %s. Is it missing from the BUILD file?", testFilePath));

        testCasesV11 = loadTestCases(testFile, AccountBalanceV11TestCollection.class);
        testCasesV20 = loadTestCases(testFile, AccountBalanceV20TestCollection.class);
        testCasesV30 = loadTestCases(testFile, AccountBalanceV30TestCollection.class);
    }

    private <T> T loadTestCases(File file, Class<T> cls) {
        return SerializationUtils
                .deserializeFromString(file, cls);
    }

    @Test
    public void testV11() {

        for (AccountBalanceV11TestEntity creditTest : testCasesV11) {

            Assert.assertEquals(
                    creditTest.getExpectedBalance(),
                    creditTest.getBalance().toBigDecimal().doubleValue(),
                    0);

            if (creditTest.getAvailableCredit().isPresent()) {
                Assert.assertTrue(creditTest.getExpectedAvailableCredit().isPresent());
                Assert.assertEquals(
                        creditTest.getExpectedAvailableCredit().get(),
                        creditTest.getAvailableCredit().get().toBigDecimal().doubleValue(),
                        0);
            } else {
                Assert.assertFalse(creditTest.getExpectedAvailableCredit().isPresent());
            }
        }
    }

    @Test
    public void testV20() {

        for (AccountBalanceV20TestEntity creditTest : testCasesV20) {

            Assert.assertEquals(
                    creditTest.getExpectedBalance(),
                    creditTest.getBalance().toBigDecimal().doubleValue(),
                    0);

            if (creditTest.getAvailableCredit().isPresent()) {
                Assert.assertTrue(creditTest.getExpectedAvailableCredit().isPresent());
                Assert.assertEquals(
                        creditTest.getExpectedAvailableCredit().get(),
                        creditTest.getAvailableCredit().get().toBigDecimal().doubleValue(),
                        0);
            } else {
                Assert.assertFalse(creditTest.getExpectedAvailableCredit().isPresent());
            }
        }
    }

    @Test
    public void testV30() {

        for (AccountBalanceV30TestEntity creditTest : testCasesV30) {

            Assert.assertEquals(
                    creditTest.getExpectedBalance(),
                    creditTest.getBalance().toBigDecimal().doubleValue(),
                    0);

            if (creditTest.getAvailableCredit().isPresent()) {
                Assert.assertTrue(creditTest.getExpectedAvailableCredit().isPresent());
                Assert.assertEquals(
                        creditTest.getExpectedAvailableCredit().get(),
                        creditTest.getAvailableCredit().get().toBigDecimal().doubleValue(),
                        0);
            } else {
                Assert.assertFalse(creditTest.getExpectedAvailableCredit().isPresent());
            }
        }
    }
}
