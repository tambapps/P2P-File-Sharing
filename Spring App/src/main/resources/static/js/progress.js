"use strict";
function updateProgress(id, progress) {
    document.getElementById(id).value = progress.toString();
}

//TODO still doesn't work
function intervalUpdateProgress(id, progressID, finishedParagraphId) {
    var errorDict = errorMap();
    var intervalID = window.setInterval(function () {
        var xmlHttp = new XMLHttpRequest();
        xmlHttp.onreadystatechange = function () {
            if (! (xmlHttp.readyState == 4 && xmlHttp.status == 200)) {
                return;
            }
            var progress = parseInt(xmlHttp.responseText.trim());
            if (progress >= 100) {
                updateProgress(id, 100);
                document.getElementById(finishedParagraphId).innerHTML = "Transfer finished";
                clearInterval(intervalID);
            } else if (progress < 0) {
                document.getElementById(finishedParagraphId).innerHTML = errorDict[progress];
                clearInterval(intervalID);
            } else {
                updateProgress(id, progress);
            }
        };
        xmlHttp.open("GET", "http://localhost:8080/progress/".concat(progressID.toString()), true); // false for synchronous request
        xmlHttp.send(null);

    }, 400);
    return intervalID;
}

function errorMap() {
    var dict = {};
    dict[-1] = "This task doesn't exist anymore";
    dict[-2] = "An error occurred during the task";
    dict[-3] = "No connection was made, the sending has been canceled";
    return dict;
}