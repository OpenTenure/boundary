package org.sola.opentenure.services.boundary.beans.security;

import java.io.IOException;
import java.util.logging.Level;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.faces.application.FacesMessage;
import javax.faces.event.AjaxBehaviorEvent;
import javax.inject.Inject;
import javax.inject.Named;
import org.sola.common.StringUtility;
import org.sola.opentenure.services.boundary.beans.AbstractBackingBean;
import org.sola.opentenure.services.boundary.beans.helpers.ErrorKeys;
import org.sola.opentenure.services.boundary.beans.helpers.MessageProvider;
import org.sola.opentenure.services.boundary.beans.validation.user.PasswordRestoreGroup;
import org.sola.opentenure.services.boundary.beans.validation.user.UserRegistrationGroup;
import org.sola.services.common.LocalInfo;
import org.sola.services.common.logging.LogUtility;
import org.sola.cs.services.ejbs.admin.businesslogic.AdminCSEJBLocal;
import org.sola.cs.services.ejbs.admin.businesslogic.repository.entities.User;

/**
 * Serves password restore page
 */
@Named
@RequestScoped
public class PasswordRestorePageBean extends AbstractBackingBean {

    @Inject
    MessageProvider msgProvider;

    @Inject
    protected UserBean userBean;

    @EJB
    AdminCSEJBLocal adminEjb;

    private String restoreCode;
    private final String pwdRestoreForm = "/user/pwdrestore";

    @PostConstruct
    private void init() {
        restoreCode = getRequestParam("code");
    }

    public String getRestoreCode() {
        return restoreCode;
    }

    public void setRestoreCode(String restoreCode) {
        this.restoreCode = restoreCode;
    }

    public UserBean getUserBean() {
        return userBean;
    }

    public void setUserBean(UserBean userBean) {
        this.userBean = userBean;
    }

    public boolean getShowPasswordChange() {
        if (!getShowPasswordChangeConfirmation()) {
            return checkUser();
        }
        return false;
    }

    private boolean checkUser() {
        if (StringUtility.isEmpty(restoreCode)) {
            return false;
        }

        User user = adminEjb.getUserByActivationCode(restoreCode);
        if (user == null) {
            getContext().addMessage(null, new FacesMessage(msgProvider.getErrorMessage(ErrorKeys.PASSWORD_RESTORE_RESTORE_CODE_NOT_FOUND)));
            return false;
        }
        if (!user.isActive()) {
            getContext().addMessage(null, new FacesMessage(msgProvider.getErrorMessage(ErrorKeys.PASSWORD_RESTORE_ACCOUNT_LOCKED)));
            return false;
        }
        return true;
    }

    public boolean getShowPasswordChangeConfirmation() {
        return StringUtility.empty(getRequestParam("action")).equalsIgnoreCase("changed");
    }

    public String formatChangeConfirmation(String msg){
        return String.format(msg, getRequest().getContextPath() + "/login.xhtml");
    }
    
    public PasswordRestorePageBean() {
        super();
    }

    public String changePassword() {
        try {
            if (userBean.validate(true, PasswordRestoreGroup.class).size() <= 0 && checkUser()) {
                runUpdate(new Runnable() {
                    @Override
                    public void run() {
                        LocalInfo.setBaseUrl(getApplicationUrl());
                        adminEjb.changePasswordByRestoreCode(restoreCode, userBean.getPassword());
                        try {
                            getExtContext().redirect(getRequest().getContextPath() + "/user/pwdrestore.xhtml?action=changed");
                        } catch (IOException ex) {
                            getContext().addMessage(null, new FacesMessage(msgProvider.getErrorMessage(ErrorKeys.GENERAL_UNEXPECTED_ERROR)));
                            LogUtility.log(ex.getLocalizedMessage(), Level.SEVERE);
                        }
                    }
                });
            }
        } catch (Exception e) {
            getContext().addMessage(null, new FacesMessage(msgProvider.getErrorMessage(ErrorKeys.GENERAL_UNEXPECTED_ERROR)));
            LogUtility.log(e.getLocalizedMessage(), Level.SEVERE);
        }
        return pwdRestoreForm;
    }
}
