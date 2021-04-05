package org.sola.opentenure.services.boundary.beans.claim;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import javax.ejb.EJB;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.sola.common.StringUtility;
import org.sola.common.mapping.MappingManager;
import org.sola.opentenure.services.boundary.beans.AbstractBackingBean;
import org.sola.opentenure.services.boundary.beans.language.LanguageBean;
import org.sola.cs.services.ejbs.claim.businesslogic.ClaimEJBLocal;
import org.sola.cs.services.ejbs.claim.entities.Claim;
import org.sola.cs.services.ejbs.claim.entities.FieldConstraintOption;
import org.sola.cs.services.ejbs.claim.entities.FieldPayload;
import org.sola.cs.services.ejbs.claim.entities.FieldTemplate;
import org.sola.cs.services.ejbs.claim.entities.FieldType;
import org.sola.cs.services.ejbs.claim.entities.FieldValueType;
import org.sola.cs.services.ejbs.claim.entities.FormPayload;
import org.sola.cs.services.ejbs.claim.entities.FormTemplate;
import org.sola.cs.services.ejbs.claim.entities.SectionElementPayload;
import org.sola.cs.services.ejbs.claim.entities.SectionPayload;
import org.sola.cs.services.ejbs.claim.entities.SectionTemplate;
import org.sola.services.common.EntityAction;

@Named
@ViewScoped
public class DynaFormBean extends AbstractBackingBean {

    @EJB
    ClaimEJBLocal claimEjb;

    @Inject
    LanguageBean langBean;
    
    private FormPayload formPayload;
    private FormTemplate formTemplate;
    HashMap<String, SectionElementPayload> tempSectionElements;

    public DynaFormBean() {
    }

    public void init(Claim claim) {
        tempSectionElements = new HashMap<>();
        if (claim != null) {
            // Get form template
            if (claim.getDynamicForm()!= null) {
                formPayload = claim.getDynamicForm();
                // Get template by name
                formTemplate = claimEjb.getFormTemplate(claim.getDynamicForm().getFormTemplateName(), langBean.getLocale());
            } else {
                if (claim.isNew()) {
                    // Get default form template
                    FormTemplate templ = claimEjb.getDefaultFormTemplate(langBean.getLocale());
                    if (templ != null) {
                        formTemplate = templ;
                        formPayload = createFormPayloadFromTemplate(formTemplate, claim.getId());
                        claim.setDynamicForm(formPayload);
                    }
                }
            }
        }
        if (formTemplate == null) {
            // Create empty template object
            formTemplate = new FormTemplate();
        }
    }

    public SectionTemplate[] getSections() {
        // Get form template
        SectionTemplate[] sections = new SectionTemplate[]{};

        if (formTemplate.getSectionTemplateList() != null) {
            sections = formTemplate.getSectionTemplateList().toArray(
                    new SectionTemplate[formTemplate.getSectionTemplateList().size()]
            );
        }
        return sections;
    }

    public SectionElementPayload[] getSectionElementPayloads(String sectionName) {
        if (formPayload == null || formPayload.getSectionPayloadList() == null
                || sectionName == null || sectionName.equals("")) {
            return null;
        }
        for (SectionPayload section : formPayload.getSectionPayloadList()) {
            if (section.getName() != null && !section.getName().equals("")
                    && section.getName().equalsIgnoreCase(sectionName)) {
                // Filter out deleted items
                if (section.getSectionElementPayloadList() != null) {
                    List<SectionElementPayload> elements = new ArrayList<>();
                    for (SectionElementPayload element : section.getSectionElementPayloadList()) {
                        if (element.getEntityAction() == null || element.getEntityAction() != EntityAction.DELETE) {
                            elements.add(element);
                        }
                    }
                    return elements.toArray(new SectionElementPayload[elements.size()]);
                }
            }
        }
        return null;
    }

    public FieldPayload getFieldPayload(String sectionName, String sectionElementId, String fieldName) {
        if (formPayload == null || formPayload.getSectionPayloadList() == null
                || sectionName == null || fieldName == null
                || sectionName.equals("") || fieldName.equals("")) {
            return null;
        }

        for (SectionPayload section : formPayload.getSectionPayloadList()) {
            if (section.getName() != null && !section.getName().equals("")
                    && section.getName().equalsIgnoreCase(sectionName)) {
                if (section.getSectionElementPayloadList() != null) {
                    for (SectionElementPayload sectionElement : section.getSectionElementPayloadList()) {
                        if (sectionElementId == null || sectionElementId.equals("")
                                || sectionElement.getId().equalsIgnoreCase(sectionElementId)) {
                            if (sectionElement.getFieldPayloadList() != null) {
                                for (FieldPayload field : sectionElement.getFieldPayloadList()) {
                                    if (field.getName().equalsIgnoreCase(fieldName)) {
                                        return field;
                                    }
                                }
                            }
                            break;
                        }
                    }
                }
                break;
            }
        }
        return null;
    }

