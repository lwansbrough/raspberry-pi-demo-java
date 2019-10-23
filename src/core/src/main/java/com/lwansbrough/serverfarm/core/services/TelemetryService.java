package com.lwansbrough.serverfarm.core.services;

import java.net.InetAddress;
import java.net.UnknownHostException;

import com.lwansbrough.serverfarm.core.models.generated.SystemHealthTelemetryProto.SystemHealthTelemetry;

import org.springframework.stereotype.Service;

@Service
public class TelemetryService extends MessageService<SystemHealthTelemetry> {
    public TelemetryService() throws Exception {
        super(SystemHealthTelemetry.class, 11000);
    }

    public void send(SystemHealthTelemetry telemetry) {
        try {
            sendMessage(InetAddress.getByName("192.168.0.12"), telemetry);
        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
