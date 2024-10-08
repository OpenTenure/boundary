// Global OpenTenure namespace
var OT = OT || {};

// Messages for map
var MSG = MSG || {};
MSG.MAP_CONTROL_LAYERS = "Layers";
MSG.MAP_CONTROL_ZOOM_TO_EXTENT = "Zoom to extent";
MSG.MAP_CONTROL_ZOOM_IN = "Zoom in";
MSG.MAP_CONTROL_ZOOM_OUT = "Zoom out";
MSG.MAP_CONTROL_EDIT_MAP = "Edit map";
MSG.MAP_CONTROL_DRAW_POLYGON = "Draw polygon";
MSG.MAP_CONTROL_IMPORT_POINTS = "Import polygon",
MSG.MAP_CONTROL_DRAW_LINE = "Draw line";
MSG.MAP_CONTROL_DRAW_POINT = "Draw point";
MSG.MAP_CONTROL_EDIT_SHAPE = "Edit shape";
MSG.MAP_CONTROL_DELETE_SHAPE = "Delete shape";
MSG.MAP_CONTROL_EDIT_PROPERTIES = "Edit properties";
MSG.MAP_CONTROL_SNAP = "Snap";
MSG.MAP_CONTROL_SNAP_SELECT = "Add to snapping";
MSG.MAP_CONTROL_CONFIRM_FEATURE_DELETE = "Are you sure you want to delete selected feature?";
MSG.MAP_CONTROL_LOADING = "Loading...";
MSG.MAP_CONTROL_NOTHING_FOUND = "Nothing was found";
MSG.MAP_CONTROL_CLAIMANT = "Claimant";
MSG.MAP_CONTROL_STATUS = "Status";
MSG.MAP_CONTROL_AREA = "Area";
MSG.MAP_CONTROL_LODGED = "Lodged";
MSG.MAP_CONTROL_INFO = "Information";
MSG.MAP_CONTROL_PAN = "Pan";
MSG.MAP_CONTROL_MAXIMIZE = "Maximize";
MSG.MAP_CONTROL_MAXIMIZE_TITLE = "Maximize map";
MSG.MAP_CONTROL_GOOGLE_MAP = "Google Map";
MSG.MAP_CONTROL_GOOGLE_EARTH = "Google Earth";
MSG.MAP_CONTROL_SEARCH = "Search";
MSG.MAP_CONTROL_TYPE = "Type";
MSG.MAP_CONTROL_PARENT = "Parent";
MSG.MAP_CONTROL_CLOSE = "Clode";
MSG.MAP_CONTROL_IMPORT = "Import";
MSG.MAP_CONTROL_POINTS_TYPE = "Points type";
MSG.MAP_CONTROL_POINTS = "Points";
MSG.MAP_CONTROL_CRS = "CRS";
MSG.MAP_CONTROL_WKT = "Well-known text (WKT)";
MSG.MAP_CONTROL_COMMA_SEPARATED = "Comma-separated (,)";
MSG.MAP_CONTROL_SEMICOLON = "Semicolon (;)";
MAP_CONTROL_PARCEL_NOT_POLYGON = "Provide polygon";
MAP_CONTROL_PARCEL_3POINT_MIN = "Provide at least 3 points";

