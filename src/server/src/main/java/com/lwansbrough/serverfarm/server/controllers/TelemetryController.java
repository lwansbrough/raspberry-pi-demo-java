package com.lwansbrough.serverfarm.server.controllers;

import com.lwansbrough.serverfarm.core.models.generated.SystemHealthTelemetryProto.SystemHealthTelemetry;
import com.lwansbrough.serverfarm.core.services.message.MessageEvent;
import com.lwansbrough.serverfarm.server.models.SystemHealth;
import com.lwansbrough.serverfarm.server.services.TelemetryMonitorService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class TelemetryController {

    @Autowired
    private TelemetryMonitorService telemetryMonitorService;
    @Autowired
    private SimpMessagingTemplate messageSender;

    @MessageMapping("/telemetry/health")
    @SendTo("/telemetry/health")
    public SystemHealth health(SystemHealth telemetry) throws Exception {
        return new SystemHealth(telemetryMonitorService.getLatestSystemHealth());
    }

    @EventListener
    public void onTelemetryEvent(MessageEvent<SystemHealthTelemetry> event) throws MessagingException {
        messageSender.convertAndSend("/telemetry/health", new SystemHealth(event.getData()));
    }
}
