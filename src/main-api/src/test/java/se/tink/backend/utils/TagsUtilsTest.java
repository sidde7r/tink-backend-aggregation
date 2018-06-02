package se.tink.backend.utils;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Test;
import org.junit.runner.RunWith;
import se.tink.backend.core.Transaction;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(JUnitParamsRunner.class)
public class TagsUtilsTest {

    @Test
    public void transactionWithOneTag() throws Exception {

        Transaction transaction = new Transaction();
        transaction.setNotes("This is with a #tag");

        assertThat(TagsUtils.hasTags(transaction)).isTrue();
        assertThat(TagsUtils.extractUniqueTags(transaction)).containsExactly("tag");
    }

    @Test
    public void transactionWithMultipleTags() throws Exception {

        Transaction transaction = new Transaction();
        transaction.setNotes("This is with a #tag #tag #and");

        assertThat(TagsUtils.hasTags(transaction)).isTrue();
        assertThat(TagsUtils.extractUniqueTags(transaction)).containsExactly("tag", "and");
    }

    @Test
    public void transactionWithoutTags() throws Exception {

        Transaction transaction = new Transaction();
        transaction.setNotes("This is without tag");

        assertThat(TagsUtils.hasTags(transaction)).isFalse();
        assertThat(TagsUtils.extractUniqueTags(transaction)).isEmpty();
    }

    @Test
    public void transactionWithoutNotes() throws Exception {

        Transaction transaction = new Transaction();
        transaction.setNotes(null);

        assertThat(TagsUtils.hasTags(transaction)).isFalse();
        assertThat(TagsUtils.extractUniqueTags(transaction)).isEmpty();
    }

    @Test
    public void transactionsWithMultipleTags() throws Exception {

        Transaction transaction1 = new Transaction();
        transaction1.setNotes("This is with #tag2 #common");

        Transaction transaction2 = new Transaction();
        transaction2.setNotes("This is with #tag1 #common");

        assertThat(TagsUtils.hasTags(transaction1)).isTrue();
        assertThat(TagsUtils.hasTags(transaction2)).isTrue();
        assertThat(TagsUtils.extractUniqueTags(ImmutableList.of(transaction1, transaction2)))
                .containsOnly("tag1", "tag2", "common");
    }

    @Test
    public void removeTagsIgnoreCase() {
        String note = "#IgnoreCase #sameCase #tag  #";
        Set<String> tags = Sets.newHashSet("IGNOREcase", "sameCase", "Tags");
        String result = TagsUtils.removeTags(note, tags);
        String expected = " #tag  #";
        assertThat(result).isEqualTo(expected);
    }

    @Test
    public void notRemoveTagsForEmptyTagSet() {
        String note = "#tag1 #tag2 #tag3  #";
        Set<String> tags = Collections.emptySet();
        String result = TagsUtils.removeTags(note, tags);
        String expected = "#tag1 #tag2 #tag3  #";
        assertThat(result).isEqualTo(expected);
    }

    @Test
    public void testBuildNoteWithTags() {
        String note = "This is my awesome note and it's longer than usual.";
        List<String> tags = Lists.newArrayList();
        tags.add("tag1");
        tags.add("tag2");
        tags.add("tag3");
        tags.add("tag4");
        tags.add("tag5");
        tags.add("tag6");
        assertThat(TagsUtils.buildNote(note, tags)).isEqualTo("This is my awesome note and it's longer than usual. #tag1 #tag2 #tag3 #tag4 #tag5 #tag6");
    }

    @Test
    public void buildNotesWithSameWordsInTagsAndNotes() throws Exception {
        String note1 = "This note contains a tag called";
        List<String> tags1 = Lists.newArrayList("tag");

        String note2 = "This note contains tag1 called #tag1 and tag2 called";
        List<String> tags2 = Lists.newArrayList("tag1", "tag2");

        // We allow this, since we always extract unique tags from the notes.
        String note3 = "This note contains tag1 twice, here #tag1 and here #tag1, and sets tag2 but not tag1 here";
        List<String> tags3 = Lists.newArrayList("tag1", "tag2");

        assertThat(TagsUtils.buildNote(note1, tags1)).isEqualTo("This note contains a tag called #tag");
        assertThat(TagsUtils.buildNote(note2, tags2)).isEqualTo("This note contains tag1 called #tag1 and tag2 called #tag2");
        assertThat(TagsUtils.buildNote(note3, tags3 )).isEqualTo("This note contains tag1 twice, here #tag1 and here #tag1, and sets tag2 but not tag1 here #tag2");
    }

    @Test
    public void testExtractingTags() {
        Transaction t1 = new Transaction();
        t1.setNotes("This is a note with #tag1 #tag2");

        List<String> tags = Lists.newArrayList();
        tags.add("tag1");
        tags.add("tag2");
        assertThat(TagsUtils.extractUniqueTags(t1)).isEqualTo(tags);
    }

    @Test
    public void testExtractEmptyTagsList() {
        Transaction t1 = new Transaction();
        t1.setNotes("This is a note with");

        assertThat(TagsUtils.extractUniqueTags(t1).isEmpty()).isTrue();
    }

    @Test
    public void testExtractNote() {
        Transaction t1 = new Transaction();
        t1.setNotes("This is a note with #tag1 #tag2");

        String note = "This is a note with";
        assertThat(TagsUtils.removeTrailingTags(t1.getNotes())).isEqualTo(note);
    }

    @Test
    public void testNoteStringWithTagInBetween() {
        String note = "This is a note with a #tag in between";
        assertThat(TagsUtils.removeTrailingTags(note)).isEqualTo("This is a note with a #tag in between");
    }

    @Test
    public void testNoteTagsWithTagInBetween() {
        String note = "This is a note with a #tag in between";
        List<String> tags = Lists.newArrayList();
        tags.add("tag");
        assertThat(TagsUtils.extractUniqueTags(note)).isEqualTo(tags);
    }

    @Test
    public void testComplexTag() {
        String note = "This is a note with a #complex_tag_here";
        List<String> tags = Lists.newArrayList();
        tags.add("complex_tag_here");
        assertThat(TagsUtils.extractUniqueTags(note)).isEqualTo(tags);
    }

    @Test
    public void testTheFroggeCase() {
        String note = "This is a #note with #tags #that-xxxxx #with_underscore";
        List<String> tags = Lists.newArrayList();
        tags.add("note");
        tags.add("tags");
        tags.add("that-xxxxx");
        tags.add("with_underscore");
        List<String> extracted = TagsUtils.extractUniqueTags(note);
        assertThat(TagsUtils.extractUniqueTags(note)).isEqualTo(tags);
    }

    @Test
    public void testNotAddTagAtTheEndOfNoteIfItsInNote() {
        String note = "aaa #bbb ccc";
        List<String> expectedTags = Lists.newArrayList();
        expectedTags.add("bbb");


        List<String> resultTags = TagsUtils.extractUniqueTags(note);
        assertThat(resultTags).isEqualTo(expectedTags);

        String resultNote = TagsUtils.buildNote(note, expectedTags);
        assertThat(resultNote).isEqualTo(note);
    }

    @Test
    public void testEmptyExtractNote() {
        Transaction t1 = new Transaction();
        t1.setNotes("#tag1 #tag2");

        assertThat(Strings.isNullOrEmpty(TagsUtils.removeTrailingTags(t1.getNotes()))).isTrue();
    }
}
