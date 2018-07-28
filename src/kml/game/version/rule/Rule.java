package kml.game.version.rule;

import com.google.gson.annotations.Expose;

public class Rule {
    @Expose
    private RuleAction action;
    @Expose
    private OSRule os;
    @Expose
    private FeaturesRule features;


}
