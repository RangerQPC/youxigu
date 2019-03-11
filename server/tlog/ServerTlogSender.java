package com.weichu.xiaoyouxi.tcs.tlog;



public class ServerTlogSender extends TlogSendImpl {

	@Override
	public void sendLog(String content) {
		YLogManager ylog = YLogManager.getInstance();
		ylog.write(content);
	}

}