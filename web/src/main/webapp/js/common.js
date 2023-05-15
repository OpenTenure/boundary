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