package org.sola.opentenure.services.boundary.beans.security;

import java.io.IOException;
import java.util.logging.Level;
import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.faces.application.FacesMessage;
import javax.faces.event.AjaxBehaviorEvent;
import javax.inject.Inject;
import javax.inject.Named;
import org.sola.common.StringUtility;
import org.sola.opentenure.services.boundary.beans.AbstractBackingBean;
import org.sola.opentenure.services.boundary.beans.helpers.CaptchaImage;
import org.sola.opentenure.services.boundary.beans.helpers.ErrorKeys;
import org.sola.opentenure.services.boundary.beans.helpers.MessageProvider;
import org.sola.opentenure.services.boundary.beans.validation.user.UserRegistrationGroup;
import org.sola.services.common.LocalInfo;
import org.sola.services.common.logging.LogUtility;
import org.sola.cs.services.ejbs.admin.businesslogic.AdminCSEJBLocal;

/**
 * Serves password restore page
 */
@Named
@RequestScoped
public class PasswordForgotPageBean extends AbstractBackingBean {

    @Inject
    MessageProvider msgProvider;

    @Inject
    protected UserBean userBean;

    @EJB
    AdminCSEJBLocal adminEjb;

    private String captcha;
    private String emailValidation;
    private final String pwdForgotForm = "/user/forgotpwd";

    public String getEmailValidation() {
        return emailValidation;
    }

    public UserBean getUserBean() {
        return userBean;
    }

    public void setUserBean(UserBean userBean) {
        this.userBean = userBean;
    }

    public String getCaptcha() {
        return captcha;
    }

    public void setCaptcha(String captcha) {
        this.captcha = captcha;
    }

    public boolean getShowRestorePassword() {
        return !getShowSendConfirmation();
    }

    public boolean getShowSendConfirmation() {
        return StringUtility.empty(getRequestParam("action")).equalsIgnoreCase("sent");
    }

    public void validateEmail(AjaxBehaviorEvent event) {
        emailValidation = validateUI(userBean, userBean.getPropertyEmail(), "divEmail", UserRegistrationGroup.class);
    }

    public PasswordForgotPageBean() {
        super();
    }

    public String sendRestoreLink() {
        String c = (String) getSession().getAttribute(CaptchaImage.CAPTCHA_KEY);

        // Check captcha
        if (StringUtility.isEmpty(captcha) || !captcha.equals(c)) {
            getContext().addMessage(null, new FacesMessage(msgProvider.getErrorMessage(ErrorKeys.USER_REGISTRATION_WRONG_CAPTCHA)));
            return pwdForgotForm;
        }

        // Check email
        if (StringUtility.isEmpty(userBean.getEmail())) {
            getContext().addMessage(null, new FacesMessage(msgProvider.getErrorMessage(ErrorKeys.USER_REGISTRATION_EMAIL_REQUIRED)));
            return pwdForgotForm;
        }

        if (!adminEjb.isUserEmailExists(userBean.getEmail())) {
            getContext().addMessage(null, new FacesMessage(msgProvider.getErrorMessage(ErrorKeys.PASSWORD_RESTORE_EMAIL_NOT_FOUND)));
            return pwdForgotForm;
        }

        if (!adminEjb.isUserActiveByEmail(userBean.getEmail())) {
            getContext().addMessage(null, new FacesMessage(msgProvider.getErrorMessage(ErrorKeys.PASSWORD_RESTORE_ACCOUNT_LOCKED)));
            return pwdForgotForm;
        }

        // Send email
        try {
            runUpdate(new Runnable() {
                @Override
                public void run() {
                    LocalInfo.setBaseUrl(getApplicationUrl());
                    adminEjb.restoreUserPassword(userBean.getEmail());
                    try {
                        getExtContext().redirect(getRequest().getContextPath() + "/user/forgotpwd.xhtml?action=sent");
                    } catch (IOException ex) {
                        getContext().addMessage(null, new FacesMessage(msgProvider.getErrorMessage(ErrorKeys.GENERAL_UNEXPECTED_ERROR)));
                        LogUtility.log(ex.getLocalizedMessage(), Level.SEVERE);
                    }
                }
            });
        } catch (Exception e) {
            getContext().addMessage(null, new FacesMessage(msgProvider.getErrorMessage(ErrorKeys.GENERAL_UNEXPECTED_ERROR)));
            LogUtility.log(e.getLocalizedMessage(), Level.SEVERE);
        }

        return pwdForgotForm;
    }
}
