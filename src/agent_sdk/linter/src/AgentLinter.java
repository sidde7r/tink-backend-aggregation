package se.tink.agent.linter;

import com.google.auto.service.AutoService;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.matchers.Description;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.NewClassTree;

@AutoService(BugChecker.class)
@BugPattern(
        name = AgentLinter.NAME,
        summary = "Disallowed usage of method or class",
        severity = BugPattern.SeverityLevel.ERROR)
public class AgentLinter extends BugChecker
        implements BugChecker.MethodInvocationTreeMatcher, BugChecker.NewClassTreeMatcher {

    static final String NAME = "AgentLinter";

    @Override
    public Description matchNewClass(NewClassTree tree, VisitorState state) {
        return evaluateRules(tree, state);
    }

    @Override
    public Description matchMethodInvocation(MethodInvocationTree tree, VisitorState state) {
        return evaluateRules(tree, state);
    }

    private Description evaluateRules(ExpressionTree tree, VisitorState state) {
        return LinterRules.RULES.stream()
                .filter(rule -> rule.matches(tree, state))
                .findFirst()
                .map(rule -> describeMatch(rule, tree))
                .orElse(Description.NO_MATCH);
    }

    private Description describeMatch(LinterRule rule, ExpressionTree tree) {
        String message =
                String.format(
                        "Disallowed usage of method or class. Use '%s' instead.",
                        rule.getChangeTo());

        return Description.builder(
                        tree,
                        AgentLinter.NAME,
                        rule.getReadMoreLink(),
                        BugPattern.SeverityLevel.ERROR,
                        message)
                .addFix(SuggestedFix.replace(tree, rule.getChangeTo()))
                .build();
    }
}
