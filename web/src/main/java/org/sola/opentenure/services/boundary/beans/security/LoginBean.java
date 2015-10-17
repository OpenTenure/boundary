package org.sola.opentenure.services.boundary.beans.security;

import java.io.IOException;
import java.io.Serializable;
import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.inject.Inject;
import javax.inject.Named;
import org.sola.opentenure.services.boundary.beans.helpers.ErrorKeys;
import org.sola.opentenure.services.boundary.beans.helpers.MessageProvider;
import org.sola.opentenure.services.boundary.beans.validation.user.LoginGroup;
import org.sola.cs.services.ejbs.admin.businesslogic.AdminCSEJBLocal;

@Named
@RequestScoped
public class LoginBean implements Serializable {

    @Inject
    MessageProvider msgProvider;
    
    @Inject
    ActiveUserBean activeUserBean;
    
    @EJB
    AdminCSEJBLocal admin;
       
    public ActiveUserBean getActiveUserBean(){
        return activeUserBean;
    }
     
    public void setActiveUserBean(ActiveUserBean activeUser){
        this.activeUserBean = activeUser;
    }
     
    public void login() throws IOException {
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        
        if(!validate(context, true)){
            return;
        }
        
        try {
            request.login(activeUserBean.getUserName(), activeUserBean.getPassword());
            if(!admin.isUserActive(activeUserBean.getUserName())){
                // Log out user
                request.logout();
                context.addMessage(null, new FacesMessage(msgProvider.getErrorMessage(ErrorKeys.LOGIN_ACCOUNT_BLOCKED)));
                return;
            }
            // Redirect
            if(activeUserBean.getCanModerateClaim() || activeUserBean.getCanRecordClaim() || activeUserBean.getCanReviewClaim()){
                context.getExternalContext().redirect(request.getContextPath() + "/Dashboard.xhtml");
            } else {
                context.getExternalContext().redirect(request.getContextPath() + "/index.xhtml");
            }
        } catch (ServletException e) {
            context.addMessage(null, new FacesMessage(msgProvider.getErrorMessage(ErrorKeys.LOGIN_FAILED)));
        }
    }
    
    private boolean validate(FacesContext context, boolean showMessage){
        boolean isValid = true;
        
        if(activeUserBean.validate(showMessage, LoginGroup.class).size() > 1){
            isValid = false;
        }
        
        return isValid;
    }
    
    public void logout() throws IOException {
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        try {
            request.logout();
            // Redirect
            context.getExternalContext().redirect(request.getContextPath() + "/login.xhtml");
        } catch (ServletException e) {
            context.addMessage(null, new FacesMessage(msgProvider.getErrorMessage(ErrorKeys.LOGIN_LOGOUT_FAILED)));
        }
    }
}
