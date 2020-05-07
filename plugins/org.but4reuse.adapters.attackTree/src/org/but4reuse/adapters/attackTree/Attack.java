package org.but4reuse.adapters.attackTree;

import java.util.ArrayList;
import java.util.List;

import org.but4reuse.adapters.IElement;
import org.but4reuse.adapters.impl.AbstractElement;

/**
 * This class an attack tree's attack element.
 * @author Simon NAVEAU
 * @author Benoît MARTEL
 * @version 1
 */
public class Attack extends AbstractElement{

    /**
     * The name of the attack
     */
    private String name;
    
    /**
     * The attack father
     */
    private AbstractElement father;
    

    /**
     * The list of the operators children
     */
    private ArrayList<Operator> operatorChildren;

    /**
     * The list of the sub attacks composing this attack
     */
    private ArrayList<Attack> attackChildren;

    
    /**
     * Constructor of the Attack class.
     * 
     * @param name the attack name
     * @param OperatorChildren the attack sub operations
     * @param AttackChildren the attack sub attacks
     * @throws Exception
     */
    public Attack(String name, ArrayList<Operator> operatorChildren, ArrayList<Attack> attackChildren) throws Exception {
    	this.name = name;
        this.operatorChildren = operatorChildren;
        this.attackChildren = attackChildren;
    }
    
    /**
     * Constructor of the Attack class.
     * 
     */
    public Attack() {
        this.operatorChildren = new ArrayList<Operator>();
        this.attackChildren = new ArrayList<Attack>();
    }
    
    /**
     * Constructor of the Attack class.
     * 
     * @param name the attack name
     */
    public Attack(String name) {
        this.name = name;
        this.operatorChildren = new ArrayList<Operator>();
        this.attackChildren = new ArrayList<Attack>();
    }
    

    /**
     * Similarity method.
     * Compare if the element passed in parameter is similar to this attack.
     * 
 	 * @param anotherElement element to compare the attack
     */
	@Override
	public double similarity(IElement anotherElement) {
		if(equals(anotherElement))
			return 1;
		return 0;
	}
	
	@Override
	public String getText() {
		return name;
	}
	
	/**
	 * Get the attack's father.
	 * 
	 * @return the attack's father
	 */
	public AbstractElement getFather() {
		return father;
	}
	
	/**
	 * Get the attack's name.
	 * 
	 * @return the attack's name
	 */
	public String getName() {
		return this.name;
	}
	
	/**
	 * Set the attack's name.
	 * 
	 * @param name the new name of the attack
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Add a child operator.
	 * 
	 * @param operator the child operator to add
	 */
	public void addChildOperator(Operator operator) {
		this.operatorChildren.add(operator);
	}
	
	/**
	 * Add an child attack.
	 * 
	 * @param attack the child attack to add
	 */
	public void addChildAttack(Attack attack) {
		this.attackChildren.add(attack);
	}
	
	/**
	 * Get operator children.
	 * 
	 * @return the operator children
	 */
	public ArrayList<Operator> getOperatorChildren(){
		return this.operatorChildren;
	}
	
	/**
	 * Get attack children.
	 * 
	 * @return the attack children
	 */
	public ArrayList<Attack> getAttackChildren(){
		return this.attackChildren;
	}
	
	/**
	 * Set operator children.
	 * @param operators the new list of operator children
	 */
	public void setOperatorChildren(ArrayList<Operator> operators){
		this.operatorChildren = operators;
	}
	
	/**
	 * Set operator attack.
	 * 
	 * @param attacks the new list of attack children
	 */
	public void setAttackChildren(ArrayList<Attack> attacks){
		this.attackChildren = attacks;
	}
	
	/**
	 * Set father.
	 * 
	 * @param father the new father
	 */
	public void setFather(AbstractElement father) {
		this.father = father;
	}
	
	/**
	 * Get all elements located under this attack.
	 * 
	 * @return list of elements located under this attack
	 */
	public List<IElement> getAllSubElements(){
		ArrayList<IElement> elements = new ArrayList<>();
		elements = getAllSubElementsRec(this,elements);
		return elements;
		
	}
	
	/**
	 * Get the root of this attack.
	 * 
	 * @return the root attack.
	 * @throws Exception throws an exception if the object is invalid
	 */
	public Attack getRoot() throws Exception {
		
		AbstractElement tmp = this;
    	AbstractElement root = null;
    	
    	// Tant que l'objet que l'on récupère possède un père alors on continu de remonter.
    	// Lorsqu'on a plus de père alors on a trouver la racine de notre attaque.
    	while(tmp != null) {
    		root = tmp;
    		if (tmp instanceof Attack)
    			tmp = ((Attack)tmp).getFather();
    		else if (tmp instanceof Operator)
    			tmp = ((Operator)tmp).getFather();
    		else
    			throw new Exception("Invalid object");	
    	}
    	
    	// On vérifie que la racine est pas nulle et que c'est bien une attaque
    	// Si pas le cas alors on renvoi null.
    	if(root != null && root instanceof Attack)
    		return (Attack)root;
    	else
    		return null;
	}
	
	
	/**
	 * Recursive method to get all elements located under element passed in parameter.
	 * 
	 * @param element the element to get all elements located under this one
	 * @param elements the list of all elements found
	 * 
	 * @return the list of all elements found
	 */
	 public ArrayList<IElement> getAllSubElementsRec(AbstractElement element, ArrayList<IElement> elements) {
		 elements.add(element);
		 if (element.getClass() == Attack.class) {
			 for(Operator o : ((Attack)element).getOperatorChildren()) {
				 getAllSubElementsRec(o,elements);
			 }
			 for(Attack a : ((Attack)element).getAttackChildren()) {
				getAllSubElementsRec(a,elements);
			 }
		 }
		 else {
			 for(Attack a : ((Operator)element).getChildren()) {
				 getAllSubElementsRec(a,elements);
			 }   
		 }
		 return elements;

	 }
	
	@Override
	public String toString() {
	    return "Attack{" +
	            "name='" + name + '\'' +
	            ", OperatorChildren=" + operatorChildren.size() +
	            ", AttackChildren=" + attackChildren.size() +
	            ", father= "+father +'}';
	}
	    
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof Attack)
			if(((Attack) obj).getName().equals(this.name))
				return true;
		return false;
	}
	
}
