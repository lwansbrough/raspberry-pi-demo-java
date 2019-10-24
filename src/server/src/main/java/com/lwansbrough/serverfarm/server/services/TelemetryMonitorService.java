package com.lwansbrough.serverfarm.server.services;

import com.lwansbrough.serverfarm.core.models.generated.SystemHealthTelemetryProto.SystemHealthTelemetry;
import com.lwansbrough.serverfarm.core.services.message.MessageEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
public class TelemetryMonitorService {
    private static Logger logger = LoggerFactory.getLogger(TelemetryMonitorService.class);

    private SystemHealthTelemetry latestSystemHealthTelemetry;

    public SystemHealthTelemetry getLatestSystemHealth() {
        return latestSystemHealthTelemetry;
    }

    @EventListener
    public void onTelemetryEvent(MessageEvent<SystemHealthTelemetry> event) {
        SystemHealthTelemetry telemetry = event.getData();
        latestSystemHealthTelemetry = telemetry;
        logger.info("Received telemetry: Temperature: " + telemetry.getCpuTemperature());
    }
}
