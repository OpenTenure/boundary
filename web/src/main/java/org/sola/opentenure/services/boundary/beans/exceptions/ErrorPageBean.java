package org.sola.opentenure.services.boundary.beans.exceptions;

import java.util.Map;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.sola.cs.common.messaging.MessageUtility;
import org.sola.cs.common.messaging.ServiceMessage;
import org.sola.opentenure.services.boundary.beans.AbstractBackingBean;
import org.sola.opentenure.services.boundary.beans.language.LanguageBean;
import org.sola.services.common.faults.FaultUtility;
import static org.sola.services.common.faults.FaultUtility.getCause;
import org.sola.services.common.logging.LogUtility;

/**
 * Handles errors for displaying on the page
 */
@RequestScoped
@Named
public class ErrorPageBean extends AbstractBackingBean {

    @Inject
    LanguageBean langBean;
    
    public String getStackTrace() {
        // Get the current JSF context
        Map requestMap = getExtContext().getRequestMap();

        // Fetch the exception
        Throwable t = (Throwable) requestMap.get("javax.servlet.error.exception");
        LogUtility.log("Unexpected error occured", (Exception)t);
        
        if (FaultUtility.hasCause(t, OTWebException.class)) {
            OTWebException ex = getCause(t, OTWebException.class);
            return ex.getMessage();
        } else {
            return MessageUtility.getLocalizedMessageText(ServiceMessage.GENERAL_UNEXPECTED, langBean.getLocale());
        }
    }
}
