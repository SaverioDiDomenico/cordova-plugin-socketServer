# create a tcp server on android"

examle
```````````
document.addEventListener("deviceready", function(){
	var socketServer=cordova.plugins.socketServer;
	socketServer.startServer(8080,function(data){
		if(data.type=="data"){
			var socketId=data.socketId;
			socketServer.write(socketId,"hello:"+Math.random());
		}
		else if(data.type=="connect"){
			console.log("connect");
		}
		else if(data.type=="close"){
			console.log("close");
		}
	  	var str=JSON.stringify(data);
	  	console.log(str);
	},function(){
		console.log("error");
	});

}, false);

```````````
