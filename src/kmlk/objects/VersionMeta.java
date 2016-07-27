package kmlk.objects;

import java.net.URL;
import kmlk.enums.VersionType;

/**
 * @website http://krotium.com
 * @author DarkLBP
 */

public class VersionMeta {
    private final String id;
    private final URL url;
    private final VersionType type;
    public VersionMeta(String id, URL url, VersionType type){
        this.id = id;
        this.url = url;
        this.type = type;
    }
    public boolean hasURL(){return (this.url != null);}
    public boolean hasID(){return (this.id != null);}
    public boolean hasType(){return (this.type != null);}
    public String getID(){return this.id;}
    public URL getURL(){return this.url;}
    public VersionType getType(){return this.type;}
}
