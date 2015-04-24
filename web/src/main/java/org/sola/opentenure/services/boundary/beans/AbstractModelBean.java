package org.sola.opentenure.services.boundary.beans;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Set;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import org.sola.opentenure.services.boundary.beans.validation.ValidatorFactory;

/**
 * Abstract class for all model beans
 */
public abstract class AbstractModelBean implements Serializable {
    @Inject 
    ValidatorFactory validatorFactory;
    
    public AbstractModelBean(){
        
    }
    
    /**
     * Validates current object instance.
     *
     * @param <T> Type of {@link AbstractModelBean}
     * @param showMessage Boolean value indicating whether to display message in
     * case of validation violations.
     * @param group List of validation groups.
     * @return 
     */
    public <T extends AbstractModelBean> Set<ConstraintViolation<T>> validate(boolean showMessage, Class<?>... group) {
        T bean = (T) this;
        Set<ConstraintViolation<T>> warningsList = validatorFactory.getValidator().validate(bean, group);

        if (showMessage) {
            showMessage(warningsList);
        }
        return warningsList;
    }

    /** 
     * Validates bean's property and returns set of violations if any
     * @param <T> Bean type
     * @param propertyName Property name
     * @param group List of validation groups. 
     * @return 
     */
    public <T extends AbstractModelBean> Set<ConstraintViolation<T>> validateProperty(String propertyName, Class<?>... group){
        return validatorFactory.getValidator().validateProperty((T)this, propertyName, group);
    }
    
    /** 
     * Returns first violation from provided list
     * @param <T> Bean type
     * @param warningsList List of violations
     * @return 
     */
    public <T extends AbstractModelBean> String getFirstError(Set<ConstraintViolation<T>> warningsList){
        if (warningsList.size() > 0) {
            for (Iterator<ConstraintViolation<T>> it = warningsList.iterator(); it.hasNext();) {
                ConstraintViolation<T> constraintViolation = it.next();
                return constraintViolation.getMessage();
            }
        }
        return "";
    }
    
    /**
     * Shows validation message by adding messages to the FacesContext.
     *
     * @param <T> Type of {@link AbstractModelBean}
     * @param warningsList The list of validation violations to show on the
     * message.
     */
    public <T extends AbstractModelBean> void showMessage(Set<ConstraintViolation<T>> warningsList) {
        if (warningsList.size() > 0) {
            FacesContext context = FacesContext.getCurrentInstance();

            for (Iterator<ConstraintViolation<T>> it = warningsList.iterator(); it.hasNext();) {
                ConstraintViolation<T> constraintViolation = it.next();
                context.addMessage(null, new FacesMessage(constraintViolation.getMessage()));
            }
        }
    }
}
