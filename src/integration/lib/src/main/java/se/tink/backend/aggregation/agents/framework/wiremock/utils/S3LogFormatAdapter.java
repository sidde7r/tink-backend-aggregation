package se.tink.backend.aggregation.agents.framework.wiremock.utils;

import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import se.tink.backend.aggregation.agents.framework.wiremock.utils.parsingrules.ParsingRule;
import se.tink.backend.aggregation.agents.framework.wiremock.utils.parsingrules.parsingoperations.InsertEmptyLineBefore;
import se.tink.backend.aggregation.agents.framework.wiremock.utils.parsingrules.parsingoperations.RemoveLine;
import se.tink.backend.aggregation.agents.framework.wiremock.utils.parsingrules.parsingoperations.RequestResponseSubstitution;
import se.tink.backend.aggregation.agents.framework.wiremock.utils.parsingrules.parsingoperations.StripPrefix;
import se.tink.backend.aggregation.agents.framework.wiremock.utils.parsingrules.predicates.Contains;
import se.tink.backend.aggregation.agents.framework.wiremock.utils.parsingrules.predicates.HasPrefix;
import se.tink.backend.aggregation.agents.framework.wiremock.utils.parsingrules.predicates.IsEmptyResponsePrefix;
import se.tink.backend.aggregation.agents.framework.wiremock.utils.parsingrules.predicates.IsTimestamp;

public final class S3LogFormatAdapter {

    private static final String S3_REQUEST_SIGNATURE = "Client out-bound request";
    private static final String S3_RESPONSE_SIGNATURE = "Client in-bound response";
    private static final String NEW_REQUEST_SIGNATURE = "REQUEST";
    private static final String NEW_RESPONSE_SIGNATURE = "RESPONSE";

    private final ImmutableList<ParsingRule> ruleChain =
            ImmutableList.<ParsingRule>builder()
                    .add(
                            ParsingRule.of(
                                    new Contains(S3_REQUEST_SIGNATURE),
                                    new RequestResponseSubstitution(NEW_REQUEST_SIGNATURE)))
                    .add(
                            ParsingRule.of(
                                    new Contains(S3_RESPONSE_SIGNATURE),
                                    new RequestResponseSubstitution(NEW_RESPONSE_SIGNATURE)))
                    .add(
                            ParsingRule.of(
                                    new IsTimestamp().or(new IsEmptyResponsePrefix()),
                                    new RemoveLine()))
                    .add(ParsingRule.of(new HasPrefix().negate(), new InsertEmptyLineBefore()))
                    .add(ParsingRule.of((s) -> true, new StripPrefix()))
                    .build();

    public List<String> toMockFileFormat(final String rawText) {

        final List<String> lines = Arrays.asList(rawText.split("\n"));
        final List<String> result = new ArrayList<>(lines.size());

        for (final String line : lines) {

            for (final ParsingRule rule : ruleChain) {

                if (rule.test(line)) {
                    result.addAll(rule.performOperation(line));
                    break;
                }
            }
        }

        return result;
    }
}
