package com.doteyplay.game.service.bo.tollgate;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.doteyplay.core.bhns.AbstractSimpleService;
import com.doteyplay.game.MessageCommands;
import com.doteyplay.game.config.template.BattleDataTemplate;
import com.doteyplay.game.config.template.TollgateDataManager;
import com.doteyplay.game.config.template.TollgateNodeDataTemplate;
import com.doteyplay.game.constants.common.CommonResponseType;
import com.doteyplay.game.constants.common.RewardType;
import com.doteyplay.game.constants.tollgate.TollgateErrorType;
import com.doteyplay.game.constants.tollgate.TollgateRewardExp;
import com.doteyplay.game.domain.pet.Pet;
import com.doteyplay.game.domain.role.Role;
import com.doteyplay.game.domain.tollgate.BattleResult;
import com.doteyplay.game.domain.tollgate.RoleTollgate;
import com.doteyplay.game.message.proto.CommonRespProBuf.PCommonResp;
import com.doteyplay.game.message.tollgate.BattleResultMessage;
import com.doteyplay.game.message.tollgate.NodeChangeMessage;
import com.doteyplay.game.message.tollgate.ShowTollgateDetailMessage;
import com.doteyplay.game.message.tollgate.TollgateChangeMessage;
import com.doteyplay.game.message.utils.ResponseMessageUtils;
import com.doteyplay.game.service.runtime.GlobalRoleCache;
import com.doteyplay.game.util.excel.TemplateService;


/**
 * 
 * @className:TollgateInfoService.java
 * @classDescription: �ؿ�������.
 * 
 * @author:Tom.Zheng
 * @createTime:2014��7��16�� ����3:29:31
 */
