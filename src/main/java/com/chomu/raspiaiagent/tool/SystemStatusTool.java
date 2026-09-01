package com.chomu.raspiaiagent.tool;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.OperatingSystemMXBean;

@Component
public class SystemStatusTool {

    @Tool(description = "현재 서버(라즈베리파이)의 JVM 메모리 사용량, 힙 상태, CPU 부하 등 시스템 상태를 조회한다. " +
            "사용자가 서버 상태, 메모리, 성능, 리소스에 대해 질문할 때 사용한다.")
    public SystemStatusResult getSystemStatus() {
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage heap = memoryBean.getHeapMemoryUsage();
        MemoryUsage nonHeap = memoryBean.getNonHeapMemoryUsage();

        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        Runtime runtime = Runtime.getRuntime();

        return new SystemStatusResult(
                heap.getUsed() / (1024 * 1024),
                heap.getMax() / (1024 * 1024),
                nonHeap.getUsed() / (1024 * 1024),
                runtime.freeMemory() / (1024 * 1024),
                runtime.totalMemory() / (1024 * 1024),
                osBean.getAvailableProcessors(),
                osBean.getSystemLoadAverage()
        );
    }

    public record SystemStatusResult(
            long usedHeapMb,
            long maxHeapMb,
            long usedNonHeapMb,
            long freeMemoryMb,
            long totalMemoryMb,
            int availableProcessors,
            double systemLoadAverage
    ) {}
}