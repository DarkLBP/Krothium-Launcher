package kml.objects;

import java.nio.charset.StandardCharsets;

/**
 * @website https://krothium.com
 * @author DarkLBP
 */
public class RequestBody {
    private final byte[] body;
    public RequestBody(){
        body = new byte[0];
    }
    public RequestBody(byte[] data){
        body = data;
    }
    public byte[] getBytes(){return this.body;}
    public String getString(){return new String(body, StandardCharsets.UTF_8);}
}
