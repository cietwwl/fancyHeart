//
//  GateMap.cpp
//  fancyHeart
//
//  Created by zhai on 14-6-18.
//
//

#include "GateMap.h"

GateMap* GateMap::create(BaseUI* delegate,int  gateId)
{
    GateMap* gateMap=new GateMap();
    if (gateMap && gateMap->init("publish/gateMap/gateMap.ExportJson",gateId)) {
        gateMap->autorelease();
        return gateMap;
    }
    CC_SAFE_DELETE(gateMap);
    return nullptr;
}

bool GateMap::init(std::string fileName,int gateId)
{
	if(!BaseUI::init(fileName))
	{
		return false;
	}
    Manager::getInstance()->gateId=0;
    this->resetUI(gateId);
	return true;
}

void GateMap::resetUI(int gateId)//init map
{
    auto size=Director::getInstance()->getOpenGLView()->getDesignResolutionSize();
    gateItem=Manager::getInstance()->getGateItem(gateId);
    Widget* bgLayer=layout->getChildByName("bgLayer");
    auto button=static_cast<Button*>(bgLayer->getChildByName("btn_return"));
    button->addTouchEventListener(CC_CALLBACK_2(GateMap::touchButtonEvent, this));
    XGate* xGate=XGate::record(Value(gateId));
    string path=Value(xGate->getMapId()).asString()+".tmx";
    //地图
    TMXTiledMap* map=TMXTiledMap::create(path);
    bgLayer->addChild(map,-1);
    //地图层
    TMXLayer* mapLayer = map->getLayer("item");
    //地图格子数量
    Size layerSize = map->getMapSize();
    //地图每个格子的宽高
    Size tileSize=map->getTileSize();
    //获取对象组
    auto group = map->getObjectGroup("object");
    //获取所有对象
    auto& objects = group->getObjects();
    for (auto obj:objects) {
        ValueMap& dict=obj.asValueMap();
        float x=dict["x"].asFloat();
        float y=dict["y"].asFloat();
        int id=dict["id"].asInt();
        //通过对象的x坐标获取所在的列
        int col=floor(x/map->getTileSize().width);
        //通过y坐标获取所在的行，由于地图坐标0，0点在左上角，所以要进行转换
        int row=floor((layerSize.height*tileSize.height-y-10)/tileSize.height);
        //获取坐标所在的sprite,默认全部不显示，通过服务器传递的数据判断显示哪个
        Sprite* sprite=mapLayer->getTileAt(Vec2(col,row));
        if (sprite) {
            sprite->setTag(id);
            this->sprites.insert(id, sprite);
            sprite->setVisible(false);
        }
    }
    //通过服务器传递的数据判断显示哪个
    for (int i=0; i<gateItem->items_size(); i++) {
        PNodeItem* nodeItem= gateItem->mutable_items(i);
        Sprite*  sprite=this->sprites.at(nodeItem->xid());
        if (sprite) {
            sprite->setVisible(true);
        }
    }

    bgLayer->setTouchEnabled(true);
    bgLayer->setEnabled(true);
    bgLayer->addTouchEventListener(CC_CALLBACK_2(GateMap::touchEvent, this));
}

void GateMap::touchButtonEvent(Ref *pSender, TouchEventType type)
{
    auto btn=static_cast<Button*>(pSender);
    if (!btn) {
        return;
    }
    if (type==TouchEventType::ENDED) {
        switch (btn->getTag()) {
            case 10705:
                this->clear(true);
                break;
                
            default:
                break;
        }
    }
}

//遍历所有显示对象，判断鼠标点击的位置是否在显示对象的区域内，来判断点击的哪个对象
Sprite* GateMap::getTouchSprite(cocos2d::Vec2 pos)
{
    for(Map<int, Sprite*>::iterator it = this->sprites.begin(); it != this->sprites.end(); it++) {
        auto sprite=it->second;
        //全局坐标转换局部坐标
        Vec2 nsp = sprite->convertToNodeSpace(pos);
        Rect bb;
        bb.size = sprite->getContentSize();
        if (bb.containsPoint(nsp) && sprite->isVisible())
        {
            return sprite;
        }
    }
    return nullptr;
}

void GateMap::touchEvent(Ref *pSender, TouchEventType type)
{
    Widget* widget=static_cast<Widget*>(pSender);
    if(type==TouchEventType::BEGAN)
    {
        auto pos=widget->getTouchStartPos();
        auto sprite=this->getTouchSprite(pos);
        if (sprite) {
            sprite->stopAllActions();
            sprite->runAction(Sequence::create(ScaleTo::create(0.15,1.1),ScaleTo::create(0.15, 1),NULL) );
        }
    }
    if(type==TouchEventType::ENDED)
    {
        auto pos=widget->getTouchEndPos();
        auto sprite=this->getTouchSprite(pos);
        if (sprite) {
            XNode* xn=XNode::record(Value(sprite->getTag()));
            if (xn->getType()==1) {//进战役
                GateSelect* gateSelect=GateSelect::create(this, gateItem->gateid(),sprite->getTag());
                gateSelect->show(this);
            }else if(xn->getType()==0){//进关卡
                this->resetUI(xn->getGateID());
            }
        }
        
    }
}

void GateMap::onExit()
{
    this->sprites.clear();
    BaseUI::onExit();
}