package socketServer;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

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

public class socketServer extends CordovaPlugin {
	private String OutString;
	ServerSocket myServerSocket;
	boolean ServerOn = true;
	boolean ServerActivated = false;
	
	@Override
	public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) throws JSONException {
		try {
			if ("startServer".equals(action)) {
				//final int port = Integer.valueOf(args.getString(0));  
				final int port = args.getInt(0);  
				if(ServerActivated){
					callbackContext.error("has been startServer");
					return false;
				}
				cordova.getThreadPool().execute(new Runnable() {
					public void run() {
						try{ 
							myServerSocket = new ServerSocket(port); 
							ServerActivated = true;
						} catch(IOException ioe) { 
							System.exit(-1); 
						}

						while(ServerOn) {
							try{ 
								Socket clientSocket = myServerSocket.accept();
								ClientServiceThread ClientThread = new ClientServiceThread(clientSocket, callbackContext);
								ClientThread.start();
							} catch(IOException ioe) {
								ioe.printStackTrace();
							}
						}
						try {
							myServerSocket.close();
						} catch(Exception e) {
							System.exit(-1); 
						}
						//callbackContext.success();
					}
				});
				return true;
			} else {
				callbackContext.error(action + "not supported");
				return false;
			}
		} catch (Exception e) {
			callbackContext.error(e.getMessage());
			return false;
		}
	}
	
	class ClientServiceThread extends Thread { 
		Socket myClientSocket;
		boolean m_bRunThread = true; 
		CallbackContext myCallbackContext;

		public ClientServiceThread() { 
			super(); 
		} 

		ClientServiceThread(Socket socket, CallbackContext callbackContext) {
			myClientSocket = socket; 
			myCallbackContext=callbackContext;
		}

	    /*public static String bytes2HexString(byte[] b) {  
	        StringBuffer result = new StringBuffer();  
	        String hex;  
	        for (int i = 0; i < b.length; i++) {  
	            hex = Integer.toHexString(b[i] & 0xFF);  
	            if (hex.length() == 1) {  
	                hex = '0' + hex;  
	            }  
	            result.append(hex.toUpperCase());  
	        }  
	        return result.toString();  
	    } */

		public void run() {
			try{
				InputStream input=myClientSocket.getInputStream();
				OutputStream output = myClientSocket.getOutputStream();
				byte buffer[] = new byte[1024 * 10];
				int count=0;
				while(m_bRunThread) {
					if(!ServerOn){ 
						m_bRunThread = false;
					}
					else{
						count = input.read(buffer);
						if(count==-1){break;}
						else{
							myCallbackContext.success(buffer);
						}

						HttpClient httpClient = new DefaultHttpClient();
						String url = "http://192.168.1.103:9090/debug";
						HttpPost httpPost = new HttpPost(url);
						try {
				            StringEntity entity0 = new StringEntity("data", "utf-8");
				            entity0.setContentType("application/json");
				            httpPost.setEntity(entity0);
							//执行请求对象
							try {
								//第三步：执行请求对象，获取服务器发还的相应对象
								HttpResponse response = httpClient.execute(httpPost);
								//第四步：检查相应的状态是否正常：检查状态码的值是200表示正常
								if (response.getStatusLine().getStatusCode() == 200) {
									//第五步：从相应对象当中取出数据，放到entity当中
									HttpEntity entity = response.getEntity();
									BufferedReader reader = new BufferedReader(new InputStreamReader(entity.getContent()));
									String result = reader.readLine();
									//Log.d("HTTP", "POST:" + result);
								}
							} catch (Exception e) {
								e.printStackTrace();
							}
						} catch (Exception e) {
							e.printStackTrace();
						}

					}
				} 
			} 
			catch(Exception e) { 
				e.printStackTrace(); 
			} 
			finally{ 
				try{
					//input.close(); 
					myClientSocket.close();
				} 
				catch(IOException ioe) { 
					ioe.printStackTrace(); 
				} 
			} 
		} 
	}   

}