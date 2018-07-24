package se.tink.backend.aggregation.credit.safe.creditsafe;

import java.util.Iterator;
import java.util.Set;
import javax.xml.namespace.QName;
import javax.xml.soap.MimeHeader;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import org.apache.commons.io.output.ByteArrayOutputStream;
import se.tink.backend.aggregation.log.AggregationLogger;

public class SOAPLogHandler implements SOAPHandler<SOAPMessageContext> {

    private static final AggregationLogger log = new AggregationLogger(
            SOAPLogHandler.class);

    @Override
    public Set<QName> getHeaders() {
        return null;
    }

    @Override
    public void close(MessageContext context) {
    }

    @Override
    public boolean handleFault(SOAPMessageContext context) {
        log.info("HandleFault");
        log(context);
        return true;
    }

    @Override
    public boolean handleMessage(SOAPMessageContext context) {
        log.info("HandleMessage");
        log(context);
        return true;
    }

    private void log(SOAPMessageContext smc) {

        try {
            SOAPMessage message = smc.getMessage();
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            message.writeTo(stream);

            MimeHeaders mimeHeaders = message.getMimeHeaders();
            Iterator allHeaders = mimeHeaders.getAllHeaders();
            log.info("Headers: ");
            while (allHeaders.hasNext()) {
                Object o = allHeaders.next();
                MimeHeader header = (MimeHeader)o;

                log.info("\t" + header.getName() + ": " + header.getValue());
            }

            log.info("Message:");
            log.info("\t" + stream.toString());
        } catch (Exception e) {
            log.error("Exception in handler", e);
        }
    }
}
