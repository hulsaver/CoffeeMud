package com.planet_ink.coffee_mud.Items;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class StdContainer extends StdItem implements Container
{
	public String ID(){	return "StdContainer";}
	protected boolean isLocked=false;
	protected boolean hasALock=false;
	protected boolean isOpen=true;
	protected boolean hasALid=false;
	protected int capacity=0;
	protected long containType=0;

	public StdContainer()
	{
		super();
		name="a container";
		displayText="a nondescript container sits here.";
		description="I'll bet you could put stuff in it!";
		capacity=25;
		baseGoldValue=10;
		recoverEnvStats();
		material=EnvResource.RESOURCE_COTTON;
	}

	public Environmental newInstance()
	{
		return new StdContainer();
	}
	
	public int capacity()
	{
		return capacity;
	}
	public void setCapacity(int newValue)
	{
		capacity=newValue;
	}

	public boolean okAffect(Environmental myHost, Affect affect)
	{
		if(!super.okAffect(myHost,affect))
			return false;
		if(affect.amITarget(this))
		{
			MOB mob=affect.source();
			switch(affect.targetMinor())
			{
			case Affect.TYP_PUT:
				if((affect.tool()!=null)
				&&(affect.tool() instanceof Item))
				{
					Item newitem=(Item)affect.tool();
					if(hasALid()&&(!isOpen()))
					{
						mob.tell(mob,null,name()+" is closed.");
						return false;
					}
					else
					if(newitem.amWearingAt(Item.WIELD))
					{
						mob.tell(mob,null,"You are wielding that!");
						return false;
					}
					else
					if(newitem.amWearingAt(Item.HELD))
					{
						mob.tell(mob,null,"You are holding that!");
						return false;
					}
					else
					if(!newitem.amWearingAt(Item.INVENTORY))
					{
						mob.tell(mob,null,"You are wearing that!");
						return false;
					}
					else
					if(capacity<=0)
					{
						mob.tell("You can't put anything in "+newitem.name()+"!");
						return false;
					}
					else
					{
						if(!canContain(newitem))
						{
							mob.tell("You can't put "+newitem.name()+" in "+name()+".");
							return false;
						}
						else
						if(newitem.envStats().weight()>capacity)
						{
							mob.tell(newitem.name()+" won't fit in "+name()+".");
							return false;
						}
						else
						if((recursiveWeight(this)+newitem.envStats().weight())>capacity)
						{
							mob.tell(name()+" is full.");
							return false;
						}
						if(!affect.source().isMine(this))
							if(!ExternalPlay.drop(affect.source(),newitem,true))
								return false;
						return true;
					}
				}
				break;
			case Affect.TYP_GET:
				if((affect.tool()!=null)
				&&(affect.tool() instanceof Item))
				{
					Item newitem=(Item)affect.tool();
					if(newitem.container()==this)
					{
						if((!Sense.canBeSeenBy(newitem,mob))
						&&((affect.sourceMajor()&Affect.MASK_GENERAL)==0))
						{
							mob.tell("You can't see that.");
							return false;
						}
						else
						if(hasALid()&&(!isOpen()))
						{
							mob.tell(mob,null,name()+" is closed.");
							return false;
						}
						else
						if(mob.envStats().level()<newitem.envStats().level()-10)
						{
							mob.tell(newitem.name()+" is too powerful to endure possessing it.");
							return false;
						}
						else
						if((recursiveWeight(newitem)>(mob.maxCarry()-mob.envStats().weight()))&&(!mob.isMine(this)))
						{
							mob.tell(newitem.name()+" is too heavy.");
							return false;
						}
						else
						return true;
					}
					else
					{
						mob.tell("You don't see that here.");
						return false;
					}
				}
				else
				if((recursiveWeight(this)>(mob.maxCarry()-mob.envStats().weight()))&&(!mob.isMine(this)))
				{
					mob.tell(name()+" is too heavy.");
					return false;
				}
				break;
			case Affect.TYP_CLOSE:
				if(isOpen)
				{
					if(!hasALid)
					{
						mob.tell("There is nothing to close on "+name()+".");
						return false;
					}
					else
						return true;
				}
				else
				{
					mob.tell(name()+" is already closed.");
					return false;
				}
				//break;
			case Affect.TYP_OPEN:
				if(!hasALid)
				{
					mob.tell("There is nothing to open on "+name()+".");
					return false;
				}
				if(isOpen)
				{
					mob.tell(name()+" is already open!");
					return false;
				}
				else
				{
					if(isLocked)
					{
						mob.tell(name()+" is locked.");
						return false;
					}
					else
						return true;
				}
				//break;
			case Affect.TYP_LOCK:
			case Affect.TYP_UNLOCK:
				if(!hasALid)
				{
					mob.tell("There is nothing to lock or unlock on "+name()+".");
					return false;
				}
				if(isOpen)
				{
					mob.tell(name()+" is open!");
					return false;
				}
				else
				if(!hasALock)
				{
					mob.tell("There is no lock!");
					return false;
				}
				else
				{
					if((!isLocked)&&(affect.targetMinor()==Affect.TYP_UNLOCK))
					{
						mob.tell(name()+" is not locked.");
						return false;
					}
					else
					if((isLocked)&&(affect.targetMinor()==Affect.TYP_LOCK))
					{
						mob.tell(name()+" is already locked.");
						return false;
					}
					else
					{
						for(int i=0;i<mob.inventorySize();i++)
						{
							Item item=mob.fetchInventory(i);
							if((item!=null)
							&&(item instanceof Key)
							&&(item.container()==null)
							&&(Sense.canBeSeenBy(item,mob)))
							{
								if(((Key)item).getKey().equals(keyName()))
									return true;
							}
						}
						mob.tell("You don't have the key.");
						return false;
					}
				}
				//break;
				default:
					break;
			}
		}
		return true;
	}

	public void affect(Environmental myHost, Affect affect)
	{
		if(affect.amITarget(this))
		{

			MOB mob=affect.source();
			switch(affect.targetMinor())
			{
			case Affect.TYP_GET:
				if((affect.tool()!=null)
				&&(affect.tool() instanceof Item))
				{
					Item newitem=(Item)affect.tool();
					if(newitem.container()==this)
						newitem.setContainer(null);
					remove();
				}
				else
				if(!mob.isMine(this))
				{
					this.setContainer(null);
					recursiveGetRoom(mob,this);
					mob.location().recoverRoomStats();
				}
				else
				{
					this.setContainer(null);
					remove();
					mob.location().recoverRoomStats();
				}
				break;
			case Affect.TYP_PUT:
				if((affect.tool()!=null)
				&&(affect.tool() instanceof Item))
				{
					Item newitem=(Item)affect.tool();
					newitem.setContainer(this);
					mob.location().recoverRoomStats();
				}
				break;
			case Affect.TYP_THROW:
				if((mob.isMine(this))
				&&(affect.tool()!=null)
				&&(affect.tool() instanceof Room))
				{
					setContainer(null);
					recursiveDropMOB(mob,(Room)affect.tool(),this,this instanceof DeadBody);
					mob.location().recoverRoomStats();
					if(mob.location()!=affect.tool())
						((Room)affect.tool()).recoverRoomStats();
				}
				break;
			case Affect.TYP_DROP:
				if(mob.isMine(this))
				{
					setContainer(null);
					recursiveDropMOB(mob,mob.location(),this,this instanceof DeadBody);
					mob.location().recoverRoomStats();
				}
				break;
			case Affect.TYP_EXAMINESOMETHING:
				if(Sense.canBeSeenBy(this,mob))
				{
					StringBuffer buf=new StringBuffer("");
					if((mob.getBitmap()&MOB.ATT_SYSOPMSGS)>0)
						buf.append(ID()+"\n\rRejuv :"+baseEnvStats().rejuv()+"\n\rUses  :"+usesRemaining()+"\n\rHeight: "+baseEnvStats().height()+"\n\rAbilty:"+baseEnvStats().ability()+"\n\rLevel :"+baseEnvStats().level()+"\n\rDeath : "+dispossessionTimeLeftString()+"\n\r"+description()+"'\n\rKey  : "+keyName()+"\n\rMisc  :'"+text());
					else
						buf.append(description()+"\n\r");
					if((isOpen)&&((capacity()>0)||(!(this instanceof Armor))))
					{
						buf.append(name()+" contains:\n\r");
						Vector newItems=new Vector();
						if((this instanceof Drink)&&(((Drink)this).liquidRemaining()>0))
						{
							GenLiquidResource l=new GenLiquidResource();
							int myResource=((Drink)this).liquidType();
							l.setMaterial(myResource);
							((Drink)l).setLiquidType(myResource);
							l.setBaseValue(EnvResource.RESOURCE_DATA[myResource&EnvResource.RESOURCE_MASK][1]);
							l.baseEnvStats().setWeight(1);
							String name=EnvResource.RESOURCE_DESCS[myResource&EnvResource.RESOURCE_MASK].toLowerCase();
							l.setName("some "+name);
							l.setDisplayText("some "+name+" sits here.");
							l.setDescription("");
							l.recoverEnvStats();
							newItems.addElement(l);
						}
							
						if(mob.isMine(this))
						{
							for(int i=0;i<mob.inventorySize();i++)
							{
								Item item=mob.fetchInventory(i);
								if((item!=null)&&(item.container()==this))
									newItems.addElement(item);
							}
							buf.append(ExternalPlay.niceLister(mob,newItems,true));
						}
						else
						{
							Room room=mob.location();
							if(room!=null)
							for(int i=0;i<room.numItems();i++)
							{
								Item item=room.fetchItem(i);
								if((item!=null)&&(item.container()==this))
									newItems.addElement(item);
							}
							buf.append(ExternalPlay.niceLister(mob,newItems,true));
						}
					}
					else
					if(hasALid())
						buf.append(name()+" is closed.");
					mob.tell(buf.toString());
				}
				else
					mob.tell("You can't see that!");
				break;
			case Affect.TYP_CLOSE:
				if((!hasALid)||(!isOpen)) return;
				isOpen=false;
				break;
			case Affect.TYP_OPEN:
				if((!hasALid)||(isOpen)||(isLocked)) return;
				isLocked=false;
				isOpen=true;
				break;
			case Affect.TYP_LOCK:
				if((!hasALid)||(!hasALock)||(isLocked)) return;
				isOpen=false;
				isLocked=true;
				break;
			case Affect.TYP_UNLOCK:
				if((!hasALid)||(!hasALock)||(isOpen)||(!isLocked))
					return;
				isLocked=false;
				break;
			default:
				break;
			}
		}
		super.affect(myHost,affect);
	}
	protected int recursiveWeight(Item thisContainer)
	{
		int weight=thisContainer.envStats().weight();
		if(owner()==null) return weight;
		if(owner() instanceof MOB)
			for(int i=0;i<((MOB)owner()).inventorySize();i++)
			{
				Item thisItem=((MOB)owner()).fetchInventory(i);
				if((thisItem!=null)&&(thisItem.container()==thisContainer))
					weight+=recursiveWeight(thisItem);
			}
		else
		if(owner() instanceof Room)
			for(int i=0;i<((Room)owner()).numItems();i++)
			{
				Item thisItem=((Room)owner()).fetchItem(i);
				if((thisItem!=null)&&(thisItem.container()==thisContainer))
					weight+=recursiveWeight(thisItem);
			}
				
		return weight;
	}

	public long containTypes(){return containType;}
	public void setContainTypes(long containTypes){containType=containTypes;}
	public boolean canContain(Environmental E)
	{ 
		if (!(E instanceof Item)) return false;
		if(containType==0) return true;
		for(int i=0;i<20;i++)
			if(Util.isSet((int)containType,i))
				switch(Util.pow(2,i))
				{
				case CONTAIN_LIQUID:
					if((((Item)E).material()&EnvResource.MATERIAL_LIQUID)>0)
						return true;
					break;
				case CONTAIN_COINS:
					if(E instanceof Coins)
						return true;
					break;
				case CONTAIN_SWORDS:
					if((E instanceof Weapon)
					&&(((Weapon)E).weaponClassification()==Weapon.CLASS_SWORD))
						return true;
					break;
				case CONTAIN_DAGGERS:
					if((E instanceof Weapon)
					&&(((Weapon)E).weaponClassification()==Weapon.CLASS_DAGGER))
						return true;
					break;
				case CONTAIN_OTHERWEAPONS:
					if((E instanceof Weapon)
					&&(((Weapon)E).weaponClassification()!=Weapon.CLASS_SWORD)
					&&(((Weapon)E).weaponClassification()!=Weapon.CLASS_DAGGER))
						return true;
					break;
				case CONTAIN_ONEHANDWEAPONS:
					if((E instanceof Weapon)
					&&(((Weapon)E).rawLogicalAnd()==false))
						return true;
					break;
				case CONTAIN_BODIES:
					if(E instanceof DeadBody)
						return true;
					break;
				case CONTAIN_READABLES:
					if((E instanceof Item)
					&&(((Item)E).isReadable()))
						return true;
					break;
				case CONTAIN_SCROLLS:
					if(E instanceof Scroll)
						return true;
					break;
				}
		return false;
	}
		

	

	public boolean isLocked(){return isLocked;}
	public boolean hasALock(){return hasALock;}
	public boolean isOpen(){return isOpen;}
	public boolean hasALid(){return hasALid;}
	public void setLidsNLocks(boolean newHasALid, boolean newIsOpen, boolean newHasALock, boolean newIsLocked)
	{
		hasALid=newHasALid;
		isOpen=newIsOpen;
		hasALock=newHasALock;
		isLocked=newIsLocked;
	}

	protected static void recursiveGetRoom(MOB mob, Item thisContainer)
	{

		// caller is responsible for recovering any env
		// stat changes!
		if(Sense.isHidden(thisContainer))
			thisContainer.baseEnvStats().setDisposition(thisContainer.baseEnvStats().disposition()&((int)EnvStats.ALLMASK-EnvStats.IS_HIDDEN));
		mob.location().delItem(thisContainer);
		thisContainer.remove();
		if(!mob.isMine(thisContainer))
			mob.addInventory(thisContainer);
		thisContainer.recoverEnvStats();
		boolean nothingDone=true;
		do
		{
			nothingDone=true;
			for(int i=0;i<mob.location().numItems();i++)
			{
				Item thisItem=mob.location().fetchItem(i);
				if((thisItem!=null)&&(thisItem.container()==thisContainer))
				{
					recursiveGetRoom(mob,thisItem);
					nothingDone=false;
					break;
				}
			}
		}while(!nothingDone);
	}
	public void setMiscText(String newMiscText)
	{
		miscText=newMiscText;
		if(!(this instanceof GenContainer))
			setKeyName(miscText);
	}
	public String keyName()
	{
		return miscText;
	}
	public void setKeyName(String newKeyName)
	{
		miscText=newKeyName;
	}
	protected static void recursiveDropMOB(MOB mob, 
										   Room room, 
										   Item thisContainer, 
										   boolean bodyFlag)
	{
		// caller is responsible for recovering any env
		// stat changes!

		if(Sense.isHidden(thisContainer))
			thisContainer.baseEnvStats().setDisposition(thisContainer.baseEnvStats().disposition()&((int)EnvStats.ALLMASK-EnvStats.IS_HIDDEN));
		mob.delInventory(thisContainer);
		thisContainer.remove();
		if(!bodyFlag) bodyFlag=(thisContainer instanceof DeadBody);
		if(bodyFlag)
		{
			room.addItem(thisContainer);
			thisContainer.setDispossessionTime(0);
		}
		else
			room.addItemRefuse(thisContainer,Item.REFUSE_PLAYER_DROP);
		thisContainer.recoverEnvStats();
		boolean nothingDone=true;
		do
		{
			nothingDone=true;
			for(int i=0;i<mob.inventorySize();i++)
			{
				Item thisItem=mob.fetchInventory(i);
				if((thisItem!=null)&&(thisItem.container()==thisContainer))
				{
					recursiveDropMOB(mob,room,thisItem,bodyFlag);
					nothingDone=false;
					break;
				}
			}
		}while(!nothingDone);
	}
	
	protected void reallyGetContents(Item container, Environmental own, Vector V)
	{
		if(container==null) return;
		if(own instanceof MOB)
		{
			for(int i=0;i<((MOB)own).inventorySize();i++)
			{
				Item I=((MOB)own).fetchInventory(i);
				if((I.container()==container)
				&&(!V.contains(I)))
				{
					V.addElement(I);
					reallyGetContents(I,own,V);
				}
			}
		}
		else
		if(own instanceof Room)
		{
			for(int i=0;i<((Room)own).numItems();i++)
			{
				Item I=((Room)own).fetchItem(i);
				if((I.container()==container)
				&&(!V.contains(I)))
				{
					V.addElement(I);
					reallyGetContents(I,own,V);
				}
			}
		}
	}
	
	public Vector getContents()
	{
		Vector V=new Vector();
		if(owner()!=null)
			reallyGetContents(this,owner(),V);
		return V;
	}
}
