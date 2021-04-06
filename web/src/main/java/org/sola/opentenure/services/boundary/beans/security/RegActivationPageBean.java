package org.sola.opentenure.services.boundary.beans.security;

import java.io.IOException;
import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import org.sola.opentenure.services.boundary.beans.AbstractBackingBean;
import org.sola.opentenure.services.boundary.beans.helpers.CaptchaImage;
import org.sola.opentenure.services.boundary.beans.helpers.ErrorKeys;
import org.sola.opentenure.services.boundary.beans.helpers.MessageProvider;
import org.sola.cs.services.ejbs.admin.businesslogic.AdminCSEJBLocal;

/**
 * Serves Regactivation page
 */
@Named
@RequestScoped
public class RegActivationPageBean extends AbstractBackingBean {

    @Inject
    MessageProvider msgProvider;

    @EJB
    AdminCSEJBLocal admin;

    private String captchaValue;
    private String activationCode;
    private String userName;
    private boolean activated = false;

    public boolean isActivated() {
        return activated;
    }

    public String getActivationCode() {
        return activationCode;
    }

    public void setActivationCode(String activationCode) {
        this.activationCode = activationCode;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getCaptchaValue() {
        return captchaValue;
    }

    public void setCaptchaValue(String captchaValue) {
        this.captchaValue = captchaValue;
    }

    public void pageLoad() {
        if (!isPostback()) {
            activationCode = getRequestParam("code");
            userName = getRequestParam("user");
        }
    }

    public RegActivationPageBean() {

    }

    public String activate() throws IOException {
        final FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        String activationForm = "/user/regactivation";

        if (activated) {
            return activationForm;
        }

        // Check captcha
        javax.servlet.http.HttpSession session = request.getSession();
        String c = (String) session.getAttribute(CaptchaImage.CAPTCHA_KEY);
        if (!captchaValue.equals(c)) {
            context.addMessage(null, new FacesMessage(msgProvider.getErrorMessage(ErrorKeys.USER_REGISTRATION_WRONG_CAPTCHA)));
            return activationForm;
        }

        // Activate
        try {
            runUpdate(new Runnable() {
                @Override
                public void run() {
//                    if (admin.activeteCommuninityRecorderUser(getUserName(), getActivationCode())) {
//                        activated = true;
//                    } else {
//                        context.addMessage(null, new FacesMessage(msgProvider.getErrorMessage(ErrorKeys.USER_ACTIVATION_ACTIVATION_FAILED)));
//                    }
                }
            });
        } catch (Exception e) {
            context.addMessage(null, new FacesMessage(msgProvider.getErrorMessage(ErrorKeys.GENERAL_UNEXPECTED_ERROR)));
        }

        return activationForm;
    }
}
