package com.planet_ink.coffee_mud.Abilities.Prayers;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prayer_MassHarm extends Prayer
{
	public String ID() { return "Prayer_MassHarm"; }
	public String name(){ return "Mass Harm";}
	public int quality(){ return MALICIOUS;}
	public int holyQuality(){ return HOLY_EVIL;}
	public Environmental newInstance(){	return new Prayer_MassHarm();}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		Hashtable h=ExternalPlay.properTargets(this,mob,auto);
		if(h==null) return false;

		boolean success=profficiencyCheck(0,auto);
		int numEnemies=h.size();
		for(Enumeration e=h.elements();e.hasMoreElements();)
		{
			MOB target=(MOB)e.nextElement();
			if(target!=mob)
			{
				if(success)
				{
					// it worked, so build a copy of this ability,
					// and add it to the affects list of the
					// affected MOB.  Then tell everyone else
					// what happened.
					FullMsg msg=new FullMsg(mob,target,this,affectType(auto)|Affect.MASK_MALICIOUS,auto?"<T-NAME> become(s) surrounded by a dark cloud.":"^S<S-NAME> sweep(s) <S-HIS-HER> hands over <T-NAMESELF>, "+prayingWord(mob)+".^?");
					if(mob.location().okAffect(mob,msg))
					{
						mob.location().send(mob,msg);
						int harming=Dice.roll(4,adjustedLevel(mob)/numEnemies,numEnemies);
						ExternalPlay.postDamage(mob,target,this,harming,Affect.MASK_GENERAL|Affect.TYP_UNDEAD,Weapon.TYPE_BURSTING,"The unholy spell <DAMAGE> <T-NAME>!");
					}
				}
				else
					maliciousFizzle(mob,target,"<S-NAME> sweep(s) <S-HIS-HER> hands over <T-NAMESELF>, "+prayingWord(mob)+", but "+hisHerDiety(mob)+" does not heed.");
			}
		}


		// return whether it worked
		return success;
	}
}
