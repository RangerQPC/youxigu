package com.weichu.xiaoyouxi.tcs.tlog;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.weichu.xiaoyouxi.util.string.StringTool;
import com.youxigu.gs.core.Configuration;

public class TLogSender extends Task {

	private static final Logger logger = LoggerFactory.getLogger(TLogSender.class);
	private static ConcurrentLinkedQueue<TLog> queue = new ConcurrentLinkedQueue<TLog>();

	private static final Lock lock = new ReentrantLock();
	private static final Condition hasNew = lock.newCondition();

	private static final int OS_WINDOWS = 0;
	private static final int OS_LINUX = 1;

	private static int OS = TLogSender.OS_WINDOWS;
	
	private static final short POOLSIZE =  1;

	public static int TASK_ID = 20000;

	private static boolean loadok = false;
	
	private TlogSendImpl sender = null;
	
	static boolean needSendLog = false;
	
	private static int index = 1;
	
	private static int groupId = 0;
	
	private static int serverId = 0;
	
	private static final Lock eventLock = new ReentrantLock();
	
	public static boolean isLoadok() {
		return loadok;
	}

	private static TLogSender singleton;

	public static void initGroupId(int groupId)
	{
		TLogSender.groupId = groupId;
	}
	
	public static TLogSender getInstance() {
		if (singleton == null)
			singleton = new TLogSender(0);
		return singleton;
	}

	public void init(TlogSendImpl sender)
	{
		this.sender = sender;
		loadok = true;
	}
	

	private TLogSender(int objNum) {
		super(objNum);
		try {		
			OS = Integer.parseInt(Configuration.getProperties().getProperty("tlog_os","1"));
		} catch (Throwable e) {
			OS  = OS_LINUX;
		}
	}

	public static void addLog(short logType, String content) {
		if(!loadok)
		{
			return;
		}
		String _content = StringTool.getString(StringTool.getBytes(content));
		queue.add(new TLog(logType, _content));
		try {
			lock.lock();
			if (queue.size() >= POOLSIZE || OS == TLogSender.OS_WINDOWS)
			{
				logger.debug("addLog and singall all ...."+ " content ="+_content);
				hasNew.signalAll();
				logger.debug("addLog and singall all over ...."+ " content ="+_content);
			}
		} finally {
			lock.unlock();
		}
	}

	public static void addLog(short logType,short writeType, String content) {
		if(!loadok)
		{
			return;
		}
		String _content = StringTool.getString(StringTool.getBytes(content));
		queue.add(new TLog(logType ,writeType , _content));
		try {
			lock.lock();
			if (queue.size() >= POOLSIZE || OS == TLogSender.OS_WINDOWS)
			{
				logger.debug("addLog and singall all .... type " +writeType+ " content ="+_content);
				hasNew.signalAll();
				logger.debug("addLog and singall all over .... type " +writeType+ " content ="+_content);
			}
		} finally {
			lock.unlock();
		}
	}
	
	public static void addLogNow(short logType, String content) {
		if(!loadok)
		{
			return;
		}
		String _content = StringTool.getString(StringTool.getBytes(content));
		queue.add(new TLog(logType, _content));
		try {
			logger.debug("addLognow and singall all .... type"+ " content ="+_content);
			lock.lock();
			hasNew.signalAll();
			logger.debug("addLognow and singall all over .... "+ " content ="+_content);
		} finally {
			lock.unlock();
		}
	}
	
	public static void addLogNow(short logType,short writeType, String content) {
		if(!loadok)
		{
			return;
		}
		String _content = StringTool.getString(StringTool.getBytes(content));
		queue.add(new TLog(logType, writeType, _content));
		try {
			logger.debug("addLognow2 and singall all .... type " +writeType + " content ="+_content);
			lock.lock();
			hasNew.signalAll();
			logger.debug("addLognow2 and singall all over .... type " +writeType+ " content ="+_content);
		} finally {
			lock.unlock();
		}
	}


	private static boolean sendLog(TLog log) {
		boolean result = true;
		try {
			switch (log.getType()) {
			case TLog.DEBUG: {
					singleton.sender.sendLog(log.getContent());
					logger.debug("sendLog over DEBUG and singall all .... ");
				}
				break;
			case TLog.INFO:  {
					singleton.sender.sendLog(log.getContent());
					logger.debug("sendLog over DEBUG and singall all .... ");
					}
				break;
			case TLog.ERROR:  {
					singleton.sender.sendLog(log.getContent());
					logger.debug("sendLog over DEBUG and singall all .... ");
				}
				break;
			default:
				break;
			}
		} catch (Throwable e) {
			result = false;
			logger.error("", e);
		}
		return result;

	}


	/**
	 * 修改 创建 eventId算法 毫秒 * 100 * 100 * 100 + 大区id * 100 * 100+ 服务器id * 100 + 索引
	 * @return
	 */
	public static long createEventId(){
		long result =100L * 100L * 100L * System.currentTimeMillis();
		long group = 100L * 100L* groupId;
		long server = 100L * serverId;
		int _index = 0;
		try {
			eventLock.lock();
			result =100L * 100L * 100L * System.currentTimeMillis();
			_index = index;
			++index;
			if(index >= 100)
			{
				index = 1;
			}
		}catch(Exception e)
		{
		}
		finally{
			eventLock.unlock();
		}
		result = result +group + server + _index ;
	    return result;
	}

	@Override
	public void execute() {
		if(!loadok)
		{
			return;
		}
		try {
			lock.lock();
			try {
				hasNew.await();
			} catch (InterruptedException e) {
				logger.error("", e);
			}
		} finally {
			lock.unlock();
		}
		int num = 0;
		TLog log = null;
		while ((log = queue.poll()) != null) {
			if (sendLog(log))
				num++;
		}
		logger.debug("Successful send " + num + " Tlog!!");

	}

	@Override
	public void stop() {
		TLog log = null;
		while ((log = queue.poll()) != null) {
			sendLog(log);
		}
		logger.info("The Tlog Thread Job is closed");
	}

}

