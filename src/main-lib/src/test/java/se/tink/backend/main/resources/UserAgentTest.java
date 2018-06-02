package se.tink.backend.main.resources;

import net.sf.uadetector.OperatingSystemFamily;
import net.sf.uadetector.ReadableUserAgent;
import net.sf.uadetector.UserAgentStringParser;
import net.sf.uadetector.service.UADetectorServiceFactory;

import org.junit.Assert;
import org.junit.Test;

public class UserAgentTest {
    
    @Test
    public void testMobileOperatingSystem() {
        UserAgentStringParser parser = UADetectorServiceFactory.getResourceModuleParser();
        
        String[] androidUserAgents = new String[]{
                "Tink Mobile/1.9.0 (Android; 4.4.2, LGE Nexus 4)",
                
                // Taken from https://developer.chrome.com/multidevice/user-agent
                "Mozilla/5.0 (Linux; Android 4.0.4; Galaxy Nexus Build/IMM76B) AppleWebKit/535.19 (KHTML, like Gecko) Chrome/18.0.1025.133 Mobile Safari/535.19",
                "Mozilla/5.0 (Linux; U; Android 4.1.1; en-gb; Build/KLP) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Safari/534.30",
                "Mozilla/5.0 (Linux; Android 4.4; Nexus 5 Build/_BuildID_) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/30.0.0.0 Mobile Safari/537.36",
                
        };
        for (String userAgent : androidUserAgents) {
            ReadableUserAgent agent = parser.parse(userAgent);
            Assert.assertEquals(userAgent, OperatingSystemFamily.ANDROID, agent.getOperatingSystem().getFamily());
        }
        
        String[] iosUserAgents = new String[]{
                "Tink Mobile/1.7.6 (iOS; 8.1, iPhone Simulator)",
                "Mozilla/5.0 (iPhone; CPU iPhone OS 8_1_2 like Mac OS X) AppleWebKit/600.1.4 (KHTML, like Gecko) Version/8.0 Mobile/12B440 Safari/600.1.4",
                
                // Taken from https://developer.chrome.com/multidevice/user-agent
                "Mozilla/5.0 (iPhone; U; CPU iPhone OS 5_1_1 like Mac OS X; en) AppleWebKit/534.46.0 (KHTML, like Gecko) CriOS/19.0.1084.60 Mobile/9B206 Safari/7534.48.3",
        };
        for (String userAgent : iosUserAgents) {
            ReadableUserAgent agent = parser.parse(userAgent);
            Assert.assertEquals(userAgent, OperatingSystemFamily.IOS, agent.getOperatingSystem().getFamily());
        }
        
        String[] unknownUserAgents = new String[]{
                "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/40.0.2214.94 Safari/537.36",
                
                // Taken from https://techblog.willshouse.com/2012/01/03/most-common-user-agents/
                "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_1) AppleWebKit/600.2.5 (KHTML, like Gecko) Version/8.0.2 Safari/600.2.5",
                "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:35.0) Gecko/20100101 Firefox/35.0",
                "Mozilla/5.0 (Windows NT 6.1; WOW64; Trident/7.0; rv:11.0) like Gecko",
                "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/39.0.2171.95 Safari/537.36",
        };
        for (String userAgent : unknownUserAgents) {
            ReadableUserAgent agent = parser.parse(userAgent);
            Assert.assertNotEquals(userAgent, OperatingSystemFamily.IOS, agent.getOperatingSystem().getFamily());
            Assert.assertNotEquals(userAgent, OperatingSystemFamily.ANDROID, agent.getOperatingSystem().getFamily());
        }
        
    }
    
}