// Map control
OT.Map = function (mapOptions) {
    var that = this;
    mapOptions = mapOptions ? mapOptions : {};

    // Enable cors for IE
    $.support.cors = true;

    // Boolean flag, indicating whether map can be edited. If false, editing tools will be hidden
    var isMapEditable = mapOptions.isMapEditable ? mapOptions.isMapEditable : false;

    // Boolean flag, indicating whether to show search field or not
    var showSearch = typeof mapOptions.showSearch !== 'undefined' ? mapOptions.showSearch : false;

    var projectId = typeof mapOptions.projectId !== 'undefined' ? mapOptions.projectId : "";

    // Boolean flag, indicating whether CS is offline or not
    var isOffline = mapOptions.isOffline ? mapOptions.isOffline : false;

    // Map toolbar reference
    var mapToolbar;

    // Map legend
    var mapLegendTree;

    // Selected legend item
    var selectedNode = null;

    // Boolean flag, used to indicate whether map editing is on or off
    var enableMapEditing = false;

    // OL Map
    var map;

    // Map container
    var mapPanelContainer;

    // Map container html container id
    var mapContainerName = mapOptions.mapContainerName ? mapOptions.mapContainerName : "mapCtrl";

    // Idicates whether map was rendered or not
    var isRendered = false;

    // Initial layers
    var layers = mapOptions.layers ? mapOptions.layers : [];

    // Map height
    var mapHeight = mapOptions.mapHeight ? mapOptions.mapHeight : 500;

    // Force line tool to show/hide
    var showLine = typeof mapOptions.showLine !== 'undefined' ? mapOptions.showLine : true;

    // Force point tool to show/hide
    var showPoint = typeof mapOptions.showPoint !== 'undefined' ? mapOptions.showPoint : true;

    // Map max extent bounds, used for full extent action
    this.maxExtentBounds = mapOptions.maxExtentBounds ? mapOptions.maxExtentBounds : null;

    // Initial zoom, required for proper zooming when rendering a map
    var initialZoomBounds = mapOptions.initialZoomBounds ? mapOptions.initialZoomBounds : null;

    // Default feature source CRS
    this.sourceCrs = mapOptions.sourceCrs ? mapOptions.sourceCrs : "EPSG:4326";

    // Default destination CRS
    this.destCrs = mapOptions.destCrs ? mapOptions.destCrs : "EPSG:3857";

    // Default language code, used for localization
    this.languageCode = mapOptions.languageCode ? mapOptions.languageCode : "en-us";

    // Web application URL, used to form JSON requests
    var applicationUrl = mapOptions.applicationUrl ? mapOptions.applicationUrl : "";

    // Initial snapping layers
    var snappingLayers = mapOptions.snappingLayers ? mapOptions.snappingLayers : [];

    // Layer containing claims, selected for snapping. These claims are read-only and layer is not displayed in the legend
    var layerSnappingFeatures = new OT.Map.Layer.VectorLayer(
            OT.Map.LAYER_IDS.SNAPPING_FEATURES,
            "Snapping",
            {
                isEditable: false,
                displayInLayerSwitcher: false,
                styleMap: OT.Map.Styles.styleSnappingClaim,
                virtualNodeStyle: OT.Map.Styles.styleClaimNode
            });

    // Public getters
    this.getMap = function () {
        return map;
    };
    this.getMapToolbar = function () {
        return mapToolbar;
    };
    this.getMapLegend = function () {
        return mapLegendTree;
    };
    this.getIsMapEditable = function () {
        return isMapEditable;
    };

    /** Sets snapping layers */
    this.setSnappingLayers = function (layers) {
        if (layers) {
            // All layer with selected features for snapping
            layers.push(layerSnappingFeatures);
            // Search for toolbar button
            for (var i = 0; i < mapToolbar.items.items.length; i++) {
                var tbButton = mapToolbar.items.items[i];
                if (tbButton.id === OT.Map.TOOLBAR_BUTTON_IDS.SNAP) {
                    var control = tbButton.baseAction.control;
                    var active = control.active;

                    if (active)
                        control.deactivate();

                    control.setTargets(layers);

                    if (active)
                        control.activate();
                }
            }
        }
    };

    // Turns off or on map editing
    this.toggleMapEditing = function (enable) {
        if (enableMapEditing !== enable) {
            // Search for toolbar button
            for (var i = 0; i < mapToolbar.items.items.length; i++) {
                var tbButton = mapToolbar.items.items[i];
                if (tbButton.id === OT.Map.TOOLBAR_BUTTON_IDS.EDIT_MAP) {
                    tbButton.toggle(enable);
                }
            }
        }
    };

    // custom layer node UI class
    var LayerNodeUI = Ext.extend(
            GeoExt.tree.LayerNodeUI,
            new GeoExt.tree.TreeNodeUIEventMixin()
            );

    // Init map
    map = new OpenLayers.Map('map', {
        div: "map",
        allOverlays: false,
        projection: this.destCrs,
        displayProjection: this.sourceCrs,
        maxExtentBounds: this.maxExtentBounds,
        initialZoomBounds: this.initialZoomBounds,
        units: 'm',
        numZoomLevels: 22
    });

    // Add search control
    if (showSearch) {
        var searchControl = new OpenLayers.Control();
        OpenLayers.Util.extend(searchControl, {
            draw: function () {
                this.div = document.createElement('div');
                var inputField = document.createElement('input');

                $(inputField).attr("class", "map-search form-control");
                $(inputField).attr("placeholder", MSG.MAP_CONTROL_SEARCH);

                $(inputField).on("click", function (e) {
                    $(this).focus();
                    e.stopPropagation();
                });

                $(inputField).on("mousedown", function (e) {
                    e.stopPropagation();
                });

                var width = null;

                $(inputField).on("focus", function (e) {
                    if (width === null) {
                        width = $(this).css("width");
                    }
                    $(this).css({"width": "300px"});
                    $(this).css({"opacity": "0.8"});
                });

                $(inputField).on("blur", function (e) {
                    $(this).css({"opacity": "0.6"});
                    $(this).css({"width": width});
                });

                // make field autocomplete
                $(inputField).autocomplete({
                    source: function (request, response) {
                        var results = [];
                        $.ajax({
                            url: applicationUrl + "/ws/en-en/claim/searchmap",
                            data: {query: request.term, projectId: projectId}
                        }).done(function (data) {
                            results = data;
                        }).always(function () {
                            if (results && results.length > 0) {
                                for (var i = 0; i < results.length; i++) {
                                    results[i].label = "<b>#" + results[i].nr + "</b> (" + results[i].ownerNames + ")";
                                    results[i].value = "#" + results[i].nr + " (" + results[i].ownerNames + ")";
                                }
                            }
                            response(results);
                        });
                    },
                    minLength: 2,
                    select: function (event, ui) {
                        var f = new OpenLayers.Format.WKT().read(ui.item.geom);
                        f.geometry.transform(that.sourceCrs, that.destCrs);
                        map.zoomToExtent(f.geometry.getBounds());
                        if (document.activeElement != document.body)
                            document.activeElement.blur();
                        return true;
                    }
                }).data("ui-autocomplete")._renderItem = function (ul, item) {
                    $(ul).css({"opacity": "0.8"});

                    return $("<li class='map-search-item'></li>")
                            .data("item.autocomplete", item)
                            .append("<a class='map-search-link'>" + item.label + "</a>")
                            .appendTo(ul);
                };

                $(inputField).data("ui-autocomplete")._resizeMenu = function () {
                    this.menu.element.outerWidth(300);
                };

                $(this.div).css({"z-index": "3000"});
                $(this.div).append(inputField);

                return this.div;
            },
            CLASS_NAME: "OT.Map.Control.SearchControl"
        });

        map.addControl(searchControl);
    }

    try {
        if (!isOffline) {
            var gsat = new OpenLayers.Layer.Google(MSG.MAP_CONTROL_GOOGLE_EARTH, {numZoomLevels: 20, type: google.maps.MapTypeId.SATELLITE});
            var gmap = new OpenLayers.Layer.Google(MSG.MAP_CONTROL_GOOGLE_MAP, {numZoomLevels: 20, visibility: false});
            map.addLayers([gsat, gmap]);
        }
    } catch (e) {
        console.log(e);
    }

    map.events.register('addlayer', map, handleAddLayer);

    if (layers.length > 0) {
        map.addLayers(layers);
    }

    // Check for base layers
    var hasBaseLayer = false;
    if (map.layers.length > 0) {
        for (var i = 0; i < map.layers.length; i++) {
            if (map.layers[i].isBaseLayer === true) {
                hasBaseLayer = true;
                break;
            }
        }
    }

    if (!hasBaseLayer) {
        // Add dummy base layer
        var emptyBase = new OpenLayers.Layer("Empty", {isBaseLayer: true, minResolution: 0.001, maxResolution: 200});
        map.addLayers([emptyBase]);
        map.setLayerIndex(emptyBase, 0);
    }

    map.events.register('changelayer', map, handleLayerChange);

    var mapPanel = new GeoExt.MapPanel({
        region: 'center',
        zoom: 6,
        map: map
    });

    mapLegendTree = new Ext.tree.TreePanel({
        region: 'west',
        title: MSG.MAP_CONTROL_LAYERS,
        width: 250,
        autoScroll: true,
        listeners: {
            beforeclick: nodeSelectionHandler
        },
        collapsible: true,
        split: true,
        enableDD: true,
        // apply the tree node component plugin to layer nodes
        plugins: [{
                ptype: "gx_treenodecomponent"
            }],
        loader: {
            applyLoader: false,
            uiProviders: {
                "custom_ui": LayerNodeUI
            }
        },
        root: {
            nodeType: "gx_layercontainer",
            loader: {
                baseAttrs: {
                    uiProvider: "custom_ui"
                },
                createNode: function (attr) {
                    if (attr.layer.CLASS_NAME === 'OpenLayers.Layer.WMS') {
                        attr.component = {
                            xtype: "gx_wmslegend",
                            baseParams: {
                                LEGEND_OPTIONS: typeof attr.layer.legendOptions === 'undefined' ? '' : attr.layer.legendOptions
                            },
                            layerRecord: mapPanel.layers.getByLayer(attr.layer),
                            showTitle: false,
                            cls: "legend"
                        };
                    } else {
                        if (attr.layer.CLASS_NAME === 'OpenLayers.Layer.Vector') {
                            attr.component = {
                                xtype: "gx_vectorlegend",
                                untitledPrefix: "",
                                layerRecord: mapPanel.layers.getByLayer(attr.layer),
                                showTitle: false,
                                cls: "legend",
                                clickableTitle: true,
                                selectOnClick: true,
                                node: attr,
                                listeners: {
                                    ruleselected: function (legend, event) {
                                        nodeSelectionHandler(legend.node, event, true);
                                    },
                                    ruleunselected: function (legend, event) {
                                        nodeSelectionHandler(legend.node, event, true);
                                    }
                                }
                            };
                        } else {
                            attr.component = {
                                untitledPrefix: "",
                                layerRecord: mapPanel.layers.getByLayer(attr.layer),
                                showTitle: false,
                                cls: "legend"
                            };
                        }
                    }
                    return GeoExt.tree.LayerLoader.prototype.createNode.call(this, attr);
                }
            }
        },
        rootVisible: false,
        lines: false
    });

    Ext.QuickTips.init();

    // Create toolbar
    var mapToolbarItems = [];

    mapToolbarItems.push({
        id: OT.Map.TOOLBAR_BUTTON_IDS.ZOOM_TO_EXTENT,
        iconCls: 'zoomToExtentIcon',
        text: MSG.MAP_CONTROL_ZOOM_TO_EXTENT,
        tooltip: MSG.MAP_CONTROL_ZOOM_TO_EXTENT,
        handler: function () {
            map.zoomToExtent(that.maxExtentBounds);
        }
    });
    mapToolbarItems.push(new GeoExt.Action({
        id: OT.Map.TOOLBAR_BUTTON_IDS.ZOOM_IN,
        control: new OpenLayers.Control.ZoomBox({out: false}),
        iconCls: 'zoomInIcon',
        toggleGroup: "draw",
        group: "draw",
        map: map,
        text: MSG.MAP_CONTROL_ZOOM_IN,
        tooltip: MSG.MAP_CONTROL_ZOOM_IN
    }));
    mapToolbarItems.push(new GeoExt.Action({
        id: OT.Map.TOOLBAR_BUTTON_IDS.ZOOM_OUT,
        control: new OpenLayers.Control.ZoomBox({out: true}),
        toggleGroup: "draw",
        group: "draw",
        iconCls: 'zoomOutIcon',
        map: map,
        text: MSG.MAP_CONTROL_ZOOM_OUT,
        tooltip: MSG.MAP_CONTROL_ZOOM_OUT
    }));

    mapToolbarItems.push("-");

    mapToolbarItems.push(new GeoExt.Action({
        id: OT.Map.TOOLBAR_BUTTON_IDS.PAN,
        control: new OpenLayers.Control(),
        toggleGroup: "draw",
        group: "draw",
        iconCls: 'panIcon',
        map: map,
        text: MSG.MAP_CONTROL_PAN,
        tooltip: MSG.MAP_CONTROL_PAN
    }));

    var claimInfoControl = new OpenLayers.Control();
    OpenLayers.Util.extend(claimInfoControl, {
        draw: function () {
            this.clickHandler = new OpenLayers.Handler.Click(claimInfoControl,
                    {click: handleInfoClick},
                    {delay: 0, single: true, double: false, stopSingle: false, stopDouble: true});

        },
        activate: function () {
            return this.clickHandler.activate() &&
                    OpenLayers.Control.prototype.activate.apply(this, arguments);
        },
        deactivate: function () {
            var deactivated = false;
            if (OpenLayers.Control.prototype.deactivate.apply(this, arguments)) {
                this.clickHandler.deactivate();
                deactivated = true;
            }
            return deactivated;
        },
        CLASS_NAME: "OT.Map.Control.ClaimInfo"
    });

    mapToolbarItems.push(new GeoExt.Action({
        id: OT.Map.TOOLBAR_BUTTON_IDS.CLAIM_INFO,
        control: claimInfoControl,
        iconCls: 'informationIcon',
        map: map,
        toggleGroup: "draw",
        group: "draw",
        pressed: true,
        text: MSG.MAP_CONTROL_INFO,
        tooltip: MSG.MAP_CONTROL_INFO
    }));

    if (isMapEditable) {
        // Define default layer for editing 
        var defaultEditingLayer = new OpenLayers.Layer.Vector("", {});

        // Enable editing tools
        mapToolbarItems.push("-");

        mapToolbarItems.push({
            id: OT.Map.TOOLBAR_BUTTON_IDS.EDIT_MAP,
            iconCls: 'editMapIcon',
            text: MSG.MAP_CONTROL_EDIT_MAP,
            tooltip: MSG.MAP_CONTROL_EDIT_MAP,
            enableToggle: true,
            toggleHandler: onMapEditToggle,
            pressed: false
        });

        mapToolbarItems.push("-");

        mapToolbarItems.push({
            id: OT.Map.TOOLBAR_BUTTON_IDS.IMPORT_POINTS,
            iconCls: 'importPointsIcon',
            editingTool: true,
            disabled: true,
            toggleGroup: "draw",
            group: "draw",
            text: MSG.MAP_CONTROL_IMPORT_POINTS,
            tooltip: MSG.MAP_CONTROL_IMPORT_POINTS,
            handler: onImportPointsClick
        });

        mapToolbarItems.push(new GeoExt.Action({
            id: OT.Map.TOOLBAR_BUTTON_IDS.DRAW_POLYGON,
            control: new OT.Map.Control.DrawFeature(defaultEditingLayer,
                    OpenLayers.Handler.Polygon,
                    {handlerOptions: {holeModifier: "altKey"}, featureAdded: featureAdded}),
            iconCls: 'polygonIcon',
            map: map,
            editingTool: true,
            toggleGroup: "draw",
            group: "draw",
            disabled: true,
            text: MSG.MAP_CONTROL_DRAW_POLYGON,
            tooltip: MSG.MAP_CONTROL_DRAW_POLYGON
        }));

        if (showLine) {
            mapToolbarItems.push(new GeoExt.Action({
                id: OT.Map.TOOLBAR_BUTTON_IDS.DRAW_LINE,
                control: new OT.Map.Control.DrawFeature(defaultEditingLayer,
                        OpenLayers.Handler.Path, {handlerOptions: {}, featureAdded: featureAdded}),
                iconCls: 'polylineIcon',
                map: map,
                editingTool: true,
                toggleGroup: "draw",
                group: "draw",
                disabled: true,
                text: MSG.MAP_CONTROL_DRAW_LINE,
                tooltip: MSG.MAP_CONTROL_DRAW_LINE
            }));
        }

        if (showPoint) {
            mapToolbarItems.push(new GeoExt.Action({
                id: OT.Map.TOOLBAR_BUTTON_IDS.DRAW_POINT,
                control: new OpenLayers.Control.DrawFeature(defaultEditingLayer,
                        OpenLayers.Handler.Point, {handlerOptions: {}, featureAdded: featureAdded}),
                iconCls: 'pointIcon',
                map: map,
                editingTool: true,
                toggleGroup: "draw",
                group: "draw",
                disabled: true,
                text: MSG.MAP_CONTROL_DRAW_POINT,
                tooltip: MSG.MAP_CONTROL_DRAW_POINT
            }));
        }

        mapToolbarItems.push("-");

        mapToolbarItems.push(new GeoExt.Action({
            id: OT.Map.TOOLBAR_BUTTON_IDS.EDIT_SHAPE,
            control: new OpenLayers.Control.ModifyFeature(defaultEditingLayer, null),
            iconCls: 'shapeEditIcon',
            map: map,
            editingTool: true,
            toggleGroup: "draw",
            group: "draw",
            disabled: true,
            text: MSG.MAP_CONTROL_EDIT_SHAPE,
            tooltip: MSG.MAP_CONTROL_EDIT_SHAPE
        }));

        mapToolbarItems.push(new GeoExt.Action({
            id: OT.Map.TOOLBAR_BUTTON_IDS.DELETE_FEATURE,
            control: new OpenLayers.Control.SelectFeature(defaultEditingLayer,
                    {clickout: true, multiple: false, hover: true, box: false,
                        clickFeature: function (feature) {
                            this.unselect(feature);
                            if (confirm(MSG.MAP_CONTROL_CONFIRM_FEATURE_DELETE)) {
                                this.layer.removeFeatures(feature);
                                customizeMapToolbar();
                            }
                        }
                    }
            ),
            iconCls: 'featureDeleteIcon',
            map: map,
            editingTool: true,
            toggleGroup: "draw",
            group: "draw",
            disabled: true,
            text: MSG.MAP_CONTROL_DELETE_SHAPE,
            tooltip: MSG.MAP_CONTROL_DELETE_SHAPE
        }));

        mapToolbarItems.push(new GeoExt.Action({
            id: OT.Map.TOOLBAR_BUTTON_IDS.EDIT_FEATURE,
            control: new OpenLayers.Control.SelectFeature(defaultEditingLayer,
                    {clickout: true, multiple: false, hover: true, box: false,
                        clickFeature: function (feature) {
                            this.unselect(feature);
                            if (typeof this.layer.editFeatureFunc !== 'undefined') {
                                this.layer.editFeatureFunc(feature);
                            }
                        }
                    }
            ),
            iconCls: 'featureEditIcon',
            map: map,
            editingTool: true,
            toggleGroup: "draw",
            group: "draw",
            disabled: true,
            text: MSG.MAP_CONTROL_EDIT_PROPERTIES,
            tooltip: MSG.MAP_CONTROL_EDIT_PROPERTIES
        }));

        mapToolbarItems.push("-");

        mapToolbarItems.push(new GeoExt.Action({
            id: OT.Map.TOOLBAR_BUTTON_IDS.SNAP,
            control: new OpenLayers.Control.Snapping({
                layer: defaultEditingLayer,
                targets: snappingLayers,
                greedy: false
            }),
            iconCls: 'snapIcon',
            map: map,
            toggleHandler: snapClicked,
            editingTool: true,
            enableToggle: true,
            disabled: true,
            text: MSG.MAP_CONTROL_SNAP,
            tooltip: MSG.MAP_CONTROL_SNAP
        }));

        var selectSnapFeatureControl = new OpenLayers.Control();
        OpenLayers.Util.extend(selectSnapFeatureControl, {
            draw: function () {
                this.clickHandler = new OpenLayers.Handler.Click(selectSnapFeatureControl,
                        {click: handleSelectSnapFeatureClick},
                        {delay: 0, single: true, double: false, stopSingle: false, stopDouble: true});

            },
            activate: function () {
                return this.clickHandler.activate() &&
                        OpenLayers.Control.prototype.activate.apply(this, arguments);
            },
            deactivate: function () {
                var deactivated = false;
                if (OpenLayers.Control.prototype.deactivate.apply(this, arguments)) {
                    this.clickHandler.deactivate();
                    deactivated = true;
                }
                return deactivated;
            },
            CLASS_NAME: "OT.Map.Control.SelectSnapFeature"
        });

        mapToolbarItems.push(new GeoExt.Action({
            id: OT.Map.TOOLBAR_BUTTON_IDS.SNAP_SELECT,
            control: selectSnapFeatureControl,
            iconCls: 'selectSnapIcon',
            map: map,
            editingTool: true,
            toggleGroup: "draw",
            group: "draw",
            disabled: true,
            text: MSG.MAP_CONTROL_SNAP_SELECT,
            tooltip: MSG.MAP_CONTROL_SNAP_SELECT
        }));
    }

    mapToolbarItems.push("-");
    mapToolbarItems.push(new Ext.Action({
        id: OT.Map.TOOLBAR_BUTTON_IDS.MAXIMIZE_MAP,
        iconCls: 'maximizeIcon',
        enableToggle: true,
        text: MSG.MAP_CONTROL_MAXIMIZE,
        tooltip: MSG.MAP_CONTROL_MAXIMIZE_TITLE,
        toggleHandler: maximizeMap
    }));

    mapToolbar = new Ext.Toolbar({
        enableOverflow: true,
        items: mapToolbarItems
    });

    var mapStatusBar = new Ext.Toolbar({
        items: [new Ext.form.Label({id: "lblScaleBar", text: ""}),
            '->',
            new Ext.form.Label({id: "lblMapMousePosition", text: ""}),
            {xtype: 'tbspacer', width: 10}
        ]
    });

    mapPanelContainer = new Ext.Panel({
        layout: 'border',
        height: mapHeight,
        tbar: mapToolbar,
        bbar: mapStatusBar,
        items: [mapLegendTree, mapPanel]
    });

    // Turn on/off feature selection button
    function snapClicked(item, pressed) {
        if (pressed) {
            // Enable button
            enableDisableToolbarButton(OT.Map.TOOLBAR_BUTTON_IDS.SNAP_SELECT, true);
        } else {
            // Remove features for snapping and disable selection button
            if (layerSnappingFeatures) {
                layerSnappingFeatures.removeAllFeatures();
            }
            enableDisableToolbarButton(OT.Map.TOOLBAR_BUTTON_IDS.SNAP_SELECT, false);
        }
    }

    function enableDisableToolbarButton(buttonId, enable) {
        for (var i = 0; i < mapToolbar.items.items.length; i++) {
            var tbButton = mapToolbar.items.items[i];
            if (tbButton.id === buttonId) {
                var control = tbButton.baseAction.control;
                if (enable) {
                    tbButton.enable();
                } else {
                    control.deactivate();
                    tbButton.disable();
                }
                break;
            }
        }
    }

    // Maximize or minimize map control
    function maximizeMap(item, pressed) {
        var escContainerName = "#" + mapContainerName.replace(":", "\\:");
        if (pressed) {
            $(escContainerName).addClass("fullScreen");
            mapPanelContainer.setHeight($(window).height());
            mapPanelContainer.setWidth($('body').innerWidth());
        } else {
            $(escContainerName).removeClass("fullScreen");
            mapPanelContainer.setHeight(mapHeight);
            mapPanelContainer.setWidth($(escContainerName).parent().width());
        }
    }

    /** Renders map into provided html container */
    this.renderMap = function () {
        setTimeout(function () {
            if (!isRendered) {
                var mapWidth = $("#" + mapContainerName.replace(":", "\\:")).parent().width();
                var winHeight = $(window).height();

                $("#loadDiv").width(mapWidth);
                $("#loadDiv").height(winHeight);
                $("#loadDiv").show();

                mapPanelContainer.render(mapContainerName);
                map.addControl(new OpenLayers.Control.MousePosition({div: document.getElementById("lblMapMousePosition")}));
                map.addControl(new OT.Map.Control.ScaleBar({div: document.getElementById("lblScaleBar")}));
                mapPanelContainer.setWidth(mapWidth);
                map.zoomToExtent(initialZoomBounds);

                isRendered = true;
                mapPanelContainer.setHeight(winHeight);

                setTimeout(function () {
                    mapPanelContainer.setHeight(mapHeight);
                    $("#loadDiv").hide();
                }, 100);
            }
        }, 10);
    };

    // Subscribe to map resize event to adjust map width/height
    $(window).resize(function () {
        var escContainerName = "#" + mapContainerName.replace(":", "\\:");
        if ($(escContainerName).hasClass("fullScreen")) {
            mapPanelContainer.setHeight($(window).height());
            mapPanelContainer.setWidth($('body').innerWidth());
        } else {
            mapPanelContainer.setWidth($(escContainerName).parent().width());
        }
    });

    // Toolbar legend and layer handlers

    // When map legend node gets selected, underlying layer will be recorded as selected
    function nodeSelectionHandler(node, e, forceSelect) {
        if (forceSelect) {
            node = mapLegendTree.getNodeById(node.id);
            mapLegendTree.getSelectionModel().select(node);
        }
        // Clean selected node editing css class
        if (selectedNode === null || selectedNode.id !== node.id) {
            selectedNode = node;
            if (enableMapEditing) {
                customizeMapToolbar();
            }
        }
        return true;
    }

    // Turns on and off map editing
    function onMapEditToggle(item, pressed) {
        enableMapEditing = pressed;
        customizeMapToolbar();
    }

    function onImportPointsClick() {
        if ($("#importPointsDialog").length === 0) {
            var html = '<div class="modal fade" id="importPointsDialog" tabindex="-1" role="dialog" aria-hidden="true"> \
                        <div class="modal-dialog" style="width:500px;"> \
                            <div class="modal-content"> \
                                <div class="modal-header"> \
                                    <button type="button" class="close" data-dismiss="modal"><span aria-hidden="true">&times;</span><span class="sr-only">' + MSG.MAP_CONTROL_CLOSE + '</span></button> \
                                    <h4 class="modal-title">' + MSG.MAP_CONTROL_IMPORT + '</h4> \
                                </div> \
                                <div class="modal-body" style="padding: 0px 5px 0px 5px;"> \
                                    <div class="content"> \
                                        <div class="row"> \
                                            <div class="col-md-6"> \
                                                <label>' + MSG.MAP_CONTROL_POINTS_TYPE + '</label> \
                                                <select id="cbxPointsType" class="form-control"></select> \
                                            </div> \
                                            <div class="col-md-4"> \
                                                <label>' + MSG.MAP_CONTROL_CRS + '</label> \
                                                <select id="cbxCrs" class="form-control"></select> \
                                            </div> \
                                        </div> \
                                        <div class="LineSpace"></div> \
                                        <label>' + MSG.MAP_CONTROL_POINTS + '</label> \
                                        <textarea id="txtPoints" rows="6" style="font-size: smaller;" class="form-control"></textarea> \
                                    </div> \
                                </div> \
                                <div class="modal-footer" style="margin-top: 0px;padding: 15px 20px 15px 20px;"> \
                                    <button type="button" class="btn btn-default" data-dismiss="modal">' + MSG.MAP_CONTROL_CLOSE + '</button> \
                                    <button type="button" id="btnImportPoints" class="btn btn-primary" onclick="mapControl.importPoints()">' + MSG.MAP_CONTROL_IMPORT + '</button> \
                                </div> \
                            </div> \
                        </div> \
                    </div>';

            var escContainerName = "#" + mapContainerName.replace(":", "\\:");
            $(escContainerName).append(html);

            // Populate lists
            $("#cbxPointsType").append($("<option />").val("wkt").text(MSG.MAP_CONTROL_WKT));
            $("#cbxPointsType").append($("<option />").val(",").text(MSG.MAP_CONTROL_COMMA_SEPARATED));
            $("#cbxPointsType").append($("<option />").val(";").text(MSG.MAP_CONTROL_SEMICOLON));

            $("#cbxCrs").append($("<option />").val(that.sourceCrs).text(that.sourceCrs));
            if (!isNullOrEmpty(that.printCrs) && that.printCrs !== that.sourceCrs) {
                $("#cbxCrs").append($("<option />").val(that.printCrs).text(that.printCrs));
            }
        }

        $("#importPointsDialog").modal('show');
    }

    this.importPoints = function () {
        var coords = $("#txtPoints").val().trim();
        var pointsType = $("#cbxPointsType").val();
        var crsCode = $("#cbxCrs").val();

        if (isNullOrEmpty(coords)) {
            alertErrorMessage($.i18n("err-parcel-no-coords"));
            return;
        }

        try {
            var parcel;
            var separator;
            coords = coords.replace(/(\r\n|\n|\r)/gm, "");

            if (pointsType === "wkt") {
                if (coords.toLowerCase().indexOf("polygon") < 0) {
                    alert(MSG.MAP_CONTROL_PARCEL_NOT_POLYGON);
                    return;
                }
            } else {
                // Check number of coordinates and complete polygon if needed
                var arrayCoords;
                if (coords.endsWith(";") || coords.endsWith(",")) {
                    coords = coords.substring(0, coords.length);
                }

                separator = pointsType;
                arrayCoords = coords.split(separator);

                if (arrayCoords.length > 2) {
                    if (arrayCoords[0].replace(/ /g, "") !== arrayCoords[arrayCoords.length - 1].replace(/ /g, "")) {
                        // Add last point to complete polygon
                        arrayCoords.push(arrayCoords[0].trim());
                    }
                }

                if (arrayCoords.length < 4) {
                    alert(MSG.MAP_CONTROL_PARCEL_3POINT_MIN);
                    return;
                }

                coords = "";
                for (var i = 0; i < arrayCoords.length; i++) {
                    if (coords !== "") {
                        coords += ",";
                    }
                    coords += arrayCoords[i];
                }

                coords = "Polygon((" + coords + "))";
            }

            parcel = new OpenLayers.Format.WKT().read(coords);
            parcel.geometry.transform(crsCode, that.destCrs);
            selectedNode.layer.addFeatures(parcel);

            // Zoom to boundary
            map.zoomToExtent(parcel.geometry.getBounds(), closest = true);

            // Show attributes popup
            $("#importPointsDialog").modal('hide');
            featureAdded(parcel);

        } catch (ex) {
            alert(ex);
        }
    };

    // Layer property change handler
    function handleLayerChange(evt) {
        if (evt.property === 'visibility') {
            if (selectedNode !== null && selectedNode.layer.id === evt.layer.id) {
                if (enableMapEditing) {
                    customizeMapToolbar();
                }
            }
        }
    }

    function handleAddLayer(evt) {
        // Move snapping layer on top
        if (map && layerSnappingFeatures) {
            if (evt.layer.name !== layerSnappingFeatures.name) {
                // Check snapping layer in the list
                var found = false;
                for (var i = 0; i < map.layers.length; i++) {
                    if (map.layers[i].name === layerSnappingFeatures.name) {
                        // Move on top
                        map.setLayerIndex(layerSnappingFeatures, map.getLayerIndex(evt.layer));
                        found = true;
                    }
                }
                if (!found) {
                    // Add snapping layer
                    map.addLayer(layerSnappingFeatures);
                }
            }
        }
    }

    // Customizes map toolbar
    function customizeMapToolbar() {
        if (!enableMapEditing || selectedNode === null || !selectedNode.layer.isEditable || !selectedNode.layer.visibility) {
            disableMapEditing();
            return;
        }

        removeLayerEditingIcon();
        var selectedLayer = selectedNode.layer;

        if (typeof selectedNode.ui.textNode !== 'undefined') {
            $(selectedNode.ui.textNode).parent().parent().addClass('editingNode');
        }

        // Enable toolbar items
        for (var i = 0; i < mapToolbar.items.items.length; i++) {
            var tbButton = mapToolbar.items.items[i];
            if (tbButton.editingTool) {

                // Enable shape editing tool
                var allowDrawing = true;
                if (typeof selectedLayer.allowMultipleFeatures !== 'undefined') {
                    if (!selectedLayer.allowMultipleFeatures && selectedLayer.features.length > 0) {
                        allowDrawing = false;
                    }
                }

                if (tbButton.id === OT.Map.TOOLBAR_BUTTON_IDS.SNAP) {
                    var control = tbButton.baseAction.control;
                    var active = control.active;

                    if (active)
                        control.deactivate();

                    control.setLayer(selectedLayer);

                    if (active)
                        control.activate();
                    else
                        tbButton.enable();

                } else if (tbButton.id === OT.Map.TOOLBAR_BUTTON_IDS.SNAP_SELECT) {
                    // Do nothing
                } else if (tbButton.id === OT.Map.TOOLBAR_BUTTON_IDS.IMPORT_POINTS
                        && allowDrawing && selectedLayer.allowPolygon) {
                    tbButton.enable();
                } else if (tbButton.id === OT.Map.TOOLBAR_BUTTON_IDS.DELETE_FEATURE) {
                    var control = tbButton.baseAction.control;
                    var active = control.active;

                    if (active)
                        control.deactivate();

                    control.setLayer(selectedLayer);

                    if (active)
                        control.activate();
                    else
                        tbButton.enable();

                } else if (tbButton.id === OT.Map.TOOLBAR_BUTTON_IDS.EDIT_SHAPE) {
                    var control = tbButton.baseAction.control;
                    var active = control.active;

                    if (active)
                        control.deactivate();

                    control.layer = selectedLayer;
                    control.virtualStyle = selectedLayer.virtualNodeStyle;

                    if (active)
                        control.activate();
                    else
                        tbButton.enable();

                } else if (tbButton.id === OT.Map.TOOLBAR_BUTTON_IDS.EDIT_FEATURE && typeof selectedLayer.editFeatureFunc !== 'undefined') {
                    var control = tbButton.baseAction.control;
                    var active = control.active;

                    if (active)
                        control.deactivate();

                    control.setLayer(selectedLayer);

                    if (active)
                        control.activate();
                    else
                        tbButton.enable();
                } else if (((tbButton.id === OT.Map.TOOLBAR_BUTTON_IDS.DRAW_POLYGON && selectedLayer.allowPolygon) ||
                        (tbButton.id === OT.Map.TOOLBAR_BUTTON_IDS.DRAW_LINE && selectedLayer.allowLine) ||
                        (tbButton.id === OT.Map.TOOLBAR_BUTTON_IDS.DRAW_POINT && selectedLayer.allowPoint))
                        && allowDrawing) {

                    var control = tbButton.baseAction.control;
                    var active = control.active;

                    if (active)
                        control.deactivate();

                    control.layer = selectedLayer;
                    control.handler.style = selectedLayer.virtualNodeStyle;

                    if (active)
                        control.activate();
                    else
                        tbButton.enable();

                } else {
                    tbButton.toggle(false);
                    tbButton.disable();
                }
            }
        }
    }

    // Calls feature editing functions, related to the layer it belongs to
    function featureAdded(feature) {
        customizeMapToolbar();
        if (feature !== null && typeof feature.layer.editFeatureFunc !== 'undefined') {
            feature.layer.editFeatureFunc(feature);
        }
    }

    // Removes editing icon from selected layer
    function removeLayerEditingIcon() {
        if (typeof mapLegendTree.root.childNodes !== 'undefined') {
            for (var i = 0; i < mapLegendTree.root.childNodes.length; i++) {
                if (typeof mapLegendTree.root.childNodes[i].ui.textNode !== 'undefined') {
                    $(mapLegendTree.root.childNodes[i].ui.textNode).parent().parent().removeClass('editingNode');
                }
            }
        }
    }

    // Disable all editing tools
    function disableMapEditing() {
        removeLayerEditingIcon();
        for (var i = 0; i < mapToolbar.items.items.length; i++) {
            if (mapToolbar.items.items[i].editingTool) {
                mapToolbar.items.items[i].toggle(false);
                mapToolbar.items.items[i].disable();
            }
        }
    }

    // Claim information tool
    var getObjectUrl = applicationUrl + "/ws/" + this.languageCode + "/claim/getobjectbypoint";
    var viewClaimUrl = applicationUrl + "/claim/ViewClaim.xhtml";
    var viewBoundaryUrl = applicationUrl + "/boundary/ViewBoundary.xhtml";
    var mapWaitContent = "<div id='mapWaitContent' class='mapWaitDiv'>" + MSG.MAP_CONTROL_LOADING + "</div>";
    var mapNoResutlsContent = "<div id='mapNoResutlsContent' class='mapNoResultsDiv'>" + MSG.MAP_CONTROL_NOTHING_FOUND + "</div>";
    var mapClaimInfoContent = "<div id='mapClaimInfoContent' class='mapClaimInfoDiv'>" +
            "<div class='line'>" +
            "<a href='' id='claimNr' target='_blank'></a>" +
            "</div>" +
            "<div class='line'>" + MSG.MAP_CONTROL_CLAIMANT +
            "<br /><b><span id='claimantName'></span></b> " +
            "</div>" +
            "<div class='line'>" + MSG.MAP_CONTROL_LODGED +
            " <br /><b><span id='claimLodgingDate'></span></b>" +
            "</div>" +
            "<div class='line'>" + MSG.MAP_CONTROL_STATUS +
            " <br /><b><span id='claimStatus'></span></b>" +
            "</div>" +
            "<div class='line'>" + MSG.MAP_CONTROL_AREA +
            " <br /><b><span id='claimArea'></span></b>" +
            "</div>" +
            "</div>";

    var mapBoundaryInfoContent = "<div id='mapBaoundaryInfoContent' class='mapClaimInfoDiv'>" +
            "<div class='line'>" +
            "<a href='' id='boundaryName' target='_blank'></a>" +
            "</div>" +
            "<div class='line'>" + MSG.MAP_CONTROL_TYPE +
            "<br /><b><span id='boundaryType'></span></b> " +
            "</div>" +
            "<div id='boundaryDivParent'><div class='line'>" + MSG.MAP_CONTROL_PARENT +
            "<br /><b><span id='boundaryParent'></span></b> " +
            "</div></div>" +
            "<div class='line'>" + MSG.MAP_CONTROL_STATUS +
            " <br /><b><span id='boundaryStatus'></span></b>" +
            "</div>" +
            "</div>";

    var infoPopup = null;
    var xhr;

    function handleInfoClick(evt)
    {
        if (typeof xhr !== 'undefined') {
            xhr.abort();
        }

        var lonlat = map.getLonLatFromViewPortPx(evt.xy);
        var coords = map.getLonLatFromViewPortPx(evt.xy).transform(that.destCrs, that.sourceCrs);

        if (infoPopup === null) {
            infoPopup = new OpenLayers.Popup.FramedCloud(MSG.MAP_CONTROL_INFO,
                    lonlat,
                    new OpenLayers.Size(220, 220),
                    mapWaitContent,
                    null, true, null);
            infoPopup.panMapIfOutOfView = true;
            map.addPopup(infoPopup);
        } else {
            infoPopup.lonlat = lonlat;
            infoPopup.setContentHTML(mapWaitContent);
            infoPopup.show();
        }

        xhr = $.ajax({
            url: getObjectUrl + '?x=' + coords.lon + '&y=' + coords.lat + '&projectId=' + projectId,
            type: 'GET',
            crossDomain: true,
            dataType: 'json',
            success: function (response) {
                populateFeatureInfo(response);
            },
            error: function (xhr, status) {
                infoPopup.setContentHTML(mapNoResutlsContent);
            }
        });
    }

    function populateFeatureInfo(response) {
        try {
            if (response === "") {
                infoPopup.setContentHTML(mapNoResutlsContent);
            } else {
                if (typeof response.nr !== "undefined") {
                    // This is claim
                    var lodgingDate = "";
                    if (response.lodgementDate) {
                        var lDate = new Date(parseDate(response.lodgementDate));
                        lodgingDate = (lDate.getDate() < 10 ? "0" + lDate.getDate() : lDate.getDate())
                                + "/"
                                + (lDate.getMonth() + 1 < 10 ? "0" + (lDate.getMonth() + 1) : lDate.getMonth() + 1)
                                + "/" + lDate.getFullYear() + " "
                                + (lDate.getHours() < 10 ? "0" + lDate.getHours() : lDate.getHours())
                                + ":"
                                + (lDate.getMinutes() < 10 ? "0" + lDate.getMinutes() : lDate.getMinutes());
                        //lodgingDate = new Date(parseDate(response.lodgementDate)).toDateString();
                    }
                    infoPopup.setContentHTML(mapClaimInfoContent);
                    $("#claimNr").attr('href', viewClaimUrl + '?id=' + response.id);
                    $("#claimNr").text('#' + response.nr);
                    $("#claimantName").text(response.claimantName);
                    $("#claimLodgingDate").text(lodgingDate);
                    $("#claimStatus").text(response.statusName);
                    $("#claimArea").html(response.claimArea + " m<sup>2</sup>");
                }
                if (typeof response.level !== "undefined") {
                    // This is boundary
                    infoPopup.setContentHTML(mapBoundaryInfoContent);
                    $("#boundaryName").attr('href', viewBoundaryUrl + '?id=' + response.id);
                    $("#boundaryName").text(response.name);
                    $("#boundaryType").text(response.typeName);
                    if (response.parentName === null || response.parentName === "") {
                        $("#boundaryDivParent").hide();
                    } else {
                        $("#boundaryParent").text(response.parentName);
                        $("#boundaryDivParent").show();
                    }
                    $("#boundaryStatus").text(response.statusName);
                }
            }
        } catch (ex) {
            infoPopup.hide();
            alert(ex);
        }
    }

    function handleSelectSnapFeatureClick(evt) {
        if (typeof xhr !== 'undefined') {
            xhr.abort();
        }

        var coords = map.getLonLatFromViewPortPx(evt.xy).transform(that.destCrs, that.sourceCrs);

        xhr = $.ajax({
            url: getObjectUrl + '?x=' + coords.lon + '&y=' + coords.lat + '&projectId=' + projectId,
            type: 'GET',
            crossDomain: true,
            dataType: 'json',
            success: function (response) {
                try {
                    if (response !== "") {
                        if (layerSnappingFeatures) {
                            // Check if claim alredy in the list
                            var featureExists = false;
                            var type = "claim";
                            if (typeof response.level !== "undefined") {
                                type = "boundary";
                            }
                            for (var i = 0; i < layerSnappingFeatures.features.length; i++) {
                                if (layerSnappingFeatures.features[i].attributes.id === response.id && layerSnappingFeatures.features[i].attributes.type === type) {
                                    featureExists = true;
                                    // Remove feature (deselect)
                                    layerSnappingFeatures.removeFeatures([layerSnappingFeatures.features[i]]);
                                    break;
                                }
                            }
                        }

                        if (!featureExists) {
                            // Check it's not the current claim
                            if (type === "claim") {
                                var claimLayer = map.getLayer(OT.Map.LAYER_IDS.CURRENT_CLAIM);
                                if (claimLayer) {
                                    for (var i = 0; i < claimLayer.features.length; i++) {
                                        if (claimLayer.features[i].attributes.nr === response.nr) {
                                            featureExists = true;
                                            break;
                                        }
                                    }
                                }
                            }
                            // Check in the boundary layer
                            if (!featureExists) {
                                if (type === "boundary") {
                                    var boundaryLayer = map.getLayer(OT.Map.LAYER_IDS.CURRENT_BOUNDARY);
                                    if (boundaryLayer) {
                                        for (var i = 0; i < boundaryLayer.features.length; i++) {
                                            if (boundaryLayer.features[i].attributes.id === response.id) {
                                                featureExists = true;
                                                break;
                                            }
                                        }
                                    }
                                }
                            }

                            // Add feature to the snapping layer
                            if (!featureExists) {
                                var featureToAdd = new OpenLayers.Format.WKT().read(response.geom);
                                featureToAdd.attributes = {id: response.id, type: type};
                                featureToAdd.geometry.transform(that.sourceCrs, that.destCrs);
                                layerSnappingFeatures.addFeatures([featureToAdd]);
                            }
                        }
                    }
                } catch (ex) {
                    alert(ex);
                }
            },
            error: function (xhr, status) {

            }
        });
    }
};

