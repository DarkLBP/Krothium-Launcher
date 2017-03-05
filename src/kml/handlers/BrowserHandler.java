package kml.handlers;

import kml.Utils;
import kml.enums.OSArch;

import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;

/**
 * @author DarkLBP
 * website https://krothium.com
 */
public class BrowserHandler implements URLStreamHandlerFactory{
    private AdsHandler ADS_HANDLER;
    private AdsHandlerSecure ADS_HANDLER_SECURE;
    public BrowserHandler() {
        String userAgent = "Mozilla/5.0 %s AppleWebKit/537.36 (KHTML, like Gecko) Chrome/56.0.2924.87 Safari/537.36";
        switch (Utils.getPlatform()) {
            case LINUX:
                if (Utils.getOSArch() == OSArch.NEW) {
                    userAgent = String.format(userAgent, "(X11; Linux x86_64)");
                } else {
                    userAgent = String.format(userAgent, "(X11; Linux i686)");
                }
                break;
            case WINDOWS:
                if (Utils.getOSArch() == OSArch.NEW) {
                    userAgent = String.format(userAgent, "(Windows NT " + System.getProperty("os.version") + "; Win64; x64)");
                } else {
                    userAgent = String.format(userAgent, "(Windows NT " + System.getProperty("os.version") + ")");
                }
                break;
            case OSX:
                userAgent = String.format(userAgent, "(Macintosh; Intel Mac OS X " + System.getProperty("os.version").replace(".", "_") + ")");
                break;
            default:
                userAgent = String.format(userAgent, "(Windows NT 10.0; Win64; x64)");
        }
        ADS_HANDLER = new AdsHandler(userAgent);
        ADS_HANDLER_SECURE = new AdsHandlerSecure(userAgent);
    }
    @Override
    public URLStreamHandler createURLStreamHandler(String protocol) {
        if (protocol.equalsIgnoreCase("http")){
            return ADS_HANDLER;
        } else if (protocol.equalsIgnoreCase("https")) {
            return ADS_HANDLER_SECURE;
        }
        return null;
    }

}
