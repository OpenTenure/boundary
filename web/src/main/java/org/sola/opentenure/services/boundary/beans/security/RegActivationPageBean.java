package org.sola.opentenure.services.boundary.beans.security;

import java.io.IOException;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletRequest;
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

    public String activate() throws IOException {
        String activationForm = "/user/regactivation";
        return activationForm;
    }
}
