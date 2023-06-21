package com.example.demo.websocket;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

/**
 * Listener class for WebSocket events.
 */
@Component
public class WebsocketEventListener implements ApplicationListener<SessionConnectEvent> {

    @Autowired
    WebsocketSessionManager websocketSessionManager;

    /**
     * Handles the session connect event.
     *
     * @param event The SessionConnectEvent object representing the session connect event.
     */
    @Override
    public void onApplicationEvent(SessionConnectEvent event) {
        // Do nothing
    }

    /**
     * Handles the session disconnect event.
     *
     * @param event The SessionDisconnectEvent object representing the session disconnect event.
     */
    @EventListener
    public void handleSessionDisconnect(SessionDisconnectEvent event) {
        websocketSessionManager.deleteSession(event.getSessionId());
        System.out.println("close session: " + event.getSessionId());
    }
}
