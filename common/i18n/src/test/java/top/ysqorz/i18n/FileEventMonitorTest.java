package top.ysqorz.i18n;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import top.ysqorz.i18n.common.FileEventMonitor;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.StandardWatchEventKinds;
import java.util.Date;
import java.util.Queue;
import java.util.concurrent.CountDownLatch;

/**
 * ...
 *
 * @author yaoshiquan
 * @date 2023/8/21
 */
public class FileEventMonitorTest {
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
     * 测试监听多个文件，每个文件关心的事件不一样
     */
    @Test
    public void testWatchMore() throws Exception {
        File file = new File("E:\\Project\\IdeaProjects\\DemoRepository\\common\\i18n\\target\\test-classes\\test-watch.txt");
        Object extra = new Object();
        fileEventMonitor.watch(file, extra, StandardWatchEventKinds.ENTRY_MODIFY);
        fileEventMonitor.watch(new File("E:\\Project\\IdeaProjects\\DemoRepository\\common\\i18n\\target\\test-classes\\demo.txt"),
                null, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);
        fileEventMonitor.startWatch((file1, extra1, eventKind) ->
                System.out.printf("Detected file event: %s, event: %s, time: %s%n", file1.getAbsolutePath(), eventKind, new Date())
        );
        System.out.println("Main thread start sleeping");
        Thread.sleep(1000 * 60 * 5);
        System.out.println("Main thread finished sleeping");
    }

    /**
     * 测试停止监听后重新启动监听
     */
    @Test
    public void testRestartWatch() throws Exception {
        File file = new File("E:\\Project\\IdeaProjects\\DemoRepository\\common\\i18n\\target\\test-classes\\test-watch.txt");
        fileEventMonitor.watch(file, null, StandardWatchEventKinds.ENTRY_MODIFY);
        fileEventMonitor.startWatch((file1, extra1, eventKind) ->
                System.out.printf("Detected file event: %s, event: %s, time: %s%n", file1.getAbsolutePath(), eventKind, new Date())
        );
        Thread.sleep(1000);
        System.out.println("modify file: " + file.getAbsolutePath());
        appendStr2File(file, System.currentTimeMillis() + ""); // 文件发生更改，会走到回调

        Thread.sleep(1000);
        System.out.println("unWatch file: " + file.getAbsolutePath());
        fileEventMonitor.unWatch(file);
        appendStr2File(file, System.currentTimeMillis() + ""); // 文件发生更改，但由于取消了监听，不会走到回调

        Thread.sleep(1000);
        System.out.println("reWatch file: " + file.getAbsolutePath());
        fileEventMonitor.watch(file, null, StandardWatchEventKinds.ENTRY_MODIFY);
        appendStr2File(file, System.currentTimeMillis() + ""); // 文件发生更改，会走到回调

        Thread.sleep(1000);
    }

    @Test
    public void testConcurrentWatch() throws Exception {
        int total = 100;
        CountDownLatch countDownLatch = new CountDownLatch(total);
        Thread[] threads = new Thread[total];
        for (int i = 0; i < total; i++) {
            int finalI = i;
            threads[i] = new Thread(() -> {
                try {
                    File file = new File("E:\\Project\\IdeaProjects\\DemoRepository\\common\\i18n\\target\\test-classes", "concurrent-watch-" + finalI);
                    fileEventMonitor.watch(file, null, StandardWatchEventKinds.ENTRY_CREATE);
                    countDownLatch.countDown();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
        for (Thread thread : threads) {
            thread.start();
        }
        countDownLatch.await();
        // watchContext应该只有一个，且里面注册的文件一定是total个
        Field field = FileEventMonitor.class.getDeclaredField("watchContextQueue");
        field.setAccessible(true);
        Queue<?> watchContextQueue = (Queue<?>) field.get(fileEventMonitor);
        Assert.assertEquals(1, watchContextQueue.size());

        Object watchContext = watchContextQueue.poll();
        Assert.assertNotNull(watchContext);
        Method watchedCount = watchContext.getClass().getDeclaredMethod("watchedCount");
        watchedCount.setAccessible(true);
        int watchedFileCount = (int) watchedCount.invoke(watchContext);
        Assert.assertEquals(total, watchedFileCount);
    }

    public void appendStr2File(File file, String str) throws IOException {
        try (PrintWriter printWriter = new PrintWriter(new FileWriter(file, true))) {
            printWriter.println(str);
        }
    }
}
