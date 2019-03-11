package com.weichu.xiaoyouxi.tcs.tlog;

public class TLog {

	
	public static final short DEBUG = 0;
	public static final short INFO = 1;
	public static final short ERROR = 2;
	
	public static final short WRITE_FILE = 0;
	public static final short WRITE_SO = 1;
	
	private int type = 0;
	private int writeType = 0;

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	private String content = "";

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public TLog(int logType, String content) {
		this.type = logType;
		this.content = content;
		this.writeType = TLog.WRITE_SO;
	}

	public TLog(int logType,int writeType, String content) {
		this.type = logType;
		this.content = content;
		this.writeType = writeType;
	}

	public int getWriteType() {
		return writeType;
	}

	public void setWriteType(int writeType) {
		this.writeType = writeType;
	}
	
	
}
