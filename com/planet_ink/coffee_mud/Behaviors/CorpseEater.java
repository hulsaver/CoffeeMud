package com.planet_ink.coffee_mud.Behaviors;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class CorpseEater extends ActiveTicker
{
	public CorpseEater()
	{
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		minTicks=10; maxTicks=30; chance=25;
		tickReset();
	}

	public Behavior newInstance()
	{
		return new CorpseEater();
	}

	public void tick(Environmental ticking, int tickID)
	{
		super.tick(ticking,tickID);
		if((canAct(ticking,tickID))&&(ticking instanceof MOB))
		{
			MOB mob=(MOB)ticking;
			Room thisRoom=mob.location();
			if(thisRoom.numItems()==0) return;
			for(int i=0;i<thisRoom.numItems();i++)
			{
				Item I=thisRoom.fetchItem(i);
				if((I instanceof DeadBody)&&(Sense.canBeSeenBy(I,mob)||Sense.canSmell(mob)))
				{
					for(int i2=0;i2<thisRoom.numItems();i2++)
					{
						Item I2=thisRoom.fetchItem(i2);
						if(I2.location()==I)
							I2.setLocation(null);
					}
					thisRoom.show(mob,null,Affect.MSG_NOISYMOVEMENT,"<S-NAME> eat(s) "+I.name());
					I.destroyThis();
					return;
				}
			}

		}
	}
}