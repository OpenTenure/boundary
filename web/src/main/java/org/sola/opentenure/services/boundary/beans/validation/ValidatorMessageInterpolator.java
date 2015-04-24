/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.sola.opentenure.services.boundary.beans.validation;

import java.io.Serializable;
import java.util.Locale;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.validation.Configuration;
import javax.validation.MessageInterpolator;
import javax.validation.Validation;
import org.sola.opentenure.services.boundary.beans.helpers.MessageProvider;

/**
 * Message interpolator to support localization messages while bean validation
 */
@Named
@SessionScoped
public class ValidatorMessageInterpolator extends org.hibernate.validator.messageinterpolation.ResourceBundleMessageInterpolator implements
        MessageInterpolator, Serializable {

    @Inject
    private MessageProvider msgProvider;
    private final MessageInterpolator defaultMessageInterpolator;

    public ValidatorMessageInterpolator() {
        Configuration<?> config = Validation.byDefaultProvider().configure();
        defaultMessageInterpolator = config.getDefaultMessageInterpolator();
    }

    @Override
    public String interpolate(String messageTemplate, MessageInterpolator.Context context) {
        String message;
        if (context.getConstraintDescriptor().getPayload() != null
                && context.getConstraintDescriptor().getPayload().contains(Localized.class)) {
            // Retrieve message from bundle
            message = msgProvider.getErrorMessage(messageTemplate);
        } else {
            message = defaultMessageInterpolator.interpolate(messageTemplate, context);
        }
        return message;
    }

    @Override
    public String interpolate(String messageTemplate, MessageInterpolator.Context context, Locale locale) {
        String message;
        if (context.getConstraintDescriptor().getPayload() != null
                && context.getConstraintDescriptor().getPayload().contains(Localized.class)) {
            // Retrieve message from bundle
            message = msgProvider.getErrorMessage(messageTemplate);
        } else {
            message = defaultMessageInterpolator.interpolate(messageTemplate, context);
        }
        return message;
    }
}
