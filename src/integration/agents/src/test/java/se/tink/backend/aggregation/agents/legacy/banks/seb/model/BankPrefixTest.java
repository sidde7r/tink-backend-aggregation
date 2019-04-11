package se.tink.backend.aggregation.agents.banks.seb.model;

import org.junit.Assert;
import org.junit.Test;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import se.tink.libraries.account.identifiers.TestAccount;

public class BankPrefixTest {

    @Test
    public void testDanskeBank() {
        Assert.assertEquals("DB", getBankPrefixFrom(TestAccount.DANSKEBANK_FH));
        Assert.assertEquals("DB", getBankPrefixFrom(TestAccount.DANSKEBANK_ANOTHER_FH));
    }

    @Test
    public void testHandelsbanken() {
        Assert.assertEquals("SHB", getBankPrefixFrom(TestAccount.HANDELSBANKEN_FH));
    }

    @Test
    public void testICABanken() {
        Assert.assertEquals("ICAB", getBankPrefixFrom(TestAccount.ICABANKEN_FH));
    }

    @Test
    public void testNordea() {
        Assert.assertEquals("NB", getBankPrefixFrom(TestAccount.NORDEA_EP));
    }

    @Test
    public void testLansforsakringar() {
        Assert.assertEquals("LF", getBankPrefixFrom(TestAccount.LANSFORSAKRINGAR_FH));
    }

    @Test
    public void testSavingsBank() {
        Assert.assertEquals("SWED", getBankPrefixFrom(TestAccount.SAVINGSBANK_AL));
    }

    @Test
    public void testSEB() {
        Assert.assertEquals("SEB", getBankPrefixFrom(TestAccount.SEB_DL));
        Assert.assertEquals("SEB", getBankPrefixFrom(TestAccount.SEB_JR));
        Assert.assertEquals("SEB", getBankPrefixFrom(TestAccount.SEB_ANOTHER_JR));
    }

    @Test
    public void testSkandiabanken() {
        Assert.assertEquals("SKB", getBankPrefixFrom(TestAccount.SKANDIABANKEN_FH));
    }

    @Test
    public void testSwedbank() {
        Assert.assertEquals("SWED", getBankPrefixFrom(TestAccount.SWEDBANK_FH));
    }

    @Test
    public void testAmfaRegexp() {
        // AMFA9660("AMFA9660", "AMFA", "^(966[0-9])\\d{7}$"),
        Assert.assertEquals("AMFA", getBankPrefixFrom("96601234567"));
    }

    @Test
    public void testAvanzaRegexp() {
        // AZA9550("AZA9550", "AZA", "^(95[5-6][0-9])\\d{7}$"),
        Assert.assertEquals("AZA", getBankPrefixFrom("95501234567"));
    }

    @Test
    public void testFortisBankRegexp() {
        // BNPPF9470("BNPPF9470", "BNPPF", "^(947[0-9])\\d{7}$"),
        Assert.assertEquals("BNPPF", getBankPrefixFrom("94701234567"));
    }

    @Test
    public void testBluestepFinansRegexp() {
        // BSTP9680("BSTP9680", "BSTP", "^(968[0-9])\\d{7}$"),
        Assert.assertEquals("BSTP", getBankPrefixFrom("96801234567"));
    }

    @Test
    public void testCitibankRegexp() {
        // CITI9040("CITI9040", "CITI", "^(904[0-9])\\d{7}$"),
        for (int i = 0; i < 10; i++) {
            Assert.assertEquals("CITI", getBankPrefixFrom(String.format("904%d1234567", i)));
        }
    }

    @Test
    public void testDanskeBankRegexps() {
        // DB1200("DB1200", "DB", "^(1[23]\\d{2})\\d{7}$"),
        Assert.assertEquals("DB", getBankPrefixFrom("12001234567"));
        Assert.assertEquals("DB", getBankPrefixFrom("13001234567"));

        // DB2400("DB2400", "DB", "^24\\d{9}$"),
        Assert.assertEquals("DB", getBankPrefixFrom("24123456789"));

        // DB4993("DB4993", "DB", "^(4993|336[3-7])\\d{6}$"),
        // Officially maps to Nordea, not Danske Bank. SEB have acknowledged that this is a bug and
        // will fix
        // accordingly.
        Assert.assertEquals("DB", getBankPrefixFrom("4993214387"));
        for (int i = 3; i < 8; i++) {
            // Officially maps to Nordea, not Danske Bank.
            String s = String.format("336%d123456", i);
            Assert.assertEquals(s, "DB", getBankPrefixFrom(s));
        }

        // DB6044("DB6044", "DB", "^6044993\\d{6}$|^604336[3-7]\\d{6}$"),
        // Officially maps to Nordea, not Danske Bank. SEB have acknowledged that this is a bug and
        // will fix
        // accordingly.
        Assert.assertEquals("DB", getBankPrefixFrom("6044993123456"));
        for (int i = 3; i < 8; i++) {
            // Officially maps to Nordea, not Danske Bank. SEB have acknowledged that this is a bug
            // and will fix
            // accordingly.
            Assert.assertEquals("DB", getBankPrefixFrom(String.format("604336%d123456", i)));
        }

        // DB9180("DB9180", "DB", "^(918[0-9])\\d{7}$"),
        for (int i = 0; i < 10; i++) {
            Assert.assertEquals("DB", getBankPrefixFrom(String.format("918%d1234567", i)));
        }
    }

