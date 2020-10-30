package se.tink.backend.aggregation.utils;

import com.google.common.collect.ImmutableList;
import java.util.Arrays;
import java.util.Collections;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.utils.masker.Base64Masker;
import se.tink.backend.aggregation.utils.masker.StringMasker;

public class Base64MaskerTest {

    private static final String MASK = "\\*\\*HASHED:[-A-Za-z0-9+/=]{2}\\*\\*";
    private static final Pattern MASK_PATTERN = Pattern.compile(MASK);
    private StringMasker masker;

    // Secrets with all possible alignments of 'length % 3'.
    // Secrets are numerical so that they can easily be found if left in the string.
    private final String MY_SECRET_1 = "123456";
    private final String MY_SECRET_2 = "1234567";
    private final String MY_SECRET_3 = "12345678";

    private final String TEXT_BLOB_1 = "randomtextwith%swhichshouldbemasked";
    private final String TEXT_BLOB_2 = "arandomtextwith%swhichshouldbemasked";
    private final String TEXT_BLOB_3 = "myrandomtextwith%swhichshouldbemasked";

    private final String TEXT_BEGINNING_SECRET = MY_SECRET_1 + "randomtext";
    private final String TEXT_ENDING_SECRET = "randomtext" + MY_SECRET_1;

    // Text blob with all secret versions with all alignment combinations
    ImmutableList<String> TEXT_BLOBS =
            ImmutableList.<String>builder()
                    .add(String.format(TEXT_BLOB_1, MY_SECRET_1))
                    .add(String.format(TEXT_BLOB_1, MY_SECRET_2))
                    .add(String.format(TEXT_BLOB_1, MY_SECRET_3))
                    .add(String.format(TEXT_BLOB_2, MY_SECRET_1))
                    .add(String.format(TEXT_BLOB_2, MY_SECRET_2))
                    .add(String.format(TEXT_BLOB_2, MY_SECRET_3))
                    .add(String.format(TEXT_BLOB_3, MY_SECRET_1))
                    .add(String.format(TEXT_BLOB_3, MY_SECRET_2))
                    .add(String.format(TEXT_BLOB_3, MY_SECRET_3))
                    .build();

    @Before
    public void setup() {

        Base64Masker maskerBuilder =
                new Base64Masker(Arrays.asList(MY_SECRET_1, MY_SECRET_2, MY_SECRET_3));

        masker = new StringMasker(Collections.singletonList(maskerBuilder));
    }

    @Test
    public void ensure_secretWithAnyAlignment_inBlobWithAnyAlignment_isMasked() {

        // Test all combinations of alignments for secrets and blobs.
        for (String blob : TEXT_BLOBS) {

            String maskedBase64Blob = masker.getMasked(Base64.encodeBase64String(blob.getBytes()));

            Assert.assertTrue(
                    "FAILED: Did not mask secret", MASK_PATTERN.matcher(maskedBase64Blob).find());

            String[] splitBlob = maskedBase64Blob.split(MASK);

            Assert.assertTrue(
                    "FAILED: Leaking secret start",
                    !containsSecret(
                            new String(
                                    Base64.decodeBase64(reconstructPaddingAfter(splitBlob[0])))));
            Assert.assertTrue(
                    "FAILED: Leaking secret end "
                            + new String(
                                    Base64.decodeBase64(reconstructPaddingBefore(splitBlob[1]))),
                    !containsSecret(
                            new String(
                                    Base64.decodeBase64(reconstructPaddingBefore(splitBlob[1])))));
        }
    }

    @Test
    public void ensure_blobBeginningWithSecret_isMasked() {

        String maskedBase64Blob =
                masker.getMasked(Base64.encodeBase64String(TEXT_BEGINNING_SECRET.getBytes()));

        Assert.assertTrue(
                "FAILED: Did not mask secret", MASK_PATTERN.matcher(maskedBase64Blob).find());

        String[] splitBlob = maskedBase64Blob.split(MASK);
        Assert.assertTrue(
                "FAILED: Leaking secret",
                !containsSecret(
                        new String(Base64.decodeBase64(reconstructPaddingBefore(splitBlob[0])))));
    }

    @Test
    public void ensure_blobEndingWithSecret_isMasked() {

        String maskedBase64Blob =
                masker.getMasked(Base64.encodeBase64String(TEXT_ENDING_SECRET.getBytes()));

        Assert.assertTrue(
                "FAILED: Did not mask secret", MASK_PATTERN.matcher(maskedBase64Blob).find());

        String[] splitBlob = maskedBase64Blob.split(MASK);
        Assert.assertTrue(
                "FAILED: Leaking secret",
                !containsSecret(
                        new String(Base64.decodeBase64(reconstructPaddingAfter(splitBlob[0])))));
    }

    @Test
    public void ensure_shortSecret_isIgnored() {

        Base64Masker maskerBuilder = new Base64Masker(Collections.singletonList("1234"));

        Assert.assertTrue(
                maskerBuilder.getValuesToMask().stream()
                        .map(Pattern::toString)
                        .collect(Collectors.toList())
                        .isEmpty());
    }

    // If any digits are found in the string we are leaking whole or part of secret.
    private boolean containsSecret(final String string) {
        return string.matches(".*\\d.*");
    }

    // Make valid b64 string by adding padding in beginning of string
    private String reconstructPaddingBefore(final String b64String) {

        int missingPadding = b64String.length() % 4;
        return StringUtils.repeat("0", missingPadding) + b64String;
    }

    // Make valid b64 string by adding padding at end of string
    private String reconstructPaddingAfter(final String b64String) {

        int missingPadding = b64String.length() % 4;
        return b64String + StringUtils.repeat("=", missingPadding);
    }
}
