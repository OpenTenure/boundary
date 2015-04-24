package org.sola.opentenure.services.boundary.beans.admin;

import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.sola.common.RolesConstants;
import org.sola.opentenure.services.boundary.beans.AbstractBackingBean;
import org.sola.opentenure.services.boundary.beans.helpers.ErrorKeys;
import org.sola.opentenure.services.boundary.beans.helpers.MessageProvider;
import org.sola.opentenure.services.boundary.beans.map.MapSettingsBean;
import org.sola.opentenure.services.boundary.beans.referencedata.ReferenceData;
import org.sola.services.common.logging.LogUtility;
import org.sola.services.ejb.cache.businesslogic.CacheEJBLocal;

/**
 * Contains methods to reset cache objects
 */
@Named
@RequestScoped
public class CacheResetPageBean extends AbstractBackingBean {

    @Inject
    ReferenceData refData;

    @Inject
    MapSettingsBean mapSettings;

    @Inject
    MessageProvider msgProvider;
    
    @EJB
    CacheEJBLocal cacheEjb;
    
    public void resetCache() {
        // Check user rights
        if (!isInRole(RolesConstants.ADMIN_MANAGE_SETTINGS)) {
            // Redirect to the index page
            try {
                getContext().getExternalContext().redirect(getRequest().getContextPath() + "/index.xhtml");
            } catch (Exception e) {
                LogUtility.log(msgProvider.getMessage(ErrorKeys.GENERAL_REDIRECT_FAILED), e);
            }
        }

        refData.resetCache();
        mapSettings.init();
        cacheEjb.clearAll();
    }
}
