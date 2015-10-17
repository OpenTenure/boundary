package org.sola.opentenure.services.boundary.beans;

import java.util.logging.Level;
import javax.annotation.Resource;
import javax.ejb.EJBAccessException;
import javax.enterprise.context.Dependent;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.transaction.Status;
import javax.transaction.UserTransaction;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.xml.ws.WebServiceContext;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.sola.common.DynamicFormException;
import org.sola.common.SOLAException;
import org.sola.common.logging.LogUtility;
import org.sola.cs.common.messaging.ServiceMessage;
import org.sola.opentenure.services.boundary.beans.exceptions.ExceptionFactory;
import org.sola.services.common.faults.OTRestException;
import org.sola.services.common.faults.FaultUtility;
import static org.sola.services.common.faults.FaultUtility.getCause;
import org.sola.services.common.faults.OTMissingAttachmentsException;
import org.sola.common.SOLAMD5Exception;
import org.sola.common.SOLANoDataException;
import org.sola.services.common.LocalInfo;
import org.sola.services.common.faults.SOLAObjectExistsException;
import org.sola.services.common.faults.SOLAValidationException;

public class AbstractWebRestService {

    @Context
    private HttpServletRequest request;

    @Context
    private UriInfo uri;

    @Resource
    private WebServiceContext context;
    
    /**
     * Holds a reference to the UserTransction. Injected using @Resource
     */
    @Resource
    private UserTransaction tx;
    
    private ObjectMapper mapper;
    
    protected final String LOCALE_CODE = "localeCode";

    public AbstractWebRestService() {
        super();
    }
    
    /**
     * Starts a transaction.
     *
     * @throws Exception
     */
    protected void beginTransaction() throws Exception {
        tx.begin();
    }

    /**
     * Commits a transaction as long as the transaction is not in the
     * NO_TRANSACTION state.
     *
     * @throws Exception
     */
    protected void commitTransaction() throws Exception {
        if (tx.getStatus() != Status.STATUS_NO_TRANSACTION) {
            tx.commit();
        }
    }

    /**
     * Rolls back the transaction as long as the transaction is not in the
     * NO_TRANSACTION state. This method should be called in the finally clause
     * wherever a transaction is started.
     *
     * @throws Exception
     */
    protected void rollbackTransaction() throws Exception {
        if (tx.getStatus() != Status.STATUS_NO_TRANSACTION) {
            tx.rollback();
        }
    }
    
    /**
     * Provides common fault handling and transaction functionality for secured
     * web methods that perform data updates but do not perform validation.
     *
     * @param runnable Anonymous inner class that implements the
     * {@linkplain java.lang.Runnable Runnable} interface
     * @throws Exception
     */
    protected void runUpdate(Runnable runnable) throws Exception {
        try {
            beginTransaction();
            runnable.run();
            commitTransaction();
        } finally {
            rollbackTransaction();
            cleanUp();
        }
    }
    
    /**
     * Performs clean up actions after the web method logic has been executed.
     */
    protected void cleanUp() {
        LocalInfo.remove();
    }
        
    /**
     * Returns Jackson JSON mapper
     *
     * @return
     */
    public ObjectMapper getMapper() {
        if (mapper == null) {
            mapper = new ObjectMapper();
            mapper.configure(SerializationConfig.Feature.WRITE_DATES_AS_TIMESTAMPS, false);
        }
        return mapper;
    }

    public String getLanguageCode() {
        return request.getLocale().toLanguageTag();
    }
    
    /**
     * Returns URI information.
     */
    public UriInfo getUriInfo() {
        return uri;
    }
        
    /**
     * Returns application URL
     */
    public String getApplicationUrl() {
        HttpServletRequest r = getRequest();
        return r.getRequestURL().substring(0, r.getRequestURL().length() - r.getRequestURI().length());
    }

    /**
     * Returns web service context.
     */
    public WebServiceContext getContext() {
        return context;
    }

    /**
     * Returns request object.
     */
    public HttpServletRequest getRequest() {
        return request;
    }

    /**
     * Returns {@link HttpSession} object object
     */
    public HttpSession getSession() {
        return getRequest().getSession();
    }

    /**
     * Process exception and returns back in appropriate format.
     */
    protected OTRestException processException(Exception t, String localeCode) {
        String stackTraceAsStr = FaultUtility.getStackTraceAsString(t);

        try {
            String msg = "SOLA OT FaultId = "
                    + FaultUtility.createFaultId(getRequest().getRemoteUser())
                    + System.getProperty("line.separator")
                    + stackTraceAsStr;
            LogUtility.log(msg, Level.SEVERE);
        } catch (Exception logEx) {
            return ExceptionFactory.buildGeneralException(ServiceMessage.EXCEPTION_FAILED_LOGGING, localeCode);
        }

        try {
            // Identify the type of exception and raise the appropriate Service Fault
            if (t.getClass() == OTRestException.class) {
                return (OTRestException) t;
            } else if (FaultUtility.hasCause(t, SOLAObjectExistsException.class)) {
                return ExceptionFactory.buildObjectExist(
                        FaultUtility.getCause(t, SOLAObjectExistsException.class).getMessage(),
                        localeCode);
            } else if (FaultUtility.hasCause(t, OTMissingAttachmentsException.class)) {
                OTMissingAttachmentsException ex = FaultUtility.getCause(t, OTMissingAttachmentsException.class);
                return ExceptionFactory.buildMissingServerAttachments(ex.getAttachments(), localeCode);
            } else if (FaultUtility.hasCause(t, DynamicFormException.class)) {
                DynamicFormException ex = getCause(t, DynamicFormException.class);
                return new OTRestException(Response.Status.BAD_REQUEST.getStatusCode(), ex.getMessage());
            } else if (FaultUtility.hasCause(t, SOLANoDataException.class)) {
                SOLANoDataException ex = FaultUtility.getCause(t, SOLANoDataException.class);
                return ExceptionFactory.buildMissingData(ex.getMessage(), localeCode);
            } else if (FaultUtility.hasCause(t, SOLAMD5Exception.class)) {
                return ExceptionFactory.buildMD5Error(localeCode);
            } else if (FaultUtility.hasCause(t, SOLAValidationException.class)) {
                // TODO: Improve handling of validation
                SOLAValidationException ex = FaultUtility.getCause(t, SOLAValidationException.class);
                return ExceptionFactory.buildGeneralException(ServiceMessage.RULE_VALIDATION_FAILED, localeCode);
            } else if (FaultUtility.hasCause(t, EJBAccessException.class)) {
                return ExceptionFactory.buildForbidden(localeCode);
            } else if (FaultUtility.isOptimisticLocking(t, stackTraceAsStr)) {
                // Optimistic locking exception
                return ExceptionFactory.buildGeneralException(ServiceMessage.GENERAL_OPTIMISTIC_LOCK, localeCode);
            } else if (FaultUtility.hasCause(t, SOLAException.class)) {
                SOLAException ex = getCause(t, SOLAException.class);
                Object[] msgParms = ex.getMessageParameters();
                return ExceptionFactory.buildGeneralException(ex.getMessage(), localeCode, ex.getMessageParameters());
            } else {
                // Unhandled Exception.
                return ExceptionFactory.buildUnexpected(localeCode);
            }
        } catch (Exception formatEx) {
            // Catch all in case the format throws an exception. Note that the
            // exception details in the log will not match the details of the
            // format exception (i.e. the one in the log will be the original
            // exception).      
            return ExceptionFactory.buildGeneralException(ServiceMessage.EXCEPTION_FAILED_FORMATTING, localeCode);
        }
    }
}
