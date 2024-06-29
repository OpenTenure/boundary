package org.sola.opentenure.services.boundary.beans.dashboard;

import java.util.List;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.sola.common.RolesConstants;
import org.sola.opentenure.services.boundary.beans.AbstractBackingBean;
import org.sola.opentenure.services.boundary.beans.language.LanguageBean;
import org.sola.cs.services.ejb.search.businesslogic.SearchCSEJBLocal;
import org.sola.cs.services.ejb.search.repository.entities.ClaimSearchResult;
import org.sola.opentenure.services.boundary.beans.project.ProjectBean;

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
    
    @Inject
    ProjectBean projectBean;
    
    private boolean showAllForReview = false;
    private boolean showAllForModeration = false;
    
    
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
        List<ClaimSearchResult> result = searchEjb.searchAssignedClaims(langBean.getLocale(), projectBean.getProjectId());
        if(result == null || result.size() < 1){
            return null;
        } else {
            return result.toArray(new ClaimSearchResult[result.size()]);
        }
    }
    
    public ClaimSearchResult[] searchForReview() {
        List<ClaimSearchResult> result = searchEjb.searchClaimsForReview(langBean.getLocale(), projectBean.getProjectId(), showAllForReview);
        if(result == null || result.size() < 1){
            return null;
        } else {
            return result.toArray(new ClaimSearchResult[result.size()]);
        }
    }
    
    public ClaimSearchResult[] searchForModeration() {
        List<ClaimSearchResult> result = searchEjb.searchClaimsForModeration(langBean.getLocale(), projectBean.getProjectId(), showAllForModeration);
        if(result == null || result.size() < 1){
            return null;
        } else {
            return result.toArray(new ClaimSearchResult[result.size()]);
        }
    }
}
