package kml.game.version.rule;

import com.google.gson.annotations.Expose;

import java.util.ArrayList;

public class ExtractRule {
    @Expose
    private ArrayList<String> exclude;
    @Expose
    private ArrayList<String> include;

    public ArrayList<String> getExclude() {
        return exclude;
    }

    public ArrayList<String> getInclude() {
        return include;
    }
}
