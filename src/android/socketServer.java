package socketServer;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.LOG;
import org.apache.cordova.PluginResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
// Cordova
import android.util.Log;
import java.util.UUID;
import android.util.Base64;
import java.util.HashMap;

public class socketServer extends CordovaPlugin {
	private String OutString;
	ServerSocket myServerSocket;
	boolean ServerOn = true;
	boolean ServerActivated = false;
	HashMap<String, Socket> socketHashMap;

	@Override
	public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) throws JSONException {
		try {
			if ("startServer".equals(action)) {
				final int port = args.getInt(0);  
				if(ServerActivated){
					callbackContext.error("has been startServer");
					return false;
				}
				socketHashMap = new HashMap<String, Socket>();
				this.startServer(port,callbackContext);
				return true;
			}
			else if("write".equals(action)){
				final String uuid = args.getString(0); 
				String data = args.getString(1);
				if(socketHashMap.containsKey(uuid)){
					Socket socket=socketHashMap.get(uuid);
					OutputStream output=socket.getOutputStream();
					output.write(data.getBytes(),0,data.getBytes().length);
					return true;
				} else {
					return false;
				}
			}
			else if("disconnect".equals(action)){
				final String uuid = args.getString(0); 
				String data = args.getString(1);
				if(socketHashMap.containsKey(uuid)){
					socketHashMap.get(uuid).close();
					return true;
				} else {
					return false;
				}
			}
			else if("close".equals(action)){
				myServerSocket.close();
				return true;
			}
			else {
				callbackContext.error(action + "not supported");
				return false;
			}
		} catch (Exception e) {
			callbackContext.error(e.getMessage());
			return false;
		}
	}

	private void startServer(final int port, final CallbackContext callbackContext){
		cordova.getThreadPool().execute(new Runnable() {
			public void run() {
				try{ 
					myServerSocket = new ServerSocket(port); 
					ServerActivated = true;
					while(true) {
						try{ 
							Socket clientSocket = myServerSocket.accept();
							clientSocket.setSoTimeout(10000);
							String uuid = UUID.randomUUID().toString();
							socketHashMap.put(uuid,clientSocket);
							ClientServiceThread ClientThread = new ClientServiceThread(uuid, clientSocket, callbackContext);
							ClientThread.start();
						} catch(IOException ioe) {
							ioe.printStackTrace();
						}
					}

				} catch(IOException ioe) { 
					ioe.printStackTrace();
					//System.exit(-1); 
				} finally {
					if (myServerSocket != null) {
						try {
							myServerSocket.close();
							myServerSocket = null;
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			}
		});
	}

	public void sendPluginResult(CallbackContext callbackContext,JSONObject obj){
		PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, obj);
		pluginResult.setKeepCallback(true);
		callbackContext.sendPluginResult(pluginResult);
	}

	class ClientServiceThread extends Thread { 
		Socket myClientSocket;
		CallbackContext myCallbackContext;
		String myuuid="";
		JSONObject finishObject = new JSONObject();

		public ClientServiceThread() { 
			super();
		} 

		ClientServiceThread(String uuid, Socket socket, CallbackContext callbackContext) {
			myClientSocket = socket; 
			myCallbackContext=callbackContext;
			myuuid=uuid;
		}

		public void run() {
			try{
				finishObject.put("type", "close");
				finishObject.put("metadata", "finish");
				finishObject.put("socketId", myuuid);

				InputStream input=myClientSocket.getInputStream();
				OutputStream output = myClientSocket.getOutputStream();
				byte buffer[] = new byte[1024 * 4];
				int len=0;
				JSONObject eventObject = new JSONObject();
				eventObject.put("type", "connect");
				eventObject.put("socketId", myuuid);
				socketServer.this.sendPluginResult(myCallbackContext,eventObject);
				JSONObject dataObject = new JSONObject();
				dataObject.put("type", "data");	

				//while((len = input.read(buffer))!= -1) {
				while(true) {
					len = input.read(buffer);
					if(len==-1){
						JSONObject closeObject0 = new JSONObject();
						closeObject0.put("type", "close");
						closeObject0.put("socketId", myuuid);
						closeObject0.put("metadata", "-1");
						socketServer.this.sendPluginResult(myCallbackContext,closeObject0);	
						break;
					}
					else{
						dataObject.put("length", len);
						String str=new String(buffer, 0, len);
						dataObject.put("buffer", Base64.encodeToString(str.getBytes("UTF-8"), Base64.NO_WRAP));
						dataObject.put("socketId", myuuid);
						dataObject.put("HostName", myClientSocket.getInetAddress().getHostName());
						dataObject.put("HostAddress", myClientSocket.getInetAddress().getHostAddress());
						PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, dataObject);
						pluginResult.setKeepCallback(true);
						myCallbackContext.sendPluginResult(pluginResult);
					}
				}
				input.close(); 
				output.close();
				myClientSocket.close();
				JSONObject closeObject = new JSONObject();
				closeObject.put("type", "close");
				closeObject.put("metadata", "afterwhile");
				closeObject.put("socketId", myuuid);
				socketServer.this.sendPluginResult(myCallbackContext,closeObject);
			}
			catch(Exception e) {
				e.printStackTrace();
			}
			finally{
				try{
					//input.close();
					socketHashMap.remove(myuuid);
					myClientSocket.close();
					socketServer.this.sendPluginResult(myCallbackContext,finishObject);
				} 
				catch(IOException ioe) {
					ioe.printStackTrace();
				}
			}
		}
	}
}
