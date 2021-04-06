package org.sola.opentenure.services.boundary.beans.dashboard;

import java.util.List;
import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.sola.common.RolesConstants;
import org.sola.opentenure.services.boundary.beans.AbstractBackingBean;
import org.sola.opentenure.services.boundary.beans.language.LanguageBean;
import org.sola.cs.services.ejb.search.businesslogic.SearchCSEJBLocal;
import org.sola.cs.services.ejb.search.repository.entities.ClaimSearchResult;

/**
 * Provides method and listeners for dashboard
 */
@Named
@RequestScoped
public class DashboardBean extends AbstractBackingBean {
    @EJB
    SearchCSEJBLocal searchEjb;
    
    @Inject
    LanguageBean langBean;
    
    private boolean showAllForReview = false;
    private boolean showAllForModeration = false;
    
    public DashboardBean(){
    }
    
    public boolean isShowAllForReview() {
        return showAllForReview;
    }

    public void setShowAllForReview(boolean showAllForReview) {
        this.showAllForReview = showAllForReview;
    }

    public boolean isShowAllForModeration() {
        return showAllForModeration;
    }

    public void setShowAllForModeration(boolean showAllForModeration) {
        this.showAllForModeration = showAllForModeration;
    }
    
    public ClaimSearchResult[] searchAssigned() {
        List<ClaimSearchResult> result = searchEjb.searchAssignedClaims(langBean.getLocale());
        if(result == null || result.size() < 1){
            return null;
        } else {
            return result.toArray(new ClaimSearchResult[result.size()]);
        }
    }
    
    public ClaimSearchResult[] searchForReview() {
        List<ClaimSearchResult> result = searchEjb.searchClaimsForReview(langBean.getLocale(), showAllForReview);
        if(result == null || result.size() < 1){
            return null;
        } else {
            return result.toArray(new ClaimSearchResult[result.size()]);
        }
    }
    
    public ClaimSearchResult[] searchForModeration() {
        List<ClaimSearchResult> result = searchEjb.searchClaimsForModeration(langBean.getLocale(), showAllForModeration);
        if(result == null || result.size() < 1){
            return null;
        } else {
            return result.toArray(new ClaimSearchResult[result.size()]);
        }
    }
}