public class TollgateInfoService extends
		AbstractSimpleService<ITollgateInfoService> implements
		ITollgateInfoService {
	private static Logger logger = Logger.getLogger(TollgateInfoService.class);

	private RoleTollgate roleTollgate = null;

	@Override
	public int getPortalId() {
		// TODO Auto-generated method stub
		return PORTAL_ID;
	}

	@Override
	public void initlize() {
		roleTollgate = new RoleTollgate(this.getServiceId());

		roleTollgate.initlize();

		logger.error("��ɫ�ĸ������ݼ��سɹ���roleId ="+this.getServiceId());

	}

	/**
	 * ���û��������еĹؿ���Ϣ.
	 */
	@Override
	public ShowTollgateDetailMessage showTollgateDetailInfo() {
		// TODO Auto-generated method stub
		ShowTollgateDetailMessage message = new ShowTollgateDetailMessage();

		roleTollgate.showTollgateDetailInfo(message);

		return message;
	}

	/**
	 * ���Ϳ�����ر�ĳ���ڵ����Ϣ. ������,��һ�ڵ���Ǽ��ı���Ϣ.
	 */
	@Override
	public void sendNodeChangeInfo(int tollgateId, int nodeId) {
		NodeChangeMessage message = new NodeChangeMessage();
		message.setTollgateId(tollgateId);
		message.addUpdateItem(nodeId, 1, 3, 0);

		this.sendMessage(message);

	}

	private void sendTollgateChangeMessage(int tollgateId) {
		TollgateChangeMessage message = new TollgateChangeMessage();

		message.addOperateTollgate(tollgateId, true);

		this.sendMessage(message);
	}

	@Override
	public void enterBattle(int tollgateId, int nodeId, int groupId) {
		// TODO Auto-generated method stub
		// ����ؿ�����ս���¼�.
		
		// 1.����Ѿ�����.2.�����������ڽ���ս��,�ظ��ɹ�.3.���������ڽ��������ؿ�,�����伤�������ؿ�,����������.
		boolean isOpenTollgateAndNode = roleTollgate.isOpenTollgateAndNode(
				tollgateId, nodeId);

		if (!isOpenTollgateAndNode) {
			// ���ʹ�����Ϣ.
			Role role = getRole();
			// δ����ýڵ㡣

			ResponseMessageUtils.sendResponseMessage(
					MessageCommands.ENTER_BATTLE_MESSAGE.ordinal(),
					TollgateErrorType.NoNode.ordinal(), role);
		} else {
			//
			Role role = getRole();
			role.setPetGroupId(groupId);
			ResponseMessageUtils.sendResponseMessage(
					MessageCommands.ENTER_BATTLE_MESSAGE.ordinal(),
					TollgateErrorType.Success.ordinal(), role);

			// ���ͳɹ�����Ϣ
		}
	}

	/**
	 * ����ս�����
	 * 
	 * @param star
	 */
	@Override
	public void acceptBattleResult(int tollgateId, int nodeId, int star) {
		// ����ս�����,����������µľ���.
		boolean openTollgateAndNode = roleTollgate.isOpenTollgateAndNode(
				tollgateId, nodeId);

		if (!openTollgateAndNode) {
			return;
		}
		if (star < 0 || star > 3) {
			return;
		}

		roleTollgate.acceptBattleResult(tollgateId, nodeId, star);

		addBattleResultReward(tollgateId, nodeId, star);
		// ������һ��.��˳��������.
		
		openNextTollgate(tollgateId, nodeId, star);

	}

	private void openNextTollgate(int tollgateId, int nodeId, int star) {
		Set<Integer> allOpenTollgateIds = roleTollgate.getAllOpenTollgate();
		TollgateNodeDataTemplate nextNodeData = TollgateDataManager
				.getInstance().getNextNodeData(tollgateId, nodeId,
						allOpenTollgateIds);

		if (nextNodeData == null) {
			int nextTollgate = TollgateDataManager.getInstance()
					.getNextTollgate(tollgateId, allOpenTollgateIds);

			// �����µĹؿ�.�����͸��ͻ���.
			boolean openTollgate = roleTollgate
					.isOpenTollgate(nextTollgate);
			if (!openTollgate) {
				int firstNodeId = TollgateDataManager.getInstance()
						.getFirstNodeId(nextTollgate);
				roleTollgate.openTollgateOrNodeAndUpdateDB(nextTollgate,
						firstNodeId);
				sendTollgateChangeMessage(tollgateId);

			}

		} else {

			boolean isOpen = roleTollgate.isOpenTollgateAndNode(
					nextNodeData.getTollgateGateId(), nextNodeData.getId());
			if (isOpen) {
				// ��һ���Ѿ�����,�Ͳ��ù���.
				NodeChangeMessage message = new NodeChangeMessage();
				message.setTollgateId(tollgateId);
				message.addUpdateItem(nodeId, 3, star, 2);//��ʷ�ڵ��Ǹ���.
				this.sendMessage(message);
			} else {
				// ������һ��.
				roleTollgate.openTollgateOrNodeAndUpdateDB(
						nextNodeData.getTollgateGateId(),
						nextNodeData.getId());
				NodeChangeMessage message = new NodeChangeMessage();
				message.setTollgateId(tollgateId);
				message.addUpdateItem(nextNodeData.getId(), 3, 0, 1);//��ǰ�ڵ�.������.
				message.addUpdateItem(nodeId, 3, star, 2);//��ʷ�ڵ��Ǹ���.

				this.sendMessage(message);
			
			}

		}
	}

	/**
	 * ��ָ���Ĺؿ���Ϣ,�ҵ�ָ���Ľ�����Ϣ,��������ս�ӽ���.����ÿһ������Ľ���.
	 * 
	 * @param tollgateId
	 * @param nodeId
	 * @param star
	 * @param petIds
	 */
	private void addBattleResultReward(int tollgateId, int nodeId, int star) {

		TollgateNodeDataTemplate nextNodeData = TollgateDataManager
				.getInstance().getTollgateData(tollgateId, nodeId);
		if (nextNodeData == null) {
			return;
		}
		int opreateType = nextNodeData.getOpreateType();

		if (opreateType == 0) {
			return;
		}
		int battleId = nextNodeData.getOpreateId();

		Map<Integer, BattleDataTemplate> all = TemplateService.getInstance()
				.getAll(BattleDataTemplate.class);

		if (!all.containsKey(battleId)) {
			return;
		}
		BattleDataTemplate temp = all.get(battleId);

		int petExp = temp.getPetExp();// ս�Ӿ���

		int gameCoin = temp.getGameCoin();// ��ɫ���

		int dropGroupId = temp.getDropGroupId();
		BattleResult result = new BattleResult();
		result.gameCoin = gameCoin;
		result.star = star;
		recordRoleHistory(result, false);
		recordPetCurrent(result, false);
		addRoleExp(tollgateId);
		addPetExp(petExp,getRole());
		addGameCoin(gameCoin,getRole());
		addDropGroupId(dropGroupId);
		recordRoleHistory(result, true);
		recordPetCurrent(result, true);
		showBattleResultMsg(result);

	}

	private void recordRoleHistory(BattleResult result, boolean isNew) {
		Role role = GlobalRoleCache.getInstance().getRoleById(getServiceId());
		if (isNew) {
			result.battleRoleResult.recordNewRole(
					role.getRoleBean().getLevel(), role.getRoleBean().getExp());
		} else {
			result.battleRoleResult.recordOldRole(
					role.getRoleBean().getLevel(), role.getRoleBean().getExp());

		}

	}

	private void recordPetCurrent(BattleResult result, boolean isNew) {
		Role role = GlobalRoleCache.getInstance().getRoleById(getServiceId());
		List<Pet> curPetList = role.getPetManager().getCurPetList();
		for (Pet pet : curPetList) {
			if (isNew) {
				result.recordNewPet(pet.getId(), pet.getBean().getLevel(), pet
						.getBean().getExp());
			} else {
				result.recordOldPet(pet.getId(), pet.getBean().getLevel(), pet
						.getBean().getExp());
			}
		}

	}

	private void showBattleResultMsg(BattleResult result) {
		BattleResultMessage message = new BattleResultMessage();
		message.setBattleResult(result);
		this.sendMessage(message);
	}

	private void addRoleExp(int tollgateId) {

		int tollgateShowType = TollgateDataManager.getInstance()
				.getTollgateShowType(tollgateId);

		int costEnergyPoint = TollgateRewardExp
				.getCostEnergyPoint(tollgateShowType);

		int roleExp = TollgateRewardExp
				.rewardRoleExpByEnergyPoint(costEnergyPoint);

		if (roleExp < 0) {
			throw new RuntimeException("�����������鲻��Ϊ����");
		}

		Role role = GlobalRoleCache.getInstance().getRoleById(getServiceId());
		// ԭ�����ȿ�����,�ټӾ���.��������û�о���.
		role.addExp(roleExp);
		// ������.
		if (role.addEnergy(-costEnergyPoint, RewardType.BATTLE, true)) {
			// �Ӿ���
		}

	}
	/**
	 * ���ӳ���ľ���.
	 * @param petExp
	 */
	private void addPetExp(int petExp,Role role) {
		role.getPetManager().addCurPetListExp(petExp);

	}
	/**
	 * ������Ϸ��.
	 * @param gameCoin
	 */
	private void addGameCoin(int gameCoin,Role role) {

		if (gameCoin < 0) {
			throw new RuntimeException("����������Ϸ�Ҳ���Ϊ����");
		}
		// ��Ǯ
		role.addMoney(gameCoin, RewardType.BATTLE, true);

	}
	/**
	 * ���ӵ�������Ʒ
	 * @param dropGroupId
	 */
	private void addDropGroupId(int dropGroupId) {

	}

	private Role getRole() {
		Role role = GlobalRoleCache.getInstance().getRoleById(getServiceId());
		return role;
	}
	/**
	 * ���ݽ���ԭ������,�����ڵ���ֱ�ӽ���ս��,����ֱ�ӽ�����������.
	 * �˲���,���Բ�����������.�ɿͻ��˸������.
	 * @param tollgateId
	 * @param sourceNodeId
	 */
	public void enterNode(int tollgateId,int sourceNodeId){
		// ����ԭ������,����ȡ��Ҫ��ս��,���ǽ��������ؿ�.
				TollgateNodeDataTemplate tollgateData = TollgateDataManager
						.getInstance().getTollgateData(tollgateId, sourceNodeId);

				int opreateType = tollgateData.getOpreateType();

				int opreateId = tollgateData.getOpreateId();
				// ������������ؿ�.
				if (opreateType == 0) {
					// ����opreateId ���͸�client ����Ĺؿ�����.
					// ���ؿ������Ƿ��Ѿ�����,���û�м���,������.
					if (!roleTollgate.isOpenTollgate(opreateId)) {
						int nodeId = TollgateDataManager.getInstance().getFirstNodeId(
								opreateId);
						roleTollgate.openTollgateOrNodeAndUpdateDB(opreateId, nodeId);
						// ��ͻ��˷����¿����ĸ���������.
						sendTollgateChangeMessage(opreateId);
					} else {

					}

				}

				if (opreateType == 1) {
					// ���Ϳͻ���,����һ��ս��.
				}
	}
	

	public void release() {
		if (roleTollgate != null) {
			roleTollgate.release();
			roleTollgate = null;
		}
	}

}