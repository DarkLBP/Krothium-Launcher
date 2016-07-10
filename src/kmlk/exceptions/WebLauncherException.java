package kmlk.exceptions;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @website http://krotium.com
 * @author DarkLBP
 */

public class WebLauncherException extends Exception{
    public WebLauncherException(String path, int errorCode, OutputStream out) throws IOException{
        super(path + " returned error code " + errorCode);
        out.write("HTTP/1.1 200 OK\r\n".getBytes());
        out.write("\r\n".getBytes());
        switch (errorCode){
            case 400:
                out.write("<html><h1>BAD REQUEST</h1></html>".getBytes());
            case 404:
                out.write("<html><h1>NOT FOUND</h1></html>".getBytes());
                break;
        } 
        out.close();
    }
    public WebLauncherException(int errorCode, OutputStream out) throws IOException{
        super("Error code " + errorCode + " has been thrown");
        out.write("HTTP/1.1 200 OK\r\n".getBytes());
        out.write("\r\n".getBytes());
        switch (errorCode){
            case 400:
                out.write("<html><h1>BAD REQUEST</h1></html>".getBytes());
                break;
            case 404:
                out.write("<html><h1>NOT FOUND</h1></html>".getBytes());
                break;
        }
        out.close();
    }
}
