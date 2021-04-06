package org.sola.opentenure.services.boundary.beans.security;

import java.io.IOException;
import java.util.logging.Level;
import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.AjaxBehaviorEvent;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import org.sola.opentenure.services.boundary.beans.AbstractBackingBean;
import org.sola.opentenure.services.boundary.beans.helpers.CaptchaImage;
import org.sola.opentenure.services.boundary.beans.helpers.ErrorKeys;
import org.sola.opentenure.services.boundary.beans.helpers.MessageProvider;
import org.sola.opentenure.services.boundary.beans.validation.user.UserRegistrationGroup;
import org.sola.services.common.LocalInfo;
import org.sola.services.common.logging.LogUtility;
import org.sola.cs.services.ejbs.admin.businesslogic.AdminCSEJBLocal;
import org.sola.cs.services.ejbs.admin.businesslogic.repository.entities.User;

/**
 * Provides method and listeners for user registration page
 */
@Named
@RequestScoped
public class RegistrationPageBean extends AbstractBackingBean {

    @EJB
    protected AdminCSEJBLocal admin;

    @Inject
    protected UserBean userBean;

    @Inject
    MessageProvider msgProvider;

    private String captchaValue;
    private String userNameValidation;
    private String passwordValidation;
    private String passwordConfirmationValidation;
    private String firstNameValidation;
    private String lastNameValidation;
    private String emailValidation;
    private String mobileValidation;
    private String captchaValidation;

    public String getCaptchaValue() {
        return captchaValue;
    }

    public String getPasswordValidation() {
        return passwordValidation;
    }

    public String getPasswordConfirmationValidation() {
        return passwordConfirmationValidation;
    }

    public String getFirstNameValidation() {
        return firstNameValidation;
    }

    public String getLastNameValidation() {
        return lastNameValidation;
    }

    public String getEmailValidation() {
        return emailValidation;
    }

    public String getMobileValidation() {
        return mobileValidation;
    }

    public String getCaptchaValidation() {
        return captchaValidation;
    }

    public void setCaptchaValue(String captchaValue) {
        this.captchaValue = captchaValue;
    }

    public UserBean getUserBean() {
        return userBean;
    }

    public void setUserBean(UserBean userBean) {
        this.userBean = userBean;
    }

    public String getUserNameValidation() {
        return userNameValidation;
    }

    public void validateUserName(AjaxBehaviorEvent event) {
        String error = userBean.getFirstError(userBean.validateProperty(userBean.getPropertyUserName(), UserRegistrationGroup.class));

        // Check user name
        if (error.equals("")) {
            if (checkUserNameExists()) {
                error = msgProvider.getErrorMessage(ErrorKeys.USER_REGISTRATION_USER_EXISTS);
            }
        }
        userNameValidation = prepareUIValidationResult(error, "divUserName");
    }
     
    public void validateFirstName(AjaxBehaviorEvent event) {
        firstNameValidation = validateUI(userBean, userBean.getPropertyFirstName(), "divFirstName", UserRegistrationGroup.class);   
    }

    public void validateLastName(AjaxBehaviorEvent event) {
        lastNameValidation = validateUI(userBean, userBean.getPropertyLastName(), "divLastName", UserRegistrationGroup.class);
    }

    public void validateMobile(AjaxBehaviorEvent event) {
        mobileValidation = validateUI(userBean, userBean.getPropertyMobilNumber(), "divMobileNumber", UserRegistrationGroup.class);
    }

    public void validatePassword(AjaxBehaviorEvent event) {
        passwordValidation = validateUI(userBean, userBean.getPropertyPassword(), "divPassword", UserRegistrationGroup.class);
        validatePasswordConfirmation(event);
    }

    public void validateEmail(AjaxBehaviorEvent event) {
        String error = userBean.getFirstError(userBean.validateProperty(userBean.getPropertyEmail(), UserRegistrationGroup.class));

        // Check email existence
        if (error.equals("")) {
            if (checkEmailExists()) {
                error = msgProvider.getErrorMessage(ErrorKeys.USER_REGISTRATION_EMAIL_EXISTS);
            }
        }
        emailValidation = prepareUIValidationResult(error, "divEmail");
    }

