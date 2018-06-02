package se.tink.backend.connector.utils;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Optional;
import org.junit.Assert;
import org.junit.Test;

public class TagValidationUtilsTest {

    @Test
    public void asTagListProperlyReturnsStringList() {
        Optional<List<String>> optionalTagList = TagValidationUtils.asTagList(
                (Object) Lists.newArrayList("foo", "bar"));

        if (optionalTagList.isPresent()) {
            List<String> tagList = optionalTagList.get();
            Assert.assertEquals("First element in tag list correct", "foo", tagList.get(0));
        } else {
            Assert.fail("Expected list but got empty. Possible error in test");
        }
    }

    @Test
    public void asTagListThrowExceptionForNonListInput() {
        Assert.assertEquals(Optional.empty(), TagValidationUtils.asTagList("#foo"));
    }

    @Test
    public void asTagListThrowExceptionForNonStringList() {
        Assert.assertEquals(Optional.empty(), TagValidationUtils.asTagList(Lists.newArrayList(1, 2, 3, 4)));
    }

    @Test
    public void normalizeTagIllegalWhitespace() {
        Assert.assertEquals(Optional.empty(), TagValidationUtils.normalizeTag("abc def"));
        Assert.assertEquals(Optional.empty(), TagValidationUtils.normalizeTag("abc\ndef"));
    }

    @Test
    public void normalizeTagStripWhitespace() {
        Assert.assertEquals(Optional.of("foo"), TagValidationUtils.normalizeTag(" foo\n"));
    }

    @Test
    public void normalizeTagStripHashbang() {
        Assert.assertEquals(Optional.of("foo"), TagValidationUtils.normalizeTag("#foo"));
    }

    @Test
    public void normalizeTagRejectIllegalCharacters() {
        Assert.assertEquals(Optional.empty(), TagValidationUtils.normalizeTag("foo#bar"));
        Assert.assertEquals(Optional.empty(), TagValidationUtils.normalizeTag("👰🎂"));
    }

    @Test
    public void normalizeTagSuccessful() {
        Assert.assertEquals(Optional.of("1SmörgåsMacka"), TagValidationUtils.normalizeTag("1SmörgåsMacka"));
    }

    @Test
    public void normalizeTagListEqual() {
        List<String> list = Lists.newArrayList("#oxford", "#cornwall", "#leeds", "#kirkby");
        List<String> normalizedList = TagValidationUtils.normalizeTagList(list);
        Assert.assertEquals(Lists.newArrayList("#oxford", "#cornwall", "#leeds", "#kirkby"), normalizedList);
    }

    @Test
    public void normalizeTagListSuccessful() {
        List<String> normalizeList = TagValidationUtils.normalizeTagList(
                Lists.newArrayList("#oxford", " cornwall\n", "\tcornwall"));
        Assert.assertEquals(Lists.newArrayList("#oxford", "#cornwall"), normalizeList);
    }
}