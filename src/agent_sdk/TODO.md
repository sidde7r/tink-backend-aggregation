# Lint

Some methods/functionality must be controlled by the framework:
- Testability - generate static/known/configured values during test
- Testability - sleep should never occur during a test
- Logging - the log state must be maintained over thread boundaries
- Performance - agents should not be able to spawn new threads, it introduces performance and soundness issues


Scan agent code for usages of:

Class|[method]:             Should use instead:

java.util.Random            sdk.security.RandomGenerator
java.security.SecureRandom  sdk.security.RandomGenerator
java.util.UUID              sdk.security.RandomGenerator

Instant.now([...])          sdk.chrono.TimeGenerator
LocalDate.now([...])        sdk.chrono.TimeGenerator
LocalDateTime.now([...])    sdk.chrono.TimeGenerator
new Date()                  sdk.chrono.TimeGenerator

Thread.sleep([...])         sdk.chrono.Sleep
com.google.common.util.concurrent.Uninterruptibles.sleepUninterruptibly             sdk.chrono.Sleep


"Spawn thread"              NOT ALLOWED (?) --> New threads must carry over the MDC context, we cannot allow agents to spawn threads


# Bazel build package lint

1. No direct references to //src/libraries/... (these must be defined in libs/BUILD)
2. No dependencies outside of `@agent_sdk_maven//:...` (deps.bzl)
3. Shadow check

The reason for the first two points is that these might not introduce shadowed versions immediately,
but will cause problems down the road. So it's better to catch it, and deny it, sooner.
