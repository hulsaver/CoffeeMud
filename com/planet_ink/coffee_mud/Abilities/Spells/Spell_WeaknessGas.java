package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


public class Spell_WeaknessGas extends Spell
{
	public String ID() { return "Spell_WeaknessGas"; }
	public String name(){return "Weakness to Gas";}
	public String displayText(){return "(Weakness to Gas)";}
	public int quality(){return MALICIOUS;};
	protected int canAffectCode(){return CAN_MOBS;}
	public Environmental newInstance(){return new Spell_WeaknessGas();}
	public int classificationCode(){return Ability.SPELL|Ability.DOMAIN_TRANSMUTATION;}

	public boolean okAffect(Environmental myHost, Affect affect)
	{
		if(!super.okAffect(myHost,affect))
			return false;

		if((affected==null)||(!(affected instanceof MOB)))
			return true;

		MOB mob=(MOB)affected;
		if((affect.amITarget(mob))&&(Util.bset(affect.targetCode(),Affect.MASK_HURT))
		   &&(affect.sourceMinor()==Affect.TYP_GAS))
		{
			int recovery=(int)Math.round(Util.mul((affect.targetCode()-Affect.MASK_HURT),1.5));
			affect.modify(affect.source(),affect.target(),affect.tool(),affect.sourceCode(),affect.sourceMessage(),affect.targetCode()+recovery,affect.targetMessage(),affect.othersCode(),affect.othersMessage());
		}
		return true;
	}

	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;
		if(canBeUninvoked())
			mob.tell("Your weakness to gasses is now gone.");

		super.unInvoke();

	}

	public void affectCharStats(MOB affectedMOB, CharStats affectedStats)
	{
		super.affectCharStats(affectedMOB,affectedStats);
		affectedStats.setStat(CharStats.SAVE_GAS,affectedStats.getStat(CharStats.SAVE_GAS)-100);
	}
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);
		if(success)
		{
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"A shimmering porous field appears around <T-NAMESELF>.":"^S<S-NAME> invoke(s) a porous field around <T-NAMESELF>.^?");
			if(mob.location().okAffect(mob,msg))
			{
				mob.location().send(mob,msg);
				success=maliciousAffect(mob,target,0,-1);
			}
		}
		else
			maliciousFizzle(mob,target,"<S-NAME> attempt(s) to invoke weakness to gas, but fail(s).");

		return success;
	}
}
