package org.but4reuse.adapters.attackTree;

import org.but4reuse.adapters.IElement;
import org.but4reuse.adapters.impl.AbstractElement;

public class AttackElement extends AbstractElement {
	
	private OperatorElement child;
	private OperatorElement parent;
	private String name;
	
	public AttackElement(String name) {
		this.name = name;
	}
	
	public AttackElement(String name, OperatorElement child, OperatorElement parent) {
		this.name = name;
		this.child= child;
		this.parent = parent;
	}
	

	@Override
	public double similarity(IElement anotherElement) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getText() {
		return name;
	}
	
	public OperatorElement getChild() {
		return this.child;
	}
	
	public OperatorElement getParent() {
		return this.parent;
	}
	
	public void setChild(OperatorElement child) {
		this.child = child;
	}
	
	public void setParent(OperatorElement parent) {
		this.parent = parent;
	}

	
}
