package org.sola.opentenure.services.boundary.beans.referencedata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import org.sola.common.ConfigConstants;
import org.sola.common.StringUtility;
import org.sola.opentenure.services.ejbs.claim.entities.ClaimStatus;
import org.sola.opentenure.services.ejbs.claim.entities.FieldConstraintType;
import org.sola.opentenure.services.ejbs.claim.entities.LandUse;
import org.sola.services.ejb.refdata.entities.LandUseType;
import org.sola.services.boundary.transferobjects.system.MapExtentTO;
import org.sola.services.common.repository.entities.AbstractCodeEntity;
import org.sola.services.ejb.search.businesslogic.SearchEJBLocal;
import org.sola.services.ejb.system.businesslogic.SystemEJBLocal;
import org.sola.services.ejb.refdata.businesslogic.RefDataEJBLocal;
import org.sola.services.ejb.refdata.entities.FieldType;
import org.sola.services.ejb.refdata.entities.GenderType;
import org.sola.services.ejb.refdata.entities.IdType;
import org.sola.services.ejb.refdata.entities.Language;
import org.sola.services.ejb.refdata.entities.RejectionReason;
import org.sola.services.ejb.refdata.entities.RrrType;
import org.sola.services.ejb.refdata.entities.SourceType;

/**
 * Holds methods to retrieve reference data. ALl data are cached when first time
 * received from DB.
 */
@Named
@ApplicationScoped
public class ReferenceData {
    
    @EJB
    SearchEJBLocal searchEjb;

    @EJB
    SystemEJBLocal systemEjb;
    
    @EJB
    RefDataEJBLocal refDataEjb;

    private HashMap cache;
    private final String CLAIM_STATUS = "CLAIM_STATUS";
    private final String SOURCE_TYPES = "SOURCE_TYPES";
    private final String SOURCE_TYPES_ACTIVE = "SOURCE_TYPES_ACTIVE";
    private final String ID_TYPES = "ID_TYPES";
    private final String ID_TYPES_ACTIVE = "ID_TYPES_ACTIVE";
    private final String GENDER_TYPES = "GENDER_TYPES";
    private final String GENDER_TYPES_ACTIVE = "GENDER_TYPES_ACTIVE";
    private final String RIGHT_TYPES = "RIGHT_TYPES";
    private final String RIGHT_TYPES_ACTIVE = "RIGHT_TYPES_ACTIVE";
    private final String LAND_USE = "LAND_USE";
    private final String REJECTION_REASON = "REJECTION_REASON";
    private final String REJECTION_REASON_ACTIVE = "REJECTION_REASON_ACTIVE";
    private final String LAND_USE_ACTIVE = "LAND_USE_ACTIVE";
    private final String LANGUAGES = "LANGUAGES";
    private final String MAP_EXTENT = "MAP_EXTENT";
    private final String COMMUNITY_AREA = "COMMUNITY_AREA";
    private final String DYNA_FORM_FIELD_TYPES = "DYNA_FORM_FIELD_TYPES";
    private final String DYNA_FORM_FIELD_CONSTRAINT_TYPES = "DYNA_FORM_FIELD_CONSTRAINT_TYPES";
    private final String DYNA_FORM_FIELD_TYPES_ACTIVE = "DYNA_FORM_FIELD_TYPES_ACTIVE";
    private final String DYNA_FORM_FIELD_CONSTRAINT_TYPES_ACTIVE = "DYNA_FORM_FIELD_CONSTRAINT_TYPES_ACTIVE";
    
    @PostConstruct
    private void init() {
        cache = new HashMap();
    }

    public ReferenceData() {
        super();
    }
    
    /** Cleans cache objects */
    public void resetCache(){
        cache.clear();
    }
    
