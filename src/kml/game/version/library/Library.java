package kml.game.version.library;

import com.google.gson.annotations.Expose;
import kml.game.version.rule.ExtractRule;
import kml.game.version.rule.Rule;

/**
 * @author DarkLBP
 * website https://krothium.com
 */

public class Library {
    @Expose
    private String name;
    @Expose
    private ExtractRule extract;
    @Expose
    private Natives natives;
    @Expose
    private Rule[] rules;
    @Expose
    private LibraryDownloads downloads;

    public String getName() {
        return name;
    }

    public ExtractRule getExtract() {
        return extract;
    }

    public Natives getNatives() {
        return natives;
    }

    public Rule[] getRules() {
        return rules;
    }

    public LibraryDownloads getDownloads() {
        return downloads;
    }
}
