package org.but4reuse.adapters.attackTree;

import java.util.ArrayList;

import org.but4reuse.adapters.IElement;
import org.but4reuse.adapters.impl.AbstractElement;


/**
 * Operator Class.
 * This class an attack tree's operator.
 * 
 * @author Simon NAVEAU
 * @author Benoît MARTEL
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
    
    /**
     * The attack father
     */
    private Attack father;
    
    /**
     * Constructor of the class.
     * 
     * @param type the operator type
     * @param children the operation's operands
     * @throws Exception
     */
    public Operator(OperatorType type, ArrayList<Attack> children){
    	this.type = type;
        this.children = children;
    }
    
    /**
     * Constructor of the class.
     * 
     * @param type the operator type
     */
    public Operator(OperatorType type){
    	this.type = type;
        this.children = new ArrayList<Attack>();
    }
    
    /**
     * Add child attack.
     * 
     * @param attack the attack to add
     */
    public void addChildAttack(Attack attack) {
    	this.children.add(attack);
    }
    
    /**
     * Set father.
     * 
     * @param father the new father
     */
    public void setFather(Attack father) {
    	this.father = father;
    }
    
    /**
     * Get father.
     * 
     * @return the father's attack.
     */
    public Attack getFather() {
    	return father;
    }
    
    /**
     * Get operator type.
     * 
     * @return the operator type
     */
    public OperatorType getOperatorType() {
    	return this.type;
    }
    
    /**
     * Get the attack children.
     * 
     * @return the attack children
     */
	public ArrayList<Attack> getChildren() {
		return children;
	}
    
    /**
     * Similarity method.
     * Compare if the element passed in parameter is similar to this operator.
     * 
 	 * @param anotherElement element to compare the operator
     */
	@Override
	public double similarity(IElement anotherElement) {
		if(anotherElement instanceof Operator) {
			Operator anOperator = (Operator)anotherElement;
			
			if(anOperator.getFather().getName().equals(this.father.getName()))
				if(anOperator.getOperatorType() == type)
					return 1;
			
		}
		return 0;
	}
	
	/**
	 * {@inheritDoc}
	 */
    @Override
    public String toString() {
        return "Operator{" +
                "type=" + type +
                ", children=" + children.size() +
                ", father= "+father +
                '}';
    }

    /**
     * {@inheritDoc}
     */
	@Override
	public String getText() {
		// TODO Auto-generated method stub
		return null;
	}
		
}