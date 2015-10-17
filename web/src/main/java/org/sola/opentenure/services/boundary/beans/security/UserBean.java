package org.sola.opentenure.services.boundary.beans.security;

import javax.enterprise.context.RequestScoped;
import javax.inject.Named;
import javax.validation.constraints.Pattern;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotEmpty;
import org.sola.opentenure.services.boundary.beans.AbstractModelBean;
import org.sola.opentenure.services.boundary.beans.helpers.ErrorKeys;
import org.sola.opentenure.services.boundary.beans.validation.Localized;
import org.sola.opentenure.services.boundary.beans.validation.user.AccountActivationGroup;
import org.sola.opentenure.services.boundary.beans.validation.user.LoginGroup;
import org.sola.opentenure.services.boundary.beans.validation.user.PasswordChangeGroup;
import org.sola.opentenure.services.boundary.beans.validation.user.PasswordRestoreGroup;
import org.sola.opentenure.services.boundary.beans.validation.user.PasswordsCheck;
import org.sola.opentenure.services.boundary.beans.validation.user.UserProfileGroup;
import org.sola.opentenure.services.boundary.beans.validation.user.UserRegistrationGroup;
import org.sola.cs.services.ejbs.admin.businesslogic.repository.entities.User;

/**
 * Wrapper user bean, exposing properties of
 * {@link org.sola.cs.services.ejbs.admin.businesslogic.repository.entities.User} 
 * and some other supplementary properties 
 */
@Named
@RequestScoped
@PasswordsCheck(message = ErrorKeys.USER_REGISTRATION_PASSWORDS_MISMATCH, payload = Localized.class,
        groups = {UserRegistrationGroup.class, LoginGroup.class, PasswordChangeGroup.class, PasswordRestoreGroup.class})
public class UserBean extends AbstractModelBean {

    private User user;
    private String passwordRepeat;
    private String oldPassword;

    /** returns userName property name */
    public String getPropertyUserName(){
        return "userName";
    }
    
    /** returns firstName property name */
    public String getPropertyFirstName(){
        return "firstName";
    }
    
    /** returns lastName property name */
    public String getPropertyLastName(){
        return "lastName";
    }
    
    /** returns mobileNumber property name */
    public String getPropertyMobilNumber(){
        return "mobileNumber";
    }
    
    /** returns password property name */
    public String getPropertyPassword(){
        return "password";
    }
    
    /** returns passwordRepeat property name */
    public String getPropertyPasswordRepeat(){
        return "passwordRepeat";
    }
    
    /** returns oldPassword property name */
    public String getPropertyOldPassword(){
        return "oldPassword";
    }
    
    /** returns email property name */
    public String getPropertyEmail(){
        return "email";
    }
    
    /** returns description property name */
    public String getPropertyDescription(){
        return "description";
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    @Length(min = 3, max = 20, message = ErrorKeys.USER_REGISTRATION_USERNAME_LENGTH, 
            payload = Localized.class, groups = {UserRegistrationGroup.class})
    @NotEmpty(message = ErrorKeys.LOGIN_USERNAME_REQUIRED, 
            payload = Localized.class, groups = {UserRegistrationGroup.class, LoginGroup.class})
    public String getUserName() {
        return user.getUserName();
    }

    public void setUserName(String userName){
        user.setUserName(userName);
    }
    
    @NotEmpty(message = ErrorKeys.LOGIN_PASSWORD_REQUIRED, payload = Localized.class, 
            groups = {UserRegistrationGroup.class, LoginGroup.class, 
                PasswordChangeGroup.class, PasswordRestoreGroup.class})
    public String getPassword() {
        return user.getPassword();
    }

    public void setPassword(String password){
        user.setPassword(password);
    }

    @NotEmpty(message = ErrorKeys.PROFILE_CURRENT_PASSWORD_REQUIRED, 
            payload = Localized.class, groups = {PasswordChangeGroup.class})
    public String getOldPassword() {
        return oldPassword;
    }

    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }
    
    @NotEmpty(message = ErrorKeys.USER_REGISTRATION_FIRSTNAME_REQUIRED, 
            payload = Localized.class, groups = {UserRegistrationGroup.class, UserProfileGroup.class})
    public String getFirstName() {
        return user.getFirstName();
    }

    public void setFirstName(String firstName){
        user.setFirstName(firstName);
    }
    
    @NotEmpty(message = ErrorKeys.USER_REGISTRATION_LASTNAME_REQUIRED, 
            payload = Localized.class, groups = {UserRegistrationGroup.class, UserProfileGroup.class})
    public String getLastName() {
        return user.getLastName();
    }

    public void setLastName(String lastName){
        user.setLastName(lastName);
    }
    
    @NotEmpty(message = ErrorKeys.USER_REGISTRATION_EMAIL_REQUIRED, 
            payload = Localized.class, groups = {UserRegistrationGroup.class, UserProfileGroup.class})
    @Pattern(regexp = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
            + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$", 
            message = ErrorKeys.USER_REGISTRATION_BAD_EMAIL,
            payload = Localized.class, groups = {UserRegistrationGroup.class, UserProfileGroup.class})
    public String getEmail() {
        return user.getEmail();
    }

    public void setEmail(String email){
        user.setEmail(email);
    }
    
    @NotEmpty(message = ErrorKeys.USER_ACTIVATION_CODE_REQUIRED, 
            payload = Localized.class, groups = {AccountActivationGroup.class})
    public String getActivationCode() {
        return user.getActivationCode();
    }

    public void setActivationCode(String code){
        user.setActivationCode(code);
    }
    
    @NotEmpty(message = ErrorKeys.USER_REGISTRATION_MOBILE_REQUIRED, 
            payload = Localized.class, groups = {UserRegistrationGroup.class, UserProfileGroup.class})
    @Pattern(regexp = "^(([\\+\\(])*(\\d)+([\\d\\+\\-\\)\\(\\s\\.])*){4}$", 
            message = ErrorKeys.USER_REGISTRATION_BAD_MOBILE,
            payload = Localized.class, groups = {UserRegistrationGroup.class, UserProfileGroup.class})
    public String getMobileNumber(){
        return user.getMobileNumber();
    }
    
    public void setMobileNumber(String number){
        user.setMobileNumber(number);
    }

    @NotEmpty(message = ErrorKeys.USER_REGISTRATION_PASSWORD_CONFIRMATION_REQUIRED, 
            payload = Localized.class, groups = {UserRegistrationGroup.class, 
                PasswordChangeGroup.class, PasswordRestoreGroup.class})
    public String getPasswordRepeat() {
        return passwordRepeat;
    }

    public void setPasswordRepeat(String passwordRepeat) {
        this.passwordRepeat = passwordRepeat;
    }
    
    public String getDescription() {
        return user.getDescription();
    }

    public void setDescription(String description) {
        user.setDescription(description);
    }
    
    public UserBean() {
        user = new User();
    }
    
    public UserBean(User user) {
        this.user = user;
    }
}
