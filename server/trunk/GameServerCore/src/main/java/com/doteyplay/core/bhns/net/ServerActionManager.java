package com.doteyplay.core.bhns.net;

import org.apache.mina.core.session.IoSession;

import com.doteyplay.luna.common.action.ActionController;
import com.doteyplay.luna.common.action.BaseAction;
import com.doteyplay.luna.common.message.DecoderMessage;
/**
 * �������������
 * @author 
 * 
 */
public class ServerActionManager implements ActionController
{

	private static ServerActionManager instance = new ServerActionManager();

	public static ServerActionManager getInstance()
	{
		return instance;
	}

	@Override
	public BaseAction getAction(DecoderMessage arg0)
	{
		return DefaultServerAction.getInstance();
	}

	@Override
	public void sessionClose(IoSession arg0)
	{
	}

	@Override
	public void sessionOpen(IoSession session)
	{
		
	}

}