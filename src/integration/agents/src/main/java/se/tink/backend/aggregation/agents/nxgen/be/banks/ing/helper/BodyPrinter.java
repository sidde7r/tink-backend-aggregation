package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.helper;

import java.awt.event.KeyEvent;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

@Slf4j
public class BodyPrinter {

    public static void print(HttpRequest request) {
        StringBuilder sb = new StringBuilder("REQUEST:\n");
        byte[] body = (byte[]) request.getBody();
        printHex(sb, body);
        log.info(sb.toString());
    }

    public static void print(HttpResponse response) {
        StringBuilder sb = new StringBuilder("RESPONSE:\n");
        byte[] body = response.getBody(byte[].class);
        printHex(sb, body);
        log.info(sb.toString());
    }

    private static void printHex(StringBuilder sb, byte[] body) {
        for (int i = 0; i < body.length; i += 16) {
            for (int s = i; s < i + 16 && s < body.length; s++) {
                sb.append(String.format("%02x ", body[s]));
            }
            for (int s = i; s < i + 16 && s < body.length; s++) {
                if (isPrintableChar((char) body[s])) {
                    sb.append(String.format("%s", (char) body[s]));
                } else {
                    sb.append(".");
                }
            }
            sb.append("\n");
        }
    }

    private static boolean isPrintableChar(char c) {
        Character.UnicodeBlock block = Character.UnicodeBlock.of(c);
        return (!Character.isISOControl(c))
                && c != KeyEvent.CHAR_UNDEFINED
                && block != null
                && block != Character.UnicodeBlock.SPECIALS;
    }
}
