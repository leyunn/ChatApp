package com.example.demo.websocket;
import com.example.demo.model.UserDTO;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Component
public class WebsocketSessionManager {
    private Map<String, String> sessionMap; // Map to session ID, uid

    public WebsocketSessionManager() {
        sessionMap = new HashMap<>();
    }

    public void addSession(String sessionId, String userId) {
        sessionMap.put(sessionId, userId);
    }


    public void deleteSession(String sessionId) {
        if(sessionExistsById(sessionId)){
            sessionMap.remove(sessionId);
        }
    }

    public List<UserDTO> filterOnlineUser(List<UserDTO> users){
        List<UserDTO> onlineUsers = new ArrayList<>();
        for(UserDTO user: users){
            if(sessionExistsByUserId(user.getId())){
                onlineUsers.add(user);
            }

        }
        return onlineUsers;
    }

    public boolean sessionExistsByUserId(String userId) {
        return sessionMap.containsValue(userId);
    }

    public boolean sessionExistsById(String sessionId) {
        return sessionMap.containsKey(sessionId);
    }
}
