package se.tink.backend.aggregation.nxgen.http;

import org.junit.Assert;
import org.junit.Test;

public final class FormTest {
    @Test
    public void ensureDefaultConstructor_yieldsEmptyString() {
        final Form form = new Form();
        Assert.assertEquals(form.serialize(), "");
    }

    @Test
    public void ensureEmptyForm_yieldsEmptyString() {
        final Form form = new Form.Builder().build();
        Assert.assertEquals(form.serialize(), "");
    }

    @Test(expected = NullPointerException.class)
    public void ensureExceptionIsThrown_whenKey_isNull() {
        new Form.Builder().put(null, "hoy").build();
    }

    @Test(expected = NullPointerException.class)
    public void ensureExceptionIsThrown_whenValue_isNull() {
        new Form.Builder().put("hoylaon", null).build();
    }

    @Test
    public void ensureBlankKeyAndValue() {
        final Form form = new Form.Builder()
                .put("", "")
                .build();
        Assert.assertEquals(form.serialize(), "="); // Disputable
    }

    @Test
    public void ensureBlankValuelessKey() {
        final Form form = new Form.Builder()
                .put("")
                .build();
        Assert.assertEquals(form.serialize(), ""); // Disputable
    }

    @Test
    public void ensureKeyValueForm() {
        final Form form = new Form.Builder()
                .put("coding", "fun")
                .build();
        Assert.assertEquals(form.serialize(), "coding=fun");
    }

    @Test
    public void ensureKeyValueForm2() {
        final Form form = new Form.Builder()
                .put("coding", "fun")
                .put("immutability", "good")
                .build();
        Assert.assertEquals(form.serialize(), "coding=fun&immutability=good");
    }

    @Test
    public void ensureValuelessForm() {
        final Form form = new Form.Builder()
                .put("compressResponse")
                .build();
        Assert.assertEquals(form.serialize(), "compressResponse");
    }

    @Test
    public void ensureCombinedForm() {
        final Form form = new Form.Builder()
                .put("coding", "fun")
                .put("compressResponse")
                .put("immutability", "good")
                .build();
        Assert.assertEquals(form.serialize(), "coding=fun&compressResponse&immutability=good");
    }
}
