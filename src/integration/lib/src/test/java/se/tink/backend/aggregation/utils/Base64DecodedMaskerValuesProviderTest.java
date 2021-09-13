package se.tink.backend.aggregation.utils;

import com.google.api.client.repackaged.org.apache.commons.codec.binary.Base64;
import java.util.Arrays;
import java.util.Collections;
import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.aggregation.utils.masker.Base64DecodedMaskerValuesProvider;
import se.tink.backend.aggregation.utils.masker.StringMasker;

public class Base64DecodedMaskerValuesProviderTest {

    @Test
    public void whenBase64StringIsGivenItsDecodedFormMustBeIncluded() {

        // given
        String givenSecretInNormalForm = "secret";
        String givenSecretInBase64Form =
                Base64.encodeBase64String(givenSecretInNormalForm.getBytes());
        String givenStringLog = "{key: " + givenSecretInNormalForm + "}";

        // when
        final StringMasker masker = new StringMasker();
        final Base64DecodedMaskerValuesProvider provider =
                new Base64DecodedMaskerValuesProvider(
                        Collections.singletonList(givenSecretInBase64Form));
        masker.addValuesToMask(provider, p -> true);

        // then
        Assert.assertFalse(masker.getMasked(givenStringLog).contains(givenSecretInNormalForm));
    }

    @Test
    public void whenInvalidBase64StringIsGivenItShouldNotCrash() {

        // given
        String givenSecretInNormalForm = "secret";
        String givenSecretInBase64Form =
                Base64.encodeBase64String(givenSecretInNormalForm.getBytes());
        String givenStringLog = "{key: " + givenSecretInNormalForm + "}";
        String givenInvalidBase64Secret = "**";

        // when
        final StringMasker masker = new StringMasker();
        final Base64DecodedMaskerValuesProvider provider =
                new Base64DecodedMaskerValuesProvider(
                        Arrays.asList(givenSecretInBase64Form, givenInvalidBase64Secret));
        masker.addValuesToMask(provider, p -> true);

        // then
        Assert.assertFalse(masker.getMasked(givenStringLog).contains(givenSecretInNormalForm));
    }
}
