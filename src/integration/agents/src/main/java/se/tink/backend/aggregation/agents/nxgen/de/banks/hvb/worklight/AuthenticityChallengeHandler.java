package se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.worklight;

import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.utils.encoding.EncodingUtils;

public final class AuthenticityChallengeHandler {
    private static final Logger logger =
            LoggerFactory.getLogger(AuthenticityChallengeHandler.class);

    private AuthenticityChallengeHandler() {
        throw new AssertionError();
    }

    public static String challengeToAuthenticityRealmString(
            final String wlChallengeData, final String moduleName, final String appId) {
        final ImmutableList<String> strings =
                ImmutableList.copyOf(wlChallengeData.split("\\+")); // Assert len == 2
        final String firstString = strings.get(0);

        final List<Byte> buffer = new ArrayList<>(); // TODO better alternative?

        int numberOfBytes = 0;
        for (int i = 0; i < firstString.length(); ++i) {
            final int firstNumber = Integer.parseInt(firstString.substring(i, i + 3));
            i += 3;
            final int secondNumber = Integer.parseInt(firstString.substring(i, i + 3));
            i += 3;
            final char seventhChar = firstString.charAt(i);
            switch (seventhChar) {
                case 'N':
                    numberOfBytes =
                            cda98(buffer, moduleName, numberOfBytes, firstNumber, secondNumber);
                    break;
                case 'X':
                    {
                        final String substring = cda10(i + 1, firstString);
                        for (int j = 0; j < numberOfBytes; ++j) {
                            buffer.set(
                                    j,
                                    (byte)
                                            (buffer.get(j)
                                                    ^ substring.charAt(
                                                            moduloesque(j, substring.length()))));
                        }
                        i += substring.length() + 1;
                        break;
                    }
                case 'C':
                    numberOfBytes = cda98(buffer, appId, numberOfBytes, firstNumber, secondNumber);
                    break;
                default:
                    logger.error(
                            "Found unexpected AuthenticityChallenge character: {}", seventhChar);
                    break;
            }
        }

        final String bsodString = bsod(buffer);
        return "i" + bsodString;
    }

    private static int cda98(
            final List<Byte> buffer,
            final String staticName,
            final int numberOfBytes,
            final int firstNumber,
            final int secondNumber) {
        final int length = staticName.length();
        final int a;
        final int b;
        if (moduloesque(firstNumber, length) > moduloesque(secondNumber, length)) {
            a = moduloesque(firstNumber, length);
            b = moduloesque(secondNumber, length);
        } else {
            b = moduloesque(firstNumber, length);
            a = moduloesque(secondNumber, length);
        }
        int newNumberOfBytes = numberOfBytes;
        if (a < staticName.length()) {
            newNumberOfBytes += a - b + 1;
            buffer.addAll(
                    Arrays.asList(ArrayUtils.toObject(staticName.substring(b, a + 1).getBytes())));
        }
        return newNumberOfBytes;
    }

    private static int moduloesque(final int n, final int m) {
        return n - n / m * m;
    }

    private static String cda10(final int offset, final String firstString) {
        final int sIndex = firstString.indexOf('S', offset);
        return firstString.substring(offset, sIndex);
    }

    private static String bsod(final List<Byte> buffer) {
        final Byte[] bufferArray = new Byte[buffer.size()];
        buffer.toArray(bufferArray);
        return EncodingUtils.encodeAsBase64String(ArrayUtils.toPrimitive(bufferArray));
    }
}
