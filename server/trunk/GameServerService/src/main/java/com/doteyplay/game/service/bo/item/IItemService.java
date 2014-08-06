package com.doteyplay.game.service.bo.item;

import java.util.Collection;
import java.util.Map;

import com.doteyplay.core.bhns.ISimpleService;
import com.doteyplay.game.BOConst;
import com.doteyplay.game.constants.item.OutfitInstallResult;
import com.doteyplay.game.constants.item.OutfitUpgradeResult;
import com.doteyplay.game.domain.item.RoleItem;
import com.doteyplay.game.domain.outfit.Outfit;

public interface IItemService extends ISimpleService
{
	public final static int PORTAL_ID = BOConst.BO_ITEM;

	public void initialize();

	public boolean lock();
	
	public void unlock();
	
	public RoleItem addOrRemoveItem(int itemId, int itemNum,boolean isSync);

	public boolean hasItemInBag(int itemId,int itemNum);

	public RoleItem findItemInBag(int itemId);

	public boolean useItem(int itemId, int itemNum);
	
	public OutfitInstallResult installOutfit(long petId,int outfitIdx);
	
	public OutfitUpgradeResult upgradeQualityRemoveOutfit(long petId);
	
	public boolean createItem(int itemId, int num);
	
	public Collection<RoleItem> getBagItemList();
	
	public Collection<RoleItem> getOutfitItemList(long petId);
	
	public Map<Long, Outfit> getAllOutfitMap();
}