//Core javascript functions for KMLK inner functionality
//Created by DarkLBP (https://krothium.com)
var status_interval = setInterval(function(){status();}, 1000);
var progress_value = 0;
var play_value = "";
var profile_value = "";
var keepAlive_interval = setInterval(function(){keepAlive();}, 1000);
var keepAlive_requested = false;
var authenticate_requested = false;
function authenticate(){
    if (!authenticate_requested){
        var username = document.getElementById("username").value;
        var password = document.getElementById("password").value;
        if (username === "" || password === "" || username === null || password === null){
            swal("Error", "Invalid credentials!", "error");
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
                        swal("Error", response, "error");
                    }
                }
            };
            xhr.onerror = function(){
                swal("Error", "Failed to send authentication query.", "error");
            };
            xhr.open("POST", "/action/authenticate", true);
            xhr.send(parameters);  
            authenticate_requested = true;
        }
    }
}
function register(){
    redirect("https://krothium.com/index.php?/register/");
}
function loadProfileData(){
    var name_base = window.location.href.split("?")[1];
    if (name_base === null){
        swal("Error", "Invalid profile request!", "error");
    } else {
        var response = postRequest("profiledata", name_base);
        var data = response.split(":");
        if (data.constructor === Array){
            if (data.length === 9){
                var name = fromBase64(data[0]);
                document.getElementById("profileTitle").innerHTML = '<i class="fa fa-newspaper-o"></i> Profile: ' + name;
                document.getElementById("profileName").value = name;
                document.getElementById("snapshot").checked = (fromBase64(data[2]) === "true");
                document.getElementById("oldBeta").checked = (fromBase64(data[3]) === "true");
                document.getElementById("oldAlpha").checked = (fromBase64(data[4]) === "true");
                response = postRequest("versions", null);
                var vers = response.split(":");
                if (vers.constructor === Array){
                    var data_length = vers.length;
                    var value = "";
                    for (var i = 0; i < data_length; i++){
                        if (vers[i] === "latest"){
                            value += '<option value="latest">Use Latest Release</option>';
                        } else {
                            var name = fromBase64(vers[i]);
                            value += '<option value="' + vers[i] + '">' + name + '</option>';
                        }
                    }
                    document.getElementById("versionList").innerHTML = value;
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
            }else{
                swal("Error", "Server replied with wrong amount of data.", "error");
            }
        } else {
            swal("Error", "Could not load profile data.", "error");
        }
    }
}
function refreshVersionList(){
    var parameters = toBase64(document.getElementById("snapshot").checked.toString()) + ":" + toBase64(document.getElementById("oldBeta").checked.toString()) + ":" + toBase64(document.getElementById("oldAlpha").checked.toString());
    var response = postRequest("versions", parameters);
    var vers = response.split(":");
    if (vers.constructor === Array){
        var data_length = vers.length;
        var value = "";
        for (var i = 0; i < data_length; i++){
            if (vers[i] === "latest"){
                value += '<option value="latest">Use Latest Release</option>';
            } else {
                var name = fromBase64(vers[i]);
                value += '<option value="' + vers[i] + '">' + name + '</option>';
            }
        }
        document.getElementById("versionList").innerHTML = value;
    }
    response = postRequest("selectedversion", null);
    document.getElementById("versionList").value = fromBase64(response.split(":")[0]);
}
function saveProfile(){
    var name_base = window.location.href.split("?")[1].replace('#', '');
    if (name_base === null){
        swal("Error", "Invalid profile request!", "error");
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
                    swal("Error", response, "error");
                } else {
                    swal({title: "Success", text: "Profile " + fromBase64(name_base) + " saved successfully.", type: "success", closeOnConfirm: false}, function(){redirect("/profiles.html");});
                }
            }
        };
        xhr.onerror = function(){
            swal("Error", "Failed to send saveprofile query.", "error");
        };
        xhr.open("POST", "/action/saveprofile", true);
        xhr.send(parameters);
    }
}
function playGame(){
    var xhr = new XMLHttpRequest();
    xhr.onerror = function(){
        swal("Error", "Failed to send play query.", "error");
    };
    xhr.open("POST", "/action/play", true);
    xhr.send();
}
function status(){
    var xhr = new XMLHttpRequest();
    xhr.onreadystatechange = function() {
        if (xhr.readyState === XMLHttpRequest.DONE) {
            var response = xhr.responseText;
            var data = response.split(":");
            if (data.constructor === Array){
                if (data.length === 2){
                    var status = data[0];
                    var progress = data[1];
                    if (progress !== progress_value){
                        document.getElementById("progress").innerHTML = '<progress value="' + progress + '" max="100"></progress>'
                        progress_value = progress;
                    }
                    if (status !== play_value){
                        switch (status){
                            case "0":
                                document.getElementById("play").innerHTML = '<a class="red-button wide playButton" onclick="playGame()" href="#">PLAY</a>';
                                break;
                            case "1":
                                document.getElementById("play").innerHTML = '<a class="red-button wide playButton" onclick="playGame()" href="#">DOWNLOADING</a>';
                                break;
                            case "2":
                                document.getElementById("play").innerHTML = '<a class="red-button wide playButton" onclick="playGame()" href="#">PLAYING</a>';
                                break;
                        }
                        play_value = status;
                    }
                }
            }
        }
    };
    xhr.open("POST", "/action/status", true);
    xhr.send();
}
function redirect(url){
    window.location.href = url;
}
function keepAlive(){
    if (!keepAlive_requested){
        var xhr = new XMLHttpRequest();
        xhr.onreadystatechange = function() {
            if (xhr.readyState === XMLHttpRequest.DONE) {
                keepAlive_requested = false;
            }
        };
        xhr.onerror = function(){
            clearInterval(status_interval);
            clearInterval(keepAlive_interval);
            swal("Error", "Connection lost with the launcher.\nYou can close this page safely.", "error");  
        };
        xhr.open("POST", "/action/keepalive", true);
        xhr.send();
        keepAlive_requested = true;
    }
}
function postRequest(action, parameters){
    var xhr = new XMLHttpRequest();
    xhr.open("POST", "/action/" + action, false);
    if (parameters !== null){
        if (parameters.constructor === File){
            xhr.setRequestHeader("Content-Length", parameters.size);
            xhr.setRequestHeader("Content-Type", parameters.type);
        } else {
            xhr.setRequestHeader("Content-Type", "text/plain");
        }
    } else {
        xhr.setRequestHeader("Content-Type", "text/plain");
    }
    xhr.send(parameters);
    return xhr.responseText;
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
        document.getElementById("signature").innerHTML = "<center>Failed to signature.</center>";
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
                swal("Error", "Failed to send logout query.", "error");
    };
    xhr.open("POST", "/action/logout", true);
    xhr.send();
}
function loadProfiles(){
    var response = postRequest("profiles", null);
    var data = response.split(":");
    if (data.constructor === Array){
        var data_length = data.length;
        var value = "";
        for (var i = 0; i < data_length; i++){
            var name = fromBase64(data[i]);
            value += '<option value="' + data[i] + '">' + name + '</option>';
        }
        document.getElementById("profiles").innerHTML = value;
    }
    response = postRequest("selectedprofile", null);
    document.getElementById("profiles").value = response;
    profile_value = response;
    response = postRequest("selectedversion", null);
    document.getElementById("version").innerHTML = "Minecraft " + fromBase64(response.split(":")[1]);
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
                    value += '<b>' + name + '</b><a class="red-button halfWideButton" href=\"/profile.html?' + data[i] + '\">Edit</a><a class="red-button halfWideButton" onclick="deleteProfile(\'' + data[i] + '\');" href="#">Delete</a><br>';
                }
                value += '<br><a class="red-button wide" href="#">Create New</a>';
                document.getElementById("profileList").innerHTML = value;
            }
        }
    };
    xhr.onerror = function(){
         swal("Error", "Failed to send profiles query.", "error");
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
                    swal("Error", "Failed to change the selected profile.", "error");
                }
                loadProfiles();
            }
        };
        xhr.onerror = function(){
            swal("Error", "Failed to send setselectedprofile query.", "error");
        };
        xhr.open("POST", "/action/setselectedprofile", true);
        xhr.send();
    }
}
function deleteProfile(base64name){
    var xhr = new XMLHttpRequest();
    xhr.onreadystatechange = function() {
        if (xhr.readyState === XMLHttpRequest.DONE) {
            var response = xhr.responseText;
            if (response !== "OK"){
                swal("Error", "Failed to delete profile " + fromBase64(base64name) + ".", "error");
            } else {
                swal("Success", "Profile " + fromBase64(base64name) + " deleted successfully.", "success");
            }
        }
    };
    xhr.onerror = function(){
        swal("Error", "Failed to send deleteprofile query.", "error");
    };
    xhr.open("POST", "/action/deleteprofile", true);
    xhr.send(base64name);
}
function toBase64(string){
    return window.btoa(string);
}
function fromBase64(string){
    return window.atob(string);
}
function updateSkin(){
    if (document.getElementById("skinFile").files.length > 0){
        var response = postRequest("changeskin", document.getElementById("skinFile").files[0]);
        if (response !== "OK"){
            swal("Error", response, "error");
        }
    } else {
        swal("Warning", "Select a skin first.", "warning");
    }
}
function updateCape(){
    if (document.getElementById("capeFile").files.length > 0){
        var response = postRequest("changecape", document.getElementById("capeFile").files[0]);
        if (response !== "OK"){
            swal("Error", response, "error");
        }
    } else {
        swal("Warning", "Select a cape first.", "warning");
    }
}