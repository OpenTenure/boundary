package org.sola.opentenure.services.boundary.beans.exceptions;

import org.sola.services.common.faults.OTRestException;
import java.util.List;
import javax.ws.rs.core.Response;
import org.sola.cs.common.messaging.MessageUtility;
import org.sola.cs.common.messaging.ServiceMessage;

/**
 * List of static methods to generate Open Tenure exceptions in JSON format.
 */
public class ExceptionFactory {

    private static final String RESPONSE_MESSAGE = "\"message\":\"%s\"";

    /**
     * Builds general Open Tenure exception.
     *
     * @param statusCode HTTP status code to assign to the response
     * @param errorCode Code of message from {@link ServiceMessage}
     * @param localeCode Locale code (e.g. en-US)
     * @param params List parameters to substitute in the message
     * @return
     */
    public static OTRestException buildGeneralException(
            int statusCode,
            String errorCode,
            String localeCode,
            Object[] params) {
        return new OTRestException(statusCode, String.format("{%s}",
                String.format(RESPONSE_MESSAGE,
                        MessageUtility.getLocalizedMessageText(errorCode, localeCode, params)))
        );
    }
    
    /**
     * Builds general Open Tenure exception.
     *
     * @param statusCode HTTP status code to assign to the response
     * @param errorCode Code of message from {@link ServiceMessage}
     * @param localeCode Locale code (e.g. en-US)
     * @return
     */
    public static OTRestException buildGeneralException(
            int statusCode,
            String errorCode,
            String localeCode) {
        return buildGeneralException(statusCode, errorCode, localeCode, null);
    }

    /**
     * Builds general Open Tenure exception with HTTP 400 code.
     *
     * @param errorCode Code of message from {@link ServiceMessage}
     * @param localeCode Locale code (e.g. en-US)
     * @return
     */
    public static OTRestException buildGeneralException(String errorCode, String localeCode) {
        return buildGeneralException(Response.Status.BAD_REQUEST.getStatusCode(),
                errorCode, localeCode);
    }

    /**
     * Builds general Open Tenure exception with HTTP 400 code.
     *
     * @param errorCode Code of message from {@link ServiceMessage}
     * @param localeCode Locale code (e.g. en-US)
     * @param params List parameters to substitute in the message
     * @return
     */
    public static OTRestException buildGeneralException(
            String errorCode,
            String localeCode,
            Object[] params) {
        return buildGeneralException(Response.Status.BAD_REQUEST.getStatusCode(),
                errorCode, localeCode, params);
    }

    /**
     * Builds general unexpected exception. HTTP 400 (BAD_REQUEST)
     *
     * @param localeCode Locale code (e.g. en-US)
     * @see ServiceMessage#GENERAL_UNEXPECTED
     * @return
     */
    public static OTRestException buildUnexpected(String localeCode) {
        return buildGeneralException(Response.Status.BAD_REQUEST.getStatusCode(),
                ServiceMessage.GENERAL_UNEXPECTED, localeCode);
    }

    /**
     * Builds unauthorized exception with HTTP 401 code (Unauthorized).
     *
     * @param localeCode Locale code (e.g. en-US)
     * @see ServiceMessage#EXCEPTION_AUTHENTICATION_FAILED
     * @return
     */
    public static OTRestException buildUnauthorized(String localeCode) {
        return buildGeneralException(Response.Status.UNAUTHORIZED.getStatusCode(),
                ServiceMessage.EXCEPTION_AUTHENTICATION_FAILED, localeCode);
    }

    /**
     * Builds unauthorized exception with HTTP 403 code (Forbidden).
     *
     * @param localeCode Locale code (e.g. en-US)
     * @see ServiceMessage#EXCEPTION_INSUFFICIENT_RIGHTS
     * @return
     */
    public static OTRestException buildForbidden(String localeCode) {
        return buildGeneralException(Response.Status.FORBIDDEN.getStatusCode(),
                ServiceMessage.EXCEPTION_INSUFFICIENT_RIGHTS, localeCode);
    }
    
