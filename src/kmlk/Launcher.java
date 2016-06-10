package kmlk;

/**
 * @website http://krotium.com
 * @author DarkLBP
 */

public class Launcher {
    private final Kernel kernel;
    private final Console console;
    public Launcher(Kernel k)
    {
        this.kernel = k;
        this.console = k.getConsole();
    }
    public void startGame(Version v)
    {
        
    }
}
