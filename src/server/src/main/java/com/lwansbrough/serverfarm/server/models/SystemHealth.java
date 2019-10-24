package com.lwansbrough.serverfarm.server.models;

import com.lwansbrough.serverfarm.core.models.generated.SystemHealthTelemetryProto.SystemHealthTelemetry;

public class SystemHealth {

    private float cpuTemperature;

    public SystemHealth(SystemHealthTelemetry telemetry) {
        this.cpuTemperature = telemetry.getCpuTemperature();
    }

    public float getCpuTemperature() {
        return cpuTemperature;
    }

}
