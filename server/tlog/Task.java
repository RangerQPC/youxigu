package com.weichu.xiaoyouxi.tcs.tlog;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 任务处理器
 * 
 * @author 崔鑫
 * 
 */
public abstract class Task implements Runnable {

	private static final Logger logger = LoggerFactory.getLogger(Task.class);
	
    //private Lock lock;

    //private Condition condition;

    private long LOOP_TIME = 500L;

    private ConcurrentHashMap<Integer, Object> objects;

    private boolean manualLoopTime = false;
    
    private boolean anyrun = false;
    
    private volatile boolean running = false;
    
    public boolean isRunning() {
		return running;
	}

	public void setRunning(boolean running) {
		this.running = running;
	}

	/**
     * 对象初始化的数量
     * @param objectNum
     */
    public Task(int objectNum) {
    	objects = new ConcurrentHashMap<Integer, Object>(objectNum);
    	anyrun = objectNum == 0;
    	
//    	lock = new ReentrantLock();
//    	condition = lock.newCondition();
    }

    /**
     * 设置每次轮训时间
     * 
     * @param intervalTime
     */
    public void setLoopTime(long intervalTime) {
    	if (intervalTime < 0) {
    		logger.error("Error: intervalTime=" + intervalTime + ", loop time is too long.");
    		LOOP_TIME = 1;
    	} else {
    		LOOP_TIME = intervalTime;
    	}
        manualLoopTime = true;
    }

    /**
     * 增加一个观察对象
     * 
     * @param id
     * @param object
     */
    public void addObject(Integer id, Object object) {
    	logger.info("addObject: " + id + ", " + object);
        // 自动调整轮巡时间
        //lock.lock();
        //try {
            if (!manualLoopTime) {
                if (objects.size() > 10)
                    LOOP_TIME = 50;
                else
                    LOOP_TIME = 500L;
            }
            objects.put(id, object);
            //condition.signal();
        //} finally {
            //lock.unlock();
        //}
    }

    /**
     * 删除一个观察对象
     * 
     * @param id
     */
    public void removeObject(Integer id) {
    	logger.info("removeObject: " + id);
    	objects.remove(id);
        
    	/*lock.lock();
        try {
            objects.remove(id);
        } finally {
            lock.unlock();
        }*/
    }

    public Object getObject(Integer id) {
    	return objects.get(id);
    	
    	/*lock.lock();
        try {
            return objects.get(id);
        } finally {
            lock.unlock();
        }*/
    }
    
    public Collection<?> getObjects() {
    	return objects.values();
    }
    
    @Override
    public void run() {
        while (running) {
        	
        	//System.out.println("testing objs.len=" + objects.size() + ", " + this.getClass());

            /*lock.lock();
            try {
            	if (objects != null) {
					while (objects.isEmpty())
						try {
							condition.await();
						} catch (InterruptedException e1) {
							e1.printStackTrace();
						}
            	}
            } finally {
                lock.unlock();
            }*/
            
//            if (objects.size() > 0)
//            	System.out.println("testing ok... " + this.getClass());

            if (anyrun || !objects.isEmpty()) {
            	try {
            		execute();
            	} catch (Throwable e) {
            		logger.error("", e);
        		}
            }

            try {
            	if(running){
            		Thread.sleep(LOOP_TIME);
            	}
            } catch (InterruptedException e) {
            	logger.error("",e);
            	break;
            }

        }
    }

    /**
     * 逻辑执行代码
     */
    public abstract void execute();
    
    public abstract void stop();

}
