package org.but4reuse.adapters.attackTree;

import java.util.ArrayList;
import java.util.List;

import org.but4reuse.adapters.IElement;
import org.but4reuse.adapters.impl.AbstractElement;

/**
 * This class an attack tree's attack element
 * @author NAVEAU Simon
 */
public class Attack extends AbstractElement{

    /**
     * The name of the attack
     */
    private String name;
    
    private AbstractElement father;

    /**
     * The list of the operators in the attack
     */
    private ArrayList<Operator> operatorChildren;

    /**
     * The list of the subAttacks composing this attack
     */
    private ArrayList<Attack> attackChildren;

    /**
     * Constructor of the class
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
    
    public Attack(String name) throws Exception {
        this.name = name;
        this.operatorChildren = new ArrayList<Operator>();
        this.attackChildren = new ArrayList<Attack>();
    }


    @Override
    public String toString() {
        return "Attack{" +
                "name='" + name + '\'' +
                ", OperatorChildren=" + operatorChildren.size() +
                ", AttackChildren=" + attackChildren.size() +
                ", father= "+father +
                '}';
    }

	@Override
	public double similarity(IElement anotherElement) {
		if(equals(anotherElement))
			return 1;
		return 0;
	}
	
	public AbstractElement getFather() {
		return father;
	}
	
	public String getName() {
		return this.name;
	}

	@Override
	public String getText() {
		// TODO Auto-generated method stub
		return toString();
	}
	
	public void addOperatorChildren(Operator operator) {
		this.operatorChildren.add(operator);
	}
	
	public void addAttackChildren(Attack attack) {
		this.attackChildren.add(attack);
	}
	
	public ArrayList<Operator> getOperatorChildren(){
		return this.operatorChildren;
	}
	
	public ArrayList<Attack> getAttackChildren(){
		return this.attackChildren;
	}
	
	public void setFather(AbstractElement e) {
		this.father = e;
	}
	
	public List<IElement> getAllSubElements(){
		ArrayList<IElement> elements = new ArrayList<>();
		elements = printFromModel(this,elements);
		System.out.println("nb total "+elements.size());
		return elements;
		
	}
	
	 public ArrayList<IElement> printFromModel(AbstractElement element, ArrayList<IElement> elements) {
		 elements.add(element);
		 if (element.getClass() == Attack.class) {
			 for(Operator o : ((Attack)element).getOperatorChildren()) {
				 printFromModel(o,elements);
			 }
			 for(Attack a : ((Attack)element).getAttackChildren()) {
				printFromModel(a,elements);
			 }
		 }
		 else {
			 for(Attack a : ((Operator)element).getChildren()) {
				 printFromModel(a,elements);
			 }   
		 }
		 return elements;

	 }
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof Attack)
			if(((Attack) obj).getName().equals(this.name))
				return true;
		return false;
	}
	
}
