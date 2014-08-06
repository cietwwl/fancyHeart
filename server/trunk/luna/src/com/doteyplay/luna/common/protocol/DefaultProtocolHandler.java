package com.doteyplay.luna.common.protocol;

import org.apache.log4j.Logger;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;

import com.doteyplay.luna.common.LunaConstants;
import com.doteyplay.luna.common.action.ActionController;
import com.doteyplay.luna.common.action.BaseAction;
import com.doteyplay.luna.common.message.DecoderMessage;

/**
 * Ĭ�ϵ�IoHandler�Ĵ�����
 */
public class DefaultProtocolHandler extends IoHandlerAdapter {
	private Logger logger = Logger.getLogger(DefaultProtocolHandler.class
			.getName());
	/**
	 * ִ��Action�ַ��Ŀ����࣬����Handlerʵ����ʱ���������
	 */
	private ActionController actionCntroller;

	public DefaultProtocolHandler(ActionController controller) {
		this.actionCntroller = controller;
	}

	@Override
	public void sessionCreated(IoSession session) {
		if (this.logger.isDebugEnabled())
			this.logger.debug("�����Ự!");
	}

	@Override
	public void sessionOpened(IoSession session) {
		if (this.logger.isDebugEnabled())
			this.logger.debug("�򿪻Ự!");
		
		this.actionCntroller.sessionOpen(session);
		
		session.getConfig().setIdleTime(IdleStatus.BOTH_IDLE,
				LunaConstants.IDLE_TIME);
	}

	@Override
	public void sessionClosed(IoSession session) {
		if (this.logger.isDebugEnabled())
			this.logger.debug("�رջỰ!");
		if (actionCntroller != null)
			actionCntroller.sessionClose(session);
		session.close(true);
	}

	@Override
	public void messageReceived(IoSession session, Object message) {
		if (this.logger.isDebugEnabled())
			this.logger.debug("���յ�����Ϣ: " + message.toString());
		DecoderMessage decoderMessage = (DecoderMessage) message;
		if (decoderMessage.getCommandId() == Short.MAX_VALUE) {// ����ǹر�ָ����ֱ�ӹرջỰ
			session.close(true);
		} else {
			BaseAction action = this.actionCntroller.getAction(decoderMessage);
			if (action != null)
				action.doAction(session, decoderMessage);
			this.logger
					.info("����������ʱ��:"
							+ (System.currentTimeMillis() - decoderMessage
									.getNewTime()));
		}
	}

	@Override
	public void sessionIdle(IoSession session, IdleStatus status) {
		if (this.logger.isDebugEnabled())
			this.logger.debug("Idle ״̬:" + status.toString());
	}

	@Override
	public void exceptionCaught(IoSession session, Throwable cause) {
		this.logger.error("�Ự�����쳣��" + session.getRemoteAddress(), cause);
	}

	public ActionController getActionCntroller() {
		return this.actionCntroller;
	}

	public void setActionCntroller(ActionController actionCntroller) {
		this.actionCntroller = actionCntroller;
	}
}