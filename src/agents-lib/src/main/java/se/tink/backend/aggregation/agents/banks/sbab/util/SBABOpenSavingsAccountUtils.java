package se.tink.backend.aggregation.agents.banks.sbab.util;

import com.google.api.client.util.Strings;
import com.google.common.base.Objects;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import se.tink.backend.aggregation.agents.banks.sbab.model.response.QuestionGroupEntity;
import se.tink.backend.aggregation.agents.banks.sbab.model.response.QuestionGroupResponse;
import se.tink.backend.aggregation.agents.exceptions.application.InvalidApplicationException;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class SBABOpenSavingsAccountUtils {

    private static final AggregationLogger log = new AggregationLogger(SBABOpenSavingsAccountUtils.class);
    private static final Pattern VALIDATION_ERROR_PATTERN = Pattern.compile("(var valideringsFel=)(?<error>[^;]*)");

    public static Optional<QuestionGroupResponse> parseQuestionGroups(String script) {
        Pattern pattern = Pattern.compile("var frageGrupper=([^;]+)");
        Matcher matcher = pattern.matcher(script);

        if (matcher.find()) {
            String list = matcher.group(1);
            String validJSON = "{\"frageGrupper\":" + list + "}";

            return Optional.of(SerializationUtils
                    .deserializeFromString(validJSON, QuestionGroupResponse.class));
        }

        return Optional.empty();
    }

    public static void checkForInitialRequestErrors(Document document) throws InvalidApplicationException {
        Element errorRow = document.select("form[id=oppnaSparkontoForm] div:has(div[class=error])").first();

        if (errorRow != null) {
            String userMessage = null;
            Element errorTextElement = errorRow.select("div[class=error] > span").first();
            Element nameOfFieldElement = errorRow.select("label").first();

            if (errorTextElement != null && nameOfFieldElement != null) {
                String errorText = errorTextElement.text();
                String nameOfField = nameOfFieldElement.text();

                if (!Strings.isNullOrEmpty(errorText) && !Strings.isNullOrEmpty(nameOfField)) {
                    userMessage = nameOfField + ": " + errorText;
                }
            }

            throw new InvalidApplicationException(userMessage);
        }

        checkForErrorWrapperErrors(document);
    }

    private static void checkForErrorWrapperErrors(Document document) throws InvalidApplicationException {
        Element errorWrapper = document.select("div.error-wrapper").first();

        if (errorWrapper != null && !Strings.isNullOrEmpty(errorWrapper.text())) {
            log.error(String.format("Application error: %s", errorWrapper.text()));
            throw new InvalidApplicationException();
        }
    }
}