OT.Map.Layer = {};
OT.Map.Control = {};

// Map toolbar buttons ids
OT.Map.TOOLBAR_BUTTON_IDS = {
    ZOOM_TO_EXTENT: "btnZoomToExtent",
    ZOOM_IN: "btnZoomIn",
    ZOOM_OUT: "btnZoomOut",
    PAN: "btnPan",
    CLAIM_INFO: "btnClaimInfo",
    EDIT_MAP: "btnEditMap",
    DRAW_POLYGON: "btnDrawPolygon",
    IMPORT_POINTS: "btnImportPoints",
    DRAW_LINE: "btnDrawLine",
    DRAW_POINT: "btnDrawPoint",
    EDIT_SHAPE: "btnEditShape",
    ADD_NODE: "btnAddNode",
    REMOVE_NODE: "btnRemoveNode",
    DELETE_FEATURE: "btnDeleteFeature",
    EDIT_FEATURE: "btnEditFeature",
    SNAP: "btnSnapping",
    SNAP_SELECT: "btnSelectForSnapping",
    MAXIMIZE_MAP: "btnMaximizeMap"
};

// Map layers ids
OT.Map.LAYER_IDS = {
    COMMUNITY_AREA: "layerCommunityArea",
    GOOGLE_EARTH: "layerGoogleEarth",
    GOOGLE_MAP: "layerGoogleMap",
    CURRENT_CLAIM: "layerCurrentClaim",
    CURRENT_BOUNDARY: "layerCurrentBoundary",
    CLAIM_ADDITIONAL_LOCATIONS: "layerClaimAdditionalLocations",
    SNAPPING_FEATURES: "layerSnappingFeatures"
};

