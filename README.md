# create a tcp server on android
应用场景：物联网设备，安卓系统上位机与下位机通讯，无显示器的板子通过热点配网等<br/>
已实现功能：tcp server，<br/>
支持事件：onconnect，ondata，onclose，<br/>
支持方法：write(socketId,data)<br/>

日后计划实现的功能：<br/>
1、tcpclient <br/>
2、websocket server <br/>
3、websocket client<br/>
4、（传感器）串口 read write<br/>
5、（传感器）串口转tcp server<br/>
6、（传感器）串口映射<br/>
7、（传感器）串口跨内网映射<br/>

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



