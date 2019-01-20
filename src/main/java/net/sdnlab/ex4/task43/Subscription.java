package net.sdnlab.ex4.task43;

import org.projectfloodlight.openflow.types.IPv4Address;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Subscription {
	public static enum OPERATOR {
		GREATER_THAN,
		LESS_EQUAL;
		
		public static OPERATOR fromString(String operator ) {
			if( operator.equalsIgnoreCase("gt")) {
				return OPERATOR.GREATER_THAN;
			} else if( operator.equalsIgnoreCase("le")) {
				return OPERATOR.LESS_EQUAL;
			} else {
				throw new IllegalArgumentException("OPERATION not known, only [gt|le] allowed");
			}
		}
		
	}
	
	public static enum TYPE {
		POWER,
		ENERGY;
		
		public static TYPE fromString(String filter_type ) {
			if( filter_type.equalsIgnoreCase("POWER") ) {
				return TYPE.POWER;
			} else if( filter_type.equals("1") ) {
				return TYPE.POWER;
			} else if( filter_type.equalsIgnoreCase("ENERGY") ) {
				return TYPE.ENERGY;
			} else if ( filter_type.equals("0")) {
				return TYPE.ENERGY;
			}else {
				throw new IllegalArgumentException("TYPEnot known, only [1|POWER|0|ENERGY] le allowed");
			}
		}
		
		static public String toIpCode(TYPE tp ) {
			if( tp == POWER ) {
				return "1";
			} else {
				return "0";
			}
		}
	}


	private IPv4Address destinationAddress = IPv4Address.of("127.0.0.1");
	// the transportlayerport
	private int port = 0;
	private String name = "";
	private OPERATOR operator = OPERATOR.GREATER_THAN;
	private int referenceValue = 0;
	private TYPE filterType = TYPE.ENERGY;
	

	public Subscription(String name, IPv4Address destinationAddress, int port,  TYPE filterType , OPERATOR operator, int referenceValue) {
		super();
		this.name = name;
		this.destinationAddress = destinationAddress;
		this.port = port;
		this.operator = operator;
		this.referenceValue = referenceValue;
		this.filterType = filterType;
	}


	
	public Subscription() {
		// TODO Auto-generated constructor stub
	}



	public IPv4Address getDestinationAddress() {
		return destinationAddress;
	}



	public int getPort() {
		return port;
	}


	public String getName() {
		return name;
	}


	public OPERATOR getOperator() {
		return operator;
	}


	public int getReferenceValue() {
		return referenceValue;
	}

	public TYPE getFilterType() {
		return filterType;
	}


}
