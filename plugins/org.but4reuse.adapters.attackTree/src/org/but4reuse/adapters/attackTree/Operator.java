package org.but4reuse.adapters.attackTree;

import java.util.ArrayList;

import org.but4reuse.adapters.IElement;
import org.but4reuse.adapters.impl.AbstractElement;


/**
 * This class an attack tree's operator
 * @author NAVEAU Simon
 */
public class Operator extends AbstractElement {

    /**
     * The type of the operator
     */
    public OperatorType type;

    /**
     * The list of the attack in the operation
     */
    public ArrayList<Attack> children;
    
    private AbstractElement father;
   

    /**
     * Constructor of the class
     * @param type the operator type
     * @param children the operation's operands
     * @throws Exception
     */
    public Operator(OperatorType type, ArrayList<Attack> children){
        this.type = type;
        this.children = children;
    }
    
    public void addAttackChildren(Attack attack) {
    	this.children.add(attack);
    }
    
    public void setFather(AbstractElement e) {
    	father = e;
    }
    
    public AbstractElement getFather() {
    	return father;
    }
    
    @Override
    public String toString() {
        return "Operator{" +
                "type=" + type +
                ", children=" + children.size() +
                '}';
    }

	@Override
	public double similarity(IElement anotherElement) {
		if(anotherElement instanceof Operator) {
			Operator anOperator = (Operator)anotherElement;

			if(anOperator.getChildren().size() == this.children.size()) {
				int cpt = 0;
				for(Attack attack : this.children) {
					for(Attack other : anOperator.getChildren()) {
						if(attack.equals(other))
							cpt++;
					}
				}
				if(cpt == this.children.size())
					return 1;
				
			}
		}
		return 0;
	}

	@Override
	public String getText() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public ArrayList<Attack> getChildren() {
		return children;
	}
	
	
}