    /**
     * Loads section element into hash map for further editing
     */
    public void loadSectionElementPayload(String secName, SectionElementPayload secPayload) {
        tempSectionElements.put(secName,
                MappingManager.getMapper().map(secPayload, SectionElementPayload.class));
    }

    /**
     * Returns section element payload from hash map, previously loaded or
     * created by <code>createSectionElementPayload</code> or
     * <code>loadSectionElementPayload</code> methods.
     *
     * @param sectionName Section name to use as a key value to search hash map
     * @return
     */
    public SectionElementPayload getTempSectionElement(String sectionName) {
        return tempSectionElements.get(sectionName);
    }

    /**
     * Returns field payload from section payload element, previously loaded or
     * created by <code>createSectionElementPayload</code> or
     * <code>loadSectionElementPayload</code> methods.
     *
     * @param sectionName Section name to use as a key value to search hash map
     * @param fieldName Field name to search in discovered section element
     * @return
     */
    public FieldPayload getTempFieldPayload(String sectionName, String fieldName) {
        if (sectionName != null && fieldName != null) {
            SectionElementPayload secElement = getTempSectionElement(sectionName);
            if (secElement != null && secElement.getFieldPayloadList() != null) {
                for (FieldPayload f : secElement.getFieldPayloadList()) {
                    if (f.getName() != null && f.getName().equalsIgnoreCase(fieldName)) {
                        return f;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Creates new section element payload, based on provided section template
     */
    public SectionElementPayload createSectionElementPayload(SectionTemplate secTempl) {
        if (secTempl != null && formPayload != null && formPayload.getSectionPayloadList() != null) {
            // Search for section payload
            for (SectionPayload secPayload : formPayload.getSectionPayloadList()) {
                if (secPayload.getName() != null && secPayload.getName().equalsIgnoreCase(secTempl.getName())) {
                    // Create new section element payload
                    SectionElementPayload secElement = new SectionElementPayload();
                    secElement.setId(UUID.randomUUID().toString());
                    secElement.setSectionPayloadId(secPayload.getId());
                    secElement.setFieldPayloadList(createPayloadFieldsFromTemplate(secTempl, secElement.getId()));
                    tempSectionElements.put(secTempl.getName(), secElement);
                    return secElement;
                }
            }
        }
        return new SectionElementPayload();
    }

    private FormPayload createFormPayloadFromTemplate(FormTemplate fTempl, String claimId) {
        if (fTempl == null) {
            return null;
        }

        FormPayload fPayload = new FormPayload();
        fPayload.setId(UUID.randomUUID().toString());
        fPayload.setClaimId(claimId);
        fPayload.setFormTemplateName(fTempl.getName());
        fPayload.setSectionPayloadList(null);
        fPayload.setSectionPayloadList(new ArrayList<SectionPayload>());

        if (fTempl.getSectionTemplateList() != null) {
            for (SectionTemplate secTempl : fTempl.getSectionTemplateList()) {
                SectionPayload secPayload = createSectionPayloadFromTemplate(secTempl, fPayload.getId());
                secPayload.setSectionElementPayloadList(new ArrayList<SectionElementPayload>());
                if (secTempl.getMaxOccurrences() <= 1) {
                    // Create payload fields
                    SectionElementPayload secElement = new SectionElementPayload();
                    secElement.setId(UUID.randomUUID().toString());
                    secElement.setSectionPayloadId(secPayload.getId());
                    secElement.setFieldPayloadList(createPayloadFieldsFromTemplate(secTempl, secElement.getId()));
                    secPayload.getSectionElementPayloadList().add(secElement);
                }
                fPayload.getSectionPayloadList().add(secPayload);
            }
        }
        return fPayload;
    }

    private List<FieldPayload> createPayloadFieldsFromTemplate(SectionTemplate secTempl, String sectionElementId) {
        List<FieldPayload> fields = new ArrayList<>();
        if (secTempl.getFieldTemplateList() != null) {
            for (FieldTemplate fTempl : secTempl.getFieldTemplateList()) {
                FieldPayload fPayload = createPayloadFieldFromTemplate(fTempl);
                fPayload.setSectionElementPayloadId(sectionElementId);
                fields.add(fPayload);
            }
        }
        return fields;
    }

    private FieldPayload createPayloadFieldFromTemplate(FieldTemplate fTempl) {
        FieldPayload fPayload = new FieldPayload();
        if (fTempl != null) {
            fPayload.setDisplayName(fTempl.getDisplayName());
            fPayload.setFieldType(fTempl.getFieldType());

            if (fTempl.getFieldType() != null && fTempl.getFieldType().equalsIgnoreCase(FieldType.TYPE_BOOL)) {
                fPayload.setFieldValueType(FieldValueType.TYPE_BOOL);
            } else {
                if (fTempl.getFieldType() != null
                        && (fTempl.getFieldType().equalsIgnoreCase(FieldType.TYPE_DECIMAL)
                        || fTempl.getFieldType().equalsIgnoreCase(FieldType.TYPE_INTEGER))) {
                    fPayload.setFieldValueType(FieldValueType.TYPE_NUMBER);
                } else {
                    fPayload.setFieldValueType(FieldValueType.TYPE_TEXT);
                }
            }
            fPayload.setId(UUID.randomUUID().toString());
            fPayload.setName(fTempl.getName());
        }
        return fPayload;
    }

    // Created section payload from sectino template.
    private SectionPayload createSectionPayloadFromTemplate(SectionTemplate secTempl, String formPayloadId) {
        if (secTempl != null && formTemplate != null && formTemplate.getSectionTemplateList() != null) {
            SectionPayload secPayload = new SectionPayload();
            secPayload.setId(UUID.randomUUID().toString());
            secPayload.setDisplayName(secTempl.getDisplayName());
            secPayload.setElementDisplayName(secTempl.getElementDisplayName());
            secPayload.setElementName(secTempl.getElementName());
            secPayload.setFormPayloadId(formPayloadId);
            secPayload.setMaxOccurrences(secTempl.getMaxOccurrences());
            secPayload.setMinOccurrences(secTempl.getMinOccurrences());
            secPayload.setName(secTempl.getName());
            secPayload.setSectionElementPayloadList(new ArrayList<SectionElementPayload>());
            return secPayload;
        }
        return null;
    }

    public void saveSectionElement(String sectionName) throws Exception {
        if (sectionName != null) {
            SectionElementPayload secElementPayload = tempSectionElements.get(sectionName);
            if (secElementPayload != null) {
                // Validate
                validateSectionElement(secElementPayload, sectionName);
                
                // Look for parent section
                for (SectionPayload secPayload : formPayload.getSectionPayloadList()) {
                    if (secPayload.getName() != null && secPayload.getName().equalsIgnoreCase(sectionName)) {
                        // Check if section element already exists
                        if (secPayload.getSectionElementPayloadList() == null) {
                            secPayload.setSectionElementPayloadList(new ArrayList<SectionElementPayload>());
                        }
                        for (SectionElementPayload existingElement : secPayload.getSectionElementPayloadList()) {
                            if (existingElement.getId().equals(secElementPayload.getId())) {
                                // Update element
                                MappingManager.getMapper().map(secElementPayload, existingElement);
                                return;
                            }
                        }
                        // Eelement not found, add to the list
                        secPayload.getSectionElementPayloadList().add(secElementPayload);
                    }
                }
            }
        }
    }

    private void validateSectionElement(SectionElementPayload secElement, String secName) throws Exception {
        if (secElement == null || secName == null || secElement.getFieldPayloadList() == null) {
            return;
        }
        // Search for section template
        if (formTemplate.getSectionTemplateList() != null) {
            for (SectionTemplate secTmpl : formTemplate.getSectionTemplateList()) {
                if (StringUtility.empty(secTmpl.getName()).equalsIgnoreCase(secName)) {
                    // Validate fields
                    if (secTmpl.getFieldTemplateList() != null && secElement.getFieldPayloadList() != null) {
                        for (FieldTemplate fTmpl : secTmpl.getFieldTemplateList()) {
                            for (FieldPayload fPayload : secElement.getFieldPayloadList()) {
                                if (StringUtility.empty(fTmpl.getName()).equalsIgnoreCase(StringUtility.empty(fPayload.getName()))) {
                                    fPayload.validate(fTmpl);
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    public void deleteSectionElement(String secName, SectionElementPayload secElement) {
        if (secElement != null) {
            if (secElement.isNew()) {
                // Just delete from the list
                if (secName != null) {
                    for (SectionPayload secPayload : formPayload.getSectionPayloadList()) {
                        if (secPayload.getSectionElementPayloadList() != null) {
                            int i = 0;
                            for (SectionElementPayload element : secPayload.getSectionElementPayloadList()) {
                                if (element.getId().equals(secElement.getId())) {
                                    secPayload.getSectionElementPayloadList().remove(i);
                                    return;
                                }
                                i += 1;
                            }
                        }
                    }
                }
            } else {
                // Set entity action
                secElement.setEntityAction(EntityAction.DELETE);
            }
        }
    }

    public int calculateColumnSpanNumber(int gridColumns, int requiredColumns) {
        return (gridColumns - (gridColumns % requiredColumns)) / requiredColumns;
    }
    
    public String getFieldValue(FieldTemplate fTempl, String value){
        if(fTempl == null || value == null){
            return "";
        }
        if(fTempl.getFieldType() != null && fTempl.getFieldOptions() != null){
            for(FieldConstraintOption option : fTempl.getFieldOptions()){
                if(StringUtility.empty(option.getName()).equalsIgnoreCase(value)){
                    return option.getDisplayName();
                }
            }
        }
        return value;
    }
}
