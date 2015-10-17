package org.sola.opentenure.services.boundary.beans.responses;

import org.sola.common.DateUtility;
import org.sola.common.StringUtility;
import org.sola.cs.services.ejbs.claim.entities.Claim;

/**
 * List of static methods to generate Open Tenure success response in JSON
 * format.
 */
public class ResponseFactory {

    private static final String OK_MESSAGE = "\"result\":\"OK\"";

    /**
     * Builds general OK response.
     *
     * @return
     */
    public static final String buildOk() {
        return "{" + OK_MESSAGE + "}";
    }
    
    /**
     * Builds general response with result in a form {"result":"value"}.
     * @param value Value to embed into response.
     * @return
     */
    public static final String buildResultResponse(String value) {
        String response = "{\"result\":\"%s\"}";
        if(StringUtility.isEmpty(value)){
            return String.format(response, "");
        }
        return String.format(response, value);
    }

    /**
     * Builds successful claim submission response.
     *
     * @param claim object, which was successfully saved
     * @return
     */
    public static final String buildClaimSubmission(Claim claim) {
        String response = OK_MESSAGE;
        if (claim != null) {
            response += ",\"nr\": \"" + claim.getNr() + "\"";
            response += ",\"version\": " + claim.getVersion() + "";
            response += ",\"lodgementDate\":\""
                    + DateUtility.getDateInISO8601(claim.getLodgementDate())
                    + "\"";
            response += ",\"challengeExpiryDate\":\""
                    + DateUtility.getDateInISO8601(claim.getChallengeExpiryDate())
                    + "\"";
        }
        return String.format("{%s}", response);
    }
}
