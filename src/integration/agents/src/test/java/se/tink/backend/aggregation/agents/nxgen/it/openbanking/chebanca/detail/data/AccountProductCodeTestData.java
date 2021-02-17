package se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.detail.data;

import java.util.Arrays;
import java.util.List;
import org.junit.Ignore;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.libraries.serialization.utils.SerializationUtils;

@Ignore
public class AccountProductCodeTestData {
    public static List<String> getCreditCardCodes() {
        return Arrays.asList("CAPC", "CA30");
    }

    public static List<String> getSavingsCodes() {
        return Arrays.asList("CDEP", "IMPB");
    }

    public static List<String> getCheckingCodes() {
        return Arrays.asList(
                "MPAC", "MPAZ", "MPBC", "MPBL", "MPFC", "MPFU", "MPGC", "MPGI", "MPGR", "MPRC",
                "MPRO", "MPVC", "MPVE", "MPYC", "MP0C", "MP00", "AZIM", "0600", "0601", "0603",
                "0604", "0608", "0631", "0632", "0633", "0641", "0642", "0643", "0644", "0645",
                "0646", "0647", "0648", "0649", "0650", "0651", "0652", "0653", "0654", "0655",
                "0656", "0657", "0658", "0665", "0666", "0694", "0695", "0697", "0698");
    }

    public static AccountEntity getCheckingAccountEntity(String accountProductCode) {
        return SerializationUtils.deserializeFromString(
                "{\n"
                        + "  \"accountId\": \"12345\",\n"
                        + "  \"product\": {\n"
                        + "      \"code\": \""
                        + accountProductCode
                        + "\",\n"
                        + "      \"description\": \"CONTO YELLOW\"\n"
                        + "  },\n"
                        + "  \"currency\": \"EUR\",\n"
                        + "  \"iban\": \"IT04G0305801604100571657883\",\n"
                        + "  \"name\": \"CHECKING ACC\"\n"
                        + "            }",
                AccountEntity.class);
    }

    public static AccountEntity getSavingsAccountEntity(String accountProductCode) {
        return SerializationUtils.deserializeFromString(
                "{\n"
                        + "  \"accountId\": \"12345\",\n"
                        + "  \"product\": {\n"
                        + "      \"code\": \""
                        + accountProductCode
                        + "\",\n"
                        + "      \"description\": \"CONTO DEP\"\n"
                        + "  },\n"
                        + "  \"currency\": \"EUR\",\n"
                        + "  \"iban\": \"IT04G0305801604100571657883\",\n"
                        + "  \"name\": \"SAVINGS ACC\"\n"
                        + "            }",
                AccountEntity.class);
    }

    public static AccountEntity getCreditCardAccountEntity(String accountProductCode) {
        return SerializationUtils.deserializeFromString(
                "{\n"
                        + "  \"accountId\": \"12345\",\n"
                        + "  \"product\": {\n"
                        + "      \"code\": \""
                        + accountProductCode
                        + "\",\n"
                        + "      \"description\": \"CONTO DEP\"\n"
                        + "  },\n"
                        + "  \"currency\": \"EUR\",\n"
                        + "  \"iban\": \"IT04G0305801604100571657883\",\n"
                        + "  \"name\": \"SAVINGS ACC\"\n"
                        + "            }",
                AccountEntity.class);
    }
}
