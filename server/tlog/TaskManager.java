package com.weichu.xiaoyouxi.tcs.tlog;

import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.youxigu.gs.core.DaemonThreadFactory;

/**
 * 全局唯一任务管理器
 * @author 崔鑫
 * modify by Edward
 * 此机制仅用于连续执行的高CPU运算量任务,会一直单独占用CPU的一个核心，如怪物AI
 */
public final class TaskManager {
    public static TaskManager Singleton = new TaskManager();

    private HashMap<Integer, Task> tasks = new HashMap<Integer, Task>();
    
    private ExecutorService exector;
    
    private static final Logger logger = LoggerFactory.getLogger(TaskManager.class);

    /**
     * 获取一个任务
     * @param cls
     * @return
     */
    public Task getTask(Integer id) {
        return tasks.get(id);
    }
    
    /**
     * 增加一个任务
     * @param cls
     * @return
     */
    public Task addTask(Integer id, Task task) {
        return tasks.put(id, task);
    }
    
    public int getTaskSize() {
    	return tasks.size();
    }
    
	public void shutDown() {
		for (Task task : TaskManager.Singleton.tasks.values()) {
			task.setRunning(false);
		}
		if (exector!=null) {
			exector.shutdown();
			try {
				if (!exector.awaitTermination(2, TimeUnit.SECONDS)) {
					exector.shutdownNow();
					if (!exector.awaitTermination(60, TimeUnit.SECONDS)){
						logger.error("TaskManager not shutdown!!!");
					}
				}
			} catch (InterruptedException e) {
				exector.shutdownNow();
				Thread.currentThread().interrupt();
			}
			logger.info("TaskManager is shutdown!!!");
		}
		for (Task task : TaskManager.Singleton.tasks.values()) {
			task.stop();
		}
	}
    
    public void readyToStart() {
		int threadSize = TaskManager.Singleton.getTaskSize();
		
		TaskManager.Singleton.exector = Executors.newFixedThreadPool(threadSize,DaemonThreadFactory.Singleton);
		for (Task task : TaskManager.Singleton.tasks.values()) {
			task.setRunning(true);
			TaskManager.Singleton.exector.execute(task);
		}
    }
    
}
