package org.sola.opentenure.services.boundary.beans.security;

import java.io.IOException;
import java.io.Serializable;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.sola.opentenure.services.boundary.beans.helpers.ErrorKeys;
import org.sola.opentenure.services.boundary.beans.helpers.MessageProvider;
import org.sola.opentenure.services.boundary.beans.validation.user.LoginGroup;
import org.sola.cs.services.ejbs.admin.businesslogic.AdminCSEJBLocal;
import org.sola.opentenure.services.boundary.beans.project.ProjectBean;

@Named
@RequestScoped
public class LoginBean implements Serializable {

    @Inject
    MessageProvider msgProvider;
    
    @Inject
    ActiveUserBean activeUserBean;
    
    @Inject
    ProjectBean projectBean;
    
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
            // Check user can access the server
            if(!activeUserBean.getCanAccessServer()){
                // Log out user
                request.logout();
                context.addMessage(null, new FacesMessage(msgProvider.getErrorMessage(ErrorKeys.GENERAL_NO_ACCESS_TO_SERVER)));
                return;
            }
            // Check user has projects assigned
            projectBean.init();
            if(projectBean.getProjectNames().length < 1){
                // Log out user
                request.logout();
                context.addMessage(null, new FacesMessage(msgProvider.getErrorMessage(ErrorKeys.GENERAL_NO_ACCESS_TO_PROJECTS)));
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
            FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
            // Redirect
            context.getExternalContext().redirect(request.getContextPath() + "/login.xhtml");
        } catch (ServletException e) {
            context.addMessage(null, new FacesMessage(msgProvider.getErrorMessage(ErrorKeys.LOGIN_LOGOUT_FAILED)));
        }
    }
}
