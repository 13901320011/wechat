package com.wechat.controller;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.util.JSONPObject;

@ServerEndpoint(value = "/websocket")
@Component
public class MyWebSocket {
    //静态变量，用来记录当前在线连接数。应该把它设计成线程安全的。
    private static int onlineCount = 0;
    private static int nameNO = 1;
    //concurrent包的线程安全Set，用来存放每个客户端对应的MyWebSocket对象。
    private static CopyOnWriteArrayList<MyWebSocket> webSocketSet = new CopyOnWriteArrayList<MyWebSocket>();

    //与某个客户端的连接会话，需要通过它来给客户端发送数据
    private Session session;
    
    private static int[] no;
    /**
     * 连接建立成功调用的方法*/
    @OnOpen
    public void onOpen(Session session) {
        this.session = session;
        this.setName("客户"+nameNO++);
        webSocketSet.add(this);     //加入set中
        addOnlineCount();           //在线数加1
        System.out.println("有新连接加入！当前在线人数为" + getOnlineCount());
        no=new int[onlineCount];
        try {
			sendInfo();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose() {
    	 try {
	//		sendInfo2(this.getName()+"离开聊天");
	        webSocketSet.remove(this);  //从set中删除
	        subOnlineCount();           //在线数减1
//	        System.out.println("有一连接关闭！当前在线人数为" + getOnlineCount());
	    //    sendInfo("NO" + getOnlineCount());
    	 } catch (Exception e) {
    		 // TODO Auto-generated catch block
    		 e.printStackTrace();
    	 }
    }

    /**
     * 收到客户端消息后调用的方法
     *
     * @param message 客户端发送过来的消息*/
    @OnMessage
    public void onMessage(String message, Session session) {
    //    System.out.println("来自客户端的消息:" + message);

        //群发消息
       try {
    //	   sendInfo(nameNO+++",");
		sendInfo2();
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
    }

    /**
     * 发生错误时调用
     */
    @OnError
    public void onError(Session session, Throwable error) {
        System.out.println("发生错误");
        error.printStackTrace();
    }


    public synchronized void sendMessage(String message) throws Exception {
        this.session.getBasicRemote().sendText(message);
        //this.session.getAsyncRemote().sendText(message);
    }
    public synchronized void end() throws Exception {
    	MyWebSocket ws = this;
    	for(int i=0;i<webSocketSet.size();i++){
    		MyWebSocket item = webSocketSet.get(i);
    		try {
            	if(item.equals(ws) ){
            		item.sendMessage("e你赢了!");
            	}else{
            		item.sendMessage("e很遗憾!");
            	}
            } catch (Exception e) {
                continue;
            }
    		no[i]=0;
    	}
    }

    /**
     * 群发自定义消息
     * */
    public static void sendInfo() throws Exception {
    	String message ="";
    	for(int i:no)
    		message += i+"%,";
    	message = message.substring(0,message.length()-1);
    	for (MyWebSocket item : webSocketSet) {
            try {
                item.sendMessage(message);
            } catch (IOException e) {
                continue;
            }
        }
    }
    public synchronized  void sendInfo2() throws Exception {
    	
    	MyWebSocket ws = this;
    	for(int i=0;i<webSocketSet.size();i++){
    		MyWebSocket item = webSocketSet.get(i);
    		try {
            	if(item.equals(ws) ){
            		no[i]=no[i]+1;
            		if(no[i]==100){
            			end();
            			return ;
            		}
            	}
            } catch (Exception e) {
                continue;
            }
    	}
    	sendInfo();
    }
    public static synchronized int getOnlineCount() {
        return onlineCount;
    }

    public static synchronized void addOnlineCount() {
        MyWebSocket.onlineCount++;
    }

    public static synchronized void subOnlineCount() {
        MyWebSocket.onlineCount--;
    }
    private String name ;


	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
    
}