package net.zousys.mathtrading.interfaces.tpicap.service;

import net.zousys.mathtrading.interfaces.tpicap.ICAPMessage;
import net.zousys.mathtrading.interfaces.tpicap.ICAPSource;
import net.zousys.mathtrading.interfaces.tpicap.ParsingException;
import net.zousys.mathtrading.interfaces.tpicap.model.ICAPMessageRepo;
import net.zousys.mathtrading.interfaces.tpicap.model.ServerStatus;
import net.zousys.mathtrading.interfaces.util.FileReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class PoolingMonitorTest {

    @Mock
    private ExecutorService monitorService;

    @Mock
    private ICAPSource icapSource;

    @Mock
    private ICAPMessageRepo icapMessageRepo;

    @Mock
    private ServerStatus serverStatus;

    @InjectMocks
    private PoolingMonitor poolingMonitor;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        // Initialize mocks with Mockito 5.4.0
        MockitoAnnotations.openMocks(this);
        // Set configuration properties
        ReflectionTestUtils.setField(poolingMonitor, "poolingActive", true);
        ReflectionTestUtils.setField(poolingMonitor, "poolingPath", tempDir.toString());
        // Mock ServerStatus behavior
        AtomicInteger poolingFiles = new AtomicInteger(0);
        when(serverStatus.getPoolingFiles()).thenReturn(poolingFiles);
    }

    @Test
    void testStartMonitoringWhenActive() {
        // Arrange
        ArgumentCaptor<Runnable> runnableCaptor = ArgumentCaptor.forClass(Runnable.class);

        // Act
        poolingMonitor.startMonitoring();

        // Assert
        verify(monitorService, times(1)).submit(runnableCaptor.capture());
        assertNotNull(runnableCaptor.getValue(), "Runnable task should be submitted to ExecutorService");
    }

    @Test
    void testStartMonitoringWhenInactive() {
        // Arrange
        ReflectionTestUtils.setField(poolingMonitor, "poolingActive", false);

        // Act
        poolingMonitor.startMonitoring();

        // Assert
        verifyNoInteractions(monitorService);
    }

    @Test
    void testMonitorFolderFileCreation() throws IOException, InterruptedException, ParsingException {
        // Arrange
        WatchService watchService = mock(WatchService.class);
        WatchKey watchKey = mock(WatchKey.class);
        Watch
