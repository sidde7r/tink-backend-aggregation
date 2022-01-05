package se.tink.backend.aggregation.agents.nxgen.es.webpage.cajasur.authenticator.login;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.commons.io.FileUtils;

public class JavaScriptResourceLoader {

    private static final String JAVASCRIPT_RESOURCE_PATH =
            "src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/es/webpage/cajasur/resources/javascript/";
    private static String jsPbkdf2;
    private static String jsEnvRhino;
    private static String jsAes;
    private static String jsAesUtils;

    static String loadPbkdf2Js() {
        if (jsPbkdf2 == null) {
            jsPbkdf2 = loadFileBody(Paths.get(JAVASCRIPT_RESOURCE_PATH, "pbkdf2.js"), jsPbkdf2);
        }
        return jsPbkdf2;
    }

    static String loadEnvRhinoJs() {
        if (jsEnvRhino == null) {
            jsEnvRhino =
                    loadFileBody(
                            Paths.get(JAVASCRIPT_RESOURCE_PATH, "env_rhino_1_2.js"), jsEnvRhino);
        }
        return jsEnvRhino;
    }

    static String loadAesJs() {
        if (jsAes == null) {
            jsAes = loadFileBody(Paths.get(JAVASCRIPT_RESOURCE_PATH, "aes.js"), jsAes);
        }
        return jsAes;
    }

    static String loadAesUtilsJs() {
        if (jsAesUtils == null) {
            jsAesUtils =
                    loadFileBody(Paths.get(JAVASCRIPT_RESOURCE_PATH, "AesUtil.js"), jsAesUtils);
        }
        return jsAesUtils;
    }

    private static String loadFileBody(Path path, String lockVariable) {
        try {
            synchronized (JavaScriptResourceLoader.class) {
                if (lockVariable == null) {
                    return FileUtils.readFileToString(path.toFile(), StandardCharsets.UTF_8);
                }
                return lockVariable;
            }
        } catch (IOException e) {
            throw new IllegalStateException("Cajasur JavaScript resource files are missing");
        }
    }
}
