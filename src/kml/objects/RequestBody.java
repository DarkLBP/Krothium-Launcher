package kml.objects;

import com.google.common.base.Charsets;

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
    public String getString(){return new String(body, Charsets.UTF_8);}
}
