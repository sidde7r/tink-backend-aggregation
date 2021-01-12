package se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.helper;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import se.tink.backend.aggregation.agents.utils.encoding.EncodingUtils;

@RunWith(Parameterized.class)
public class FortisOTPCalculatorTest {

    private final String ocraKey;
    private final String enrollmentSessionId;
    private final Long currentTime;
    private final String expectedOtp;

    public FortisOTPCalculatorTest(
            String ocraKey, String enrollmentSessionId, Long currentTime, String otp) {
        this.ocraKey = ocraKey;
        this.enrollmentSessionId = enrollmentSessionId;
        this.currentTime = currentTime;
        this.expectedOtp = otp;
    }

    @Parameterized.Parameters
    public static Iterable<Object[]> data() {
        return Arrays.asList(
                new Object[][] {
                    {
                        "fdd8fdef91bf1a701a0cab34fcee26b62f4f09da8d6a9a682272c71b4391a94c86a20a329942d924541bba320e531cd52271bd2eb2120e2cc649907c388e56b0",
                        "7dec1281-dc8d-45bf-9aca-b0f275dca3e7",
                        1607608894L,
                        "68222843"
                    },
                    {
                        "ce78d86e67c92891481b24576f4a2fe4a99bdc4a53237500d5ecbcb15a8ff189574d13085bfffea33d4e1ad074009f7a7e81c3e8dac20a3762ae5378b60a85db",
                        "803112e4-d58c-4b07-ba17-54b90ee00bd2",
                        1608122935L,
                        "39061751"
                    },
                    {
                        "3fec23a4d9829fa1ddc253f5815fdb6356452d28a891d18d606b14fba22c1be663305fb1b87d4cee653e724b93d9141a4d0233fb3c571403b52a256ebf1203b4",
                        "a44c119f-3b5c-4b42-a033-04c7b7865d92",
                        1608126751L,
                        "60079598"
                    },
                    {
                        "c375ac4a7ea566c50d29694792b504041d97f2d1a2b1e11fbcebf298b8cb0e8c9e572e9a96d89d12a10911689941628af160c1d4713f1eeceb6a06af23adf5a2",
                        "b5df309a-ed43-4c1e-826f-21e4afb5db5b",
                        1608129385L,
                        "05050265"
                    },
                    {
                        "3477d887094bf23d980c52880bff79748b1e9187c5c7534750dd9644bc31bf25bbefb27b1dc0046e8263a180243225f2231b6393ff4333bcc3537f023e208a71",
                        "e0328c9c-2358-4414-a1e8-e7cc8d6c9a83",
                        1608131602L,
                        "12420966"
                    },
                    {
                        "ba9acca551559353e50623899553a934613465aeb2a9769e7bb1cb03356020e4cf51d14d058ec3c2fbb171dfa4c02d7004e622449d6b50d5a5bda4ccd4f890e2",
                        "ed93726e-14a7-4c07-abba-593b793da575",
                        1608189614L,
                        "73659574"
                    }
                });
    }

    @Test
    public void calculateOTP() {
        String challenge =
                EncodingUtils.encodeHexAsString(enrollmentSessionId.replace("-", "").getBytes());
        String resultOtp = FortisOTPCalculator.calculateOTP(ocraKey, challenge, currentTime);
        assertEquals(expectedOtp, resultOtp);
    }
}
