package se.tink.libraries.payloadparser;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import org.junit.Test;

public class PayloadParserTest {

    private static final String PAYLOAD = "value1 value2";
    private static final String EXCEPTION_MSG = "No matching constructor found.";

    @Test
    public void parseWhenClassHasNoConstructorShouldThrowException() {
        // given

        // when
        Throwable t =
                catchThrowable(() -> PayloadParser.parse(PAYLOAD, PayloadNoConstructor.class));

        // then
        assertThat(t).isInstanceOf(PayloadParserException.class).hasMessage(EXCEPTION_MSG);
    }

    @Test
    public void parseWhenClassHasTooManyArgsConstructionShouldThrowException() {
        // given

        // when
        Throwable t = catchThrowable(() -> PayloadParser.parse(PAYLOAD, TooManyArgs.class));

        // then
        assertThat(t).isInstanceOf(PayloadParserException.class).hasMessage(EXCEPTION_MSG);
    }

    @Test
    public void parseWhenClassHasTooFewArgsConstructionShouldThrowException() {
        // given

        // when
        Throwable t = catchThrowable(() -> PayloadParser.parse(PAYLOAD, TooFewArgs.class));

        // then
        assertThat(t).isInstanceOf(PayloadParserException.class).hasMessage(EXCEPTION_MSG);
    }

    @Test
    public void parseWhenClassConstructorHasNonStringParamsShouldThrowException() {
        // given

        // when
        Throwable t = catchThrowable(() -> PayloadParser.parse(PAYLOAD, TypesNotMatch.class));

        // then
        assertThat(t).isInstanceOf(PayloadParserException.class).hasMessage(EXCEPTION_MSG);
    }

    @Test
    public void parseForMemberClassShouldThrowException() {
        // given

        // when
        Throwable t = catchThrowable(() -> PayloadParser.parse(PAYLOAD, MemberClass.class));

        // then
        assertThat(t)
                .isInstanceOf(PayloadParserException.class)
                .hasMessage("Could not instantiate member class. Choose non-member class.");
    }

    @Test
    public void parseWhenClassHasPublicConstructorShouldSucceed() {
        // given

        // when
        PayloadPublicConstructor result =
                PayloadParser.parse(PAYLOAD, PayloadPublicConstructor.class);

        // then
        assertThat(result.toString()).isEqualTo("ppca='value1', ppcb='value2");
    }

    @Test
    public void parseWhenClassHasPrivateConstructorShouldSucceed() {
        // given

        // when
        PayloadPrivateConstructor result =
                PayloadParser.parse(PAYLOAD, PayloadPrivateConstructor.class);

        // then
        assertThat(result.toString()).isEqualTo("pprca='value1', pprcb='value2");
    }

    static class MemberClass {
        private String mca;
        private String mcb;

        @Override
        public String toString() {
            return "mca='" + mca + '\'' + ", mcb='" + mcb;
        }
    }
}

class PayloadNoConstructor {
    private String pnca;
    private String pncb;

    @Override
    public String toString() {
        return "pnca='" + pnca + '\'' + ", pncb='" + pncb;
    }
}

class PayloadPublicConstructor {
    private String ppca;
    private String ppcb;

    public PayloadPublicConstructor(String a, String b) {
        this.ppca = a;
        this.ppcb = b;
    }

    @Override
    public String toString() {
        return "ppca='" + ppca + '\'' + ", ppcb='" + ppcb;
    }
}

class PayloadPrivateConstructor {
    private String pprca;
    private String pprcb;

    public PayloadPrivateConstructor(String a, String b) {
        this.pprca = a;
        this.pprcb = b;
    }

    @Override
    public String toString() {
        return "pprca='" + pprca + '\'' + ", pprcb='" + pprcb;
    }
}

class TooManyArgs {
    private String tmaa;
    private String tmab;
    private String tmac;

    public TooManyArgs(String a, String b, String c) {
        this.tmaa = a;
        this.tmab = b;
        this.tmac = c;
    }

    @Override
    public String toString() {
        return "wnoaa='" + tmaa + '\'' + ", wnoab='" + tmab + '\'' + ", wnoac='" + tmac;
    }
}

class TooFewArgs {
    private String tfaa;

    public TooFewArgs(String a) {
        this.tfaa = a;
    }

    @Override
    public String toString() {
        return "tfaa='" + tfaa;
    }
}

class TypesNotMatch {
    private String tnma;
    private String tnmb;

    public TypesNotMatch(String a, int b) {
        this.tnma = a;
        this.tnmb = String.valueOf(b);
    }

    @Override
    public String toString() {
        return "tnma='" + tnma + '\'' + ", tnmb='" + tnmb;
    }
}
