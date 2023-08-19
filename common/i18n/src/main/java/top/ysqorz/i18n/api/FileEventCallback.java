package top.ysqorz.i18n.api;

import java.io.File;
import java.nio.file.WatchEvent;
import java.util.List;

/**
 * ...
 *
 * @author yaoshiquan
 * @date 2023/8/19
 */
public interface FileEventCallback {
    /**
     * 注意不要在onEventOccurred执行耗时操作，否则影响FileEventMonitor的监听线程
     */
    void onEventOccurred(File file, List<WatchEvent<?>> watchEvents);
}
