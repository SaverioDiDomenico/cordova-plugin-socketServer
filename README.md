# create a tcp server on android

install: 

``````
cordova plugin add https://github.com/huge818/socketServer.git
````````

example
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
			socketServer.write(socketId,"server reply: ok:"+ data.buffer); //base64
		}
		else if(data.type=="connect"){
			console.log("connect");
			console.log("socketId: "+data.socketId);
		}
		else if(data.type=="close"){
			console.log("close");
			console.log("socketId: "+data.socketId);
		}
	  	var str=JSON.stringify(data);
	  	console.log(str);
	},function(){
		console.log("error");
	});

}, false);

```````````
