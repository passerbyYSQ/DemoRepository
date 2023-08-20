package top.ysqorz.i18n.api;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * ...
 *
 * @author yaoshiquan
 * @date 2023/8/19
 */
public class FileEventMonitor<T> implements AutoCloseable {
    private final Queue<WatchContext> watchKeyQueue = new ConcurrentLinkedQueue<>(); // 并发非阻塞队列
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

    public void startWatch(FileEventCallback<T> callback) {
        scheduledExecutor.scheduleAtFixedRate(new WatchTask(callback), 0, watchInterval, TimeUnit.MILLISECONDS);
    }

    public void stopWatch() {
        // TODO
    }

    public void watch(File file, T extra, WatchEvent.Kind<?>... eventKinds) throws IOException {
        if (Objects.isNull(eventKinds) || eventKinds.length == 0) {
            throw new IOException("event kinds must not empty");
        }
        WatchKey watchKey = file.toPath().register(watchService, eventKinds);
        watchKeyQueue.add(new WatchContext(watchKey, extra));
    }

    public void unWatch(File file) throws IOException {
        Iterator<WatchContext> iterator = watchKeyQueue.iterator();
        Path path = file.toPath();
        while (iterator.hasNext()) {
            WatchContext watchContext = iterator.next();
            if (Files.isSameFile(watchContext.path, path)) {
                watchContext.watchKey.cancel(); // 取消监听
                iterator.remove(); // 从队列中移除掉
                //break; // 可能对一个目录注册了多个监听，一并移除掉
            }
        }
    }

    public class WatchTask implements Runnable {
        private final FileEventCallback<T> callback;

        public WatchTask(FileEventCallback<T> callback) {
            this.callback = callback;
        }

        @Override
        public void run() {
            try {
                if (watchKeyQueue.isEmpty()) {
                    return;
                }
                for (WatchContext watchContext : watchKeyQueue) {
                    List<WatchEvent<?>> watchEvents = watchContext.watchKey.pollEvents();
                    List<WatchEvent.Kind<?>> eventKinds = watchEvents.stream()
                            .map((Function<WatchEvent<?>, WatchEvent.Kind<?>>) WatchEvent::kind)
                            .collect(Collectors.toList());
                    callback.onEventOccurred(watchContext.path.toFile(), watchContext.extra, eventKinds);
                }
            } catch (Exception ex) {
                // 防止抛出异常导致线程挂掉
                ex.printStackTrace();
            }
        }
    }

    private class WatchContext {
        private final WatchKey watchKey;
        private final Path path;
        private final T extra;

        public WatchContext(WatchKey watchKey, T extra) {
            this.watchKey = watchKey;
            this.path = (Path) watchKey.watchable();
            this.extra = extra;
        }
    }

    public interface FileEventCallback<T> {
        /**
         * 注意不要在onEventOccurred执行耗时操作，否则影响FileEventMonitor的监听线程
         */
        void onEventOccurred(File file, T extra, List<WatchEvent.Kind<?>> eventKinds);
    }
}