    /**
     * Returns list of {@link Language}
     *
     * @param langCode Language code
     * @return
     */
    public List<Language> getLanguages(String langCode) {
        List<Language> result;
        if (cache.containsKey(makeKey(LANGUAGES, langCode))) {
            result = (List<Language>) cache.get(makeKey(LANGUAGES, langCode));
        } else {
            result = refDataEjb.getLanguages(langCode);
        }
        if (!cache.containsKey(makeKey(LANGUAGES, langCode))) {
            if (result != null) {
                cache.put(makeKey(LANGUAGES, langCode), result);
            }
        }       
        return result;
    }
    
    /**
     * Returns list of {@link FieldType}
     *
     * @param langCode Language code
     * @param onlyActive Indicates whether to return only active records or all.
     * @return
     */
    public List<FieldType> getFieldTypes(String langCode, boolean onlyActive) {
        List<FieldType> result;
        if (cache.containsKey(makeKey(DYNA_FORM_FIELD_TYPES, langCode))) {
            result = (List<FieldType>) cache.get(makeKey(DYNA_FORM_FIELD_TYPES, langCode));
        } else {
            result = refDataEjb.getCodeEntityList(FieldType.class, langCode);
        }
        return getTypes(result, DYNA_FORM_FIELD_TYPES, DYNA_FORM_FIELD_TYPES_ACTIVE, langCode, onlyActive);
    }

    /**
     * Returns array of {@link FieldType}
     *
     * @param addDummy Indicates whether to create dummy record or not
     * @param langCode Language code
     * @param onlyActive Indicates whether to return only active records or all.
     * @return
     */
    public FieldType[] getFieldTypes(boolean addDummy, String langCode, boolean onlyActive) {
        ArrayList<FieldType> result = (ArrayList<FieldType>) getFieldTypes(langCode, onlyActive);
        if (addDummy) {
            result = (ArrayList<FieldType>) result.clone();
            result.add(0, createDummy(new FieldType()));
        }
        return result.toArray(new FieldType[result.size()]);
    }
    
    /**
     * Returns list of {@link FieldConstraintType}
     *
     * @param langCode Language code
     * @param onlyActive Indicates whether to return only active records or all.
     * @return
     */
    public List<FieldConstraintType> getFieldConstraintTypes(String langCode, boolean onlyActive) {
        List<FieldConstraintType> result;
        if (cache.containsKey(makeKey(DYNA_FORM_FIELD_CONSTRAINT_TYPES, langCode))) {
            result = (List<FieldConstraintType>) cache.get(makeKey(DYNA_FORM_FIELD_CONSTRAINT_TYPES, langCode));
        } else {
            result = refDataEjb.getCodeEntityList(FieldConstraintType.class, langCode);
        }
        return getTypes(result, DYNA_FORM_FIELD_CONSTRAINT_TYPES, DYNA_FORM_FIELD_CONSTRAINT_TYPES_ACTIVE, langCode, onlyActive);
    }

    /**
     * Returns array of {@link FieldConstraintType}
     *
     * @param addDummy Indicates whether to create dummy record or not
     * @param langCode Language code
     * @param onlyActive Indicates whether to return only active records or all.
     * @return
     */
    public FieldConstraintType[] getFieldConstraintTypes(boolean addDummy, String langCode, boolean onlyActive) {
        ArrayList<FieldConstraintType> result = (ArrayList<FieldConstraintType>) getFieldConstraintTypes(langCode, onlyActive);
        if (addDummy) {
            result = (ArrayList<FieldConstraintType>) result.clone();
            result.add(0, createDummy(new FieldConstraintType()));
        }
        return result.toArray(new FieldConstraintType[result.size()]);
    }
    
    /**
     * Returns list of {@link LandUseType}
     *
     * @param langCode Language code
     * @param onlyActive Indicates whether to return only active records or all.
     * @return
     */
    public List<LandUseType> getLandUses(String langCode, boolean onlyActive) {
        List<LandUseType> result;
        if (cache.containsKey(makeKey(LAND_USE, langCode))) {
            result = (List<LandUseType>) cache.get(makeKey(LAND_USE, langCode));
        } else {
            result = refDataEjb.getCodeEntityList(LandUseType.class, langCode);
        }
        return getTypes(result, LAND_USE, LAND_USE_ACTIVE, langCode, onlyActive);
    }

