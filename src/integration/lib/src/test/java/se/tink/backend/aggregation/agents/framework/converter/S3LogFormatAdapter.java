package se.tink.backend.aggregation.agents.framework.converter;

import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import se.tink.backend.aggregation.agents.framework.wiremock.utils.parsingrules.ParsingRule;
import se.tink.backend.aggregation.agents.framework.wiremock.utils.parsingrules.parsingoperations.BeautifyJsonString;
import se.tink.backend.aggregation.agents.framework.wiremock.utils.parsingrules.parsingoperations.InsertEmptyLineBefore;
import se.tink.backend.aggregation.agents.framework.wiremock.utils.parsingrules.parsingoperations.RemoveLine;
import se.tink.backend.aggregation.agents.framework.wiremock.utils.parsingrules.parsingoperations.RequestResponseSubstitution;
import se.tink.backend.aggregation.agents.framework.wiremock.utils.parsingrules.parsingoperations.StripPrefix;
import se.tink.backend.aggregation.agents.framework.wiremock.utils.parsingrules.predicates.Contains;
import se.tink.backend.aggregation.agents.framework.wiremock.utils.parsingrules.predicates.ContainsAny;
import se.tink.backend.aggregation.agents.framework.wiremock.utils.parsingrules.predicates.HasPrefix;
import se.tink.backend.aggregation.agents.framework.wiremock.utils.parsingrules.predicates.IsEmptyResponsePrefix;
import se.tink.backend.aggregation.agents.framework.wiremock.utils.parsingrules.predicates.IsHeader;
import se.tink.backend.aggregation.agents.framework.wiremock.utils.parsingrules.predicates.IsTimestamp;
import se.tink.backend.aggregation.agents.framework.wiremock.utils.parsingrules.predicates.StartsWith;

public final class S3LogFormatAdapter {

    private static final String S3_REQUEST_SIGNATURE = "Client out-bound request";
    private static final String S3_RESPONSE_SIGNATURE = "Client in-bound response";
    public static final String SEPARATOR =
            "-----------------------------------------------------------------------------------";
    private static final String NEW_REQUEST_SIGNATURE = SEPARATOR + "\nREQUEST";
    private static final String NEW_RESPONSE_SIGNATURE = SEPARATOR + "\nRESPONSE";
    private static final String[] HEADERS = S3LogFormatAdapterHeadersToRemove.asArray();

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
                                    new IsTimestamp()
                                            .or(new IsEmptyResponsePrefix())
                                            .or(new IsHeader().and(new ContainsAny(HEADERS))),
                                    new RemoveLine()))
                    .add(ParsingRule.of(new StartsWith("{"), new BeautifyJsonString()))
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
