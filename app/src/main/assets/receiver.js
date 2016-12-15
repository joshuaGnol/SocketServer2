document.title = "{{title}}"

window.onload = init;

var canvas;
var divScreen;

function init(){
    var p = new Player({
        useWorker:true,
        workerFile:"/broadway/decoder.min.js"
    });

    divScreen = document.getElementById("screen");
    
    canvas = p.canvas;
    canvas.width = 0;
    canvas.height = 0;

    divScreen.appendChild(canvas);

    var reconnectTimeout = null;

    var connect = function(){
        var ws_url=location.href.replace('http://', 'ws://');
        var ws_new_socket_url = ws_url.replace('8080', '6060');

        var ws = new WebSocket(ws_new_socket_url);
        ws.binaryType = "arraybuffer";

         console.log("ws_url: " + ws_url);
         console.log("ws_new_socket_url: " + ws_new_socket_url);
         console.log("socket: " + ws);

        ws.onmessage = function(evt) {
            console.log("onMessage: ");
            p.decode(new Uint8Array(evt.data));
//            p.decode(evt.data);
        };

        ws.onclose = function(){
            console.log("onClose");
            if(reconnectTimeout != null){
                clearTimeout(reconnectTimeout);
            }
            reconnectTimeout = setTimeout(connect, 1000);
        }

        ws.onerror = function(){
            console.log("onError");
            if(reconnectTimeout != null){
                clearTimeout(reconnectTimeout);
            }
            reconnectTimeout = setTimeout(connect, 1000);
        }
    }

    connect();
}
    
var scale = 1.0;
var restoreScale = 1.0;

var scaleUp = function(){
    setScale(scale + 0.1);
}

var scaleDown = function(){
    setScale(scale - 0.1);
}

var reset = function(){
    setScale(1);
}

var fullscreen = function(){
    if (divScreen.requestFullscreen) {
      divScreen.requestFullscreen();
    } else if (divScreen.msRequestFullscreen) {
      divScreen.msRequestFullscreen();
    } else if (divScreen.mozRequestFullScreen) {
      divScreen.mozRequestFullScreen();
    } else if (divScreen.webkitRequestFullscreen) {
      divScreen.webkitRequestFullscreen();
    }
}

var adjustCanvasFullscreen = function(b){
    if(b){
        restoreScale = scale;
        setScale(1);

        divScreen.style.height = "100%";
        divScreen.style.width = "100%";

        canvas.style.height = "100%";
        canvas.style.width = "";
    } else {
        divScreen.style.height = "";
        divScreen.style.width = "";
        
        canvas.style.height = "";
        canvas.style.width = "";

        setScale(restoreScale);
    }
}

document.addEventListener('webkitfullscreenchange', function(e) {
    adjustCanvasFullscreen(document.webkitIsFullScreen);
}, false);

document.addEventListener('mozfullscreenchange', function(e) {
    adjustCanvasFullscreen(document.mozFullScreen);
}, false);

document.addEventListener('fullscreenchange', function(e) {
    adjustCanvasFullscreen(document.fullscreenEnabled);
}, false);

document.addEventListener('MSFullscreenChange', function(e) {
    adjustCanvasFullscreen(document.msFullscreenElement);
}, false);

var setScale = function(newScale){
    if(newScale >= 0.1){
        var style = "scale(" + newScale + "," + newScale + ")";
        canvas.style.webkitTransform = style;
        canvas.style.MozTransform = style;
        canvas.style.msTransform = style;
        canvas.style.OTransform = style;
        canvas.style.transform = style;
        scale = newScale;
    }
}

