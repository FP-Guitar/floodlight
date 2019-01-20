package net.sdnlab.ex4.task43;

import java.util.ArrayList;

import org.projectfloodlight.openflow.protocol.OFFactory;
import org.projectfloodlight.openflow.protocol.action.OFAction;
import org.projectfloodlight.openflow.protocol.match.Match;
import org.projectfloodlight.openflow.types.DatapathId;

public class FlowMod {
	private DatapathId switchId;
	private Match match;
	private ArrayList<OFAction> actionList;
	private OFFactory usedFactory;

	


	public FlowMod(DatapathId switchId, Match match, ArrayList<OFAction> actionList, OFFactory usedFactory) {
		super();
		this.switchId = switchId;
		this.match = match;
		this.actionList = actionList;
		this.usedFactory = usedFactory;
	}

	public DatapathId getSwitchId() {
		return switchId;
	}



	public Match getMatch() {
		return match;
	}



	public ArrayList<OFAction> getActionList() {
		return actionList;
	}




	public OFFactory getUsedFactory() {
		return usedFactory;
	}

}