    public void validateCaptcha(AjaxBehaviorEvent event) {
        String error = "";

        if (captchaValue == null || captchaValue.equals("")) {
            error = msgProvider.getErrorMessage(ErrorKeys.USER_REGISTRATION_CAPTCHA_REQUIRED);
        }

        // Check captcha
        if (error.equals("")) {
            String c = (String) getSession().getAttribute(CaptchaImage.CAPTCHA_KEY);
            if (!captchaValue.equals(c)) {
                error = msgProvider.getErrorMessage(ErrorKeys.USER_REGISTRATION_WRONG_CAPTCHA);
            }
        }
        captchaValidation = prepareUIValidationResult(error, "divCaptcha");
    }

    public void validatePasswordConfirmation(AjaxBehaviorEvent event) {
        String error = userBean.getFirstError(userBean.validateProperty(userBean.getPropertyPasswordRepeat(), UserRegistrationGroup.class));

        // Check password match
        if (error.equals("")) {
            if (userBean.getPassword() == null
                    || !userBean.getPassword().equals(userBean.getPasswordRepeat())) {
                error = msgProvider.getErrorMessage(ErrorKeys.USER_REGISTRATION_PASSWORDS_MISMATCH);
            }
        }
        passwordConfirmationValidation = prepareUIValidationResult(error, "divPasswordConfirmation");
    }

    /**
     * Default constructor
     */
    public RegistrationPageBean() {
    }

    /**
     * Registers new user (Community Recorder)
     *
     * @return
     */
    public String register() throws IOException {
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        String regForm = "/user/registration";

        if (!validate(context, true)) {
            return regForm;
        }

        // Try to register user
        try {
            runUpdate(new Runnable() {
                @Override
                public void run() {
                    LocalInfo.setBaseUrl(getApplicationUrl());
                    User user = admin.createCommunityUser(userBean.getUser());
                }
            });
        } catch (Exception e) {
            context.addMessage(null, new FacesMessage(msgProvider.getErrorMessage(ErrorKeys.GENERAL_UNEXPECTED_ERROR)));
            LogUtility.log(e.getLocalizedMessage(), Level.SEVERE);
            return regForm;
        }

        context.getExternalContext().redirect(request.getContextPath() + "/user/regconfirmation.xhtml");
        return regForm;
    }

    private boolean validate(FacesContext context, boolean showMessages) {

        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        boolean isValid = true;

        // Call user validation
        if (userBean.validate(showMessages, UserRegistrationGroup.class).size() > 0) {
            // Stop validation
            return false;
        }

        // Check captcha
        javax.servlet.http.HttpSession session = request.getSession();
        String c = (String) session.getAttribute(CaptchaImage.CAPTCHA_KEY);

        if (!captchaValue.equals(c)) {
            if (showMessages) {
                context.addMessage(null, new FacesMessage(msgProvider.getErrorMessage(ErrorKeys.USER_REGISTRATION_WRONG_CAPTCHA)));
            }
            isValid = false;
        }

        // Do extra checks
        // Check user name
        if (checkUserNameExists()) {
            if (showMessages) {
                context.addMessage(null, new FacesMessage(msgProvider.getErrorMessage(ErrorKeys.USER_REGISTRATION_USER_EXISTS)));
            }
            isValid = false;
        }

        // Check email address
        if (checkEmailExists()) {
            if (showMessages) {
                context.addMessage(null, new FacesMessage(msgProvider.getErrorMessage(ErrorKeys.USER_REGISTRATION_EMAIL_EXISTS)));
            }
            isValid = false;
        }

        return isValid;
    }

    protected boolean checkEmailExists() {
        return admin.isUserEmailExists(userBean.getEmail());
    }

    private boolean checkUserNameExists() {
        return admin.isUserNameExists(userBean.getUserName());
    }
}
