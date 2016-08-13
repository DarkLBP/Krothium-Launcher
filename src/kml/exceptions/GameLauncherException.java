package kml.exceptions;

/**
 * @website https://krothium.com
 * @author DarkLBP
 */

public class GameLauncherException extends Exception{
    public GameLauncherException() {}
    public GameLauncherException(final String message){super(message);}
    public GameLauncherException(final String message, final Throwable cause){super(message, cause);}
    public GameLauncherException(final Throwable cause){super(cause);}
}
