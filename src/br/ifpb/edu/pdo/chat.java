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
	

	@OnOpen
	public void onOpen(Session ses) throws IOException {
		ses.getBasicRemote().sendText("Bem vindo ao chat. Por favor informe seu nome");
		Usuarios.add(ses);
	}

	@OnMessage
	public void onMessage(String message, Session ses) throws IOException {
		Date date = new Date();
		SimpleDateFormat formatador = new SimpleDateFormat("dd/MM/yyyy");
		Calendar data = Calendar.getInstance();
		int horas = data.get(Calendar.HOUR_OF_DAY);
		int minutos = data.get(Calendar.MINUTE);
		//int segundos =data.get(Calendar.SECOND);

		String username = (String) ses.getUserProperties().get("usuario");

		if (username == null) {
			ses.getUserProperties().put("usuario", message);
			for (Session s : Usuarios) {
				s.getBasicRemote().sendText("Usuario: " + message + " foi adicionado ao chat");
			}
//ENVIAR PARA TODOS
		} else if (message.toLowerCase().startsWith("send -all ")) {
			String msg = message.substring(10, message.length());
			for (Session s : Usuarios) {
				s.getBasicRemote()
						.sendText(username + ": " + msg + " " + horas + "h" + minutos+ " " + formatador.format(date));
			}
//LISTAR USUARIOS
		} else if (message.equals("list")) {
			StringBuilder str = new StringBuilder();
			// Iterator<Session> iterator =Usuarios.iterator();
			for (Session c : Usuarios) {
				str.append(c.getUserProperties().get("usuario").toString());
				str.append(",");

			}
			str.delete(str.length() - 1, str.length());
			ses.getBasicRemote().sendText("Nome dos usuarios: " + str.toString());
//SAIR DO CHAT
		} else if (message.equalsIgnoreCase("bye")) {
			for (Session s : Usuarios) {
				s.getBasicRemote().sendText("Usuario: " + username + " foi removido do chat");
			}
			ses.close();
			Usuarios.remove(ses);
//RENOMEAR NOME
		} else if (message.toLowerCase().startsWith("rename")) {
			String novonome = message.substring(message.indexOf(" "), message.length());
			novonome=novonome.trim();
			boolean exist = false;
			for (Session s : Usuarios) {
				if (s.getUserProperties().get("usuario").equals(novonome)==false) {
					exist=true;		
					ses.getUserProperties().put("usuario", novonome);
					//String username1 = (String) ses.getUserProperties().get("usuario");
					for (Session se : Usuarios) {
					se.getBasicRemote().sendText("Usuario: " + username + " alterou o nome para "+novonome+"");										
					
					}					
				} if(exist){
					System.out.println("Nome quem recebe a msg"+username);										
					}else{
						ses.getBasicRemote().sendText("Usuario: " + novonome + " ja existe na lista");
						
						}
					}	
			
		}
	
//MENSAGEM PRIVADA
		 else if (message.toLowerCase().startsWith("send -user")) {
			String[] msgArray = message.split(" ");
			String nome = msgArray[2];
			System.out.println(nome);
			String mensagem = "";
			boolean exist = false;// vamos assumir que nao existe
			for (int i = 0; i < msgArray.length; i++) {
				if (i > 2) {
					mensagem = mensagem + " " + msgArray[i];
				}
			}

			for (Session s : Usuarios) {

				if (s.getUserProperties().get("usuario").toString().equals(nome)) {
					exist =true;//existe
					s.getBasicRemote().sendText(
							username + ":" + mensagem + " " + horas + "h" + minutos + " " + formatador.format(date));	
					break;//encontrou, nao precisa mais percorrer a lista
					
				}
									
				}			
				if(exist){
					System.out.println("Nome quem recebe a msg"+username);					
					
					}else{
						ses.getBasicRemote().sendText("Usuario: " + nome + " nao existe na lista");
					}			

		} else {
			ses.getBasicRemote().sendText("Comando Invalido");
		}
	}

	@OnClose
	public void onClose(Session session) {
		Usuarios.remove(session);
	}
}
