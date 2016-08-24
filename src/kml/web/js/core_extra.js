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
function bootstrap(){
    var l = readCookie("lang");
    var after = location.href.split("?")[1];
    if (l){
        var xhr = new XMLHttpRequest();
        xhr.onreadystatechange = function() {
            if (xhr.readyState === XMLHttpRequest.DONE) {
                var xhr2 = new XMLHttpRequest();
                xhr2.onreadystatechange = function() {
                    if (xhr2.readyState === XMLHttpRequest.DONE) {
                        var response = xhr2.responseText;
                        if (response === "YES"){
                            if (after === "play"){
                                redirect("/update.html?play");
                            } else {
                                redirect("/update.html?login");
                            }
                        } else {
                            if (after === "play"){
                                redirect("/play.html");
                            } else {
                                redirect("/login.html");
                            }
                        }
                    }
                };
                xhr2.onerror = function(){
                    if (after === "play"){
                        redirect("/play.html");
                    } else {
                        redirect("/login.html");
                    }
                }
                xhr2.open("POST", "/action/getlatestversion", true);
                xhr2.send();
            }
        };
        xhr.onerror = function(){
            if (after === "play"){
                redirect("/play.html");
            } else {
                redirect("/login.html");
            }
        };
        xhr.open("POST", "/action/switchlanguage", true);
        xhr.send(toBase64(l));
    }
}
function cancelUpdate(){
    var after = location.href.split("?")[1];
    if (after === "play"){
        redirect("/play.html");
    } else {
        redirect("/login.html");
    }
}
function register(){
    redirect("https://krothium.com/index.php?/register/");
}
function loadUpdate(){
    var xhr = new XMLHttpRequest();
    xhr.onreadystatechange = function() {
        if (xhr.readyState === XMLHttpRequest.DONE) {
            var response = fromBase64(xhr.responseText);
            if (response.indexOf("http") !== -1){
                document.getElementById("updateButton").target = "_blank";
                document.getElementById("updateButton").href = response;
            }
        }
    };
    xhr.onerror = function(){
        cancelUpdate();
    };
    xhr.open("POST", "/action/getupdateurl", true);
    xhr.send();
}