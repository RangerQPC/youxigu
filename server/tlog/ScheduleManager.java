package com.weichu.xiaoyouxi.tcs.tlog;

import java.util.HashMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.youxigu.gs.core.DaemonThreadFactory;
/**
 * 定时任务管理器
 * @author Edward
 * 此机制用于定时任务，适用于隔一段时间短期占用CPU的计算任务。
 * 多个Schedule共享一个线程。
 * 服务器中大多数后台任务，实时性要求不高的，都可使用这个管理器机制
 */

public final class ScheduleManager {
	public static ScheduleManager Singleton = new ScheduleManager();
	
	 private HashMap<Integer, Schedule> schedules = new HashMap<Integer, Schedule>();
	
	private ScheduledThreadPoolExecutor exector;
	
	private static final Logger logger = LoggerFactory.getLogger(ScheduleManager.class);

	  /**
     * 获取一个任务
     * @param cls
     * @return
     */
    public Schedule getSchedule(Integer id) {
        return schedules.get(id);
    }
    
    /**
     * 增加一个任务
     * @param cls
     * @return
     */
    public Schedule addSchedule(Integer id, Schedule schedule) {
        return schedules.put(id, schedule);
    }
    
    public int getScheduleSize() {
    	return schedules.size();
    }
    
	public void shutDown() {
		if (exector!=null) {
			exector.shutdown();
			try {
				if (!exector.awaitTermination(2, TimeUnit.SECONDS)) {
					exector.shutdownNow();
					if(!exector.awaitTermination(60, TimeUnit.SECONDS)){
						logger.error("ScheduleManager not shutdown!!!");
					}
				}
			} catch (InterruptedException e) {
				exector.shutdownNow();
				Thread.currentThread().interrupt();
			}
			logger.info("ScheduleManager is shutdown!!!");
		}
	}
	

	public void readyToStart() {
		int threadSize = 1;
		Singleton.exector = new ScheduledThreadPoolExecutor(threadSize,DaemonThreadFactory.Singleton);

		for (Schedule schedule : ScheduleManager.Singleton.schedules.values()) {
			Singleton.exector.scheduleAtFixedRate(schedule,
					schedule.getInitialDelay(), schedule.getPeriod(),
					schedule.getTimeUnit());
		}
	}
}
