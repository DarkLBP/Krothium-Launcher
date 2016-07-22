//Core javascript functions for KMLK inner functionality
var play_interval = null;
var progress_value = 0;
var play_value = "";
var profile_value = "";
var keepAlive_interval = setInterval(function(){keepAlive();}, 2000);
function authenticate(){
    var username = document.getElementById("username").value;
    var password = document.getElementById("password").value;
    if (username === "" || password === "" || username === null || password === null){
        alert("Invalid credentials!");
    } else {
        var parameters = toBase64(username) + ":" + toBase64(password);
        var response = postRequest("authenticate", parameters);
        if (response === "OK"){
            redirect("/play.html");
        } else {
            alert(response);
        }
    }
}
function loadProfileData(){
    var name_base = window.location.href.split("?")[1];
    if (name_base === null){
        alert("Invalid profile request!");
    } else {
        var response = postRequest("profiledata", name_base);
        var data = response.split(":");
        if (data.constructor === Array){
            if (data.length === 6){
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
                        var name = fromBase64(vers[i]);
                        value += '<option value="' + vers[i] + '">' + name + '</option>';
                    }
                    document.getElementById("versionList").innerHTML = value;
                }
                document.getElementById("versionList").value = data[1];
                if (data[5] !== "noset"){
                    document.getElementById("javaArgs").value = fromBase64(data[5]);
                }
            }else{
                alert("Server replied with wrong amount of data.");
            }
        } else {
            alert("Could not load profile data.");
        }
    }
}
function saveProfile(){
    var name_base = window.location.href.split("?")[1];
    if (name_base === null){
        alert("Invalid profile request!");
    } else {
        var parameters = name_base + ":" + toBase64(document.getElementById("profileName").value) + ":" + document.getElementById("versionList").value + ":" + toBase64(document.getElementById("snapshot").checked.toString()) + ":" + toBase64(document.getElementById("oldBeta").checked.toString()) + ":" + toBase64(document.getElementById("oldAlpha").checked.toString()) + ":" + toBase64(document.getElementById("javaArgs").value);
        var response = postRequest("saveprofile", parameters);
        if (response !== "OK"){
            alert("Failed to save the profile!");
        } else {
            alert("Profile saved successfully!");
            redirect("/profiles.html");
        }
    }
}
function playGame(){
    play_interval = setInterval(function(){playGame_Update();}, 1000);
    postRequest("play", null);
}
function playGame_Update(){
    var response = postRequest("status", "");
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
                        clearInterval(play_interval);
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
function redirect(url){
    window.location.href = url;
}
function shutdown(){
    postRequest("close", null);
}
function keepAlive(){
    postRequest("keepalive", null);
}
function postRequest(action, parameters){
    var xhr = new XMLHttpRequest();
    xhr.open("POST", "/action/" + action, false);
    xhr.send(parameters);
    return xhr.responseText;
}
function loadSignature(){
    var response = postRequest("signature", null);
    document.getElementById("signature").innerHTML = "<center>" + response + "</center>";
}
function logOut(){
    var response = postRequest("logout", null);
    if (response === "OK"){
        redirect("/login.html");
    }
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
    document.getElementById("version").innerHTML = "Minecraft " + fromBase64(response);
}
function loadProfileList(){
    var response = postRequest("profiles", null);
    var data = response.split(":");
    if (data.constructor === Array){
        var data_length = data.length;
        var value = "";
        for (var i = 0; i < data_length; i++){
            var name = fromBase64(data[i]);
            value += '<b>' + name + '</b><a class="red-button wide profileButton" href=\"/profile.html?' + data[i] + '\">Edit</a><a class="red-button wide profileButton" onclick="deleteProfile(\'' + data[i] + '\');" href="#">Delete</a><br>';
        }
        value += '<br><a class="red-button wide" href="#">Create New</a>';
        document.getElementById("profileList").innerHTML = value;
    }
}
function setSelectedProfile(){
    var selected = document.getElementById("profiles").value;
    if (selected !== profile_value){
        var response = postRequest("setselectedprofile", selected);
        if (response !== "OK"){
            alert("Failed to change the selected profile.");
        }
        loadProfiles();
    }
}
function deleteProfile(base64name){
    var response = postRequest("deleteprofile", base64name);
    if (response !== "OK"){
        alert("Failed to delete profile " + fromBase64(base64name) + ".");
    } else {
        alert("Profile " + fromBase64(base64name) + " deleted successfully.");
        redirect("/profiles.html");
    }
}
function toBase64(string){
    return window.btoa(string);
}
function fromBase64(string){
    return window.atob(string);
}