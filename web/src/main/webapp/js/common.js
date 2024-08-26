$(function () {
    $("[rel='tooltip']").tooltip();
});

fixAppProto = function (appUrl) {
    var proto = window.location.href;
    proto = proto.substring(0, proto.indexOf(":"));

    if(!appUrl.startsWith(proto)) {
        appUrl = proto + appUrl.substring(appUrl.indexOf(":"));
    }

    return appUrl;
};

/** 
 * Return true if provided string is null, undefined or empty. 
 * @param obj Object to test.
 */
function isNullOrEmpty(obj) {
    return obj === null || typeof obj === 'undefined' || obj === '';
}