    /**
     * Builds JSON conversion exception with HTTP 450 code (Custom code).
     *
     * @param localeCode Locale code (e.g. en-US)
     * @see ServiceMessage#OT_WS_BAD_JSON
     * @return
     */
    public static OTRestException buildBadJson(String localeCode) {
        return buildGeneralException(450, ServiceMessage.OT_WS_BAD_JSON, localeCode);
    }

    /**
     * Builds Not found exception with HTTP 404 code (Not found).
     *
     * @param localeCode Locale code (e.g. en-US)
     * @see ServiceMessage#OT_WS_NOT_FOUND
     * @return
     */
    public static OTRestException buildNotFound(String localeCode) {
        return buildGeneralException(Response.Status.NOT_FOUND.getStatusCode(),
                ServiceMessage.OT_WS_NOT_FOUND, localeCode);
    }

    /**
     * Builds Missing claim attachments exception with HTTP 451 code (Custom).
     *
     * @param localeCode Locale code (e.g. en-US)
     * @param ids List of missing attachment IDs
     * @see ServiceMessage#OT_WS_MISSING_CLAIM_ATTACHMENTS
     * @return
     */
    public static OTRestException buildMissingClaimAttachments(
            List<String> ids, String localeCode) {
        return new OTRestException(451, 
                buildMissingAttachmentResponse(
                        ids, 
                        ServiceMessage.OT_WS_MISSING_SERVER_ATTACHMENTS, 
                        localeCode));
    }
    
    /**
     * Builds account locked exception with HTTP 453 code (Custom code).
     *
     * @param localeCode Locale code (e.g. en-US)
     * @see ServiceMessage#GENERAL_ACCOUNT_LOCKED
     * @return
     */
    public static OTRestException buildAccountLocked(String localeCode) {
        return buildGeneralException(453, ServiceMessage.GENERAL_ACCOUNT_LOCKED, localeCode);
    }
    
    /**
     * Builds Missing server attachments exception with HTTP 452 code (Custom).
     *
     * @param localeCode Locale code (e.g. en-US)
     * @param ids List of missing attachment IDs
     * @see ServiceMessage#OT_WS_MISSING_SERVER_ATTACHMENTS
     * @return
     */
    public static OTRestException buildMissingServerAttachments(
            List<String> ids, String localeCode) {
        return new OTRestException(452, 
                buildMissingAttachmentResponse(
                        ids, 
                        ServiceMessage.OT_WS_MISSING_SERVER_ATTACHMENTS, 
                        localeCode));
    }
    
    private static String buildMissingAttachmentResponse(List<String> ids, String errorCode, String localeCode){
        String responseText = String.format(RESPONSE_MESSAGE, MessageUtility
                .getLocalizedMessageText(errorCode, localeCode));
        
        if (ids != null && ids.size() > 0) {
            String attachments = ",\"attachments\":[";
            int i = 0;
            for (String id : ids) {
                if(i>0){
                    attachments += ",";
                }
                attachments += "{\"id\":\"" + id + "\"}";
                i+=1;
            }
            attachments += "]";
            responseText += attachments;
        }
        return String.format("{%s}", responseText);
    }
    
    /**
     * Builds object exists exception with HTTP 454 code (Custom).
     * @param errorCode Error message code
     * @param localeCode Locale code (e.g. en-US)
     * @return
     */
    public static OTRestException buildObjectExist(String errorCode, String localeCode) {
        return buildGeneralException(454, errorCode, localeCode);
    }
    
    /**
     * Builds MD5 mismatch exception with HTTP 455 code (Custom).
     * @param localeCode Locale code (e.g. en-US)
     * @return
     */
    public static OTRestException buildMD5Error(String localeCode) {
        return buildGeneralException(455, ServiceMessage.GENERAL_WRONG_MD5, localeCode);
    }
    
    /**
     * Builds missing data exception with HTTP 456 code (Custom).
     * @param errorCode Error message code
     * @param localeCode Locale code (e.g. en-US)
     * @return
     */
    public static OTRestException buildMissingData(String errorCode, String localeCode) {
        return buildGeneralException(456, errorCode, localeCode);
    }
}
