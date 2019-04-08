package se.tink.backend.aggregation.agents.utils.soap;

import com.google.common.collect.Lists;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.namespace.QName;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

public class SOAPUserAgentHandler implements SOAPHandler<SOAPMessageContext> {

    private final String aggregator;

    public SOAPUserAgentHandler(String aggregator) {
        this.aggregator = aggregator;
    }

    @Override
    public boolean handleMessage(SOAPMessageContext context) {
        boolean request =
                ((Boolean) context.get(SOAPMessageContext.MESSAGE_OUTBOUND_PROPERTY))
                        .booleanValue();

        if (request) {
            @SuppressWarnings("unchecked")
            Map<String, List<String>> headers =
                    (Map<String, List<String>>) context.get(MessageContext.HTTP_REQUEST_HEADERS);
            if (null == headers) {
                headers = new HashMap<String, List<String>>();
            }

            headers.put("User-Agent", Lists.newArrayList(aggregator));
            context.put(MessageContext.HTTP_REQUEST_HEADERS, headers);
        }

        return true;
    }

    @Override
    public boolean handleFault(SOAPMessageContext context) {
        return true;
    }

    @Override
    public void close(MessageContext context) {}

    @Override
    public Set<QName> getHeaders() {
        return null;
    }
}
