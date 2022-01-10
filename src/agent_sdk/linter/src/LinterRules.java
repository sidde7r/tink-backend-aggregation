package se.tink.agent.linter;

import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.Uninterruptibles;
import com.google.errorprone.matchers.Matchers;
import com.google.errorprone.matchers.method.MethodMatchers;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public final class LinterRules {

    private LinterRules() {}

    public static final List<LinterRule> RULES =
            ImmutableList.<LinterRule>builder()
                    .add(
                            LinterRule.builder()
                                    .matcher(
                                            Matchers.anyOf(
                                                    MethodMatchers.constructor()
                                                            .forClass(Date.class.getName()),
                                                    MethodMatchers.staticMethod()
                                                            .onClass(Date.class.getName())
                                                            .withAnyName()))
                                    .changeTo("TimeGenerator.localDateTimeNow()")
                                    .readMoreLink(
                                            "https://tinkab.atlassian.net/wiki/spaces/AES/TBD")
                                    .build(),
                            LinterRule.builder()
                                    .matcher(
                                            MethodMatchers.staticMethod()
                                                    .onClass(LocalDate.class.getName())
                                                    .withAnyName())
                                    .changeTo("TimeGenerator.localDateNow()")
                                    .readMoreLink(
                                            "https://tinkab.atlassian.net/wiki/spaces/AES/TBD")
                                    .build(),
                            LinterRule.builder()
                                    .matcher(
                                            MethodMatchers.staticMethod()
                                                    .onClass(LocalDateTime.class.getName())
                                                    .withAnyName())
                                    .changeTo("TimeGenerator.localDateTimeNow()")
                                    .readMoreLink(
                                            "https://tinkab.atlassian.net/wiki/spaces/AES/TBD")
                                    .build(),
                            LinterRule.builder()
                                    .matcher(
                                            MethodMatchers.staticMethod()
                                                    .onClass(Instant.class.getName())
                                                    .withAnyName())
                                    .changeTo("TimeGenerator.instantNow()")
                                    .readMoreLink(
                                            "https://tinkab.atlassian.net/wiki/spaces/AES/TBD")
                                    .build(),
                            LinterRule.builder()
                                    .matcher(
                                            Matchers.anyOf(
                                                    MethodMatchers.constructor()
                                                            .forClass(Random.class.getName()),
                                                    MethodMatchers.constructor()
                                                            .forClass(SecureRandom.class.getName()),
                                                    MethodMatchers.staticMethod()
                                                            .onClass(SecureRandom.class.getName())
                                                            .withAnyName()))
                                    .changeTo("RandomGenerator.random*()")
                                    .readMoreLink(
                                            "https://tinkab.atlassian.net/wiki/spaces/AES/TBD")
                                    .build(),
                            LinterRule.builder()
                                    .matcher(
                                            Matchers.anyOf(
                                                    MethodMatchers.constructor()
                                                            .forClass(UUID.class.getName()),
                                                    MethodMatchers.staticMethod()
                                                            .onClass(UUID.class.getName())
                                                            .withAnyName()))
                                    .changeTo("RandomGenerator.randomUUID*()")
                                    .readMoreLink(
                                            "https://tinkab.atlassian.net/wiki/spaces/AES/TBD")
                                    .build(),
                            LinterRule.builder()
                                    .matcher(
                                            Matchers.anyOf(
                                                    MethodMatchers.staticMethod()
                                                            .onClass(Thread.class.getName())
                                                            .named("sleep"),
                                                    MethodMatchers.staticMethod()
                                                            .onClass(
                                                                    Uninterruptibles.class
                                                                            .getName())
                                                            .withAnyName()))
                                    .changeTo("Sleeper.sleep()")
                                    .readMoreLink(
                                            "https://tinkab.atlassian.net/wiki/spaces/AES/TBD")
                                    .build())
                    .build();
}
