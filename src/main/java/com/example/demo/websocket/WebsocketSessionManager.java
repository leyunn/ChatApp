package com.example.demo.websocket;

import com.example.demo.model.UserDTO;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages WebSocket sessions and their corresponding user IDs.
 */
@Component
public class WebsocketSessionManager {
    private Map<String, String> sessionMap; // Map of session ID to user ID

    /**
     * Constructs a new WebsocketSessionManager and initializes the sessionMap.
     */
    public WebsocketSessionManager() {
        sessionMap = new HashMap<>();
    }

    /**
     * Adds a new WebSocket session to the sessionMap.
     *
     * @param sessionId The ID of the WebSocket session.
     * @param userId    The ID of the user associated with the session.
     */
    public void addSession(String sessionId, String userId) {
        sessionMap.put(sessionId, userId);
    }

    /**
     * Deletes a WebSocket session from the sessionMap.
     *
     * @param sessionId The ID of the WebSocket session to be deleted.
     */
    public void deleteSession(String sessionId) {
        if (sessionExistsById(sessionId)) {
            sessionMap.remove(sessionId);
        }
    }

    /**
     * Filters a list of user DTOs to include only online users.
     *
     * @param users The list of user DTOs to filter.
     * @return The list of online user DTOs.
     */
    public List<UserDTO> filterOnlineUser(List<UserDTO> users) {
        List<UserDTO> onlineUsers = new ArrayList<>();
        for (UserDTO user : users) {
            if (sessionExistsByUserId(user.getId())) {
                onlineUsers.add(user);
            }
        }
        return onlineUsers;
    }

    /**
     * Checks if a session exists for the given user ID.
     *
     * @param userId The ID of the user.
     * @return true if a session exists for the user ID, false otherwise.
     */
    public boolean sessionExistsByUserId(String userId) {
        return sessionMap.containsValue(userId);
    }

    /**
     * Checks if a session exists for the given session ID.
     *
     * @param sessionId The ID of the session.
     * @return true if a session exists for the session ID, false otherwise.
     */
    public boolean sessionExistsById(String sessionId) {
        return sessionMap.containsKey(sessionId);
    }
}
