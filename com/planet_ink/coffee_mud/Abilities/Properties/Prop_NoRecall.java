package com.planet_ink.coffee_mud.Abilities.Properties;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prop_NoRecall extends Property
{
	public String ID() { return "Prop_NoRecall"; }
	public String name(){ return "Recall Neuralizing";}
	protected int canAffectCode(){return Ability.CAN_ROOMS|Ability.CAN_AREAS;}
	public Environmental newInstance(){	return new Prop_NoRecall();	}

	public String accountForYourself()
	{ return "No Recall Field";	}

	public boolean okAffect(Environmental myHost, Affect affect)
	{
		if(affect.sourceMinor()==affect.TYP_RECALL)
		{
			if((affect.source()!=null)&&(affect.source().location()!=null))
				affect.source().location().show(affect.source(),null,Affect.MSG_OK_ACTION,"<S-NAME> attempt(s) to recall, but the magic fizzles.");
			return false;
		}
		return super.okAffect(myHost,affect);
	}
}
