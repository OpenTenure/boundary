package org.sola.opentenure.services.boundary.beans.security;

import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import org.hibernate.validator.constraints.NotEmpty;
import org.sola.common.RolesConstants;
import org.sola.opentenure.services.boundary.beans.AbstractModelBean;
import org.sola.opentenure.services.boundary.beans.helpers.ErrorKeys;
import org.sola.opentenure.services.boundary.beans.validation.Localized;
import org.sola.opentenure.services.boundary.beans.validation.user.LoginGroup;
import org.sola.cs.services.ejbs.admin.businesslogic.AdminCSEJBLocal;

/**
 * Stores user information
 */
@SessionScoped
@Named
public class ActiveUserBean extends AbstractModelBean {

    @EJB
    AdminCSEJBLocal adminEjb;
    
    private String username;
    private String password;
    
    @NotEmpty(message = ErrorKeys.LOGIN_USERNAME_REQUIRED, 
            payload = Localized.class, groups = {LoginGroup.class})
    public String getUserName() {
        return this.username;
    }

    public void setUserName(String username) {
        this.username = username;
    }

    @NotEmpty(message = ErrorKeys.LOGIN_PASSWORD_REQUIRED, 
            payload = Localized.class, groups = {LoginGroup.class})
    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
    
    public boolean isAuthenticated(){
        String userName = FacesContext.getCurrentInstance().getExternalContext().getRemoteUser();
        return userName != null && !userName.equals("");
    }
 
    /**
     * Creates a new instance of ActiveUser bean
     */
    public ActiveUserBean() {
    }

    public boolean getCanReviewClaim(){
        return adminEjb.isInRole(RolesConstants.CS_REVIEW_CLAIM);
    }
    
    public boolean getCanModerateClaim(){
        return adminEjb.isInRole(RolesConstants.CS_MODERATE_CLAIM);
    }
    
    public boolean getCanRecordClaim(){
        return adminEjb.isInRole(RolesConstants.CS_RECORD_CLAIM);
    }
    
    public boolean getCanViewReports(){
        return adminEjb.isInRole(RolesConstants.CS_VIEW_REPORTS);
    }
    
    public boolean getHasAdminRights(){
        return adminEjb.isUserAdmin();
    }
}
