package org.sola.opentenure.services.boundary.ws;

import java.security.Principal;
import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.FormParam;
import javax.ws.rs.Path;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.SecurityContext;
import org.sola.opentenure.services.boundary.beans.AbstractWebRestService;
import org.sola.opentenure.services.boundary.beans.exceptions.ExceptionFactory;
import org.sola.opentenure.services.boundary.beans.responses.ResponseFactory;
import org.sola.opentenure.services.boundary.beans.security.ActiveUserBean;
import org.sola.cs.services.ejbs.admin.businesslogic.AdminCSEJBLocal;

/**
 * Users authentication service
 *
 */
@Path("{localeCode: [a-zA-Z]{2}[-]{1}[a-zA-Z]{2}}/auth")
@RequestScoped
public class LoginResource extends AbstractWebRestService {

    @EJB
    AdminCSEJBLocal admin;

    /**
     * Authenticates users through HTTP GET method
     *
     * @param request HttpServletRequest request
     * @param sec Security context
     * @param userName User name
     * @param localeCode Locale code
     * @param userPass User password
     * @return
     */
    @GET
    @Produces("application/json")
    @Path(value = "login")
    public String loginGet(@Context HttpServletRequest request,
            @Context SecurityContext sec,
            @PathParam(value = LOCALE_CODE) String localeCode,
            @QueryParam("username") String userName,
            @QueryParam("password") String userPass) {
        try {
            Principal user = sec.getUserPrincipal();

            if (user == null) {
                authenticate(request, userName, userPass, localeCode);
            }
            return ResponseFactory.buildOk();
            
        } catch (Exception ex) {
            throw processException(ex, localeCode);
        }
    }

    /**
     * Authenticates users through HTTP POST method
     *
     * @param request HttpServletRequest request
     * @param localeCode Locale code
     * @param userName User name
     * @param sec Security context
     * @param userPass User password
     * @return
     */
    @POST
    @Produces("application/json")
    @Path(value = "login")
    public String loginPost(@Context HttpServletRequest request,
            @PathParam(value = LOCALE_CODE) String localeCode,
            @Context SecurityContext sec,
            @FormParam("username") String userName,
            @FormParam("password") String userPass) {
        try {
            Principal user = sec.getUserPrincipal();
            if (user == null) {
                authenticate(request, userName, userPass, localeCode);
            }
            return ResponseFactory.buildOk();
        } catch (ServletException ex) {
            throw processException(ex, localeCode);
        }
    }

    /**
     * Logs out users using HTTP GET method
     *
     * @param localeCode Locale code
     * @param request HttpServletRequest request
     * @return
     */
    @GET
    @Produces("application/json")
    @Path(value = "logout")
    public String logout(
            @PathParam(value = LOCALE_CODE) String localeCode,
            @Context HttpServletRequest request) {
        try {
            request.logout();
            return ResponseFactory.buildOk();
        } catch (ServletException ex) {
            throw processException(ex, localeCode);
        }
    }

    @Inject
    ActiveUserBean activeUser;

    private void authenticate(
            HttpServletRequest request,
            String userName,
            String userPass,
            String localeCode) throws ServletException {
        try {
            // Create session if it doesn't exist, otherwise client receives wrong session ID
            String sessionId = request.getSession(true).getId();
            request.login(userName, userPass);
        } catch (ServletException ex) {
            throw ExceptionFactory.buildUnauthorized(localeCode);
        }
        if (!admin.isUserActive(userName)) {
            // Log out user
            request.logout();
            throw ExceptionFactory.buildAccountLocked(localeCode);
        }
        activeUser.setUserName(userName);
        activeUser.setPassword(userPass);
    }
}
