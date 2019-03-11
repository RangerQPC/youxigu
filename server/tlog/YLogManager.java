package com.weichu.xiaoyouxi.tcs.tlog;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class YLogManager extends Task {

	public YLogManager(int objectNum) {
		super(objectNum);
	}


	public static int YLOG_TASK = 2;
	public static YLogManager instance = new YLogManager(0);
	private static final Logger logger = LoggerFactory.getLogger(YLogManager.class);
	private static final SimpleDateFormat FILENAME = new SimpleDateFormat("yyyy_MM_dd_HH");
	private static final SimpleDateFormat DF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	private static final Lock lock = new ReentrantLock();
	private static final Condition hasNew = lock.newCondition();
	
	private static ConcurrentLinkedQueue<String> queue = new ConcurrentLinkedQueue<String>();
	
	private int worldServiceId;
	
	public void setWorldServiceId(int worldServiceId) {
		this.worldServiceId = worldServiceId;
	}

	public static YLogManager getInstance() {
		return instance;
	}
	public void init(int worldServiceId) {
		instance.setWorldServiceId(worldServiceId);
	}

	public void write(String log) {
		//logger.info(log);
		try {
			lock.lock();
			queue.add(log);
			hasNew.signalAll();
		}finally
		{
			lock.unlock();
		}
	}
	@Override
	public void execute() {
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
		writeLog();
	//	long now = System.currentTimeMillis();
		//Calendar cal = Calendar.getInstance();
		//cal.setTimeInMillis(now);
//		setLoopTime((60 - cal.get(Calendar.SECOND))*1000);
//		setLoopTime(10*1000L);
	}

	private void writeLog() {
		BufferedWriter out=null;
		String fileName =  "/data/logs/TCSYLog";
		try {
			File folder = new File(fileName);
			if(!folder.exists()){
				folder.mkdirs();
			}
			FileOutputStream fos = new FileOutputStream(new File(folder,"TCS_YLOG_"+worldServiceId+"_"+FILENAME.format(new Date())+".log"), true);
			OutputStreamWriter stream = new OutputStreamWriter(fos);
			out = new BufferedWriter(stream);

			int num = 0;
			String log = "";
			while ((log = queue.poll()) != null) {
				out.write(log);
				num++;
			}
			logger.info("now: "+DF.format(new Date())+"Successful write " + num + " Ylog!!");
			
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			try {
				out.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}


	@Override
	public void stop() {
		writeLog();
		logger.info("YLogManager stop!");
	}

}
