package top.ysqorz.i18n.api;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * ...
 *
 * @author yaoshiquan
 * @date 2023/8/19
 */
public class FileEventMonitor<T> implements AutoCloseable {
    private final Queue<WatchContext> watchContextQueue = new LinkedList<>(); // 由于对队列的操作加了锁，此处不需要使用并发队列
    private final WatchService watchService;
    private final ScheduledExecutorService scheduledExecutor = Executors.newScheduledThreadPool(1, // 一个扫描线程
            runnable -> {
                Thread thread = Executors.defaultThreadFactory().newThread(runnable);
                thread.setName(this.getClass().getSimpleName());
                thread.setDaemon(true); // 设置守护线程
                return thread;
            });
    private final Long watchInterval; // 不宜太短，但要比文件的缓存时长要短
    private volatile boolean watching; // 是否正在监听

    public FileEventMonitor(Long watchInterval) throws IOException {
        this.watchInterval = watchInterval;
        this.watchService = FileSystems.getDefault().newWatchService();
    }

    @Override
    public void close() throws IOException {
        watchService.close();
        scheduledExecutor.shutdownNow();
        watchContextQueue.clear();
    }

    public void startWatch(FileEventCallback<T> callback) {
        scheduledExecutor.scheduleAtFixedRate(new WatchTask(callback), 0, watchInterval, TimeUnit.MILLISECONDS);
        this.watching = true;
    }

    public void stopWatch() {
        this.watching = false; // 定时线程不会停止，只是每次轮询时不扫描队列检查事件
    }

    public void watch(File file, T extra, WatchEvent.Kind<?>... eventKinds) throws IOException {
        synchronized (watchContextQueue) {
            WatchContext watchContext = getWatchContext(file);
            if (Objects.isNull(watchContext)) {
                watchContextQueue.add(new WatchContext(file, eventKinds, extra));
            } else {
                watchContext.addWatchedFile(file, eventKinds, extra);
            }
        }
    }

    public void unWatch(File file) throws IOException {
        synchronized (watchContextQueue) {
            WatchContext watchContext = getWatchContext(file);
            if (Objects.isNull(watchContext)) {
                return;
            }
            watchContext.removeWatchedFile(file);
            if (watchContext.watchedCount() == 0) {
                watchContextQueue.remove(watchContext);
            }
        }
    }

    private WatchContext getWatchContext(File file) {
        for (WatchContext watchContext : watchContextQueue) {
            if (watchContext.isSameWatch(file)) {
                return watchContext;
            }
        }
        return null;
    }

    public class WatchTask implements Runnable {
        private final FileEventCallback<T> callback;

        public WatchTask(FileEventCallback<T> callback) {
            this.callback = callback;
        }

        @Override
        public void run() {
            try {
                if (!watching || watchContextQueue.isEmpty()) {
                    return;
                }
                for (WatchContext watchContext : watchContextQueue) {
                    handleWatchEvent(watchContext);
                }
            } catch (Exception ex) {
                // 防止抛出异常导致线程挂掉
                ex.printStackTrace();
            }
        }

        private void handleWatchEvent(WatchContext watchContext) {
            List<WatchEvent<?>> watchEvents = watchContext.watchKey.pollEvents(); // 目录发生了如下事件
            for (WatchEvent<?> watchEvent : watchEvents) {
                Path relativePath = (Path) watchEvent.context(); // 实际发生事件的文件
                WatchEvent.Kind<?> eventKind = watchEvent.kind();
                WatchedFileHolder fileHolder = watchContext.getWatchedFileHolder(relativePath, eventKind);
                if (Objects.nonNull(fileHolder)) {
                    callback.onEventOccurred(fileHolder.file, fileHolder.extra, watchEvent.kind());
                }
            }
        }
    }

    private class WatchedFileHolder {
        private final File file; // 关心的文件
        private final Set<WatchEvent.Kind<?>> eventKinds; // 关心的事件
        private final T extra; // 额外参数

        public WatchedFileHolder(File file, Set<WatchEvent.Kind<?>> eventKinds, T extra) {
            this.file = file;
            this.eventKinds = eventKinds;
            this.extra = extra;
        }
    }

    private class WatchContext {
        private WatchKey watchKey; // 目录的监听句柄
        private final Set<WatchEvent.Kind<?>> eventKinds = new HashSet<>(); // 注册的目录事件
        private final Map<File, WatchedFileHolder> watchedFiles = new HashMap<>(); // 真正想监听的文件

        public WatchContext(File file, WatchEvent.Kind<?>[] eventKinds, T extra) throws IOException {
            addWatchedFile(file, eventKinds, extra);
        }

        public void addWatchedFile(File file, WatchEvent.Kind<?>[] eventKinds, T extra) throws IOException {
            watchedFiles.put(file, new WatchedFileHolder(file, Arrays.stream(eventKinds).collect(Collectors.toSet()), extra));
            Collections.addAll(this.eventKinds, eventKinds); // 合并关注的事件
            if (Objects.nonNull(watchKey)) {
                //Path watchable = (Path) watchKey.watchable(); // file一定是watchable的下级条目
                watchKey.cancel(); // 由于关注的事件变了，所以重新注册获取新的WatchKey

            }
            watchKey = file.getParentFile().toPath().register(watchService, this.eventKinds.toArray(new WatchEvent.Kind[0]));
        }

        public void removeWatchedFile(File file) {
            watchedFiles.remove(file);
            if (watchedCount() == 0) {
                watchKey.cancel();
            }
        }

        public int watchedCount() {
            return watchedFiles.size();
        }

        public WatchedFileHolder getWatchedFileHolder(Path relativePath, WatchEvent.Kind<?> eventKind) { // 触发事件的文件
            Path watchedDir = (Path) watchKey.watchable();
            Path absolutePath = watchedDir.resolve(relativePath);
            WatchedFileHolder fileHolder = watchedFiles.get(absolutePath.toFile());
            if (Objects.isNull(fileHolder) || !fileHolder.eventKinds.contains(eventKind)) {
                return null;
            }
            return fileHolder;
        }

        public boolean isSameWatch(File file) {
            Path watchedDir = (Path) watchKey.watchable(); // 实际注册监听的目录
            return file.getParentFile().equals(watchedDir.toFile());
        }
    }

    public interface FileEventCallback<T> {
        /**
         * 注意不要在onEventOccurred执行耗时操作，否则影响FileEventMonitor的监听线程
         */
        void onEventOccurred(File file, T extra, WatchEvent.Kind<?> eventKind);
    }
}