    /**
     * Returns array of {@link LandUse}
     *
     * @param addDummy Indicates whether to create dummy record or not
     * @param langCode Language code
     * @param onlyActive Indicates whether to return only active records or all.
     * @return
     */
    public LandUseType[] getLandUses(boolean addDummy, String langCode, boolean onlyActive) {
        ArrayList<LandUseType> result = (ArrayList<LandUseType>) getLandUses(langCode, onlyActive);
        if (addDummy) {
            result = (ArrayList<LandUseType>) result.clone();
            result.add(0, createDummy(new LandUseType()));
        }
        return result.toArray(new LandUseType[result.size()]);
    }

    /**
     * Returns list of {@link RejectionReason}
     *
     * @param langCode Language code
     * @param onlyActive Indicates whether to return only active records or all.
     * @return
     */
    public List<RejectionReason> getRejectionReasons(String langCode, boolean onlyActive) {
        List<RejectionReason> result;
        if (cache.containsKey(makeKey(REJECTION_REASON, langCode))) {
            result = (List<RejectionReason>) cache.get(makeKey(REJECTION_REASON, langCode));
        } else {
            result = refDataEjb.getCodeEntityList(RejectionReason.class, langCode);
        }
        return getTypes(result, REJECTION_REASON, REJECTION_REASON_ACTIVE, langCode, onlyActive);
    }

    /**
     * Returns array of {@link RejectionReason}
     *
     * @param addDummy Indicates whether to create dummy record or not
     * @param langCode Language code
     * @param onlyActive Indicates whether to return only active records or all.
     * @return
     */
    public RejectionReason[] getRejectionReasons(boolean addDummy, String langCode, boolean onlyActive) {
        ArrayList<RejectionReason> result = (ArrayList<RejectionReason>) getRejectionReasons(langCode, onlyActive);
        if (addDummy) {
            result = (ArrayList<RejectionReason>) result.clone();
            result.add(0, createDummy(new RejectionReason()));
        }
        return result.toArray(new RejectionReason[result.size()]);
    }

    /**
     * Returns list of {@link RrrType}
     *
     * @param langCode Language code
     * @param onlyActive Indicates whether to return only active records or all.
     * @return
     */
    public List<RrrType> getRightTypes(String langCode, boolean onlyActive) {
        List<RrrType> result;
        if (cache.containsKey(makeKey(RIGHT_TYPES, langCode))) {
            result = (List<RrrType>) cache.get(makeKey(RIGHT_TYPES, langCode));
        } else {
            result = refDataEjb.getCodeEntityList(RrrType.class, langCode);
        }
        return getTypes(result, RIGHT_TYPES, RIGHT_TYPES_ACTIVE, langCode, onlyActive);
    }

    /**
     * Returns array of {@link RrrType}
     *
     * @param addDummy Indicates whether to create dummy record or not
     * @param langCode Language code
     * @param onlyActive Indicates whether to return only active records or all.
     * @return
     */
    public RrrType[] getRightTypes(boolean addDummy, String langCode, boolean onlyActive) {
        ArrayList<RrrType> result = (ArrayList<RrrType>) getRightTypes(langCode, onlyActive);
        if (addDummy) {
            result = (ArrayList<RrrType>) result.clone();
            result.add(0, createDummy(new RrrType()));
        }
        return result.toArray(new RrrType[result.size()]);
    }

    /**
     * Returns list of {@link ClaimStatus}
     *
     * @param langCode Language code
     * @return
     */
    public List<ClaimStatus> getClaimStatuses(String langCode) {
        if (cache.containsKey(makeKey(CLAIM_STATUS, langCode))) {
            return (List<ClaimStatus>) cache.get(makeKey(CLAIM_STATUS, langCode));
        }

        List<ClaimStatus> result = refDataEjb.getCodeEntityList(ClaimStatus.class, langCode);
        if (result != null) {
            cache.put(makeKey(CLAIM_STATUS, langCode), result);
        }
        return result;
    }

