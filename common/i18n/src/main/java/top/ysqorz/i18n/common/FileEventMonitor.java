package top.ysqorz.i18n.common;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * ...
 *
 * @author yaoshiquan
 * @date 2023/8/19
 */
public class FileEventMonitor<T> implements AutoCloseable {
    private final Queue<WatchContext> watchContextQueue = new ConcurrentLinkedQueue<>();
    private WatchService watchService;
    private final ScheduledExecutorService scheduledExecutor = Executors.newScheduledThreadPool(1, // 一个扫描线程
            runnable -> {
                Thread thread = Executors.defaultThreadFactory().newThread(runnable);
                thread.setName(this.getClass().getSimpleName());
                thread.setDaemon(true); // 设置守护线程
                return thread;
            });
    private ScheduledFuture<?> scheduledFuture;
    private final Long watchInterval; // 不宜太短，但要比文件的缓存时长要短

    public FileEventMonitor(Long watchInterval) throws IOException {
        this.watchInterval = watchInterval;
        this.watchService = FileSystems.getDefault().newWatchService();
    }

    @Override
    public void close() throws IOException {
        if (Objects.nonNull(watchService)) {
            synchronized (watchContextQueue) {
                if (Objects.nonNull(watchService)) {
                    watchService.close();
                    watchService = null; // help gc
                    scheduledExecutor.shutdownNow();
                    watchContextQueue.clear();
                }
            }
        }
    }

    public void startWatch(FileEventCallback<T> callback) throws IOException {
        if (Objects.isNull(watchService)) {
            synchronized (watchContextQueue) { // 确保原子操作
                if (Objects.isNull(watchService)) { // stopWatch之后startWatch，此时watchService为null
                    watchService = FileSystems.getDefault().newWatchService();
                    for (WatchContext watchContext : watchContextQueue) {
                        watchContext.register();  // 重新恢复之前注册的文件的监听，stopWatch之后注册的监听也在其中
                    }
                }
                if (Objects.isNull(scheduledFuture)) {
                    scheduledFuture = scheduledExecutor.scheduleAtFixedRate(new WatchTask(callback), 0, watchInterval, TimeUnit.MILLISECONDS);
                }
            }
        }
    }

    public void stopWatch() throws IOException {
        if (Objects.nonNull(watchService)) {
            synchronized (watchContextQueue) {
                if (Objects.nonNull(watchService)) {
                    scheduledFuture.cancel(false); // false表示如果扫描线程在处理，则等它处理完
                    // watchContextQueue.clear(); // 队列不能清空，因为下次重新startWatch时之前的注册信息不能丢掉，
                    watchService.close(); // 关闭watchService，否则取消监听后操作系统层面仍在继续监听，浪费资源
                    watchService = null; // help gc
                }
            }
        }
    }

    public void watch(File file, T extra, WatchEvent.Kind<?>... eventKinds) throws IOException {
        synchronized (watchContextQueue) {
            if (Objects.isNull(watchService)) { // stopWatch之后watch，此时watchService为null
                watchService = FileSystems.getDefault().newWatchService();
            }
            WatchContext watchContext = getWatchContext(file);
            if (Objects.isNull(watchContext)) {
                watchContext = new WatchContext(file); // 耗时操作
                watchContextQueue.add(watchContext);
            }
            watchContext.addWatchedFile(file, eventKinds, extra);
        }
    }

    public void unWatch(File file, WatchEvent.Kind<?>... eventKinds) throws IOException {
        synchronized (watchContextQueue) {
            WatchContext watchContext = getWatchContext(file);
            if (Objects.isNull(watchContext)) {
                return;
            }
            watchContext.removeWatchedFile(file, eventKinds);
            if (watchContext.watchedCount() == 0) {
                watchContextQueue.remove(watchContext);
            }
        }
    }

    private WatchContext getWatchContext(File file) throws IOException {
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
                if (watchContextQueue.isEmpty()) {
                    return;
                }
                for (WatchContext watchContext : watchContextQueue) {
                    handleWatchEvent(watchContext);
                }
            } catch (Exception ex) {
                ex.printStackTrace(); // 防止单次处理任务时抛出异常导致扫描线程挂掉
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

        public int removeEventKinds(WatchEvent.Kind<?>... eventKinds) {
            this.eventKinds.removeAll(Arrays.stream(eventKinds).collect(Collectors.toSet()));
            return this.eventKinds.size();
        }
    }

    private class WatchContext {
        private WatchKey watchKey; // 目录的监听句柄
        private final Map<String, WatchedFileHolder> watchedFiles = new HashMap<>(); // 真正想监听的文件，同一个目录下的文件名称不可能重复，所以可作为key

        public WatchContext(File file) throws IOException {
            register(file.getParentFile().toPath());
        }

        public void register() throws IOException {
            register((Path) watchKey.watchable());
        }

        private void register(Path dir) throws IOException {
            watchKey = dir.register(watchService,
                    StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_MODIFY,
                    StandardWatchEventKinds.ENTRY_DELETE);
        }

        public void addWatchedFile(File file, WatchEvent.Kind<?>[] eventKinds, T extra) {
            watchedFiles.put(file.getName(), new WatchedFileHolder(file, Arrays.stream(eventKinds).collect(Collectors.toSet()), extra));
        }

        public void removeWatchedFile(File file, WatchEvent.Kind<?>... eventKinds) {
            WatchedFileHolder fileHolder = watchedFiles.get(file.getName());
            if (Objects.isNull(fileHolder)) {
                return;
            }
            // eventKinds为null表示移除整个文件的监听
            if (Objects.isNull(eventKinds) || eventKinds.length == 0 || fileHolder.removeEventKinds(eventKinds) == 0) {
                watchedFiles.remove(file.getName());
                if (watchedCount() == 0 && watchKey.isValid()) {
                    watchKey.cancel();
                }
            }
        }

        public int watchedCount() {
            return watchedFiles.size();
        }

        public WatchedFileHolder getWatchedFileHolder(Path relativePath, WatchEvent.Kind<?> eventKind) { // 触发事件的文件
            WatchedFileHolder fileHolder = watchedFiles.get(relativePath.toString()); // 相对路径，其实就是文件名称
            if (Objects.isNull(fileHolder) || !fileHolder.eventKinds.contains(eventKind)) {
                return null;
            }
            return fileHolder;
        }

        public boolean isSameWatch(File file) throws IOException {
            Path watchedDir = (Path) watchKey.watchable(); // 实际注册监听的目录
            return Files.isSameFile(watchedDir, file.getParentFile().toPath());
        }
    }

    public interface FileEventCallback<T> {
        /**
         * 注意不要在onEventOccurred执行耗时操作，否则影响FileEventMonitor的监听线程
         */
        void onEventOccurred(File file, T extra, WatchEvent.Kind<?> eventKind);
    }
}
