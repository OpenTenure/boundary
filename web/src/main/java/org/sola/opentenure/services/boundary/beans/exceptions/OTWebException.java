package org.sola.opentenure.services.boundary.beans.exceptions;

/**
 * Open tenure web page exception
 */
public class OTWebException extends RuntimeException {
    public OTWebException(){
        super();
    }
    
    public OTWebException(String message){
        super(message);
    }
    
    public OTWebException(Throwable t){
        super(t);
    }
    
    public OTWebException(String msg, Throwable t){
        super(msg, t);
    }
}