    /**
     * Returns list of {@link SourceType}
     *
     * @param langCode Language code
     * @param onlyActive Indicates whether to return only active records or all.
     * @return
     */
    public List<SourceType> getDocumentTypes(String langCode, boolean onlyActive) {
        List<SourceType> result;
        if (cache.containsKey(makeKey(SOURCE_TYPES, langCode))) {
            result = (List<SourceType>) cache.get(makeKey(SOURCE_TYPES, langCode));
        } else {
            result = refDataEjb.getCodeEntityList(SourceType.class, langCode);
        }
        return getTypes(result, SOURCE_TYPES, SOURCE_TYPES_ACTIVE, langCode, onlyActive);
    }

    /**
     * Returns list of {@link IdType}
     *
     * @param langCode Language code
     * @param onlyActive Indicates whether to return only active records or all.
     * @return
     */
    public List<IdType> getIdTypes(String langCode, boolean onlyActive) {
        List<IdType> result;
        if (cache.containsKey(makeKey(ID_TYPES, langCode))) {
            result = (List<IdType>) cache.get(makeKey(ID_TYPES, langCode));
        } else {
            result = refDataEjb.getCodeEntityList(IdType.class, langCode);
        }
        return getTypes(result, ID_TYPES, ID_TYPES_ACTIVE, langCode, onlyActive);
    }

    /**
     * Returns array of {@link IdType}
     *
     * @param addDummy Indicates whether to create dummy record or not
     * @param langCode Language code
     * @param onlyActive Indicates whether to return only active records or all.
     * @return
     */
    public IdType[] getIdTypes(boolean addDummy, String langCode, boolean onlyActive) {
        ArrayList<IdType> result = (ArrayList<IdType>) getIdTypes(langCode, onlyActive);
        if (addDummy) {
            result = (ArrayList<IdType>) result.clone();
            result.add(0, createDummy(new IdType()));
        }
        return result.toArray(new IdType[result.size()]);
    }

    /**
     * Returns list of {@link GenderType}
     *
     * @param langCode Language code
     * @param onlyActive Indicates whether to return only active records or all.
     * @return
     */
    public List<GenderType> getGenderTypes(String langCode, boolean onlyActive) {
        List<GenderType> result;
        if (cache.containsKey(makeKey(GENDER_TYPES, langCode))) {
            result = (List<GenderType>) cache.get(makeKey(GENDER_TYPES, langCode));
        } else {
            result = refDataEjb.getCodeEntityList(GenderType.class, langCode);
        }
        return getTypes(result, GENDER_TYPES, GENDER_TYPES_ACTIVE, langCode, onlyActive);
    }

    /**
     * Returns array of {@link GenderType}
     *
     * @param addDummy Indicates whether to create dummy record or not
     * @param langCode Language code
     * @param onlyActive Indicates whether to return only active records or all.
     * @return
     */
    public GenderType[] getGenderTypes(boolean addDummy, String langCode, boolean onlyActive) {
        ArrayList<GenderType> result = (ArrayList<GenderType>) getGenderTypes(langCode, onlyActive);
        if (addDummy) {
            result = (ArrayList<GenderType>) result.clone();
            result.add(0, createDummy(new GenderType()));
        }
        return result.toArray(new GenderType[result.size()]);
    }

    /**
     * Returns map extent
     *
     * @return
     */
    public MapExtentTO getMapExtent() {
        if (cache.containsKey(MAP_EXTENT)) {
            return (MapExtentTO) cache.get(MAP_EXTENT);
        }

        HashMap<String, String> mapSettings = searchEjb.getMapSettingList();
        MapExtentTO result = null;

        if (mapSettings != null) {
            result = new MapExtentTO();
            result.setMinX(mapSettings.get("map-west"));
            result.setMaxY(mapSettings.get("map-north"));
            result.setMaxX(mapSettings.get("map-east"));
            result.setMinY(mapSettings.get("map-south"));
        }

        if (result != null) {
            cache.put(MAP_EXTENT, result);
        }
        return result;
    }