// Extend OpenLayers objects
OT.Map.Control.ScaleBar = OpenLayers.Class(OpenLayers.Control.ScaleBar, {
    styleValue: function (selector, key) {
        var value = 0;
        if (this.limitedStyle) {
            value = this.appliedStyles[selector][key];
        } else {
            selector = "." + this.displayClass + selector;
            rules:
                    for (var i = document.styleSheets.length - 1; i >= 0; --i) {
                var sheet = document.styleSheets[i];
                if (!sheet.disabled) {
                    var allRules;
                    try {
                        if (typeof (sheet.cssRules) == 'undefined') {
                            if (typeof (sheet.rules) == 'undefined') {
                                // can't get rules, keep looking
                                continue;
                            } else {
                                allRules = sheet.rules;
                            }
                        } else {
                            allRules = sheet.cssRules;
                        }
                    } catch (err) {
                        continue;
                    }
                    if (allRules && allRules !== null) {
                        for (var ruleIndex = 0; ruleIndex < allRules.length; ++ruleIndex) {
                            var rule = allRules[ruleIndex];
                            if (rule.selectorText &&
                                    (rule.selectorText.toLowerCase() == selector.toLowerCase())) {
                                if (rule.style[key] != '') {
                                    value = parseInt(rule.style[key]);
                                    break rules;
                                }
                            }
                        }
                    }
                }
            }
        }
        // if the key was not found, the equivalent value is zero
        return value ? value : 0;
    }
});

