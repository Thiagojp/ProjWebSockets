package br.ifpb.edu.pdo;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

@ServerEndpoint("/chat")
public class chat {

	static Set<Session> Usuarios = Collections.synchronizedSet(new HashSet<Session>());
	Date date = new Date();
	SimpleDateFormat formatador = new SimpleDateFormat("dd/MM/yyyy");
	Calendar data = Calendar.getInstance();
	int horas = data.get(Calendar.HOUR_OF_DAY);
	int minutos = data.get(Calendar.MINUTE);

	@OnOpen
	public void onOpen(Session ses) throws IOException {
		ses.getBasicRemote().sendText("Bem vindo ao chat. Por favor informe seu nome");
		Usuarios.add(ses);
	}

	@OnMessage
	public void onMessage(String message, Session ses) throws IOException {

             String username= (String) ses.getUserProperties().get("usuario");
             
             if(username==null){
            	 ses.getUserProperties().put("usuario", message);
            	 for(Session s : Usuarios){
            		 s.getBasicRemote().sendText("Usuario: " +message+" foi adicionado ao chat");		 
            	 }
           
             }else if (message.toLowerCase().startsWith("send -all ")) 
				{
					String msg = message.substring(10, message.length());
					for(Session s : Usuarios){
	            		 s.getBasicRemote().sendText(username+": "+msg+ " " + horas
									+ "h" + minutos + " " + formatador.format(date));		 
	            	 }
					
				}else if (message.equals("list")) {
					StringBuilder str = new StringBuilder();
					//Iterator<Session> iterator =Usuarios.iterator();
					for (Session c : Usuarios) {
						str.append(c.getUserProperties().get("usuario").toString());
						str.append(",");
						
					}
					str.delete(str.length() - 1, str.length());
					ses.getBasicRemote().sendText("Nome dos usuarios: "+ str.toString());
				
				}else if(message.equalsIgnoreCase("bye")){
					 for(Session s : Usuarios){
	            		 s.getBasicRemote().sendText("Usuario: " +username+" foi removido do chat");		 
	            	 }
					 ses.close();
					Usuarios.remove(ses);								
             
             }else{
            	 ses.getBasicRemote().sendText("Comando Invalido");
             }
             
	}

	@OnClose
	public void onClose(Session session) {
		Usuarios.remove(session);
	}
}
