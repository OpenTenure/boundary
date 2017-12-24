var MSG = MSG || {};
MSG.ERR = MSG.ERR || {};
MSG.ERR.MAP_CONTROL_BAD_FEATURE_STRUCTURE = "Bad feature structure";
MSG.ERR.MAP_CONTROL_BOUNDARY_GEOMETRY_MUST_BE_ONE = "Boundary geometry must be only one, not multiple";

var mapControlId;

function saveBoundaryFeature() {
    mapControl.toggleMapEditing(false);
    var wkt = new OpenLayers.Format.WKT();
    var boundaryLayer = mapControl.getMap().getLayer(OT.Map.LAYER_IDS.CURRENT_BOUNDARY);

    if (boundaryLayer.features.length > 1) {
        alert(MSG.ERR.MAP_CONTROL_BOUNDARY_GEOMETRY_MUST_BE_ONE);
        return false;
    }

    var feature;
    
    if (boundaryLayer.features.length === 1) {
        feature = boundaryLayer.features[0].clone();
        feature.geometry.transform(destCrs, sourceCrs);
        $("#" + mapControlId.replace(":", "\\:") + "\\:hCurrentBoundary").val(wkt.write(feature));
    } else {
        $("#" + mapControlId.replace(":", "\\:") + "\\:hCurrentBoundary").val("");
    }
    return true;
}
