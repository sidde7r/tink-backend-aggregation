package se.tink.backend.aggregation.agents.banks.sbab.util;

import com.google.common.base.Charsets;
import java.util.Optional;
import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import org.jsoup.Jsoup;
import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.aggregation.agents.banks.sbab.model.response.QuestionGroupResponse;
import se.tink.backend.aggregation.agents.exceptions.application.InvalidApplicationException;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class SBABOpenSavingsAccountUtilsTest {

    @Test
    public void parseQuestionGroups_ReturnsCorrectSize() {
        String javascript = "var frageGrupper=[{}, {}, {}];";
        Optional<QuestionGroupResponse> questionGroupResponse = SBABOpenSavingsAccountUtils
                .parseQuestionGroups(javascript);
        Assert.assertTrue(questionGroupResponse.isPresent());
        Assert.assertTrue(questionGroupResponse.get().getQuestionGroups().size() == 3);
    }

    /**
     * The question groups (with questions + answer alternatives) which we get from SBAB must match what we expect.
     * Otherwise, we might send incorrect values to the bank which do not correspond with the information that the
     * customer supplied in the app.
     */
    @Test
    public void questionGroups_NothingChanged_ReturnsValid() throws IOException {
        String fileName = "data/agents/sbab/question-groups-input.json";
        String incomingGroups = Files.toString(new File(fileName), Charsets.UTF_8);

        QuestionGroupResponse questionGroupResponse = SerializationUtils
                .deserializeFromString(incomingGroups, QuestionGroupResponse.class);
        boolean questionGroupsAreValid = SBABOpenSavingsAccountUtils
                .validateQuestions(questionGroupResponse.getQuestionGroups());

        Assert.assertTrue(questionGroupsAreValid);
    }

    /**
     * This uses a test case where one question group id (111) has been changed to a new value (1).
     *
     * Expected: log error.
     */
    @Test
    public void questionGroups_ChangedGroupId_ReturnsNotValid() throws IOException {
        String fileName = "data/agents/sbab/test/new-savings-account/question-groups-test-changed-invalid-input-1.json";
        String incomingGroups = Files.toString(new File(fileName), Charsets.UTF_8);

        QuestionGroupResponse questionGroupResponse = SerializationUtils
                .deserializeFromString(incomingGroups, QuestionGroupResponse.class);
        boolean questionGroupsAreValid = SBABOpenSavingsAccountUtils
                .validateQuestions(questionGroupResponse.getQuestionGroups());

        Assert.assertFalse(questionGroupsAreValid);
    }

    /**
     * This uses a test case where an unknown answer alternative with id = 0 has been added.
     *
     * Expected: log error.
     */
    @Test
    public void questionGroups_AddedNewCountry_ReturnsNotValid() throws IOException {
        String fileName = "data/agents/sbab/test/new-savings-account/question-groups-test-changed-invalid-input-2.json";
        String incomingGroups = Files.toString(new File(fileName), Charsets.UTF_8);

        QuestionGroupResponse questionGroupResponse = SerializationUtils
                .deserializeFromString(incomingGroups, QuestionGroupResponse.class);
        boolean questionGroupsAreValid = SBABOpenSavingsAccountUtils
                .validateQuestions(questionGroupResponse.getQuestionGroups());

        Assert.assertFalse(questionGroupsAreValid);
    }

    /**
     * This uses a test case where the question group with id = 200 has been taken away.
     *
     * Expected: log error.
     */
    @Test
    public void questionGroups_OneGroupRemoved_ReturnsNotValid() throws IOException {
        String fileName = "data/agents/sbab/test/new-savings-account/question-groups-test-changed-invalid-input-3.json";
        String incomingGroups = Files.toString(new File(fileName), Charsets.UTF_8);

        QuestionGroupResponse questionGroupResponse = SerializationUtils
                .deserializeFromString(incomingGroups, QuestionGroupResponse.class);
        boolean questionGroupsAreValid = SBABOpenSavingsAccountUtils
                .validateQuestions(questionGroupResponse.getQuestionGroups());

        Assert.assertFalse(questionGroupsAreValid);
    }

    /**
     * This uses a test case where a question type value has been changed. This is okay since it's nothing
     * we check for or use.
     */
    @Test
    public void questionGroups_ChangedQuestionTypeValue_ReturnsValid() throws IOException {
        String fileName = "data/agents/sbab/test/new-savings-account/question-groups-test-changed-valid-input-1.json";
        String incomingGroups = Files.toString(new File(fileName), Charsets.UTF_8);

        QuestionGroupResponse questionGroupResponse = SerializationUtils
                .deserializeFromString(incomingGroups, QuestionGroupResponse.class);
        boolean questionGroupsAreValid = SBABOpenSavingsAccountUtils
                .validateQuestions(questionGroupResponse.getQuestionGroups());

        Assert.assertTrue(questionGroupsAreValid);
    }

    /**
     * This uses a test case where a description text has been changed. This is okay since it's nothing
     * we check for or use.
     */
    @Test
    public void questionGroups_ChangedDescriptionText_ReturnsValid() throws IOException {
        String fileName = "data/agents/sbab/test/new-savings-account/question-groups-test-changed-valid-input-2.json";
        String incomingGroups = Files.toString(new File(fileName), Charsets.UTF_8);

        QuestionGroupResponse questionGroupResponse = SerializationUtils
                .deserializeFromString(incomingGroups, QuestionGroupResponse.class);
        boolean questionGroupsAreValid = SBABOpenSavingsAccountUtils
                .validateQuestions(questionGroupResponse.getQuestionGroups());

        Assert.assertTrue(questionGroupsAreValid);
    }

    /**
     * This uses a test case where a field 'sorteringsordning' has an updated value. This is okay since it's nothing
     * we check for or use.
     */
    @Test
    public void questionGroups_ChangedUnusedField_ReturnsValid() throws IOException {
        String fileName = "data/agents/sbab/test/new-savings-account/question-groups-test-changed-valid-input-3.json";
        String incomingGroups = Files.toString(new File(fileName), Charsets.UTF_8);

        QuestionGroupResponse questionGroupResponse = SerializationUtils
                .deserializeFromString(incomingGroups, QuestionGroupResponse.class);
        boolean questionGroupsAreValid = SBABOpenSavingsAccountUtils
                .validateQuestions(questionGroupResponse.getQuestionGroups());

        Assert.assertTrue(questionGroupsAreValid);
    }

    @Test(expected = InvalidApplicationException.class)
    public void initialRequestResponseWithError_ThrowsException() throws IOException, InvalidApplicationException {
        String fileName = "data/agents/sbab/test/new-savings-account/initial-request-error.html";
        String htmlResponse = Files.toString(new File(fileName), Charsets.UTF_8);
        SBABOpenSavingsAccountUtils.checkForInitialRequestErrors(Jsoup.parse(htmlResponse));
    }

    @Test
    public void initialRequestResponseWithoutError_DoesNotThrowException()
            throws IOException, InvalidApplicationException {
        String htmlResponse = "<html></html>";
        SBABOpenSavingsAccountUtils.checkForInitialRequestErrors(Jsoup.parse(htmlResponse));
    }

    @Test(expected = InvalidApplicationException.class)
    public void initialRequestResponseWithGeneralError_ThrowsException()
            throws IOException, InvalidApplicationException {
        String fileName = "data/agents/sbab/test/new-savings-account/initial-request-error-2.html";
        String htmlResponse = Files.toString(new File(fileName), Charsets.UTF_8);
        SBABOpenSavingsAccountUtils.checkForInitialRequestErrors(Jsoup.parse(htmlResponse));
    }
}