OT.Map.Layer.VectorLayer = function (id, name, params) {
    OpenLayers.Layer.Vector.call(this, name, params);
    if (id) {
        this.id = id;
    }
    this.isEditable = true;
    this.allowPolygon = true;
    this.allowPoint = false;
    this.allowLine = false;
    this.allowMultipleFeatures = false;
    this.virtualNodeStyle = "";
    this.editFeatureFunc;

    if (params.hasOwnProperty('virtualNodeStyle'))
        this.virtualNodeStyle = params.virtualNodeStyle;
    if (params.hasOwnProperty('isEditable'))
        this.isEditable = params.isEditable;
    if (params.hasOwnProperty('allowPolygon'))
        this.allowPolygon = params.allowPolygon;
    if (params.hasOwnProperty('allowPoint'))
        this.allowPoint = params.allowPoint;
    if (params.hasOwnProperty('allowLine'))
        this.allowLine = params.allowLine;
    if (params.hasOwnProperty('allowMultipleFeatures'))
        this.allowMultipleFeatures = params.allowMultipleFeatures;
    if (params.hasOwnProperty('editFeatureFunc'))
        this.editFeatureFunc = params.editFeatureFunc;
    return this;
};
OT.Map.Layer.VectorLayer.prototype = createObject(OpenLayers.Layer.Vector.prototype);
OT.Map.Layer.VectorLayer.prototype.constructor = OT.Map.Layer.VectorLayer;

