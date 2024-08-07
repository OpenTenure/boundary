package org.sola.opentenure.services.boundary.beans.language;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.component.UIInput;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.ValueChangeEvent;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Locale;
import org.sola.common.StringUtility;
import org.sola.cs.services.ejb.cache.businesslogic.CacheCSEJBLocal;
import org.sola.opentenure.services.boundary.beans.AbstractBackingBean;
import org.sola.opentenure.services.boundary.beans.helpers.LanguageBeanSorter;
import org.sola.opentenure.services.boundary.beans.helpers.MessageProvider;
import org.sola.opentenure.services.boundary.beans.map.MapSettingsBean;
import org.sola.cs.services.ejb.refdata.businesslogic.RefDataCSEJBLocal;
import org.sola.cs.services.ejb.refdata.entities.Language;

@Named
@SessionScoped
public class LanguageBean extends AbstractBackingBean {

    @EJB
    RefDataCSEJBLocal refDataEjb;

    @Inject
    MessageProvider msgProvider;
    
    @Inject
    MapSettingsBean mapSettings;
    
    @EJB
    CacheCSEJBLocal cacheEjb;
    
    private Language currentLanguage;
    private Language[] languages;
    private Language[] activeLanguages;
    private static final String OT_LANGAUGE = "ot_language";
    private static final String DELIMETER = "::::";

    @PostConstruct
    private void init() {
        loadLanguages();
    }

    public String getLocalizedPageUrl(String defaultPage) {
        try {
            if (defaultPage == null || defaultPage.equals("")) {
                return defaultPage;
            }

            String localizaedPage = defaultPage.substring(0, defaultPage.lastIndexOf("."))
                    + "_" + getLocaleCodeForBundle() + defaultPage.substring(defaultPage.lastIndexOf("."), defaultPage.length());
            URL pageUrl = FacesContext.getCurrentInstance().getExternalContext().getResource(localizaedPage);
            if (pageUrl != null) {
                return localizaedPage;
            }
            return defaultPage;
        } catch (MalformedURLException ex) {
            return defaultPage;
        }
    }

    public Language getCurrentLanguage() {
        if (currentLanguage != null) {
            return currentLanguage;
        }

        if (activeLanguages == null || activeLanguages.length < 1) {
            return new Language();
        }

        String langCode = null;

        // Get language from cookie
        Map<String, Object> cookies = FacesContext.getCurrentInstance().getExternalContext().getRequestCookieMap();
        if (cookies != null) {
            Cookie langCookie = (Cookie) cookies.get(OT_LANGAUGE);
            if (langCookie != null && langCookie.getValue() != null) {
                langCode = langCookie.getValue();
            }
        }

        // If no cookie found, get from browser
        if (langCode == null) {
            langCode = FacesContext.getCurrentInstance().getExternalContext().getRequestLocale().toLanguageTag();
        }

        if (langCode != null && !langCode.equals("")) {
            // Search for language
            for (Language lang : activeLanguages) {
                if (lang.getCode().equalsIgnoreCase(langCode)) {
                    currentLanguage = lang;
                    break;
                }
            }
            // If language not found try with prefix only
            String langName = langCode;
            if (langCode.indexOf("-") > -1) {
                langName = langCode.substring(0, langCode.indexOf("-"));
            }
            for (Language lang : activeLanguages) {
                String langName2 = lang.getCode();
                if (langName2.indexOf("-") > -1) {
                    langName2 = langName2.substring(0, langName2.indexOf("-"));
                }
                if (langName2.equalsIgnoreCase(langName)) {
                    currentLanguage = lang;
                    break;
                }
            }
        }

        // If language is still not found, select default
        if (currentLanguage == null) {
            for (Language lang : activeLanguages) {
                if (lang.isIsDefault()) {
                    currentLanguage = lang;
                    break;
                }
            }
        }

        // If deafult language is not defined, select first available language
        if (currentLanguage == null) {
            currentLanguage = activeLanguages[0];
        }
        return currentLanguage;
    }

