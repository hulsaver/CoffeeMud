package com.planet_ink.coffee_mud.Abilities.Druid;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;


import java.util.*;

/*
   Copyright 2000-2014 Bo Zimmerman

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

	   http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/

@SuppressWarnings("rawtypes")
public class Chant_Earthpocket extends Chant
{
	@Override public String ID() { return "Chant_Earthpocket"; }
	@Override public String name(){return "Earthpocket";}
	@Override public String displayText(){return "(Earthpocket: "+(super.tickDown/CMProps.getIntVar(CMProps.Int.TICKSPERMUDDAY))+")";}
	@Override public int abstractQuality(){ return Ability.QUALITY_OK_SELF;}
	@Override protected int canAffectCode(){return CAN_MOBS;}
	@Override public int classificationCode(){return Ability.ACODE_CHANT|Ability.DOMAIN_ROCKCONTROL;}
	private Container pocket=null;

	@Override
	public void unInvoke()
	{
		// undo the affects of this spell
		if(!(affected instanceof MOB))
			return;
		final MOB mob=(MOB)affected;
		super.unInvoke();

		if(canBeUninvoked())
		{
			mob.tell(_("Your earthpocket fades away, dumping its contents into your inventory!"));
			final List<Item> V=pocket.getContents();
			for(int v=0;v<V.size();v++)
			{
				V.get(v).setContainer(null);
				mob.moveItemTo(V.get(v));
			}
			pocket.destroy();
			pocket=null;
		}
	}

	@Override
	public boolean okMessage(Environmental host, CMMsg msg)
	{
		if((msg.source()!=affected)
		&&((msg.target()==pocket)||(msg.tool()==pocket))
		&&(CMath.bset(msg.sourceMajor(),CMMsg.MASK_HANDS)
		   ||CMath.bset(msg.sourceMajor(),CMMsg.MASK_MOVE)
		   ||CMath.bset(msg.sourceMajor(),CMMsg.MASK_DELICATE)
		   ||CMath.bset(msg.sourceMajor(),CMMsg.MASK_MOUTH)))
		{
			msg.source().tell(_("The dark pocket draws away from you, preventing your action."));
			return false;
		}
		return true;
	}

	@Override
	public void executeMsg(Environmental host, CMMsg msg)
	{
		super.executeMsg(host,msg);
		movePocket();
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		movePocket();
		return true;
	}

	public void movePocket()
	{
		if((affected instanceof MOB)&&(pocket!=null))
		{
			if(pocket.owner() instanceof MOB)
				pocket.removeFromOwnerContainer();
			else
			if(pocket.owner() instanceof Room)
			{
				if(((MOB)affected).location()!=null)
				{
					if(((MOB)affected).location().domainType()==Room.DOMAIN_INDOORS_CAVE)
					{
						if(CMath.bset(pocket.basePhyStats().disposition(),PhyStats.IS_NOT_SEEN))
						{
							pocket.basePhyStats().setDisposition(pocket.basePhyStats().disposition()-PhyStats.IS_NOT_SEEN);
							pocket.recoverPhyStats();
						}
						((MOB)affected).location().moveItemTo(pocket);
					}
					else
					if(!CMath.bset(pocket.basePhyStats().disposition(),PhyStats.IS_NOT_SEEN))
					{
						pocket.basePhyStats().setDisposition(pocket.basePhyStats().disposition()|PhyStats.IS_NOT_SEEN);
						pocket.recoverPhyStats();
					}
				}
				else
				if(!CMath.bset(pocket.basePhyStats().disposition(),PhyStats.IS_NOT_SEEN))
				{
					pocket.basePhyStats().setDisposition(pocket.basePhyStats().disposition()|PhyStats.IS_NOT_SEEN);
					pocket.recoverPhyStats();
				}
			}
		}
	}


	@Override
	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		MOB target=mob;
		if((auto)&&(givenTarget!=null)&&(givenTarget instanceof MOB))
			target=(MOB)givenTarget;

		if(mob.location().domainType()!=Room.DOMAIN_INDOORS_CAVE)
		{
			mob.tell(_("The earthpocket can only be summoned or seen in a cave."));
			return false;
		}

		if(target.fetchEffect(ID())!=null)
		{
			mob.tell(target,null,null,_("<S-NAME> <S-IS-ARE> already connected with an earthpocket."));
			return false;
		}

		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			invoker=mob;
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"":_("^S<S-NAME> chant(s) for a connection with a mystical dimension!^?"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				pocket=(Container)CMClass.getItem("GenContainer");
				pocket.setCapacity(Integer.MAX_VALUE);
				pocket.basePhyStats().setSensesMask(PhyStats.SENSE_ITEMNOTGET);
				pocket.basePhyStats().setWeight(0);
				pocket.setMaterial(RawMaterial.RESOURCE_NOTHING);
				pocket.setName("an earthpocket");
				pocket.setDisplayText("an empty pitch-black pocket is in the wall here.");
				pocket.setDescription("It looks like an endless black hole in the wall.  Very mystical.");
				pocket.recoverPhyStats();
				target.location().addItem(pocket);
				beneficialAffect(mob,target,asLevel,CMProps.getIntVar(CMProps.Int.TICKSPERMUDMONTH));
				target.location().show(target,null,CMMsg.MSG_OK_VISUAL,_("A dark pocket of energy appears in a nearby wall."));
			}
		}
		else
			return beneficialWordsFizzle(mob,target,_("<S-NAME> chant(s), but nothing more happens."));

		// return whether it worked
		return success;
	}
}
