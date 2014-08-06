package com.doteyplay.manager;

import java.util.Date;
import java.util.concurrent.ArrayBlockingQueue;

import com.doteyplay.bean.UserBean;
import com.doteyplay.core.dbcs.DBCS;
import com.doteyplay.dao.IUserBeanDao;
import com.doteyplay.domain.LoginResponse;
import com.doteyplay.manager.NosqlCached.CacheType;
import com.doteyplay.utils.CircularDoubleBufferedQueue;
import com.google.gson.Gson;

public class AuthManager
{
	private Gson gson = new Gson();

	private final static int USER_QUEUE_UPDATE_MAX_SIZE = 1000;
	private final static int USER_QUEUE_INIT_SIZE = 10000;
	private CircularDoubleBufferedQueue<UserBean> userBeanUpdateQueue = new CircularDoubleBufferedQueue<UserBean>(
			USER_QUEUE_INIT_SIZE);
	private ArrayBlockingQueue<UserBean> updateList = new ArrayBlockingQueue<UserBean>(
			USER_QUEUE_UPDATE_MAX_SIZE);

	private AuthManager()
	{
		// 异步写数据库
		Thread t = new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				while (true)
				{
					while (updateList.size() < USER_QUEUE_UPDATE_MAX_SIZE)
					{
						try
						{
							updateList.add(userBeanUpdateQueue.take());
						} catch (InterruptedException e)
						{
							e.printStackTrace();
						}
					}
					saveUserBeanUpdateList();
				}
			}
		});
		t.start();
	}

	public String login(String account, String password,String loginIp)
	{
		LoginResponse response = new LoginResponse();
		if (account == null || "".equals(account))
		{
			response.setLoginState(LoginResponse.LOGIN_STATE_ACCOUNT_NULL);
			return gson.toJson(response);
		}

		UserBean userBean = NosqlCached.getInstance().getObject(CacheType.USER,
				account);
		if (userBean == null)
		{
			IUserBeanDao userDao = DBCS.getExector(IUserBeanDao.class);
			userBean = userDao.selectUserBeanByName(account);

			if (userBean == null)
			{
				userBean = new UserBean();
				userBean.setName(account);
				userBean.setPassword(password);
				userBean.setUserType((byte) 1); // FIXME GAOYUAN:测试阶段所有用户都是gm
				userBean.setLastLoginIp(loginIp);
				long time = new Date().getTime();
				userBean.setLastLoginTime(time);
				userBean.setCreateTime(time);
				long userId = userDao.insertUserBean(userBean);
				userBean.setId(userId);
			}
			else
			{
				//账号被封
				if(userBean.getUserType() == 2)
				{
					response.setLoginState(LoginResponse.LOGIN_STATE_ACCOUNT_LIMIT);
					return gson.toJson(response);
				}
				
				userBean.setLastLoginIp(loginIp);
				userBean.setLastLoginTime(new Date().getTime());
			}
			
			NosqlCached.getInstance().put(CacheType.USER, userBean.getName(),
					userBean);
		}

		String sessionKey = account
				+ String.valueOf(System.currentTimeMillis());

		NosqlCached.getInstance().put(CacheType.SESSION_KEY, account,
				sessionKey);

		response.setUserBean(userBean);
		response.setAreaList(AreaManager.getInstance().getAreaList());
		response.setKey(sessionKey);
		response.setLoginState(LoginResponse.LOGIN_STATE_SUCCESS);

		return gson.toJson(response);
	}

	public UserBean checkAuth(String account, String sessionKey, int areaId)
	{
		String record = NosqlCached.getInstance().getObject(
				CacheType.SESSION_KEY, account);
		if (record != null && record.equals(sessionKey))
		{
			NosqlCached.getInstance().remove(CacheType.SESSION_KEY, account);
			UserBean bean = NosqlCached.getInstance().getObject(CacheType.USER,
					account);
			if (bean != null)
			{
				this.updateUserLoginState(bean, areaId);
				return bean;
			}
		}
		return null;
	}

	public void updateUserLoginState(UserBean userBean, int areaId)
	{
		userBean.setLastAreaId(areaId);
		try
		{
			userBeanUpdateQueue.put(userBean, true);
		} catch (InterruptedException e)
		{
			e.printStackTrace();
		}
		NosqlCached.getInstance().put(CacheType.USER, userBean.getName(),
				userBean);
	}

	public void saveUserBeanUpdateList()
	{
		IUserBeanDao dao = DBCS.getExector(IUserBeanDao.class);
		if (dao.startTransaction())
		{
			try
			{
				for (UserBean bean : updateList)
				{
					try
					{
						dao.updateUserBean(bean);
					} catch (Exception e)
					{
						e.printStackTrace();
					}

				}
				dao.commitTransaction();
			} finally
			{
				dao.endTransaction();
				updateList.clear();
			}
		}
	}

	// /////////////////////////////////////////////////////////////////////////
	private final static AuthManager instance = new AuthManager();

	public final static AuthManager getInstance()
	{
		return instance;
	}
}