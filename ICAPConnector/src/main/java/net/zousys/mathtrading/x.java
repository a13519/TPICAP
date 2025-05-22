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
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
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
        MockitoAnnotations.openMocks(this);
        // Set configuration properties using ReflectionTestUtils
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
        verify(monitorService).submit(runnableCaptor.capture());
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
        WatchEvent<?> watchEvent = mock(WatchEvent.class);
        Path filePath = tempDir.resolve("testfile.txt");

        // Mock WatchService behavior
        when(watchService.take()).thenReturn(watchKey);
        when(watchKey.pollEvents()).thenReturn(List.of(watchEvent));
        when(watchEvent.kind()).thenReturn(StandardWatchEventKinds.ENTRY_CREATE);
        when(watchEvent.context()).thenReturn(filePath.getFileName());
        when(watchKey.watchable()).thenReturn(tempDir);
        when(watchKey.reset()).thenReturn(true);

        // Create a real file
        Files.writeString(filePath, "test content");
        byte[] fileBytes = Files.readAllBytes(filePath);
        ICAPMessage icapMessage = mock(ICAPMessage.class);
        when(ICAPMessage.form(fileBytes)).thenReturn(icapMessage);

        // Use reflection to invoke private monitorFolder method
        try {
            // Start monitoring in a controlled way
            CompletableFuture.runAsync(() -> {
                try {
                    ReflectionTestUtils.invokeMethod(poolingMonitor, "monitorFolder");
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }, monitorService);

            // Simulate one iteration
            Thread.sleep(100); // Allow some time for execution

            // Assert
            verify(icapMessageRepo).push(icapMessage);
            verify(serverStatus).getPoolingFiles();
            assertFalse(Files.exists(filePath), "File should be deleted after processing");
        } finally {
            // Stop the loop by interrupting
            when(watchKey.reset()).thenReturn(false);
        }
    }

    @Test
    void testMonitorFolderParsingException() throws IOException, InterruptedException, ParsingException {
        // Arrange
        WatchService watchService = mock(WatchService.class);
        WatchKey watchKey = mock(WatchKey.class);
        WatchEvent<?> watchEvent = mock(WatchEvent.class);
        Path filePath = tempDir.resolve("invalidfile.txt");

        // Mock WatchService behavior
        when(watchService.take()).thenReturn(watchKey);
        when(watchKey.pollEvents()).thenReturn(List.of(watchEvent));
        when(watchEvent.kind()).thenReturn(StandardWatchEventKinds.ENTRY_CREATE);
        when(watchEvent.context()).thenReturn(filePath.getFileName());
        when(watchKey.watchable()).thenReturn(tempDir);
        when(watchKey.reset()).thenReturn(true);

        // Create a real file
        Files.writeString(filePath, "invalid content");
        byte[] fileBytes = Files.readAllBytes(filePath);
        when(ICAPMessage.form(fileBytes)).thenThrow(new ParsingException("Invalid format"));

        // Use reflection to invoke private monitorFolder method
        try {
            CompletableFuture.runAsync(() -> {
                try {
                    ReflectionTestUtils.invokeMethod(poolingMonitor, "monitorFolder");
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }, monitorService);

            // Simulate one iteration
            Thread.sleep(100);

            // Assert
            verify(icapMessageRepo, never()).push(any());
            verify(serverStatus).getPoolingFiles();
            assertFalse(Files.exists(filePath), "File should be deleted even on parsing error");
        } finally {
            when(watchKey.reset()).thenReturn(false);
        }
    }

    @Test
    void testMonitorFolderNewDirectory() throws IOException, InterruptedException {
        // Arrange
        WatchService watchService = mock(WatchService.class);
        WatchKey watchKey = mock(WatchKey.class);
        WatchEvent<?> watchEvent = mock(WatchEvent.class);
        Path subDir = tempDir.resolve("subdir");

        // Mock WatchService behavior
        when(watchService.take()).thenReturn(watchKey);
        when(watchKey.pollEvents()).thenReturn(List.of(watchEvent));
        when(watchEvent.kind()).thenReturn(StandardWatchEventKinds.ENTRY_CREATE);
        when(watchEvent.context()).thenReturn(subDir.getFileName());
        when(watchKey.watchable()).thenReturn(tempDir);
        when(watchKey.reset()).thenReturn(true);

        // Create a subdirectory
        Files.createDirectory(subDir);

        // Use reflection to invoke private monitorFolder method
        try {
            CompletableFuture.runAsync(() -> {
                try {
                    ReflectionTestUtils.invokeMethod(poolingMonitor, "monitorFolder");
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }, monitorService);

            // Simulate one iteration
            Thread.sleep(100);

            // Assert
            verify(watchService).register(eq(subDir), any());
        } finally {
            when(watchKey.reset()).thenReturn(false);
        }
    }

    @Test
    void testRegisterDirectory() throws IOException {
        // Arrange
        WatchService watchService = mock(WatchService.class);
        Set<Path> registeredPaths = new HashSet<>();
        Path subDir = tempDir.resolve("subdir");
        Files.createDirectory(subDir);

        // Act
        ReflectionTestUtils.invokeMethod(poolingMonitor, "registerDirectory", tempDir, watchService, registeredPaths);

        // Assert
        verify(watchService).register(eq(tempDir), any());
        verify(watchService).register(eq(subDir), any());
        assertTrue(registeredPaths.contains(tempDir));
        assertTrue(registeredPaths.contains(subDir));
    }

    @Test
    void testMonitorFolderOverflow() throws IOException, InterruptedException {
        // Arrange
        WatchService watchService = mock(WatchService.class);
        WatchKey watchKey = mock(WatchKey.class);
        WatchEvent<?> watchEvent = mock(WatchEvent.class);

        // Mock overflow event
        when(watchService.take()).thenReturn(watchKey);
        when(watchKey.pollEvents()).thenReturn(List.of(watchEvent));
        when(watchEvent.kind()).thenReturn(StandardWatchEventKinds.OVERFLOW);
        when(watchKey.reset()).thenReturn(true);

        // Act
        try {
            CompletableFuture.runAsync(() -> {
                try {
                    ReflectionTestUtils.invokeMethod(poolingMonitor, "monitorFolder");
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }, monitorService);

            // Simulate one iteration
            Thread.sleep(100);
        } finally {
            when(watchKey.reset()).thenReturn(false);
        }

        // Assert
        verifyNoInteractions(icapMessageRepo);
        verifyNoInteractions(serverStatus);
    }
}
