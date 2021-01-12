package se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.helper;

import static org.junit.Assert.*;

import java.util.Arrays;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.persistence.FortisProcessState;
import se.tink.backend.aggregation.agents.utils.encoding.EncodingUtils;

@RunWith(Parameterized.class)
public class FortisKeyDecryptorTest {

    private final String encCredentials;
    private final String encryptionKey;
    private final String smsOtp;
    private final String enrollmentSessionId;
    private final String expectedKey;

    public FortisKeyDecryptorTest(
            String encCredentials,
            String encryptionKey,
            String smsOtp,
            String enrollmentSessionId,
            String expectedKey) {
        this.encCredentials = encCredentials;
        this.encryptionKey = encryptionKey;
        this.smsOtp = smsOtp;
        this.enrollmentSessionId = enrollmentSessionId;
        this.expectedKey = expectedKey;
    }

    @Parameterized.Parameters
    public static Iterable<Object[]> data() {
        return Arrays.asList(
                new Object[][] {
                    {
                        "vIi4kU28kCSzVji40RoE6j8Ory0P4lZUj+Ki3nPOmJi0T0vKos69g9Hgqay/33s6TVT19z9HVEgNKr/D7LbJXFOj8crG4chNGSjYkBRTdfV7OnnD8HEPn9bw+SN3S5RpYcIG7wOi9/0Tmoah6rT2uZPtOCHZLlIbFBP5p80ynCs=",
                        "4948d55db654bdafb086b8aa42216cc4",
                        "35008",
                        "7dec1281-dc8d-45bf-9aca-b0f275dca3e7",
                        "fdd8fdef91bf1a701a0cab34fcee26b62f4f09da8d6a9a682272c71b4391a94c86a20a329942d924541bba320e531cd52271bd2eb2120e2cc649907c388e56b0"
                    },
                    {
                        "RbOX4Y+W0SxyDxl7lvYT5ZLdlaTvj1u2xfaIeF2eU0mjw9M8cnqZfP03L+LgGtFyRwLSOo7ot7ojHWXvQxcCIP/Ltrt0OAbdQp3Bj7/RNtNpvMjMJjC6PsdxcR9CEXB8n8EUKPGISaaEP5yDKnTP1YT1yEOt5PVFmS+BShLtdxA=",
                        "c1ae13818456c6f3f870fb02b254a8f1",
                        "51594",
                        "803112e4-d58c-4b07-ba17-54b90ee00bd2",
                        "ce78d86e67c92891481b24576f4a2fe4a99bdc4a53237500d5ecbcb15a8ff189574d13085bfffea33d4e1ad074009f7a7e81c3e8dac20a3762ae5378b60a85db"
                    },
                    {
                        "YHvUgYPOM2zeo3ypBAKZ+tmAukQpYYdoXWq5PSWlYPZpWJtlXkgeT7Gpl+6SDgPT6hq4CsPy3qCARJpB746GeTg+JRQHhtE43XuspDPN8P3bJuDpTPGh5j+qVBrCCfS6G8LS1apwlg0ypnx7voYoLN400tKuZeSu57Es6Trl6AI=",
                        "d2a8a89ca8b8311ba5694e569c8d768f",
                        "44240",
                        "a44c119f-3b5c-4b42-a033-04c7b7865d92",
                        "3fec23a4d9829fa1ddc253f5815fdb6356452d28a891d18d606b14fba22c1be663305fb1b87d4cee653e724b93d9141a4d0233fb3c571403b52a256ebf1203b4"
                    },
                    {
                        "c8SQOF+2I2yEX/UkNKOuOsa8fJyBeUjIV3E+TQLihgClwOzPb3mbRdNGyWensdiVVx0TKQipvxGJ9GrbSjEsWejkmOPbxY22eiJC+yvBWqrNpsJ+70naRWUjQK9mML2X4j2s4ohPHt7XwhDE5CYhWt39IPz2qZ0s0cmNIBGYu4M=",
                        "aa414f30b34780b14c2bb7f644fb8a87",
                        "82069",
                        "b5df309a-ed43-4c1e-826f-21e4afb5db5b",
                        "c375ac4a7ea566c50d29694792b504041d97f2d1a2b1e11fbcebf298b8cb0e8c9e572e9a96d89d12a10911689941628af160c1d4713f1eeceb6a06af23adf5a2"
                    },
                    {
                        "K6ewVZy2SNVzIryg4kGIxnhYdpmzGh6ffuGboRmCwjIM135kKeBvLjQmar6UYoB164EOzd+nLnxkXHouZrT80e8yqxkyf4hLvuwF6Gnxsm75ye1TWR3F7hrQmlhz/f8SVDZvD6Si24VzlzmeBLrIYLEzoW5Qa3ho4rtX0XuhKj8=",
                        "f967173db161f980bf56107dcfca6d17",
                        "01345",
                        "e0328c9c-2358-4414-a1e8-e7cc8d6c9a83",
                        "3477d887094bf23d980c52880bff79748b1e9187c5c7534750dd9644bc31bf25bbefb27b1dc0046e8263a180243225f2231b6393ff4333bcc3537f023e208a71"
                    },
                    {
                        "XSPnRMM1UkQ4n0qd8I+Jj1fhvxxgu4kuzSwJPIStYC6lewOARmq2paOkqJDWqUbxVAlyAYpQtOFH6Z85t70ItsRUJpf5tWJig3NuONhgd6UCTSR0E85etBA3gPH/B4sAVicS6ck8vSCXkAmle/l22iJMRtSI972LhNoEOlZwvrU=",
                        "e51c14144ad91291f47ebf743b58322a",
                        "45938",
                        "ed93726e-14a7-4c07-abba-593b793da575",
                        "ba9acca551559353e50623899553a934613465aeb2a9769e7bb1cb03356020e4cf51d14d058ec3c2fbb171dfa4c02d7004e622449d6b50d5a5bda4ccd4f890e2"
                    }
                });
    }

    @Test
    public void decryptKey() {
        FortisProcessState processState = new FortisProcessState();
        processState.setEncCredentials(encCredentials);
        processState.setEncryptionKey(encryptionKey);
        processState.setSmsOtp(smsOtp);
        processState.setEnrollmentSessionId(enrollmentSessionId);

        byte[] bytes = FortisKeyDecryptor.decryptKey(processState);

        assertEquals(expectedKey, EncodingUtils.encodeHexAsString(bytes));
    }
}
