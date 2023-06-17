package com.example.demo.websocket;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
public class WebsocketEventListener implements ApplicationListener<SessionConnectEvent> {

    @Autowired
    WebsocketSessionManager websocketSessionManager;
    @Override
    public void onApplicationEvent(SessionConnectEvent event) {
        //Do nothing
    }

    @EventListener
    public void handleSessionDisconnect(SessionDisconnectEvent event) {
        websocketSessionManager.deleteSession(event.getSessionId());
        System.out.println("close session: "+ event.getSessionId());
    }
}
