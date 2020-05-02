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
    public String name;

    /**
     * The list of the operators in the attack
     */
    public ArrayList<Operator> operatorChildren;

    /**
     * The list of the subAttacks composing this attack
     */
    public ArrayList<Attack> attackChildren;

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

    @Override
    public String toString() {
        return "Attack{" +
                "name='" + name + '\'' +
                ", OperatorChildren=" + operatorChildren.size() +
                ", AttackChildren=" + attackChildren.size() +
                '}';
    }

	@Override
	public double similarity(IElement anotherElement) {
		if(equals(anotherElement))
			return 1;
		return 0;
	}
	
	public String getName() {
		return this.name;
	}

	@Override
	public String getText() {
		// TODO Auto-generated method stub
		return toString();
	}
	
	public ArrayList<Operator> getOperatorChildren(){
		return this.operatorChildren;
	}
	
	public ArrayList<Attack> getAttackChildren(){
		return this.attackChildren;
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
