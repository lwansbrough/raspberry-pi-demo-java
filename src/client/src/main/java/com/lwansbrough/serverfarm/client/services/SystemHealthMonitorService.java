package com.lwansbrough.serverfarm.client.services;

import com.lwansbrough.serverfarm.client.helpers.CpuTemperatureHelper;
import com.lwansbrough.serverfarm.core.models.generated.SystemHealthTelemetryProto.SystemHealthTelemetry;
import com.lwansbrough.serverfarm.core.services.TelemetryService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class SystemHealthMonitorService {
    private static final Logger logger = LoggerFactory.getLogger(SystemHealthMonitorService.class);

    @Autowired
    private TelemetryService telemetryService;
    private boolean running;

    @Scheduled(fixedDelay = Long.MAX_VALUE)
    private void run() {
        logger.info("System health monitor is starting.");
        running = true;

        while (running) {
            logger.info("Publishing system health");

            SystemHealthTelemetry telemetry = SystemHealthTelemetry.newBuilder()
                .setCpuTemperature(CpuTemperatureHelper.measure())
                .build();

            telemetryService.send(telemetry);

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        // logger.info("System health monitor is stopping.");
    }
}
