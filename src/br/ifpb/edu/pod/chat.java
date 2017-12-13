package br.ifpb.edu.pod;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
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
		String username = (String) ses.getUserProperties().get("usuario");
		
		// MENSAGEM NOVO USUARIO ENTRAR NO CHAT
		if (username == null) {
			ses.getUserProperties().put("usuario", message);//cada sessão que o usuário cria ele joga num map o nome como string e a mensagem como objeto
			for (Session s : Usuarios) {
				if (s.getUserProperties().get("usuario") != null) {
					s.getBasicRemote().sendText("Usuario " + message + " entrou do chat");
				}
			}
			
			// ENVIAR PARA TODOS
		} else if (message.toLowerCase().startsWith("send -all ")) {
			String msg = message.substring(10, message.length());
			for (Session s : Usuarios) {
				if (s.getUserProperties().get("usuario") != null) {
					s.getBasicRemote().sendText(
							username + ": " + msg + " " + horas + "h" + minutos + " " + formatador.format(date));
				}
			}
			// LISTAR USUARIOS
		} else if (message.equals("list")) {
			StringBuilder str = new StringBuilder();
			// Iterator<Session> iterator =Usuarios.iterator();
			for (Session c : Usuarios) {
				if (c.getUserProperties().get("usuario") != null) {
					str.append(c.getUserProperties().get("usuario").toString());
					str.append(",");
				}
			}
			str.delete(str.length() - 1, str.length());
			ses.getBasicRemote().sendText("Nome dos usuarios: " + str.toString());

			// SAIR DO CHAT
		} else if (message.equalsIgnoreCase("bye")) {					
			ses.close();	
			
			// RENOMEAR NOME
		} else if (message.toLowerCase().startsWith("rename ")) {
			String novonome = message.substring(7, message.length());
			System.out.println(novonome);
			novonome = novonome.trim();
			boolean existe = false;
			for (Session s : Usuarios) {
				if (s.getUserProperties().get("usuario").toString().equals(novonome) && s.getUserProperties().get("usuario") != null) {
					existe = true;
					ses.getBasicRemote().sendText("Usuario " + novonome + " ja existe na lista");
					break;

				}
			}
			if (existe==false) {
				ses.getUserProperties().put("usuario", novonome);
				for (Session se : Usuarios) {
					if (se.getUserProperties().get("usuario") != null) {
						se.getBasicRemote().sendText("Usuario " + username + " alterou o nome para " + novonome + "");
					}
				}
			} 

		}
		// MENSAGEM PRIVADA
		else if (message.toLowerCase().startsWith("send -user ")) {
			String[] msgArray = message.split(" ");
			String nome = msgArray[2];
			System.out.println(nome);
			String mensagem = "";
			boolean exis = false;// vamos assumir que nao existe
			for (int i = 0; i < msgArray.length; i++) {
				if (i > 2) {
					mensagem = mensagem + " " + msgArray[i];
				}
			}
			for (Session s : Usuarios) {
				if (s.getUserProperties().get("usuario").toString().equals(nome)) {
					exis = true;// existe
					s.getBasicRemote().sendText(
							username + ":" + mensagem + " " + horas + "h" + minutos + " " + formatador.format(date));
					break;// encontrou, nao precisa mais percorrer a lista
				}
			}
			if (exis==false) {
				ses.getBasicRemote().sendText("Usuario " + nome + " nao existe na lista");/// nao acha na lista o nome do usuario

			}

		} else {
			ses.getBasicRemote().sendText("Comando Invalido");
		}
	}	
	@OnClose
	public void onClose(Session session) throws Throwable {
		Usuarios.remove(session);
		String username = (String) session.getUserProperties().get("usuario");
		if (username != null) {
			for (Session s : Usuarios) {
				if (s.getUserProperties().get("usuario") != null) {
					s.getBasicRemote().sendText("Usuario " + username + " saiu do chat");
				}
			}
		}

	}
}
