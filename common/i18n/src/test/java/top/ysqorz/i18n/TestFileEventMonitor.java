package top.ysqorz.i18n;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import top.ysqorz.i18n.api.FileEventMonitor;

import java.io.File;
import java.io.IOException;
import java.nio.file.StandardWatchEventKinds;
import java.util.Date;
import java.util.logging.Logger;

/**
 * ...
 *
 * @author yaoshiquan
 * @date 2023/8/21
 */
public class TestFileEventMonitor {
    private static final Logger log = Logger.getLogger(TestFileEventMonitor.class.getSimpleName());
    private FileEventMonitor<Object> fileEventMonitor;

    @Before
    public void createMonitor() throws IOException {
        fileEventMonitor = new FileEventMonitor<>(500L);
    }

    @After
    public void closeMonitor() throws IOException {
        fileEventMonitor.close();
    }

    @Test
    public void testWatch() throws Exception {
        File file = new File("E:\\Project\\IdeaProjects\\DemoRepository\\common\\i18n\\target\\test-classes\\test-watch.txt");
        Object extra = new Object();
        fileEventMonitor.watch(file, extra, StandardWatchEventKinds.ENTRY_MODIFY);
        fileEventMonitor.watch(new File("E:\\Project\\IdeaProjects\\DemoRepository\\common\\i18n\\target\\test-classes\\demo.txt"),
                null, StandardWatchEventKinds.ENTRY_CREATE);
        fileEventMonitor.startWatch((file1, extra1, eventKind) -> {
            log.info(String.format("Detected file event: %s, event: %s, time: %s", file1.getAbsolutePath(), eventKind, new Date()));
        });
        log.info("Main thread start sleeping");
        Thread.sleep(1000 * 60 * 5);
        log.info("Main thread finished sleeping");
    }
}
