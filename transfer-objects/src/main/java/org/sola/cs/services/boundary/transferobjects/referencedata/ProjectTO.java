/**
 * ******************************************************************************************
 * Copyright (C) 2014 - Food and Agriculture Organization of the United Nations (FAO).
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *    1. Redistributions of source code must retain the above copyright notice,this list
 *       of conditions and the following disclaimer.
 *    2. Redistributions in binary form must reproduce the above copyright notice,this list
 *       of conditions and the following disclaimer in the documentation and/or other
 *       materials provided with the distribution.
 *    3. Neither the name of FAO nor the names of its contributors may be used to endorse or
 *       promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT
 * SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,PROCUREMENT
 * OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,STRICT LIABILITY,OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * *********************************************************************************************
 */
package org.sola.cs.services.boundary.transferobjects.referencedata;

import org.sola.services.common.contracts.AbstractBasicIdTO;

public class ProjectTO extends AbstractBasicIdTO {

    private String displayName;
    private String boundary;
    private String languageCode;
    private String tilesServerType;
    private String tilesServerUrl;
    private String tilesLayerName;
    private boolean geomRequired;
    
    public ProjectTO() {
        super();
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getBoundary() {
        return boundary;
    }

    public void setBoundary(String boundary) {
        this.boundary = boundary;
    }

    public String getLanguageCode() {
        return languageCode;
    }

    public void setLanguageCode(String languageCode) {
        this.languageCode = languageCode;
    }

    public String getTilesServerType() {
        return tilesServerType;
    }

    public void setTilesServerType(String tilesServerType) {
        this.tilesServerType = tilesServerType;
    }

    public String getTilesServerUrl() {
        return tilesServerUrl;
    }

    public void setTilesServerUrl(String tilesServerUrl) {
        this.tilesServerUrl = tilesServerUrl;
    }

    public String getTilesLayerName() {
        return tilesLayerName;
    }

    public void setTilesLayerName(String tilesLayerName) {
        this.tilesLayerName = tilesLayerName;
    }

    public boolean isGeomRequired() {
        return geomRequired;
    }

    public void setGeomRequired(boolean geomRequired) {
        this.geomRequired = geomRequired;
    }
}