    @Test
    public void testNorskeBankSverigeRegexp() {
        // DNBSE9190("DNBSE9190", "DNBSE", "^(919[0-9])\\d{7}$"),
        for (int i = 0; i < 10; i++) {
            Assert.assertEquals("DNBSE", getBankPrefixFrom(String.format("919%d1234567", i)));
        }
    }

    @Test
    public void testErikPenserRegexp() {
        // ERPB9590("ERPB9590", "ERPB", "^(959[0-9])\\d{7}$"),
        for (int i = 0; i < 10; i++) {
            Assert.assertEquals("ERPB", getBankPrefixFrom(String.format("959%d1234567", i)));
        }
    }

    @Test
    public void testForexBankRegexp() {
        // FOREX9400("FOREX9400", "FOREX", "^(94[0-4][0-9])\\d{7}$"),
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 10; j++) {
                Assert.assertEquals(
                        "FOREX", getBankPrefixFrom(String.format("94%d%d1234567", i, j)));
            }
        }
    }

    @Test
    public void testSantanderRegexp() {
        // GEMB9460("GEMB9460", "GEMB", "^(946[0-9])\\d{7}$"),
        for (int i = 0; i < 10; i++) {
            Assert.assertEquals("GEMB", getBankPrefixFrom(String.format("946%d1234567", i)));
        }
    }

    @Test
    public void testICABankenRegexp() {
        // ICAB9270("ICAB9270", "ICAB", "^(927[0-9])\\d{7}$"),
        for (int i = 0; i < 10; i++) {
            Assert.assertEquals("ICAB", getBankPrefixFrom(String.format("927%d1234567", i)));
        }
    }

    @Test
    public void testIkanoRegexp() {
        // IKANO9170("IKANO9170", "IKANO", "^(917[0-9])\\d{7}$"),
        for (int i = 0; i < 10; i++) {
            Assert.assertEquals("IKANO", getBankPrefixFrom(String.format("917%d1234567", i)));
        }
    }

    @Test
    public void testLandshypotekRegexp() {
        // LAHYP9390("LAHYP9390", "LAHYP", "^(939[0-9])\\d{7}$"),
        for (int i = 0; i < 10; i++) {
            Assert.assertEquals("LAHYP", getBankPrefixFrom(String.format("939%d1234567", i)));
        }
    }

    @Test
    public void testLansforsakringarBankRegexp() {
        // LF9020("LF9020", "LF", "^(90[26][0-9]|340[0-9])\\d{7}$"),
        for (int i = 0; i < 10; i++) {
            Assert.assertEquals("LF", getBankPrefixFrom(String.format("902%d1234567", i)));
        }
        for (int i = 0; i < 10; i++) {
            Assert.assertEquals("LF", getBankPrefixFrom(String.format("906%d1234567", i)));
        }
        for (int i = 0; i < 10; i++) {
            Assert.assertEquals("LF", getBankPrefixFrom(String.format("340%d1234567", i)));
        }
    }

    @Test
    public void testLanOchSparBankRegexp() {
        // LSBSE9630("LSBSE9630", "LSBSE", "^(963[0-9])\\d{7}$"),
        for (int i = 0; i < 10; i++) {
            Assert.assertEquals("LSBSE", getBankPrefixFrom(String.format("963%d1234567", i)));
        }
    }

    @Test
    public void testMarginalenBankRegexp() {
        // MARG9230("MARG9230", "MARG", "^(923[0-9])\\d{7}$"),
        for (int i = 0; i < 10; i++) {
            Assert.assertEquals("MARG", getBankPrefixFrom(String.format("923%d1234567", i)));
        }
    }

    @Test
    public void testNordeaRegexp() {
        // NB1100(
        // "NB1100",
        // "NB",
        // "(^((11\\d{2})|(1[4-9]\\d{2})|(20\\d{2})|(3[0-3]\\d{2})|(34[1-9]\\d{1}|3[5-9]\\d{2})|(4\\d{3}))\\d{7}$)"),
        for (int i = 0; i < 100; i++) {
            Assert.assertEquals("NB", getBankPrefixFrom(String.format("11%02d1234567", i)));
        }
        for (int j = 4; j < 10; j++) {
            for (int i = 0; i < 100; i++) {
                Assert.assertEquals("NB", getBankPrefixFrom(String.format("1%d%02d1234567", j, i)));
            }
        }
        for (int i = 0; i < 100; i++) {
            Assert.assertEquals("NB", getBankPrefixFrom(String.format("20%02d1234567", i)));
        }
        for (int j = 0; j < 4; j++) {
            for (int i = 0; i < 100; i++) {
                Assert.assertEquals("NB", getBankPrefixFrom(String.format("3%d%02d1234567", j, i)));
            }
        }
        for (int j = 0; j < 10; j++) {
            for (int i = 1; i < 10; i++) {
                Assert.assertEquals("NB", getBankPrefixFrom(String.format("34%d%d1234567", i, j)));
            }
        }
        for (int j = 5; j < 10; j++) {
            for (int i = 0; i < 100; i++) {
                Assert.assertEquals("NB", getBankPrefixFrom(String.format("3%d%02d1234567", j, i)));
            }
        }
        for (int i = 0; i < 1000; i++) {
            Assert.assertEquals("NB", getBankPrefixFrom(String.format("4%03d1234567", i)));
        }

        // NB3000("NB3000", "NB", "(^\\d{2}(0[1-9]|1[012])(0[1-9]|[12][0-9]|3[01])\\d{4}$)"),
        for (int i = 0; i < 100; i++) {
            // 3300 is stripped by the SEBAccountIdentifierFormatter in the SEB agent.
            testNordeaPrefix1(String.format("3300%02d", i));
        }
    }

    @Test
    public void testNordaxFinansRegexp() {
        // NDX9640("NDX9640", "NDX", "^(964[0-9])\\d{7}$"),
        for (int i = 0; i < 10; i++) {
            // Officially maps to AMFA Bank, not Nordax Finans.
            Assert.assertEquals("NDX", getBankPrefixFrom(String.format("964%d1234567", i)));
        }
    }

    @Test
    public void testNordnetRegexp() {
        // NON9100("NON9100", "NON", "^(910[0-9])\\d{7}$"),
        for (int i = 0; i < 10; i++) {
            Assert.assertEquals("NON", getBankPrefixFrom(String.format("910%d1234567", i)));
        }
    }

    @Test
    public void testPostgirotRegexp() {
        // PGBANK9500("PGBANK9500", "PGBANK", "^(95[0-4][0-9]|9960)\\d{10}$"),
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 10; j++) {
                String s = String.format("95%d%d1234567890", i, j);

                // Doesn't work, 9548 maps to EKOBANKEN.
                Assert.assertEquals(s, "PGBANK", getBankPrefixFrom(s));
            }
        }
        Assert.assertEquals("PGBANK", getBankPrefixFrom("99601234567890"));
    }

    @Test
    public void testResursBankRegexp() {
        // RB9280("RB9280", "RB", "^(928[0-9])\\d{7}$"),
        for (int i = 0; i < 10; i++) {
            Assert.assertEquals("RB", getBankPrefixFrom(String.format("928%d1234567", i)));
        }
    }

    @Test
    public void testRoayalBankOfScotlandRegexp() {
        // RBS9090("RBS9090", "RBS", "^(909[0-9])\\d{7}$"),
        for (int i = 0; i < 10; i++) {
            Assert.assertEquals("RBS", getBankPrefixFrom(String.format("909%d1234567", i)));
        }
    }

    @Test
    public void testRiksgaldenRegexp() {
        // RGK9880("RGK9880", "RGK", "^(988[0-9])\\d{7}$"),
        for (int i = 0; i < 10; i++) {
            Assert.assertEquals("RGK", getBankPrefixFrom(String.format("988%d1234567", i)));
        }
    }

    @Test
    public void testSBABRegexp() {
        // SBAB9250("SBAB9250", "SBAB", "^(925[0-9])\\d{7}$"),
        for (int i = 0; i < 10; i++) {
            Assert.assertEquals("SBAB", getBankPrefixFrom(String.format("925%d1234567", i)));
        }
    }

    @Test
    public void testSEBRegexp() {
        // SEB5000("SEB5000", "SEB", "^(5\\d{3}|91[3-4]\\d{1})\\d{7}$"),
        for (int i = 0; i < 1000; i++) {
            Assert.assertEquals("SEB", getBankPrefixFrom(String.format("5%03d1234567", i)));
        }
        for (int i = 3; i < 5; i++) {
            for (int j = 0; j < 10; j++) {
                Assert.assertEquals("SEB", getBankPrefixFrom(String.format("91%d%d1234567", i, j)));
            }
        }
    }

    @Test
    public void testSvenskaHandelsbankenRegexp() {
        // SHB6000("SHB6000", "SHB", "^6(?!044993|043363|043364|043365|043366|043367)\\d{11,12}$"),
        Assert.assertEquals("SHB", getBankPrefixFrom("612345678901"));
        Assert.assertEquals("SHB", getBankPrefixFrom("6123456789012"));
        // TODO: Could expand this with even more tests.
    }

    @Test
    public void testSkandiabankenRegexp() {
        // SKB9150("SKB9150", "SKB", "^(91[56][0-9])\\d{7}$"),
        for (int i = 5; i < 7; i++) {
            for (int j = 0; j < 10; j++) {
                Assert.assertEquals("SKB", getBankPrefixFrom(String.format("91%d%d1234567", i, j)));
            }
        }
    }

    @Test
    public void testSwedbankRegexp() {
        // SWED7000("SWED7000", "SWED", "^7\\d{10}$"),
        Assert.assertEquals("SWED", getBankPrefixFrom("70123456789"));

        // SWED8000("SWED8000", "SWED", "^8\\d{12,14}$"),
        Assert.assertEquals("SWED", getBankPrefixFrom("8012745678901"));
        Assert.assertEquals("SWED", getBankPrefixFrom("80127456789012"));
        Assert.assertEquals("SWED", getBankPrefixFrom("801274567890123"));

        // SWED9050("SWED9050", "SWED", "^(905[0-9])\\d{7}$"),
        for (int i = 0; i < 10; i++) {
            String s = String.format("905%d1234567", i);

            // Maps to HSB Bank, not Swedbank.
            Assert.assertEquals(s, "SWED", getBankPrefixFrom(s));
        }
    }

    @Test
    public void testSparbankenSydRegexp() {
        // SYD9570("SYD9570", "SYD", "^(957[0-9])(\\d{8}$|\\d{10})$"),
        for (int i = 0; i < 10; i++) {
            Assert.assertEquals("SYD", getBankPrefixFrom(String.format("957%d12345678", i)));
            Assert.assertEquals("SYD", getBankPrefixFrom(String.format("957%d1234567890", i)));
        }
    }

    @Test
    public void testAlandsbankenRegexp() {
        // ALAND2300("ALAND2300", "ÅLAND", "^(23\\d{2})\\d{7}$");
        for (int i = 0; i < 100; i++) {
            Assert.assertEquals("ÅLAND", getBankPrefixFrom(String.format("23%02d1234567", i)));
        }
    }

    private void testNordeaPrefix1(String prefix) {
        // NB3000("NB3000", "NB", "(^(0[1-9]|1[012])(0[1-9]|[12][0-9]|3[01])\\d{4}$)"),
        for (int i = 1; i < 10; i++) {
            testNordeaPrefix2(String.format("%s0%d", prefix, i));
        }
        for (int i = 0; i < 3; i++) {
            testNordeaPrefix2(String.format("%s1%d", prefix, i));
        }
    }

    private void testNordeaPrefix2(String prefix) {
        // (0[1-9]|[12][0-9]|3[01])\\d{4}
        for (int i = 1; i < 10; i++) {
            testNordeaPrefix3(String.format("%s0%d", prefix, i));
        }
        for (int i = 1; i < 3; i++) {
            for (int j = 0; j < 10; j++) {
                testNordeaPrefix3(String.format("%s%d%d", prefix, i, j));
            }
        }
        for (int i = 0; i < 2; i++) {
            testNordeaPrefix3(String.format("%s3%d", prefix, i));
        }
    }

    private void testNordeaPrefix3(String prefix) {
        String s = String.format("%s1234", prefix);
        Assert.assertEquals(s, "NB", getBankPrefixFrom(s));
    }

    private String getBankPrefixFrom(String account) {
        SwedishIdentifier identifier = new SwedishIdentifier(account);

        // Using a fallback to be able to get nice assertion output.
        return BankPrefix.fromAccountIdentifier(identifier);
    }
}
