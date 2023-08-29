package top.ysqorz.i18n.common;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * 文件事件监听器
 *
 * @author yaoshiquan
 * @date 2023/8/24
 */
public class FileEventMonitor<T> implements AutoCloseable {
    private final Lock lock = new ReentrantLock();

    /**
     * 文件监听的注册信息
     */
    private final Queue<WatchContext> watchContextQueue = new ConcurrentLinkedQueue<>();

    /**
     * 操作系统的文件监听服务
     */
    private WatchService watchService;

    /**
     * 定时轮询线程，用于扫描队列处理事件回调
     */
    private final ScheduledExecutorService scheduledExecutor = Executors.newScheduledThreadPool(1,
            runnable -> {
                Thread thread = Executors.defaultThreadFactory().newThread(runnable);
                thread.setName(this.getClass().getSimpleName()); // 设置扫描线程名称
                thread.setDaemon(true); // 设置守护线程
                return thread;
            });

    /**
     * 定时轮询线程的轮询间隔，单位为ms
     */
    private final Long watchInterval;

    /**
     * 定时轮询线程的扫描任务
     */
    private volatile ScheduledFuture<?> future;

    public FileEventMonitor(Long watchInterval) throws IOException {
        this.watchInterval = watchInterval;
        this.watchService = FileSystems.getDefault().newWatchService();
    }

    /**
     * 关闭文件监听器，清理所有资源，不能再重新启用
     */
    @Override
    public void close() throws IOException {
        if (Objects.isNull(watchService)) {
            return;
        }
        lock.lock();
        try {
            if (Objects.nonNull(watchService)) {
                watchService.close();
                watchService = null; // help gc
                scheduledExecutor.shutdown();
                watchContextQueue.clear();
            }
        } finally {
            lock.unlock();
        }

    }

