package se.tink.backend.aggregation.agents.nxgen.es.banks.wizink;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.utils.WizinkDecoder;

public class WizinkDecoderTest {

    @Test
    public void shouldDecodeMaskedCardNumber() {
        // given
        String maskedCardNumber =
                "MjBmNTkzOTQ4NTZmZGYwM2FkZmNmOGQwNTNjNTYzODQ2YzJkNmJiYzM4NmJhOGIxOTI4YjJkZGRkMjA0ZGMxNA\\u003d\\u003d";
        String xTokenUserHeader =
                "20F59394856FDF03ADFCF8D053EF49AE460D41961241889BB8A107FDE036E820";

        // when
        String decodedMaskedCardNumber =
                WizinkDecoder.decodeMaskedCardNumber(maskedCardNumber, xTokenUserHeader);

        // then
        assertThat(decodedMaskedCardNumber).isEqualTo("**** **** **** 2244");
    }
}
