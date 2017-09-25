# create a tcp server on android

examle
```````````
document.addEventListener("deviceready", function(){
	var socketServer=cordova.plugins.socketServer;
	socketServer.startServer(8080,function(data){
		if(data.type=="data"){
			var socketId=data.socketId;
			console.log("length: "+data.length);
			console.log("socketId: "+data.socketId);
			console.log("HostAddress: "+data.HostAddress);
			console.log("HostName: "+data.HostName);
			socketServer.write(socketId,"server reply: ok:"+ data.buffer);
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
