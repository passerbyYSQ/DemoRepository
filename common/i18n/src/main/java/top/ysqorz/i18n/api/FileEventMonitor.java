package top.ysqorz.i18n.api;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * ...
 *
 * @author yaoshiquan
 * @date 2023/8/19
 */
public class FileEventMonitor implements AutoCloseable {
    private final Queue<WatchKey> watchKeyQueue = new ConcurrentLinkedQueue<>(); // 并发非阻塞队列
    private final WatchService watchService;
    private final ScheduledExecutorService scheduledExecutor = Executors.newScheduledThreadPool(1, // 一个扫描线程
            runnable -> {
                Thread thread = Executors.defaultThreadFactory().newThread(runnable);
                thread.setDaemon(true); // 设置守护线程
                return thread;
            });
    private final Long watchInterval; // 不宜太短，但要比文件的缓存时长要短

    public FileEventMonitor(Long watchInterval) throws IOException {
        this.watchInterval = watchInterval;
        this.watchService = FileSystems.getDefault().newWatchService(); // 可关闭资源，注意关闭！！！！
    }

    @Override
    public void close() throws IOException {
        watchService.close();
        scheduledExecutor.shutdownNow();
        watchKeyQueue.clear();
    }

    public void startWatch(FileEventCallback callback) {
        scheduledExecutor.scheduleAtFixedRate(new WatchFileModifiedTask(callback), 0, watchInterval, TimeUnit.MILLISECONDS);
    }

    // TODO stopWatch

    public void watch(File file, WatchEvent.Kind<?>... eventKinds) throws IOException {
        WatchKey watchKey = file.toPath().register(watchService, eventKinds);
        watchKeyQueue.add(watchKey);
    }

    public void unWatch(File file) throws IOException {
        Iterator<WatchKey> iterator = watchKeyQueue.iterator();
        while (iterator.hasNext()) {
            WatchKey watchKey = iterator.next();
            Path path = (Path) watchKey.watchable();
            if (Files.isSameFile(path, file.toPath())) {
                watchKey.cancel(); // 取消监听
                iterator.remove(); // 从队列中移除掉
                //break; // 可能对一个目录注册了多个监听，一并移除掉
            }
        }
    }

    public class WatchFileModifiedTask implements Runnable {
        private final FileEventCallback callback;

        public WatchFileModifiedTask(FileEventCallback callback) {
            this.callback = callback;
        }

        @Override
        public void run() {
            try {
                if (watchKeyQueue.isEmpty()) {
                    return;
                }
                for (WatchKey watchKey : watchKeyQueue) {
                    Path path = (Path) watchKey.watchable();
                    List<WatchEvent<?>> watchEvents = watchKey.pollEvents();
                    callback.onEventOccurred(path.toFile(), watchEvents);
                }
            } catch (Exception ex) {
                // 防止抛出异常导致线程挂掉
            }
        }
    }
}
