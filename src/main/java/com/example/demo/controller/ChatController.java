/**
 * The ChatController class handles HTTP requests related to chat functionality.
 * It interacts with the AccountService and ChatService to perform chat-related operations.
 */
package com.example.demo.controller;

import com.example.demo.model.Chatroom;
import com.example.demo.model.Message;
import com.example.demo.model.User;
import com.example.demo.model.UserDTO;
import com.example.demo.response.ErrorResponse;
import com.example.demo.response.util;
import com.example.demo.service.AccountService;
import com.example.demo.service.ChatService;
import com.example.demo.websocket.WebsocketSessionManager;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.aspectj.weaver.ast.Not;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/room")
public class ChatController {
    @Autowired
    private AccountService accountService;
    @Autowired
    private ChatService chatService;

    private WebsocketSessionManager websocketSessionManager;

    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    public ChatController(WebsocketSessionManager sessionManager, SimpMessagingTemplate messagingTemplate) {
        this.websocketSessionManager = sessionManager;
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Sends a message to a chat room.
     *
     * @param id          The user ID.
     * @param token       The authorization token.
     * @param messageForm The message form containing the room ID and message content.
     * @return The ResponseEntity indicating the success or failure of sending the message.
     */
    @PostMapping("/message/{id}")
    public ResponseEntity<?> sendMessageToRoom(@PathVariable String id, @RequestHeader("Authorization") String token, @RequestBody MessageRoomForm messageForm) {
        if (!accountService.isAuthenticated(id, token)) {
            return ResponseEntity.status(401).body(new ErrorResponse("Authentication failed"));
        }
        Chatroom room = chatService.findChatroom(messageForm.getRoomId());
        if (room == null) {
            return ResponseEntity.status(404).body(new ErrorResponse("Chatroom not found"));
        }
        User user = accountService.findUser(id);
        chatService.sendMessage(user, room, messageForm.getContent());
        // Push notification
        List<UserDTO> onlineUsers = websocketSessionManager.filterOnlineUser(chatService.getAllParticipantExceptSender(room, user));
        for (UserDTO onlineUser : onlineUsers) {
            String destination = accountService.getNotificationTopic(onlineUser.getId());
            System.out.println(destination);
            messagingTemplate.convertAndSend(destination, new Notification(room.getId(), user.getName(), messageForm.getContent())); // Send the notification
        }
        return ResponseEntity.status(200).body(util.EMPTY_RESPONSE);
    }

    /**
     * Retrieves the chat history of a chat room.
     *
     * @param id      The user ID.
     * @param token   The authorization token.
     * @param roomId  The ID of the chat room.
     * @return The ResponseEntity containing the chat history or an error response.
     */
    @GetMapping("/messages/{id}")
    public ResponseEntity<?> viewChatHistory(@PathVariable String id, @RequestHeader("Authorization") String token, @RequestParam Long roomId) {
        if (!accountService.isAuthenticated(id, token)) {
            return ResponseEntity.status(401).body(new ErrorResponse("Authentication failed"));
        }
        Chatroom room = chatService.findChatroom(roomId);
        User user = accountService.findUser(id);
        if (room == null) {
            return ResponseEntity.status(404).body(new ErrorResponse("Chatroom not found"));
        }
        if (!chatService.inChatroom(room, user)) {
            return ResponseEntity.status(403).body(new ErrorResponse("Not your chatroom"));
        }
        List<Message> messages = chatService.getAllMessage(room);

        return ResponseEntity.status(200).body(messages);
    }

    /**
     * Retrieves all chat rooms associated with a user.
     *
     * @param id    The user ID.
     * @param token The authorization token.
     * @return The ResponseEntity containing the list of chat rooms or an error response.
     */
    @GetMapping("/all/{id}")
    public ResponseEntity<?> viewChatRoom(@PathVariable String id, @RequestHeader("Authorization") String token) {
        if (!accountService.isAuthenticated(id, token)) {
            return ResponseEntity.status(401).body(new ErrorResponse("Authentication failed"));
        }
        User user = accountService.findUser(id);
        List<Chatroom> chatrooms = chatService.viewAllChatroom(user);
        return ResponseEntity.status(200).body(chatrooms);
    }

    /**
     * Retrieves the participants of a chat room.
     *
     * @param id      The user ID.
     * @param token   The authorization token.
     * @param roomId  The ID of the chat room.
     * @return The ResponseEntity containing the list of participants or an error response.
     */
    @GetMapping("/participants/{id}")
    public ResponseEntity<?> getParticipants(@PathVariable String id, @RequestHeader("Authorization") String token, @RequestParam Long roomId) {
        if (!accountService.isAuthenticated(id, token)) {
            return ResponseEntity.status(401).body(new ErrorResponse("Authentication failed"));
        }
        Chatroom room = chatService.findChatroom(roomId);
        User user = accountService.findUser(id);
        if (room == null) {
            return ResponseEntity.status(404).body(new ErrorResponse("Chatroom not found"));
        }
        if (!chatService.inChatroom(room, user)) {
            return ResponseEntity.status(403).body(new ErrorResponse("Not your chatroom"));
        }
        List<UserDTO> participants = chatService.getAllParticipant(room);
        return ResponseEntity.status(200).body(participants);
    }

    /**
     * The FriendForm class represents the form data for adding a friend.
     * It contains the friend's user ID.
     */
    @Data
    @NoArgsConstructor
    public static class FriendForm {
        private String id;
    }

    /**
     * The KeyForm class represents the form data for setting an API key.
     * It contains the API key.
     */
    @Data
    @NoArgsConstructor
    public static class KeyForm {
        private String key;
    }

    /**
     * The MessageRoomForm class represents the form data for sending a message to a chat room.
     * It contains the room ID and message content.
     */
    @Data
    @NoArgsConstructor
    public static class MessageRoomForm {
        private Long roomId;
        private String content;
    }

    /**
     * The Notification class represents a chat notification.
     * It contains the room ID, sender's name, and message content.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Notification {
        private Long roomId;
        private String senderName;
        private String content;
    }
}
