//Core javascript functions for KMLK inner functionality
//Created by DarkLBP (https://krothium.com)
var status_interval = setInterval(function(){status();}, 1000);
var status_errors = 0;
var status_errors_limit = 5;
var status_requested = false;
var progress_value = 0;
var play_value = "";
var profile_value = "";
var authenticate_requested = false;
function authenticate(){
    if (!authenticate_requested){
        var username = document.getElementById("username").value;
        var password = document.getElementById("password").value;
        if (username === "" || password === "" || username === null || password === null){
            swal("{%s}", "{%s}", "error");
        } else {
            var parameters = toBase64(username) + ":" + toBase64(password);
            var xhr = new XMLHttpRequest();
            xhr.onreadystatechange = function() {
                if (xhr.readyState === XMLHttpRequest.DONE) {
                    authenticate_requested = false;
                    var response = xhr.responseText;
                    if (response === "OK"){
                        redirect("/play.html");
                    } else {
                        swal("{%s}", response, "error");
                    }
                }
            };
            xhr.onerror = function(){
                swal("{%s}", "{%s}", "error");
            };
            xhr.open("POST", "/action/authenticate", true);
            xhr.send(parameters);  
            authenticate_requested = true;
        }
    }
}
function loadProfileData(){
    if (location.href.indexOf("?") !== -1){
        var name_base = location.href.split("?")[1];
        if (name_base === null){
            swal("{%s}", "{%s}", "error");
        } else {
            var xhr = new XMLHttpRequest();
            xhr.onreadystatechange = function() {
                if (xhr.readyState === XMLHttpRequest.DONE) {
                    var response = xhr.responseText;
                    var data = response.split(":");
                    if (data.constructor === Array){
                        if (data.length === 9){
                            var name = fromBase64(data[0]);
                            document.getElementById("profileTitle").innerHTML = '<i class="fa fa-newspaper-o"></i> {%s} ' + name;
                            document.getElementById("profileName").value = name;
                            document.getElementById("snapshot").checked = (fromBase64(data[2]) === "true");
                            document.getElementById("oldBeta").checked = (fromBase64(data[3]) === "true");
                            document.getElementById("oldAlpha").checked = (fromBase64(data[4]) === "true");
                            var xhr2 = new XMLHttpRequest();
                            xhr2.onreadystatechange = function() {
                                if (xhr2.readyState === XMLHttpRequest.DONE) {
                                    var response2 = xhr2.responseText;
                                    var vers = response2.split(":");
                                    if (vers.constructor === Array){
                                        var data_length = vers.length;
                                        var value = "";
                                        for (var i = 0; i < data_length; i++){
                                            if (vers[i] === "latest"){
                                                value += '<option value="latest">{%s}</option>';
                                            } else {
                                                var name = fromBase64(vers[i]);
                                                value += '<option value="' + vers[i] + '">' + name + '</option>';
                                            }
                                        }
                                        document.getElementById("versionList").innerHTML = value;
                                    } else {
                                        swal("{%s}", "{%s}", "error");
                                    }
                                    document.getElementById("versionList").value = data[1];
                                    if (data[5] !== "noset"){
                                        document.getElementById("gameDirectory").value = fromBase64(data[5]);
                                    }
                                    if (data[6] !== "noset"){
                                        var resolution = fromBase64(data[6]);
                                        document.getElementById("resX").value = resolution.split("x")[0];
                                        document.getElementById("resY").value = resolution.split("x")[1];
                                    }
                                    if (data[7] !== "noset"){
                                        document.getElementById("javaExecutable").value = fromBase64(data[7]);
                                    }
                                    if (data[8] !== "noset"){
                                        document.getElementById("javaArgs").value = fromBase64(data[8]);
                                    }
                                }
                            };
                            xhr2.onerror = function(){
                                swal("{%s}", "{%s}", "error");
                            };
                            xhr2.open("POST", "/action/versions", true);
                            xhr2.send();
                        }else{
                            swal("{%s}", "{%s}", "error");
                        }
                    } else {
                        swal("{%s}", "{%s}", "error");
                    }
                }
            };
            xhr.onerror = function(){
                swal("{%s}", "{%s}", "error");
            };
            xhr.open("POST", "/action/profiledata", true);
            xhr.send(name_base);
        }
    } else {
        document.getElementById("profileTitle").innerHTML = '<i class="fa fa-newspaper-o"></i> {%s}';
        var xhr = new XMLHttpRequest();
        xhr.onreadystatechange = function() {
            if (xhr.readyState === XMLHttpRequest.DONE) {
                var response2 = xhr.responseText;
                var vers = response2.split(":");
                if (vers.constructor === Array){
                    var data_length = vers.length;
                    var value = "";
                    for (var i = 0; i < data_length; i++){
                        if (vers[i] === "latest"){
                            value += '<option value="latest">{%s}</option>';
                        } else {
                            var name = fromBase64(vers[i]);
                            value += '<option value="' + vers[i] + '">' + name + '</option>';
                        }
                    }
                    document.getElementById("versionList").innerHTML = value;
                } else {
                    swal("{%s}", "{%s}", "error");
                }
            }
        };
        xhr.onerror = function(){
            swal("{%s}", "{%s}", "error");
        };
        xhr.open("POST", "/action/versions", true);
        xhr.send();
    }
}
function refreshVersionList(){
    var parameters = toBase64(document.getElementById("snapshot").checked.toString()) + ":" + toBase64(document.getElementById("oldBeta").checked.toString()) + ":" + toBase64(document.getElementById("oldAlpha").checked.toString());
    var xhr = new XMLHttpRequest();
    xhr.onreadystatechange = function() {
        if (xhr.readyState === XMLHttpRequest.DONE) {
            var response = xhr.responseText;
            var vers = response.split(":");
            if (vers.constructor === Array){
                var data_length = vers.length;
                var value = "";
                for (var i = 0; i < data_length; i++){
                    if (vers[i] === "latest"){
                        value += '<option value="latest">{%s}</option>';
                    } else {
                        var name = fromBase64(vers[i]);
                        value += '<option value="' + vers[i] + '">' + name + '</option>';
                    }
                }
                document.getElementById("versionList").innerHTML = value;
            } else {
                swal("{%s}", "{%s}", "error");
            }
            var xhr2 = new XMLHttpRequest();
            xhr2.onreadystatechange = function() {
                if (xhr2.readyState === XMLHttpRequest.DONE) {
                    var response2 = xhr2.responseText;
                    document.getElementById("versionList").value = fromBase64(response2.split(":")[0]);
                }
            };
            xhr2.onerror = function(){
                swal("{%s}", "{%s}", "error");
            };
            xhr2.open("POST", "/action/selectedversion", true);
            xhr2.send();
        }
    };
    xhr.onerror = function(){
        swal("{%s}", "{%s}v", "error");
    };
    xhr.open("POST", "/action/versions", true);
    xhr.send(parameters);
}
function saveProfile(){
    if (location.href.indexOf("?") !== -1){
        var name_base = location.href.split("?")[1].replace('#', '');
    } else {
        var name_base = "noset";
    }
    if (name_base === null){
        swal("{%s}", "{%s}", "error");
    } else {
        var name = "noset";
        var version = document.getElementById("versionList").value;
        var snapshot = toBase64(document.getElementById("snapshot").checked.toString());
        var oldbeta = toBase64(document.getElementById("oldBeta").checked.toString());
        var oldalpha = toBase64(document.getElementById("oldAlpha").checked.toString());
        var gamedir = "noset";
        var resolution = "noset";
        var javaexec = "noset";
        var javaargs = "noset";
        if (document.getElementById("profileName").value !== ""){
            name = toBase64(document.getElementById("profileName").value);
        }
        if (document.getElementById("gameDirectory").value !== ""){
            gamedir = toBase64(document.getElementById("gameDirectory").value);
        }
        if (document.getElementById("resX").value !== "" && document.getElementById("resY").value !== ""){
            resolution = toBase64(document.getElementById("resX").value + "x" + document.getElementById("resY").value);
        }
        if (document.getElementById("javaExecutable").value !== ""){
            javaexec = toBase64(document.getElementById("javaExecutable").value);
        }
        if (document.getElementById("javaArgs").value !== ""){
            javaargs = toBase64(document.getElementById("javaArgs").value);
        }
        var parameters = name_base + ":" + name + ":" + version + ":" + snapshot + ":" + oldbeta + ":" + oldalpha + ":" + gamedir + ":" + resolution + ":" + javaexec + ":" + javaargs;
        var xhr = new XMLHttpRequest();
        xhr.onreadystatechange = function() {
            if (xhr.readyState === XMLHttpRequest.DONE) {
                var response = xhr.responseText;
                if (response !== "OK"){
                    swal("{%s}", response, "error");
                } else {
                    if (name_base === "noset"){
                        swal({title: "{%s}", text: "{%s} " + fromBase64(name) + " {%s}", type: "success", closeOnConfirm: false}, function(){redirect("/profiles.html");});
                    } else {
                        swal({title: "{%s}", text: "{%s} " + fromBase64(name_base) + " {%s}", type: "success", closeOnConfirm: false}, function(){redirect("/profiles.html");});
                    }
                }
            }
        };
        xhr.onerror = function(){
            swal("{%s}", "{%s}", "error");
        };
        xhr.open("POST", "/action/saveprofile", true);
        xhr.send(parameters);
    }
}
function playGame(){
    var xhr = new XMLHttpRequest();
    xhr.onerror = function(){
        swal("{%s}", "{%s}", "error");
    };
    xhr.open("POST", "/action/play", true);
    xhr.send();
}
function status(){
    if (!status_requested){
        var xhr = new XMLHttpRequest();
        xhr.onreadystatechange = function() {
            if (xhr.readyState === XMLHttpRequest.DONE) {
                if (xhr.responseText !== ""){
                    var response = xhr.responseText;
                    var data = response.split(":");
                    if (data.constructor === Array){
                        if (data.length === 2){
                            var status = data[0];
                            var progress = data[1];
                            if (document.getElementById("progress") !== null){
                                if (progress !== progress_value){
                                    document.getElementById("progress").innerHTML = '<progress value="' + progress + '" max="100"></progress>';
                                    progress_value = progress;
                                }
                            }
                            if (document.getElementById("play") !== null){
                                if (status !== play_value){
                                    switch (status){
                                        case "0":
                                            document.getElementById("play").innerHTML = '<a class="red-button wide playButton" onclick="playGame()" href="#">{%s}</a>';
                                            break;
                                        case "1":
                                            document.getElementById("play").innerHTML = '<a class="red-button wide playButton" onclick="playGame()" href="#">{%s}</a>';
                                            break;
                                        case "2":
                                            document.getElementById("play").innerHTML = '<a class="red-button wide playButton" onclick="playGame()" href="#">{%s}</a>';
                                            break;
                                    }
                                    play_value = status;
                                }
                            }
                        }
                    }
                } else {
                    if (++status_errors > status_errors_limit){
                        clearInterval(status_interval);
                        swal("{%s}", "{%s}\n{%s}", "error");
                    }
                }
                status_requested = false;
            }
        };
        xhr.timeout = 1000;
        xhr.open("POST", "/action/status", true);
        xhr.send();
        status_requested = true;
    }
}
function loadSignature(){
    var xhr = new XMLHttpRequest();
    xhr.onreadystatechange = function() {
        if (xhr.readyState === XMLHttpRequest.DONE) {
            var response = xhr.responseText;
            document.getElementById("signature").innerHTML = "<center>" + response + "</center>";
        }
    };
    xhr.onerror = function(){
        document.getElementById("signature").innerHTML = "<center>{%s}</center>";
    };
    xhr.open("POST", "/action/signature", true);
    xhr.send();
}
function logOut(){
    var xhr = new XMLHttpRequest();
    xhr.onreadystatechange = function() {
        if (xhr.readyState === XMLHttpRequest.DONE) {
            var response = xhr.responseText;
            if (response === "OK"){
                redirect("/login.html");
            }
        }
    };
    xhr.onerror = function(){
        swal("{%s}", "{%s}", "error");
    };
    xhr.open("POST", "/action/logout", true);
    xhr.send();
}
function loadProfiles(){
    var xhr = new XMLHttpRequest();
    xhr.onreadystatechange = function() {
        if (xhr.readyState === XMLHttpRequest.DONE) {
            var response = xhr.responseText;
            var data = response.split(":");
            if (data.constructor === Array){
                var data_length = data.length;
                var value = "";
                for (var i = 0; i < data_length; i++){
                    var name = fromBase64(data[i]);
                    value += '<option value="' + data[i] + '">' + name + '</option>';
                }
                document.getElementById("profiles").innerHTML = value;
            } else {
                swal("{%s}", "{%s}", "error");
            }
            var xhr2 = new XMLHttpRequest();
            xhr2.onreadystatechange = function() {
                if (xhr2.readyState === XMLHttpRequest.DONE) {
                    var response2 = xhr2.responseText;
                    document.getElementById("profiles").value = response2;
                    profile_value = response2;
                }
            };
            xhr2.onerror = function(){
                swal("{%s}", "{%s}", "error");
            };
            xhr2.open("POST", "/action/selectedprofile", true);
            xhr2.send();
            
            var xhr3 = new XMLHttpRequest();
            xhr3.onreadystatechange = function() {
                if (xhr3.readyState === XMLHttpRequest.DONE) {
                    var response3 = xhr3.responseText;
                    document.getElementById("version").innerHTML = "Minecraft " + fromBase64(response3.split(":")[1]);
                }
            };
            xhr3.onerror = function(){
                swal("{%s}", "{%s}", "error");
            };
            xhr3.open("POST", "/action/selectedversion", true);
            xhr3.send();
        }
    };
    xhr.onerror = function(){
        swal("{%s}", "{%s}", "error");
    };
    xhr.open("POST", "/action/profiles", true);
    xhr.send();
}
function loadProfileList(){
    var xhr = new XMLHttpRequest();
    xhr.onreadystatechange = function() {
        if (xhr.readyState === XMLHttpRequest.DONE) {
            var response = xhr.responseText;
            var data = response.split(":");
            if (data.constructor === Array){
                var data_length = data.length;
                var value = "";
                for (var i = 0; i < data_length; i++){
                    var name = fromBase64(data[i]);
                    value += '<b>' + name + '</b><a class="red-button halfWideButton" href=\"/profile.html?' + data[i] + '\">{%s}</a><a class="red-button halfWideButton" onclick="deleteProfile(\'' + data[i] + '\');" href="#">{%s}</a><br>';
                }
                value += '<br><a class="red-button wide" href="/profile.html">{%s}</a>';
                document.getElementById("profileList").innerHTML = value;
            } else {
                swal("{%s}", "{%s}", "error");
            }
        }
    };
    xhr.onerror = function(){
         swal("{%s}", "{%s}", "error");
    };
    xhr.open("POST", "/action/profiles", true);
    xhr.send();
}
function setSelectedProfile(){
    var selected = document.getElementById("profiles").value;
    if (selected !== profile_value){
        var xhr = new XMLHttpRequest();
        xhr.onreadystatechange = function() {
            if (xhr.readyState === XMLHttpRequest.DONE) {
                var response = xhr.responseText;
                if (response !== "OK"){
                    swal("{%s}", "{%s}", "error");
                }
                loadProfiles();
            }
        };
        xhr.onerror = function(){
            swal("{%s}", "{%s}", "error");
        };
        xhr.open("POST", "/action/setselectedprofile", true);
        xhr.send(selected);
    }
}
function deleteProfile(base64name){
    var xhr = new XMLHttpRequest();
    xhr.onreadystatechange = function() {
        if (xhr.readyState === XMLHttpRequest.DONE) {
            var response = xhr.responseText;
            if (response !== "OK"){
                swal("{%s}", "{%s} " + fromBase64(base64name) + ".", "error");
            } else {
                swal("{%s}", "{%s} " + fromBase64(base64name) + " {%s}", "success");
                loadProfileList();
                loadProfiles();
            }
        }
    };
    xhr.onerror = function(){
        swal("{%s}", "{%s}", "error");
    };
    xhr.open("POST", "/action/deleteprofile", true);
    xhr.send(base64name);
}
function updateSkin(){
    if (document.getElementById("skinFile").files.length > 0){
        var reader = new FileReader();
        reader.onloadend = function(e) {
            var content = this.result;
            var xhr = new XMLHttpRequest();
            xhr.onreadystatechange = function() {
                if (xhr.readyState === XMLHttpRequest.DONE) {
                    var response = xhr.responseText;
                    if (response !== "OK"){
                        swal("{%s}", response, "error");
                    } else {
                        swal({title: "{%s}", text: "{%s}", type: "success", closeOnConfirm: false}, function(){redirect("/account.html");});
                    }
                }
            };
            xhr.onerror = function(){
                swal("{%s}", "{%s}", "error");
            };
            xhr.open("POST", "/action/changeskin", true);
            xhr.setRequestHeader("Content-Type", document.getElementById("skinFile").files[0].type);
            xhr.setRequestHeader("Content-Length", content.length);
            xhr.setRequestHeader("Content-Extra", document.getElementById("skinFormat").value);
            xhr.send(content);
        };
        reader.readAsDataURL(document.getElementById("skinFile").files[0]);
    } else {
        swal("{%s}", "{%s}", "warning");
    }
}
function updateCape(){
    if (document.getElementById("capeFile").files.length > 0){
        var reader = new FileReader();
        var reader = new FileReader();
        reader.onloadend = function(e) {
            var content = this.result;
            var xhr = new XMLHttpRequest();
            xhr.onreadystatechange = function() {
                if (xhr.readyState === XMLHttpRequest.DONE) {
                    var response = xhr.responseText;
                    if (response !== "OK"){
                        swal("{%s}", response, "error");
                    } else {
                        swal({title: "{%s}", text: "{%s}", type: "success", closeOnConfirm: false}, function(){redirect("/account.html");});
                    }
                }
            };
            xhr.onerror = function(){
                swal("{%s}", "{%s}", "error");
            };
            xhr.open("POST", "/action/changecape", true);
            xhr.setRequestHeader("Content-Type", document.getElementById("capeFile").files[0].type);
            xhr.setRequestHeader("Content-Length", document.getElementById("capeFile").files[0].length);
            xhr.send(content);
        };
        reader.readAsDataURL(document.getElementById("capeFile").files[0]);
    } else {
        swal("{%s}", "{%s}", "warning");
    }
}
function updateCapePreview(){
    var xhr = new XMLHttpRequest();
    xhr.onreadystatechange = function() {
        if (xhr.readyState === XMLHttpRequest.DONE) {
            var response = xhr.responseText;
            if (response !== ""){
                document.getElementById("capePreview").innerHTML = "<img src=\"" + response + "\">";
            } else {
                document.getElementById("capePreview").innerHTML = "{%s}";
            }
        }
    };
    xhr.onerror = function(){
        swal("{%s}", "{%s}", "error");
    };
    xhr.open("POST", "/action/getcape", true);
    xhr.send();
}
function updateSkinPreview(){
    var xhr = new XMLHttpRequest();
    xhr.onreadystatechange = function() {
        if (xhr.readyState === XMLHttpRequest.DONE) {
            var response = xhr.responseText;
            if (response !== ""){
                document.getElementById("skinPreview").innerHTML = "<img src=\"" + response + "\">";
            } else {
                document.getElementById("skinPreview").innerHTML = "{%s}";
            }
        }
    };
    xhr.onerror = function(){
        swal("{%s}", "{%s}", "error");
    };
    xhr.open("POST", "/action/getskin", true);
    xhr.send();
}
function deleteSkin(){
    var xhr = new XMLHttpRequest();
    xhr.onreadystatechange = function() {
        if (xhr.readyState === XMLHttpRequest.DONE) {
            var response = xhr.responseText;
            if (response !== "OK"){
                swal("{%s}", "{%s}\n{%s} " + response, "error");
            } else {
                swal("{%s}", "{%s}", "success");
                updateSkinPreview();
            }
        }
    };
    xhr.onerror = function(){
        swal("{%s}", "{%s}", "error");
    };
    xhr.open("POST", "/action/deleteskin", true);
    xhr.send();
}
function deleteCape(){
    var xhr = new XMLHttpRequest();
    xhr.onreadystatechange = function() {
        if (xhr.readyState === XMLHttpRequest.DONE) {
            var response = xhr.responseText;
            if (response !== "OK"){
                swal("{%s}", "{%s}\n{%s} " + response, "error");
            } else {
                swal("{%s}", "{%s}", "success");
                updateCapePreview();
            }
        }
    };
    xhr.onerror = function(){
        swal("{%s}", "{%s}", "error");
    };
    xhr.open("POST", "/action/deletecape", true);
    xhr.send();
}
function switchLanguage(){
    var xhr = new XMLHttpRequest();
    var l = toBase64(document.getElementById("langSelect").value);
    xhr.onreadystatechange = function() {
        if (xhr.readyState === XMLHttpRequest.DONE) {
            var response = xhr.responseText;
            if (response !== "OK"){
                swal("{%s}", "{%s}\n{%s} " + response, "error");
            } else {
                createCookie("lang", document.getElementById("langSelect").value, 365);
                location.reload();
            }
        }
    };
    xhr.onerror = function(){
        swal("{%s}", "{%s}", "error");
    };
    xhr.open("POST", "/action/switchlanguage", true);
    xhr.send(l);
}
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
    fileref.setAttribute("href", "styles/themes/" + ((c !== null) ? c : "blue") + ".css");
    document.getElementsByTagName("head")[0].appendChild(fileref);
    if (document.getElementById("style") !== null){
        document.getElementById("style").value = ((c !== null) ? c : "blue");
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
    if (l !== null){
        var xhr = new XMLHttpRequest();
        xhr.onreadystatechange = function() {
            if (xhr.readyState === XMLHttpRequest.DONE) {
                var xhr2 = new XMLHttpRequest();
                xhr2.onreadystatechange = function() {
                    if (xhr2.readyState === XMLHttpRequest.DONE) {
                        var response = xhr2.responseText;
                        if (response === "YES"){
                            redirect("/update.html");
                        } else {
                            redirect("/login.html");
                        }
                    }
                };
                xhr2.onerror = function(){
                    redirect("/login.html");
                };
                xhr2.open("POST", "/action/getlatestversion", true);
                xhr2.send();
            }
        };
        xhr.onerror = function(){
            redirect("/login.html");
        };
        xhr.open("POST", "/action/switchlanguage", true);
        xhr.send(toBase64(l));
    } else {
        redirect("/login.html");
    }
}
function cancelUpdate(){
    redirect("/login.html");
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
function loadLang(){
    var l = readCookie('lang');
    if (l !== null){
        document.getElementById("langSelect").value = l;
    }
}