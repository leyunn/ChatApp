package com.example.demo.websocket;

import com.example.demo.service.AccountService;
import com.example.demo.service.ChatService;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.socket.*;

import java.util.HashMap;
import java.util.Map;

@Controller
public class WebsocketController {
    @Autowired
    AccountService accountService;

    @Autowired
    WebsocketSessionManager websocketSessionManager;

    @MessageMapping("login")
    public void login(Auth auth, StompHeaderAccessor headerAccessor) {
        if(!accountService.isAuthenticated(auth.getId(), auth.getToken())) return;
        websocketSessionManager.addSession(headerAccessor.getSessionId(), auth.getId());
        System.out.println("new session: "+ auth.getId());
    }

    @Data
    @NoArgsConstructor
    private static class Auth{
        String id;
        String token;
    }
}