    public String getLocaleCodeForBundle() {
        return getCurrentLanguage().getCode().replace("-", "_");
    }

    public Locale getJavaLocale() {
        String[] codes = getCurrentLanguage().getCode().split("-");
        return new Locale(codes[0], codes[1]);
    }
    
    public Locale getJavaLocale(String locale) {
        String[] codes = locale.split("-");
        return new Locale(codes[0], codes[1]);
    }
    
    public String getLocale() {
        return getCurrentLanguage().getCode();
    }

    public void setLocale(String locale) {
        if (locale != null && !locale.equals("") && languages != null) {
            // Search for existing language
            for (Language lang : languages) {
                if (lang.getCode().equalsIgnoreCase(locale)) {
                    currentLanguage = lang;
                    HttpServletResponse response = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse();
                    Cookie cookie = new Cookie(OT_LANGAUGE, currentLanguage.getCode());
                    cookie.setMaxAge(315360000);
                    response.addCookie(cookie);
                    msgProvider.reloadBundles(currentLanguage.getCode());
                    // Reload languages
                    loadLanguages();
                    break;
                }
            }
        }
    }

    public void changeLocale(ValueChangeEvent e) {
        setLocale((String) e.getNewValue());
        // Reset cache and layers
        cacheEjb.clearAll();
        mapSettings.init();
        
        try {
            // Redirect to the same page to avoid postback
            HttpServletRequest req = (HttpServletRequest) getRequest();
            String qString = req.getParameter("params");
            String url = req.getRequestURI();
            if(!StringUtility.isEmpty(qString)) {
                url += "?" + qString;
            }
            getExtContext().redirect(url);
        } catch (IOException ex) {
        }
    }

    public Language[] getLanguages() {
        if (languages == null) {
            return new Language[]{};
        }
        return languages;
    }
    
    public Language[] getActiveLanguages(){
        if(activeLanguages == null){
            return new Language[]{};
        }
        return activeLanguages;
    }

    /**
     * Parses provided string containing multiple localized values and returns
     * string related to current language.
     *
     * @param mixedString Multilingual string.
     * @return
     */
    public String getLocalizedString(String mixedString) {
        return getLocalizedString(mixedString, getLocale());
    }

    /**
     * Parses provided string containing multiple localized values and returns
     * string related to requested language.
     *
     * @param mixedString Multilingual string.
     * @param langCode Language code
     * @return
     */
    public String getLocalizedString(String mixedString, String langCode) {
        if (StringUtility.isEmpty(mixedString)) {
            return mixedString;
        }
        String[] strings = mixedString.split(DELIMETER);
        String defaultValue = "";
        
        if (strings != null) {
            int i = 0;
            for (Language language : languages) {
                if (i >= strings.length) {
                    break;
                }
                if (language.getCode().equalsIgnoreCase(langCode)) {
                    return strings[i];
                }
                if (language.isIsDefault()) {
                    defaultValue = strings[i];
                }
                i += 1;
            }
        }
        
        if(!StringUtility.isEmpty(defaultValue)){
            return defaultValue;
        }
        return "";
    }

    private void loadLanguages() {
        String localeCode = getLocale();
        List<Language> langs = refDataEjb.getLanguages(localeCode);
        if (langs != null && (localeCode == null || localeCode.equals(""))) {
            // Reload languages after first initialization, when locale code was null
            languages = langs.toArray(new Language[langs.size()]);
            Arrays.sort(languages, new LanguageBeanSorter());
            langs = refDataEjb.getLanguages(getLocale());
        }
        if (langs != null) {
            languages = langs.toArray(new Language[langs.size()]);
            Arrays.sort(languages, new LanguageBeanSorter());
        }
        
        ArrayList<Language> tmpLangs = new ArrayList<>();
        
        if(languages != null){
            for(Language lang : languages){
                if(lang.isActive()){
                    tmpLangs.add(lang);
                }
            }
        }
        
        activeLanguages = tmpLangs.toArray(new Language[tmpLangs.size()]);
        
    }
}
