package org.but4reuse.adapters.attackTree;

import java.util.ArrayList;

import org.but4reuse.adapters.IElement;
import org.but4reuse.adapters.impl.AbstractElement;

public class OperatorElement extends AbstractElement {
	
	private ArrayList<IElement> childs;
	private IElement parent;
	
	private TypeOperator type;
	
	public OperatorElement(TypeOperator type, IElement parent, ArrayList<IElement> childs) {
		this.type = type;
		this.parent = parent;
		this.childs = childs;
	}

	@Override
	public double similarity(IElement anotherElement) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getText() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public TypeOperator getType() {
		return this.type;
	}
	
	public IElement getParent() {
		return this.parent;
	}
	
	public ArrayList<IElement> getChilds(){
		return this.childs;
	}

}
