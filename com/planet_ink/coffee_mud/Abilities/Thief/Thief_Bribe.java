package com.planet_ink.coffee_mud.Abilities.Thief;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Thief_Bribe extends ThiefSkill
{
	public String ID() { return "Thief_Bribe"; }
	public String name(){ return "Bribe";}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int quality(){return Ability.OK_OTHERS;}
	private static final String[] triggerStrings = {"BRIBE"};
	public String[] triggerStrings(){return triggerStrings;}
	public Environmental newInstance(){	return new Thief_Bribe();}
	private MOB lastChecked=null;

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(commands.size()<1)
		{
			mob.tell("Bribe whom?");
			return false;
		}
		Vector V=new Vector();
		V.addElement(commands.elementAt(0));
		MOB target=this.getTarget(mob,V,givenTarget);
		if(target==null) return false;

		commands.removeElementAt(0);

		if(!target.mayIFight(mob))
		{
			mob.tell("You can't bribe "+target.name()+".");
			return false;
		}

		if(commands.size()<1)
		{
			mob.tell("Bribe "+target.charStats().himher()+" to do what?");
			return false;
		}

		if(((String)commands.elementAt(0)).toUpperCase().startsWith("FOL"))
		{
			mob.tell("You can't bribe someone to follow.");
			return false;
		}

		int oldProfficiency=profficiency();

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		int amountRequired=target.getMoney()+(int)(Math.round(100.0*(Util.div(25.0,mob.charStats().getStat(CharStats.CHARISMA))))*target.envStats().level());

		boolean success=profficiencyCheck(0,auto);

		if((!success)||(mob.getMoney()<amountRequired))
		{
			FullMsg msg=new FullMsg(mob,target,this,Affect.MSG_SPEAK,"^T<S-NAME> attempt(s) to bribe <T-NAMESELF> to '"+Util.combine(commands,0)+"', but no deal is reached.^?");
			if(mob.location().okAffect(mob,msg))
				mob.location().send(mob,msg);
			if(mob.getMoney()<amountRequired)
				mob.tell(target.charStats().HeShe()+" requires "+amountRequired+" coins to do this.");
			success=false;
		}
		else
		{
			FullMsg msg=new FullMsg(mob,target,this,Affect.MSG_SPEAK,"^T<S-NAME> bribe(s) <T-NAMESELF> to '"+Util.combine(commands,0)+"' for "+amountRequired+" coins.^?");
			mob.setMoney(mob.getMoney()-amountRequired);
			mob.recoverEnvStats();
			if(mob.location().okAffect(mob,msg))
			{
				mob.location().send(mob,msg);
				try
				{
					ExternalPlay.doCommand(target,commands);
				}
				catch(Exception e)
				{
					mob.tell(target.charStats().HeShe()+" takes your money and smiles, saying '"+e.getMessage()+"'.");
				}
			}
			target.setMoney(mob.getMoney()+amountRequired);
			target.recoverEnvStats();
		}
		if(target==lastChecked)
			setProfficiency(oldProfficiency);
		lastChecked=target;
		return success;
	}

}
