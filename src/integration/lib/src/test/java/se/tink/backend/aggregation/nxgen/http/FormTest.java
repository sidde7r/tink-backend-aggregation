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
        final Form form = Form.builder().build();
        Assert.assertEquals(form.serialize(), "");
    }

    @Test(expected = NullPointerException.class)
    public void ensureExceptionIsThrown_whenKey_isNull() {
        Form.builder().put(null, "hoy").build();
    }

    @Test(expected = NullPointerException.class)
    public void ensureExceptionIsThrown_whenValue_isNull() {
        Form.builder().put("hoylaon", null).build();
    }

    @Test
    public void ensureBlankKeyAndValue() {
        final Form form = Form.builder()
                .put("", "")
                .build();
        Assert.assertEquals(form.serialize(), "="); // Disputable
    }

    @Test
    public void ensureBlankValuelessKey() {
        final Form form = Form.builder()
                .put("")
                .build();
        Assert.assertEquals(form.serialize(), ""); // Disputable
    }

    @Test
    public void ensureKeyValueForm() {
        final Form form = Form.builder()
                .put("coding", "fun")
                .build();
        Assert.assertEquals(form.serialize(), "coding=fun");
    }

    @Test
    public void ensureKeyValueForm2() {
        final Form form = Form.builder()
                .put("coding", "fun")
                .put("immutability", "good")
                .build();
        Assert.assertEquals(form.serialize(), "coding=fun&immutability=good");
    }

    @Test
    public void ensureValuelessForm() {
        final Form form = Form.builder()
                .put("compressResponse")
                .build();
        Assert.assertEquals(form.serialize(), "compressResponse");
    }

    @Test
    public void ensureValuelessRebuiltForm() {
        final Form form = Form.builder()
                .put("compressResponse")
                .build();
        Assert.assertEquals(form.serialize(), new Form.Builder(form).build().serialize());
    }

    @Test
    public void ensureCombinedForm() {
        final Form form = Form.builder()
                .put("coding", "fun")
                .put("compressResponse")
                .put("immutability", "good")
                .build();
        Assert.assertEquals(form.serialize(), "coding=fun&compressResponse&immutability=good");
    }

    @Test
    public void ensureRebuiltForm() {
        final Form form = Form.builder()
                .put("coding", "fun")
                .put("compressResponse")
                .put("immutability", "good")
                .build();

        Assert.assertEquals(form.serialize(), new Form.Builder(form).build().serialize());
    }

    @Test
    public void ensureRebuiltForm2() {
        final Form form1 = Form.builder()
                .put("coding", "fun")
                .put("compressResponse")
                .put("immutability", "good")
                .build();

        final Form form2 = new Form.Builder(form1)
                .put("immutability", "better")
                .put("compressResponse", "yes")
                .put("coding")
                .put("new thing", "shining")
                .put("new empty entry")
                .build();

        Assert.assertEquals(form2.serialize(), "coding&compressResponse=yes&immutability=better&new+thing=shining&new+empty+entry");
    }
}