    /**
     * 开始监听
     *
     * @param callback 关注的事件发生时执行的回调
     */
    public void startWatch(FileEventCallback<T> callback) throws IOException {
        if (Objects.nonNull(watchService) && Objects.nonNull(future)) {
            return;
        }
        lock.lock(); // 加锁以确保原子操作
        try {
            // stopWatch之后startWatch，此时watchService为null
            if (Objects.isNull(watchService)) {
                watchService = FileSystems.getDefault().newWatchService(); // 耗时操作
                // 重新恢复之前注册的文件的监听，stopWatch之后注册的监听也在其中
                for (WatchContext context : watchContextQueue) {
                    context.register();
                }
            }
            // 传入新回调方法时,不更新回调接口
            if (Objects.isNull(future)) {
                future = scheduledExecutor.scheduleWithFixedDelay(new WatchTask(callback), 0, watchInterval, TimeUnit.MILLISECONDS);
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * 停止监听
     */
    public void stopWatch() throws IOException {
        if (Objects.isNull(future) && Objects.isNull(watchService)) {
            return;
        }
        lock.lock();
        try {
            if (Objects.nonNull(future)) {
                future.cancel(false); // false表示如果扫描线程在处理，则等它处理完
                future = null;
            }
            if (Objects.nonNull(watchService)) {
                watchService.close(); // 关闭watchService，否则取消监听后操作系统层面仍在继续监听，浪费资源
                watchService = null; // help gc
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * 获取文件监听注册信息的上下文
     *
     * @param path 文件的所在目录
     */
    private WatchContext getWatchContext(Path path) throws IOException {
        for (WatchContext watchContext : watchContextQueue) {
            if (watchContext.isSameWatch(path)) {
                return watchContext;
            }
        }
        return null;
    }

    /**
     * 注册监听
     *
     * @param file       需要注册的文件
     * @param extra      额外参数
     * @param eventKinds 文件需要监听的事件
     */
    public void watch(File file, T extra, WatchEvent.Kind<?>... eventKinds) throws IOException {
        lock.lock();
        try {
            // stopWatch之后watch，此时watchService为null
            if (Objects.isNull(watchService)) {
                watchService = FileSystems.getDefault().newWatchService();
            }
            Path path = Paths.get(file.getParent());
            // 检测是否重复监听
            WatchContext watchContext = getWatchContext(path);
            if (Objects.isNull(watchContext)) {
                watchContext = new WatchContext(path); // 耗时操作
                watchContextQueue.add(watchContext);
            }
            watchContext.addWatchedFile(file, eventKinds, extra);
        } finally {
            lock.unlock();
        }
    }

    /**
     * 取消监听
     *
     * @param file 需要取消监听的文件
     */
    public void unWatch(File file, WatchEvent.Kind<?>... eventKinds) throws IOException {
        lock.lock();
        try {
            Path path = Paths.get(file.getParent());
            WatchContext watchContext = getWatchContext(path);
            if (Objects.isNull(watchContext)) {
                return;
            }
            watchContext.removeWatchedFile(file, eventKinds);
            if (watchContext.watchedCount() == 0) {
                watchContextQueue.remove(watchContext);
            }
        } finally {
            lock.unlock();
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
                for (WatchContext watchContext : watchContextQueue) {
                    List<WatchEvent<?>> watchEvents = watchContext.watchKey.pollEvents();
                    for (WatchEvent<?> watchEvent : watchEvents) {
                        // 判断文件是否是监控的文件，事件是否被关心
                        WatchedFileHolder holder = watchContext.getWatchedFileHolder(watchEvent.context().toString(), watchEvent.kind());
                        if (Objects.nonNull(holder)) {
                            callback.onEventOccurred(holder.file, holder.extra, watchEvent.kind());
                        }
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace(); // 防止单次处理任务时抛出异常导致扫描线程挂掉
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
        private final Map<String, WatchedFileHolder> watchedFiles = new HashMap<>(); // 真正想监听的文件

        public WatchContext(Path path) throws IOException {
            register(path);
        }

        public void register() throws IOException {
            register((Path) watchKey.watchable());
        }

        private void register(Path path) throws IOException {
            this.watchKey = path.register(watchService,
                    StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_DELETE,
                    StandardWatchEventKinds.ENTRY_MODIFY);
        }

        /**
         * 添加当前目录下需要监听的文件
         *
         * @param file       需要监听的文件
         * @param eventKinds 文件需要监听的事件
         * @param extra      额外参数
         */
        public void addWatchedFile(File file, WatchEvent.Kind<?>[] eventKinds, T extra) {
            Set<WatchEvent.Kind<?>> eventSet = Arrays.stream(eventKinds).collect(Collectors.toSet());
            watchedFiles.put(file.getName(), new WatchedFileHolder(file, eventSet, extra));
        }

        /**
         * 取消文件监听
         *
         * @param file 需要取消监听的文件
         */
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

        /**
         * 获取该上下文监听的文件数
         *
         * @return 监听的文件数
         */
        public int watchedCount() {
            return watchedFiles.size();
        }

        /**
         * 根据文件名和需要关心的事件查询
         *
         * @param fileName  文件名
         * @param eventKind 文件关心的事件
         * @return 文件包装对象
         */
        public WatchedFileHolder getWatchedFileHolder(String fileName, WatchEvent.Kind<?> eventKind) {
            WatchedFileHolder fileHolder = watchedFiles.get(fileName);
            if (Objects.nonNull(fileHolder) && fileHolder.eventKinds.contains(eventKind)) {
                return fileHolder;
            }
            return null;
        }

        /**
         * 比较是否当前上下文的监听路径
         *
         * @param path 路径
         * @return 是否同一文件
         */
        public boolean isSameWatch(Path path) throws IOException {
            return Files.isSameFile((Path) watchKey.watchable(), path);
        }
    }

    public interface FileEventCallback<T> {
        void onEventOccurred(File file, T extra, WatchEvent.Kind<?> eventKinds);
    }
}
