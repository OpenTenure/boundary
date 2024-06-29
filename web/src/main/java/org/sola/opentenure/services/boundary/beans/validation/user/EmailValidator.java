package org.sola.opentenure.services.boundary.beans.validation.user;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.validator.FacesValidator;
import jakarta.faces.validator.Validator;
import jakarta.faces.validator.ValidatorException;
import org.sola.opentenure.services.boundary.beans.helpers.ErrorKeys;
import org.sola.opentenure.services.boundary.beans.helpers.MessageProvider;

/**
 * Validates email fields
 */
@FacesValidator("emailValidator")
public class EmailValidator implements Validator {

    private final Pattern pattern;
    private Matcher matcher;
    private final MessageProvider msgProvider;
    private static final String EMAIL_PATTERN
            = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
            + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

    public EmailValidator() {
        pattern = Pattern.compile(EMAIL_PATTERN);
        msgProvider = new MessageProvider();
    }

    @Override
    public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException {
        matcher = pattern.matcher(value.toString());

        if (!matcher.matches()) {
            FacesMessage msg = new FacesMessage(msgProvider.getMessage(
                    ErrorKeys.USER_REGISTRATION_BAD_EMAIL), 
                    msgProvider.getErrorMessage(ErrorKeys.USER_REGISTRATION_BAD_EMAIL));
            msg.setSeverity(FacesMessage.SEVERITY_ERROR);
            throw new ValidatorException(msg);
        }
    }

}
