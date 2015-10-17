package org.sola.opentenure.services.boundary.beans.claim;

import java.util.List;
import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.sola.common.DateUtility;
import org.sola.opentenure.services.boundary.beans.AbstractBackingBean;
import org.sola.opentenure.services.boundary.beans.helpers.DateBean;
import org.sola.opentenure.services.boundary.beans.language.LanguageBean;
import org.sola.opentenure.services.boundary.beans.referencedata.ReferenceData;
import org.sola.cs.services.ejbs.claim.entities.ClaimStatus;
import org.sola.cs.services.ejb.search.businesslogic.SearchCSEJBLocal;
import org.sola.cs.services.ejb.search.repository.entities.ClaimSearchParams;
import org.sola.cs.services.ejb.search.repository.entities.ClaimSearchResult;

/**
 * Provides method and listeners for claim search component
 */
@Named
@RequestScoped
public class ClaimSearchComponentBean extends AbstractBackingBean {

    @EJB
    SearchCSEJBLocal searchEjb;

    @Inject
    DateBean dateBean;
    
    @Inject
    ReferenceData refData;

    @Inject
    LanguageBean langBean;
    
    private ClaimSearchResult[] searchResult;
    private ClaimSearchParams searchParams;
    private String lodgementDateFrom;
    private String lodgementDateTo;
    
    public ClaimSearchParams getSearchParams() {
        if (searchParams == null) {
            searchParams = new ClaimSearchParams();
        }
        return searchParams;
    }

    public String getLodgementDateFrom() {
        return lodgementDateFrom;
    }

    public void setLodgementDateFrom(String lodgementDateFrom) {
        this.lodgementDateFrom = lodgementDateFrom;
    }

    public String getLodgementDateTo() {
        return lodgementDateTo;
    }

    public void setLodgementDateTo(String lodgementDateTo) {
        this.lodgementDateTo = lodgementDateTo;
    }

    public boolean getHasSearchResults(){
        return searchResult != null && searchResult.length > 0;
    }
    
    public ClaimSearchResult[] getSearchResult() {
        if (searchResult == null) {
            searchResult = new ClaimSearchResult[0];
        }
        return searchResult;
    }

    public ClaimStatus[] getClaimStatuses(boolean addDummy){
        return refData.getClaimStatuses(addDummy, langBean.getLocale());
    }
    
    public ClaimSearchComponentBean() {
        super();
    }

    public void pageLoad(boolean searchByUser){
        if(!isPostback() && searchByUser){
            search(searchByUser);
        }
    }
    
    public void search(boolean onlyUserClaims) {
        getSearchParams().setLodgementDateFrom(DateUtility.convertToDate(lodgementDateFrom, dateBean.getDatePattern()));
        getSearchParams().setLodgementDateTo(DateUtility.convertToDate(lodgementDateTo, dateBean.getDatePattern()));
        getSearchParams().setLanguageCode(langBean.getLocale());
        getSearchParams().setSearchByUser(onlyUserClaims);
        
        List<ClaimSearchResult> result = searchEjb.searchClaims(getSearchParams());
        if(result == null || result.size() < 1){
            searchResult = new ClaimSearchResult[0];
        } else {
            searchResult = result.toArray(new ClaimSearchResult[result.size()]);
        }
    }
    
    public ClaimSearchResult[] searchAssigned() {
        List<ClaimSearchResult> result = searchEjb.searchAssignedClaims(langBean.getLocale());
        if(result == null || result.size() < 1){
            return null;
        } else {
            return result.toArray(new ClaimSearchResult[result.size()]);
        }
    }
    
    public ClaimSearchResult[] searchForReview(boolean includeAssigned) {
        List<ClaimSearchResult> result = searchEjb.searchClaimsForReview(langBean.getLocale(), includeAssigned);
        if(result == null || result.size() < 1){
            return null;
        } else {
            return result.toArray(new ClaimSearchResult[result.size()]);
        }
    }
    
    public ClaimSearchResult[] searchForModeration(boolean includeAssigned) {
        List<ClaimSearchResult> result = searchEjb.searchClaimsForModeration(langBean.getLocale(), includeAssigned);
        if(result == null || result.size() < 1){
            return null;
        } else {
            return result.toArray(new ClaimSearchResult[result.size()]);
        }
    }
}