// Extend drawing control
OT.Map.Control.DrawFeature = OpenLayers.Class(OpenLayers.Control.DrawFeature, {
    handlers: null,
    initialize: function (layer, handler, options) {
        OpenLayers.Control.DrawFeature.prototype.initialize.apply(this, [layer, handler, options]);
        // configure the keyboard handler
        var keyboardOptions = {
            keydown: this.handleKeypress
        };
        this.handlers = {
            keyboard: new OpenLayers.Handler.Keyboard(this, keyboardOptions)
        };
    },
    handleKeypress: function (evt) {
        var code = evt.keyCode;
        // ESCAPE pressed. Remove feature from map
        if (code === 27) {
            this.cancel();
        }
        // DELETE pressed. Remove third last vertix
        if (code === 46) {
            this.undo();
        }
        return true;
    },
    activate: function () {
        return this.handlers.keyboard.activate() &&
                OpenLayers.Control.DrawFeature.prototype.activate.apply(this, arguments);
    },
    deactivate: function () {
        var deactivated = false;
        // the return from the controls is unimportant in this case
        if (OpenLayers.Control.DrawFeature.prototype.deactivate.apply(this, arguments)) {
            this.handlers.keyboard.deactivate();
            deactivated = true;
        }
        return deactivated;
    },
    CLASS_NAME: "OT.DrawFeature"
});

