import org.eclipse.jetty.websocket.api.*;

import hexifence.gui.core.GameRoom;
import spark.ModelAndView;
import spark.Spark;

import static spark.Spark.*;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.zip.GZIPOutputStream;

public class ServerMain {
	private static int PLAYER_COUNT = 0;

	static ConcurrentHashMap<Session, PlayerData> players = new ConcurrentHashMap<Session, PlayerData>();
	static ConcurrentHashMap<GameRoom, ServerRoomData> rooms = new ConcurrentHashMap<GameRoom, ServerRoomData>();
	
	public static void createPlayer(Session session, String name) {
		players.put(session, new PlayerData(name));
	}
	
	public static GameRoom createRoom(Session session, String room_name, int dim) {
		ServerRoomData new_sev = new ServerRoomData(session);
		GameRoom new_pub_room = new GameRoom(room_name, dim, new_sev.id);
		
		rooms.put(new_pub_room, new_sev);
		
		return new_pub_room;
	}
	
	public static void main(String[] args) {
		port(Integer.valueOf(System.getenv("PORT")));
		staticFileLocation("/public");
		webSocket("/players", SocketHandler.class);
		init();

		get("/rooms", (request, response) -> {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			GZIPOutputStream zos = new GZIPOutputStream(baos);
	        ObjectOutputStream oos = new ObjectOutputStream( zos );

	        oos.writeObject(rooms.keySet().toArray(new GameRoom[rooms.size()]));
	        oos.close();
	        return baos.toByteArray(); 
			
        });
	}

}
