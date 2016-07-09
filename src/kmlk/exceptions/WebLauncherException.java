package kmlk.exceptions;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @website http://krotium.com
 * @author DarkLBP
 */

public class WebLauncherException extends Exception{
    public WebLauncherException(String path, int errorCode, OutputStream out) throws IOException
    {
        super(path + " returned error code " + errorCode);
        switch (errorCode){
            case 404:
                out.write("HTTP/1.1 404 Not Found\r\n".getBytes());
                break;
        } 
        out.close();
    }
}
