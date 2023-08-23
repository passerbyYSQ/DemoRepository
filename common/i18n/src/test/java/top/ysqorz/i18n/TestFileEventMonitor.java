package top.ysqorz.i18n;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import top.ysqorz.i18n.api.FileEventMonitor;

import java.io.File;
import java.io.IOException;
import java.nio.file.StandardWatchEventKinds;
import java.util.Date;

/**
 * ...
 *
 * @author yaoshiquan
 * @date 2023/8/21
 */
public class TestFileEventMonitor {
    private FileEventMonitor<Object> fileEventMonitor;

    @Before
    public void createMonitor() throws IOException {
        fileEventMonitor = new FileEventMonitor<>(200L);
    }

    @After
    public void closeMonitor() throws IOException {
        fileEventMonitor.close();
    }

    /**
     * 测试监听：监听多个文件，每个文件关心的事件不一样
     */
    @Test
    public void testWatch() throws Exception {
        File file = new File("E:\\Project\\IdeaProjects\\DemoRepository\\common\\i18n\\target\\test-classes\\test-watch.txt");
        Object extra = new Object();
        fileEventMonitor.watch(file, extra, StandardWatchEventKinds.ENTRY_MODIFY);
        fileEventMonitor.watch(new File("E:\\Project\\IdeaProjects\\DemoRepository\\common\\i18n\\target\\test-classes\\demo.txt"),
                null, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE);
        fileEventMonitor.startWatch((file1, extra1, eventKind) -> {
            System.out.printf("Detected file event: %s, event: %s, time: %s%n", file1.getAbsolutePath(), eventKind, new Date());
        });
        System.out.println("Main thread start sleeping");
        Thread.sleep(1000 * 60 * 5);
        System.out.println("Main thread finished sleeping");
    }
}