OT.Map.Styles = {
    styleMapCommunityLayer: new OpenLayers.StyleMap({'default': {
            strokeColor: "#F5856F",
            strokeWidth: 3,
            fillOpacity: 0
        }}),
    styleLocationsNode: {
        pointRadius: 5,
        graphicName: "circle",
        fillColor: "white",
        fillOpacity: 0.5,
        strokeWidth: 2,
        strokeOpacity: 0.3,
        strokeColor: "#E96EFF"
    },
    styleMainBoundary: new OpenLayers.StyleMap({
        "default": new OpenLayers.Style({
            fontSize: "12px",
            fontFamily: "Arial",
            labelOutlineColor: "white",
            labelOutlineWidth: 3
        }, {
            rules: [
                new OpenLayers.Rule({
                    symbolizer: {
                        "Polygon": {
                            fillColor: "#0000ff",
                            fillOpacity: 0,
                            strokeColor: "#FF5500",
                            strokeWidth: 3
                        }
                    }
                })
            ]
        })
    }),
    styleBoundary: new OpenLayers.StyleMap({
        "default": new OpenLayers.Style({
            fontSize: "12px",
            fontFamily: "Arial",
            labelOutlineColor: "white",
            labelOutlineWidth: 3
        }, {
            rules: [
                new OpenLayers.Rule({
                    symbolizer: {
                        "Point": {
                            pointRadius: 5,
                            labelYOffset: -10,
                            graphicName: "circle",
                            fillColor: "white",
                            fillOpacity: 0.7,
                            strokeWidth: 2,
                            strokeColor: "#00AAFF"
                        },
                        "Line": {
                            strokeWidth: 2,
                            strokeColor: "#00AAFF",
                            labelYOffset: 10,
                            labelAlign: "l"
                        },
                        "Polygon": {
                            fillColor: "#0000ff",
                            fillOpacity: 0,
                            strokeColor: "#33BBFF",
                            strokeWidth: 2
                        }
                    }
                })
            ]
        }),
        "select": new OpenLayers.Style(null, {
            rules: [
                new OpenLayers.Rule({
                    symbolizer: {
                        "Point": {
                            pointRadius: 7,
                            labelYOffset: -10,
                            graphicName: "circle",
                            fillColor: "white",
                            fillOpacity: 1,
                            strokeWidth: 2,
                            strokeColor: "#00AAFF"
                        },
                        "Line": {
                            strokeWidth: 3,
                            strokeColor: "#00AAFF",
                            labelYOffset: 10,
                            labelAlign: "l"
                        },
                        "Polygon": {
                            fillColor: "#33BBFF",
                            fillOpacity: 0.3,
                            strokeColor: "#00AAFF",
                            strokeWidth: 2
                        }
                    }
                })
            ]
        }),
        "temporary": new OpenLayers.Style(null, {
            rules: [
                new OpenLayers.Rule({
                    symbolizer: {
                        "Point": {
                            pointRadius: 7,
                            labelYOffset: -10,
                            graphicName: "circle",
                            fillColor: "white",
                            fillOpacity: 1,
                            strokeWidth: 2,
                            strokeColor: "#00AAFF"
                        },
                        "Line": {
                            strokeWidth: 2,
                            strokeColor: "#00AAFF",
                            labelYOffset: 10,
                            labelAlign: "l"
                        },
                        "Polygon": {
                            fillColor: "#33BBFF",
                            fillOpacity: 0.3,
                            strokeColor: "#00AAFF",
                            strokeWidth: 2
                        }
                    }
                })
            ]
        })
    }),
    styleMapLocations: new OpenLayers.StyleMap({
        "default": new OpenLayers.Style({
            label: "${getLabel}",
            fontSize: "12px",
            fontFamily: "Arial",
            labelOutlineColor: "white",
            labelOutlineWidth: 3
        }, {
            context: {getLabel: function (feature) {
                    if (typeof feature.attributes.description !== 'undefined') {
                        return feature.attributes.description;
                    } else {
                        return "";
                    }
                }},
            rules: [
                new OpenLayers.Rule({
                    symbolizer: {
                        "Point": {
                            pointRadius: 5,
                            graphicName: "circle",
                            fillColor: "white",
                            fillOpacity: 0.7,
                            labelYOffset: -10,
                            strokeWidth: 2,
                            strokeColor: "#E96EFF"
                        },
                        "Line": {
                            strokeWidth: 2,
                            strokeColor: "#E96EFF",
                            labelYOffset: 10,
                            labelAlign: "l"
                        },
                        "Polygon": {
                            fillColor: "#ED87FF",
                            fillOpacity: 0,
                            strokeColor: "#E96EFF",
                            strokeWidth: 2
                        }
                    }
                })
            ]
        }),
        "select": new OpenLayers.Style(null, {
            rules: [
                new OpenLayers.Rule({
                    symbolizer: {
                        "Point": {
                            pointRadius: 7,
                            labelYOffset: -10,
                            graphicName: "circle",
                            fillColor: "white",
                            fillOpacity: 1,
                            strokeWidth: 2,
                            strokeColor: "#E96EFF"
                        },
                        "Line": {
                            strokeWidth: 3,
                            strokeColor: "#E96EFF",
                            labelYOffset: 10,
                            labelAlign: "l"
                        },
                        "Polygon": {
                            fillColor: "#E96EFF",
                            fillOpacity: 0.3,
                            strokeColor: "#E96EFF",
                            strokeWidth: 2
                        }
                    }
                })
            ]
        }),
        "temporary": new OpenLayers.Style(null, {
            rules: [
                new OpenLayers.Rule({
                    symbolizer: {
                        "Point": {
                            pointRadius: 7,
                            labelYOffset: -10,
                            graphicName: "circle",
                            fillColor: "white",
                            fillOpacity: 1,
                            strokeWidth: 2,
                            strokeColor: "#E96EFF"
                        },
                        "Line": {
                            strokeWidth: 2,
                            strokeColor: "#E96EFF",
                            labelYOffset: 10,
                            labelAlign: "l"
                        },
                        "Polygon": {
                            fillColor: "#E96EFF",
                            fillOpacity: 0.3,
                            strokeColor: "#E96EFF",
                            strokeWidth: 2
                        }
                    }
                })
            ]
        })
    }),
    styleClaimNode: {
        pointRadius: 5,
        graphicName: "circle",
        fillColor: "white",
        fillOpacity: 0.5,
        strokeWidth: 2,
        strokeColor: "#00AAFF",
        strokeOpacity: 0.3
    },
    styleMapClaim: new OpenLayers.StyleMap({
        "default": new OpenLayers.Style({
            label: "${getLabel}",
            fontSize: "12px",
            fontFamily: "Arial",
            labelOutlineColor: "white",
            labelOutlineWidth: 3
        }, {
            context: {getLabel: function (feature) {
                    if (typeof feature.attributes.label !== 'undefined') {
                        return feature.attributes.label + "\n\n(" + (feature.attributes.area) + " m2)";
                    } else {
                        if (typeof feature.attributes.area !== 'undefined') {
                            "\n\n(" + (feature.attributes.area) + " m2)";
                        } else {
                            return "";
                        }
                    }
                }},
            rules: [
                new OpenLayers.Rule({
                    symbolizer: {
                        "Point": {
                            pointRadius: 5,
                            labelYOffset: -10,
                            graphicName: "circle",
                            fillColor: "white",
                            fillOpacity: 0.7,
                            strokeWidth: 2,
                            strokeColor: "#00AAFF"
                        },
                        "Line": {
                            strokeWidth: 2,
                            strokeColor: "#00AAFF",
                            labelYOffset: 10,
                            labelAlign: "l"
                        },
                        "Polygon": {
                            fillColor: "#0000ff",
                            fillOpacity: 0,
                            strokeColor: "#33BBFF",
                            strokeWidth: 2
                        }
                    }
                })
            ]
        }),
        "select": new OpenLayers.Style(null, {
            rules: [
                new OpenLayers.Rule({
                    symbolizer: {
                        "Point": {
                            pointRadius: 7,
                            labelYOffset: -10,
                            graphicName: "circle",
                            fillColor: "white",
                            fillOpacity: 1,
                            strokeWidth: 2,
                            strokeColor: "#00AAFF"
                        },
                        "Line": {
                            strokeWidth: 3,
                            strokeColor: "#00AAFF",
                            labelYOffset: 10,
                            labelAlign: "l"
                        },
                        "Polygon": {
                            fillColor: "#33BBFF",
                            fillOpacity: 0.3,
                            strokeColor: "#00AAFF",
                            strokeWidth: 2
                        }
                    }
                })
            ]
        }),
        "temporary": new OpenLayers.Style(null, {
            rules: [
                new OpenLayers.Rule({
                    symbolizer: {
                        "Point": {
                            pointRadius: 7,
                            labelYOffset: -10,
                            graphicName: "circle",
                            fillColor: "white",
                            fillOpacity: 1,
                            strokeWidth: 2,
                            strokeColor: "#00AAFF"
                        },
                        "Line": {
                            strokeWidth: 2,
                            strokeColor: "#00AAFF",
                            labelYOffset: 10,
                            labelAlign: "l"
                        },
                        "Polygon": {
                            fillColor: "#33BBFF",
                            fillOpacity: 0.3,
                            strokeColor: "#00AAFF",
                            strokeWidth: 2
                        }
                    }
                })
            ]
        })
    }),
    styleSnappingClaim: new OpenLayers.StyleMap({
        "default": new OpenLayers.Style({
            strokeColor: "#0099FF"
        }, {
            rules: [
                new OpenLayers.Rule({
                    symbolizer: {
                        "Polygon": {
                            fillColor: "#0000ff",
                            fillOpacity: 0,
                            strokeColor: "#0099FF",
                            strokeWidth: 2
                        }
                    }
                })
            ]
        })
    })
};

function calculateArea(feature) {
    var area = feature.geometry.getArea();
    if (area <= 100) {
        if (area % 1 <= 0.8) {
            area = area - (area % 1);
        } else {
            area = (area - (area % 1)) + 1;
        }
    } else {
        if (area > 100 && area <= 1000) {
            if (area % 10 <= 8) {
                area = area - (area % 10);
            } else {
                area = (area - (area % 10)) + 10;
            }
        } else {
            if (area > 1000 && area <= 10000) {
                if (area % 100 <= 80) {
                    area = area - (area % 100);
                } else {
                    area = (area - (area % 100)) + 100;
                }
            } else {
                if (area > 10000) {
                    if (area % 1000 <= 800) {
                        area = area - (area % 1000);
                    } else {
                        area = (area - (area % 1000)) + 1000;
                    }
                }
            }
        }
    }
    return area;
}