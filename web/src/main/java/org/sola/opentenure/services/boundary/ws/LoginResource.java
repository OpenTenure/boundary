package org.sola.opentenure.services.boundary.ws;

import java.security.Principal;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.SecurityContext;
import org.sola.cs.services.ejb.system.businesslogic.SystemCSEJBLocal;
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
    @EJB
    SystemCSEJBLocal systemEjb;

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

    @GET
    @Produces("text/plain")
    @Path(value = "getcurrentuser")
    public String getCurrentUser(
            @Context HttpServletRequest request,
            @PathParam(value = LOCALE_CODE) String localeCode) {
        try {
            return request.getRemoteUser();
        } catch (Exception ex) {
            throw processException(ex, localeCode);
        }
    }
    
    @GET
    @Produces("text/plain")
    @Path(value = "{a:canaccessproject|a:canAccessProject}/{projectId}")
    public String checkUserCanAccessProject(
            @PathParam(value = LOCALE_CODE) String localeCode, @PathParam(value = "projectId") String projectId) {
        try {
            return getMapper().writeValueAsString(systemEjb.canAccessProject(projectId));
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
