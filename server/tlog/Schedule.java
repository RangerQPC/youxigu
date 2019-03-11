package com.weichu.xiaoyouxi.tcs.tlog;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 定时任务处理器
 * 
 * @author Edward
 * 
 */
public abstract class Schedule implements Runnable {

	private static final Logger logger = LoggerFactory.getLogger(Schedule.class);

    private ConcurrentHashMap<Integer, Object> objects;

    //初始延迟时间
	private long initialDelay = 0;
    
	//时间周期
    private long period = 60000;
    
    //时间单位
    private TimeUnit timeUnit = TimeUnit.MILLISECONDS;

    
    /**
     * 对象初始化的数量
     * @param objectNum
     */
    public Schedule(int objectNum, long initialDelay, long period, TimeUnit unit) {
    	objects = new ConcurrentHashMap<Integer, Object>(objectNum);
    	this.initialDelay = initialDelay;
    	this.period = period;
    	this.timeUnit = unit;
    }

    /**
     * 增加一个观察对象
     * 
     * @param id
     * @param object
     */
    public void addObject(Integer id, Object object) {
    	logger.info("addObject: " + id + ", " + object);
        objects.put(id, object);
    }

    /**
     * 删除一个观察对象
     * 
     * @param id
     */
    public void removeObject(Integer id) {
    	logger.info("removeObject: " + id);
    	objects.remove(id);

    }

    public Object getObject(Integer id) {
    	return objects.get(id);
    }
    
    public Collection<?> getObjects() {
    	return objects.values();
    }
    
    public long getInitialDelay() {
		return initialDelay;
	}

	public void setInitialDelay(long initialDelay) {
		this.initialDelay = initialDelay;
	}

	public long getPeriod() {
		return period;
	}

	public void setPeriod(long period) {
		this.period = period;
	}

	public TimeUnit getTimeUnit() {
		return timeUnit;
	}

	public void setTimeUnit(TimeUnit unit) {
		this.timeUnit = unit;
	}
    
    @Override
    public void run() {
    	try {
    		execute();
	    } catch (Throwable e) {
	    	logger.error("", e);
		}
    }

    /**
     * 逻辑执行代码
     */
    public abstract void execute();
}
