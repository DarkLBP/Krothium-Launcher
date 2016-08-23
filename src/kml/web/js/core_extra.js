function createCookie(name,value,days){
    if (days){
        var date = new Date();
        date.setTime(date.getTime()+(days*24*60*60*1000));
        var expires = "; expires="+date.toGMTString();
    }
    else var expires = "";
    document.cookie = name+"="+value+expires+"; path=/";
}
function readCookie(name){
    var nameEQ = name + "=";
    var ca = document.cookie.split(';');
    for(var i=0;i < ca.length;i++){
        var c = ca[i];
        while (c.charAt(0) === ' ') c = c.substring(1,c.length);
        if (c.indexOf(nameEQ) === 0) return c.substring(nameEQ.length,c.length);
    }
    return null;
}
function eraseCookie(name){
    createCookie(name,"",-1);
}
function loadTheme(){
    var c = readCookie('style');
    var fileref=document.createElement("link");
    fileref.setAttribute("rel", "stylesheet");
    fileref.setAttribute("type", "text/css");
    fileref.setAttribute("href", "styles/themes/" + (c ? c : "blue") + ".css");
    document.getElementsByTagName("head")[0].appendChild(fileref);
    if (document.getElementById("style") !== null){
        console.info(c);
        document.getElementById("style").value = (c ? c : "blue");
    }
}
function switchStyle(){
    createCookie("style", document.getElementById("style").value, 365);
    loadTheme();
}
function toBase64(string){
    if (string === null){
        return "";
    }
    return btoa(string);
}
function fromBase64(string){
    if (string === null){
        return "";
    }
    return atob(string);
}
function redirect(url){
    location.href = url;
}