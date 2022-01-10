package se.tink.agent.linter;

import com.google.errorprone.VisitorState;
import com.google.errorprone.matchers.Matcher;
import com.sun.source.tree.ExpressionTree;
import lombok.Builder;

@Builder
public class LinterRule {
    private final Matcher<ExpressionTree> matcher;
    private final String changeTo;
    private final String readMoreLink;

    public boolean matches(ExpressionTree tree, VisitorState state) {
        return this.matcher.matches(tree, state);
    }

    public String getChangeTo() {
        return changeTo;
    }

    public String getReadMoreLink() {
        return readMoreLink;
    }
}
