package kmlk;

/**
 * @website http://krotium.com
 * @author DarkLBP
 */

public class AuthenticationException extends Exception{
    public AuthenticationException() {
    }
    
    public AuthenticationException(final String message) {
        super(message);
    }
    
    public AuthenticationException(final String message, final Throwable cause) {
        super(message, cause);
    }
    
    public AuthenticationException(final Throwable cause) {
        super(cause);
    }
}
