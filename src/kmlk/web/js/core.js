//Core javascript functions for KMLK inner functionality
var play_interval = null;
var progress_value = 0;
var play_value = "";
var profile_value = "";
var keepAlive_interval = setInterval(function(){keepAlive();}, 2000);
function authenticate(user, pass){
    var parameters = "u=" + user + "&p=" + pass;
    var response = postRequest("authenticate", parameters);
    if (response === "OK"){
        redirect("/play.html");
    } else {
        alert(response);
    }
}
function playGame(){
    play_interval = setInterval(function(){playGame_Update();}, 1000);
    postRequest("play", null);
}
function playGame_Update(){
    var response = postRequest("status", "");
    var data = response.split("\n");
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
    var data = response.split("\n");
    if (data.constructor === Array){
        var data_length = data.length;
        var value = "";
        for (var i = 0; i < data_length; i++){
            var name = data[i];
            value += '<option value="' + name + '">' + name + '</option>';
        }
        document.getElementById("profiles").innerHTML = value;
    }
    response = postRequest("selectedprofile", null);
    document.getElementById("profiles").value = response;
    response = postRequest("selectedversion", null);
    document.getElementById("version").innerHTML = "Minecraft " + response;
    profile_value = response;
}
function loadProfileList(){
    var response = postRequest("profiles", null);
    var data = response.split("\n");
    if (data.constructor === Array){
        var data_length = data.length;
        var value = "";
        for (var i = 0; i < data_length; i++){
            var name = data[i];
            value += '<b>' + name + '</b><a class="red-button wide profileButton" href="#">Edit</a><a class="red-button wide profileButton" onclick="deleteProfile(\'' + name + '\');" href="#">Delete</a><br>';
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
function deleteProfile(name){
    var response = postRequest("deleteprofile", name);
    if (response !== "OK"){
        alert("Failed to delete profile " + name + ".");
    } else {
        alert("Profile " + name + " deleted successfully.");
        redirect("/profiles.html");
    }
}