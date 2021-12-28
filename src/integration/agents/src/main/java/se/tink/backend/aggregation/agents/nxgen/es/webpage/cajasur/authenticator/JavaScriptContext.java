package se.tink.backend.aggregation.agents.nxgen.es.webpage.cajasur.authenticator;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

@AllArgsConstructor
@Getter
public class JavaScriptContext {

    private Context cx;
    private Scriptable scope;
}