    /**
     * Returns list of {@link SourceType}
     *
     * @param addDummy If true, empty item will be inserted on the top
     * @return
     */
    public SourceType[] getDocumentTypes(boolean addDummy, String langCode, boolean onlyActive) {
        ArrayList<SourceType> result = (ArrayList<SourceType>) getDocumentTypes(langCode, onlyActive);
        if (addDummy) {
            result = (ArrayList<SourceType>) result.clone();
            result.add(0, createDummy(new SourceType()));
        }
        return result.toArray(new SourceType[result.size()]);
    }

    /**
     * Returns list of {@link ClaimStatus}
     *
     * @param addDummy If true, empty item will be inserted on the top
     * @return
     */
    public ClaimStatus[] getClaimStatuses(boolean addDummy, String langCode) {
        ArrayList<ClaimStatus> result = (ArrayList<ClaimStatus>) getClaimStatuses(langCode);
        if (addDummy) {
            result = (ArrayList<ClaimStatus>) result.clone();
            result.add(0, createDummy(new ClaimStatus()));
        }
        return result.toArray(new ClaimStatus[result.size()]);
    }

    /**
     * Returns community area, defining where claims can be submitted. Returns
     * in WKT format.
     * @return 
     */
    public String getCommunityArea() {
        if (cache.containsKey(COMMUNITY_AREA)) {
            return cache.get(COMMUNITY_AREA).toString();
        }

        String result = systemEjb.getSetting(ConfigConstants.OT_COMMUNITY_AREA, "");

        if (result != null) {
            cache.put(COMMUNITY_AREA, result);
        }
        return result;
    }

    /**
     * Searches provided list of beans by bean code and returns found object.
     *
     * @param <T> Type of bean
     * @param list List of beans
     * @param code Bean code value
     * @return Returns bean or null if it wasn't found.
     */
    public <T extends AbstractCodeEntity> T getBeanByCode(List<T> list, String code) {
        if (list == null || list.size() < 1 || StringUtility.isEmpty(code)) {
            return null;
        }

        T result = null;
        for (Iterator<T> it = list.iterator(); it.hasNext();) {
            AbstractCodeEntity bean = it.next();
            if (bean.getCode().equals(code)) {
                result = (T) bean;
                break;
            }
        }
        return result;
    }

    /**
     * Generic method to store and retrieve from the cache list of reference
     * data values
     */
    private <T extends AbstractCodeEntity> List<T> getTypes(List<T> result,
            String key, String keyActive, String langCode, boolean onlyActive) {

        if (!cache.containsKey(makeKey(key, langCode))) {
            if (result != null) {
                cache.put(makeKey(key, langCode), result);
            }
        }

        if (result != null) {
            if (onlyActive) {
                if (cache.containsKey(makeKey(keyActive, langCode))) {
                    result = (List<T>) cache.get(makeKey(keyActive, langCode));
                } else {
                    List<T> active = new ArrayList<>();
                    for (T item : result) {
                        if (item.getStatus().equalsIgnoreCase("c")) {
                            active.add(item);
                        }
                    }
                    result = active;
                    cache.put(makeKey(keyActive, langCode), result);
                }
            }
        }
        return result;
    }

    /**
     * Searches provided list of beans by bean code and returns display name
     * value.
     *
     * @param <T> Type of bean
     * @param list List of beans
     * @param code Bean code value
     * @return Returns name or empty value if it wasn't found.
     */
    public <T extends AbstractCodeEntity> String getBeanDisplayValue(List<T> list, String code) {
        if (list == null || list.size() < 1 || StringUtility.isEmpty(code)) {
            return "";
        }

        T result = getBeanByCode(list, code);
        if (result == null) {
            return "";
        }
        return result.getDisplayValue();
    }

    private String makeKey(String key, String lang) {
        if(StringUtility.isEmpty(lang))
            return key + "_all";
        else
            return key + "_" + lang;
    }

    private <T extends AbstractCodeEntity> T createDummy(T entity) {
        entity.setCode("");
        entity.setDisplayValue(" ");
        return entity;
    }
}
