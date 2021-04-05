package org.sola.opentenure.services.boundary.beans.security;

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.AjaxBehaviorEvent;
import javax.inject.Inject;
import javax.inject.Named;
import org.sola.opentenure.services.boundary.beans.helpers.ErrorKeys;
import org.sola.opentenure.services.boundary.beans.helpers.MessageBean;
import org.sola.opentenure.services.boundary.beans.helpers.MessagesKeys;
import org.sola.opentenure.services.boundary.beans.validation.user.PasswordChangeGroup;
import org.sola.opentenure.services.boundary.beans.validation.user.UserProfileGroup;
import org.sola.services.common.contracts.CsGenericTranslator;

/**
 * Serves Profile page needs
 */
@Named
@RequestScoped
public class ProfilePageBean extends RegistrationPageBean {

    @Inject
    MessageBean msg;

    private final String form = "/user/profile";
    private String oldPasswordValidation;

    public String getOldPasswordValidation() {
        return oldPasswordValidation;
    }

    @PostConstruct
    private void init(){
        //userBean = new UserBean(admin.getCurrentUser());
        CsGenericTranslator.getMapper().map(admin.getCurrentUser(), userBean);
    }
    
    public ProfilePageBean() {
        
    }

    public void validateOldPassword(AjaxBehaviorEvent event) {
        String error = userBean.getFirstError(userBean.validateProperty(userBean.getPropertyOldPassword(), PasswordChangeGroup.class));

        // Check password match
        if (error.equals("")) {
            if (!checkOldPassword()) {
                error = msgProvider.getErrorMessage(ErrorKeys.PROFILE_BAD_OLD_PASSWORD);
            }
        }
        oldPasswordValidation = prepareUIValidationResult(error, "divOldPassword");
    }

    public String saveProfile() {
        FacesContext context = FacesContext.getCurrentInstance();

        if (!validateProfileSave(context, true)) {
            return form;
        }

        // Try to update user
        try {
            runUpdate(new Runnable() {
                @Override
                public void run() {
                    admin.saveCurrentUser(userBean.getUser());
                }
            });
        } catch (Exception e) {
            context.addMessage(null, new FacesMessage(msgProvider.getErrorMessage(ErrorKeys.GENERAL_UNEXPECTED_ERROR)));
            return form;
        }

        msg.setSuccessMessage(msgProvider.getMessage(MessagesKeys.PROFILEPAGE_PROFILE_UPDATED));
        return form;
    }

    public String changePassword() {
        final FacesContext context = FacesContext.getCurrentInstance();

        // Reset username to be sure it's current user
        userBean.setUserName(context.getExternalContext().getRemoteUser());

        if (!validatePasswordChange(context, true)) {
            return form;
        }

        // Try to update password
        try {
            final boolean out[] = new boolean[1];

            runUpdate(new Runnable() {
                @Override
                public void run() {
                    out[0] = admin.changeCurrentUserPassword(userBean.getPassword());
                }
            });

            if (!out[0]) {
                context.addMessage(null, new FacesMessage(msgProvider.getErrorMessage(ErrorKeys.PROFILE_FAILED_CHANGE_PASSWORD)));
                return form;
            }
        } catch (Exception e) {
            context.addMessage(null, new FacesMessage(msgProvider.getErrorMessage(ErrorKeys.GENERAL_UNEXPECTED_ERROR)));
            return form;
        }

        msg.setSuccessMessage(msgProvider.getMessage(MessagesKeys.PROFILEPAGE_PASSWORD_CHANGED));
        return form;
    }

    private boolean validateProfileSave(FacesContext context, boolean showMessages) {
        boolean isValid = true;

        // Call user validation
        if (userBean.validate(showMessages, UserProfileGroup.class).size() > 0) {
            // Stop validation
            return false;
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

    @Override
    protected boolean checkEmailExists() {
        return admin.isUserEmailExists(userBean.getEmail(), userBean.getUserName());
    }

    private boolean validatePasswordChange(FacesContext context, boolean showMessages) {
        boolean isValid = true;

        // Call user validation
        if (userBean.validate(showMessages, PasswordChangeGroup.class).size() > 0) {
            // Stop validation
            return false;
        }

        // Check old password
        if (!checkOldPassword()) {
            if (showMessages) {
                context.addMessage(null, new FacesMessage(msgProvider.getErrorMessage(ErrorKeys.PROFILE_BAD_OLD_PASSWORD)));
            }
            isValid = false;
        }

        return isValid;
    }

    private boolean checkOldPassword() {
        return admin.checkCurrentUserPassword(userBean.getOldPassword());
    }
}
