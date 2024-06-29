package org.sola.opentenure.services.boundary.beans.claim;

import java.util.List;
import jakarta.ejb.EJB;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.sola.cs.services.ejb.search.businesslogic.SearchCSEJBLocal;
import org.sola.cs.services.ejb.search.repository.entities.PublicDisplaySearchResult;
import org.sola.opentenure.services.boundary.beans.AbstractBackingBean;
import org.sola.opentenure.services.boundary.beans.language.LanguageBean;
import org.sola.opentenure.services.boundary.beans.project.ProjectBean;

/**
 * Provides method and listeners for boundary page
 */
@Named
@ViewScoped
public class PublicDisplayPageBean extends AbstractBackingBean {

    @EJB
    SearchCSEJBLocal searchEjb;

    @Inject
    ProjectBean projectBean;

    @Inject
    LanguageBean langBean;

    private PublicDisplaySearchResult[] publicDisplayRecords;
    private String boundaryId;
    private String fontSize = "16px";

    public PublicDisplayPageBean() {
        super();
    }

    public PublicDisplaySearchResult[] getPublicDisplayRecords() {
        return publicDisplayRecords;
    }

    public void searchPublicDisplayRecords() {
        List<PublicDisplaySearchResult> records = searchEjb.searchClaimsForPublicDisplay(langBean.getLocale(), boundaryId, projectBean.getProjectId());
        if (records != null) {
            publicDisplayRecords = records.toArray(new PublicDisplaySearchResult[records.size()]);
        } else {
            publicDisplayRecords = new PublicDisplaySearchResult[0];
        }
    }

    public String getBoundaryId() {
        return boundaryId;
    }

    public void setBoundaryId(String boundaryId) {
        this.boundaryId = boundaryId;
    }

    public String getFontSize() {
        return fontSize;
    }

    public void setFontSize(String fontSize) {
        this.fontSize = fontSize;
    }
}
