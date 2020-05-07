package org.but4reuse.adapters.attackTree;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.but4reuse.adapters.IAdapter;
import org.but4reuse.adapters.IElement;
import org.but4reuse.adapters.impl.AbstractElement;
import org.but4reuse.utils.files.FileUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


/**
 * Attack Tree Adapter
 * 
 * @author MARTEL Benoit, NAVEAU Simon, TRAVAILLE Lo�c
 */
public class AttackTreeAdapter implements IAdapter {
	

	/**
	 * is a xml file ?
	 */
	@Override
	public boolean isAdaptable(URI uri, IProgressMonitor monitor) {
		File file = FileUtils.getFile(uri);
		if (file != null && file.exists() && !file.isDirectory()
				&& FileUtils.isExtension(file, "xml") ) {
			return true;
		}
		return false;
	}

	/**
	 * Read the graph file to adapt
	 */
	@Override
	public List<IElement> adapt(URI uri, IProgressMonitor monitor) {
		List<IElement> elements = new ArrayList<IElement>();
		File file = FileUtils.getFile(uri);

		// Read the graph
		try {
			
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder;
			builder = factory.newDocumentBuilder();
			
			Document document = builder.parse(file);
			Element root = document.getDocumentElement();
			
			// Convert the xml to an attack
			Attack attack = convertTree(root);
			
			// Get all elements in this attack
			elements = attack.getAllSubElements();
			
			
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		catch (SAXException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		return elements;
	}

	@Override
	public void construct(URI uri, List<IElement> elements, IProgressMonitor monitor) {
		
		// On r�cup�re la liste de toutes les attaques afin de cr�er un seul et m�me arbre.
		ArrayList<Attack> attacks = new ArrayList<>();
		
		for(IElement element : elements)
			if (element instanceof Attack)
				attacks.add((Attack) element);
		
		System.out.println("======== Liste des attaques � ajouter =============");
		for(Attack a : attacks) 
			System.out.println(a);
		System.out.println("======== Fin de la liste des attaques � ajouter ============\n");
		
		
		try {
			
			// Notre attaque de sortie (celle qui contiendra tous les �lements dans un et m�me seul arbre)
			Attack attack = null;
			
			// On commence par r�cup�rer dans la liste la premi�re attaque qui ne poss�de pas de p�re
			// (on en d�duit que c'est notre attaque root)
			for(Attack a : attacks) {
				if(a.getFather() == null) {
					attack = new Attack(a.getName());
					attacks.remove(a);
					break;
				}
					
				
			}
			
			// Si jamais on ne trouve pas d'attaque avec un p�re qui est nulle alors on prend par d�faut la 
			// pr�mi�re attaque de la liste d'�lements.
			if (attack == null) {
				attack = new Attack(attacks.get(0).getName());
				attacks.remove(0);
			}
			
			// A ce moment la notre attaque de sortie contient un seul attribut : son nom. Rien d'autre.
				
			// On vient it�rer sur le reste de la liste d'�l�ments pour ajouter l'une apr�s l'autre les attaques qui ensemble constitueront 
			// l'arbre.
			for(Attack a : attacks)
				addAttack(attack, a);
			
			
			// On vient chercher ensuite la racine de l'attaque.
			attack = attack.getRoot();
			
			// Puis on construit le fichier xml a partir de l'objet 'attack'

			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder;
			builder = factory.newDocumentBuilder();
			Document document = builder.newDocument();
	
	        //Creating the root element
	        Element root = document.createElement("attack");
	        root.setAttribute("name", attack.getName());
	        document.appendChild(root);
	        
	        for(Attack a : attack.getAttackChildren()) {
	        	  Element at = document.createElement("attack");
		            at.setAttribute("name", a.getName());
		            root.appendChild(at);
		            for(Operator o : attack.getOperatorChildren()) {
		            	addChildToXML(o,at,document);
		            }
	            	for(Attack a2 : attack.getAttackChildren()) {
		            	addChildToXML(a2,at,document);
		            }
		         
	        }
	        
	        for(Operator o : attack.getOperatorChildren()) {
	        	Element operator = document.createElement("operator");
	        	operator.setAttribute("type", "" + o.type);
	        	root.appendChild(operator);
				for(Attack op : o.getChildren())
					addChildToXML(op,operator,document);     	      
	        }
	        
	    	// Use the given file or use a default name if a folder was given
			if (uri.toString().endsWith("/")) {
				uri = new URI(uri.toString() + "graph.xml");
				System.out.println("add path");
			}
			// Create file if it does not exist
			File file = FileUtils.getFile(uri);
			FileUtils.createFile(file);
	
	        Transformer transformer = TransformerFactory.newInstance().newTransformer();
	        DOMSource source = new DOMSource(document);
	        StreamResult sortie = new StreamResult(file);
	        transformer.setOutputProperty(OutputKeys.VERSION, "1.0");
	        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
	        transformer.setOutputProperty(OutputKeys.STANDALONE, "yes");
	        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
	        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
	        transformer.transform(source, sortie);
	        
		}
		catch(TransformerException e) {
			e.printStackTrace();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		

	}
	
	
	 /**
     * Add the children of an element as child to him recursively to create an attack Tree as XML
     * 
     * @param element the element to add
     * @param father the father of the element
     * @param doc the XML document builder
     */
    private void addChildToXML(AbstractElement element, Element father, Document doc) {
        if (element.getClass() == Attack.class) {
            Element attack = doc.createElement("attack");
            attack.setAttribute("name", ((Attack) element).getName());
            father.appendChild(attack);
            for(Operator o : ((Attack) element).getOperatorChildren()) {
            	addChildToXML(o,attack,doc);
            }
            for(Attack a : ((Attack) element).getAttackChildren()) {
            	addChildToXML(a,attack,doc);
            }
          
        } else if(element instanceof Operator) {
            Element operator = doc.createElement("operator");
            operator.setAttribute("type", "" + ((Operator) element).type);
            father.appendChild(operator);
            for(Attack a : ((Operator) element).getChildren()) {
            	addChildToXML(a,operator,doc);
            }
        }
    }
	
    /**
     * Converts the xml file from the root element to an attack object.
     * 
     * @param root the root element.
     * @return the attack object.
     * @throws Exception throws exception
     */
	private Attack convertTree(Element root) throws Exception {
		
		Attack attack =  new Attack(root.getAttribute("name"));
		
		NodeList list = root.getChildNodes();
		ArrayList<ArrayList<AbstractElement>> array;
		ArrayList<Operator> operators = new ArrayList<>();
		ArrayList<Attack> attacks = new ArrayList<>();
		
		for (int temp = 0; temp < list.getLength(); temp++) {
		    Node node = list.item(temp);
		    if (node.getNodeType() == Node.ELEMENT_NODE) {
		        Element tmpE = (Element) node;
		        
		        if (node.getNodeName().equals("operator")) {
		        	 ArrayList<Attack> listAttack = new ArrayList<>();
		        	 array = this.getChildren(node);
		             for(AbstractElement ae : array.get(1)) {
		             	if(ae instanceof Attack) {
		             		listAttack.add((Attack) ae);
		             	}
		             }
		      
		            Operator op = new Operator(getTypeFromString(tmpE.getAttribute("type")), listAttack);
		            op.setFather(attack);
		            for(Attack a : listAttack) {
                    	a.setFather(op);                    
                    }
		            operators.add(op);
		            
		        } else if (node.getNodeName().equals("attack")) {
		        	array = this.getChildren(node);
		        	ArrayList<Operator> listOperator = new ArrayList<>();
		             for(AbstractElement ae : array.get(0)) {
		             	if(ae instanceof Operator) {
		             		listOperator.add((Operator) ae);
		             	}
		             }
		        	ArrayList<Attack> listAttack = new ArrayList<>();
		             for(AbstractElement ae : array.get(1)) {
		             	if(ae instanceof Attack) {
		             		listAttack.add((Attack) ae);
		             	}
		             }
		             
		         	Attack at = new Attack(tmpE.getAttribute("name"), listOperator, listAttack);
		         	at.setFather(attack);
		         	 for(Operator o : listOperator) {
	                    	at.setFather(o);                    
	                    }
		         	 for(Attack a : listAttack) {
	                    	at.setFather(a);                    
	                    }
		            attacks.add(at);
		            
		        }
		    }
		}
		attack.setAttackChildren(attacks);
		attack.setOperatorChildren(operators);
		return attack;
	}
	
	/**
	 * Get the list of children of a node.
	 * 
	 * @param n the node 
	 * @return the list of children of a node
	 * @throws Exception throws Exception
	 */
    private ArrayList<ArrayList<AbstractElement>> getChildren(Node n) throws Exception {
    	
        NodeList list = n.getChildNodes();
        ArrayList<ArrayList<AbstractElement>> array;
        ArrayList<AbstractElement> operators = new ArrayList<>();
        ArrayList<AbstractElement> attacks = new ArrayList<>();
        
        for (int temp = 0; temp < list.getLength(); temp++) {
            Node node = list.item(temp);
            array = this.getChildren(node);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
            	
                Element tmpE = (Element) node;
                
                if (node.getNodeName().equals("operator")) {
                	ArrayList<Attack> listAttack = new ArrayList<>();
                	
                    for(AbstractElement ae : array.get(1)) {
                    	if(ae instanceof Attack) {
                    		listAttack.add((Attack) ae);
                    	}
                    }
                    
                    Operator op = new Operator(getTypeFromString(tmpE.getAttribute("type")), listAttack);
                    for(Attack a : listAttack) {
                    	a.setFather(op);                    
                    }
                    operators.add(op);
                    
                } else if (node.getNodeName().equals("attack")) {
                	
                 	ArrayList<Operator> listOperator = new ArrayList<>();
                    for(AbstractElement ae : array.get(0)) {
                     	if(ae instanceof Operator) {
                     		listOperator.add((Operator) ae);
                     	}
                     }
                	ArrayList<Attack> listAttack = new ArrayList<>();
                     for(AbstractElement ae : array.get(1)) {
                     	if(ae instanceof Attack) {
                     		listAttack.add((Attack) ae);
                     	}
                     }
                     
                    Attack at = new Attack(tmpE.getAttribute("name"), listOperator, listAttack);
                   
                    for(Attack a : listAttack) {
                    	a.setFather(at);                    
                    }
                    for(Operator o : listOperator) {
                    	o.setFather(at);                    
                    }
                    attacks.add(at);
                    
                }
            }
        }
        
        ArrayList<ArrayList<AbstractElement>> ret = new ArrayList<>();
        ret.add(operators);
        ret.add(attacks);
        return ret;
    }
    
    
    /**
     * Get the TYPE from the OperatorType enumeration corresponding to the string given in parameter
     * @param type the string corresponding to a OperatorType
     * @return the requested OperatorType
     * @throws Exception Throw an exception if the operator is not in the OperatorType enumeration
     */
    private static OperatorType getTypeFromString(String type) throws Exception {
        switch (type) {
            case "OR":
                return OperatorType.OR;
            case "AND":
                return OperatorType.AND;
            case "NOR":
                return OperatorType.NOR;
            case "XOR":
                return OperatorType.XOR;
            default:
                throw new Exception("This type " + type + " is not referenced");
        }
    }
    
    
    /**
     * Method which makes it possible to know whether an attack or an operator 
     * is present in the child elements of an attack or an operator.
     * 
     * @param e the attack or the operator to find to an another attack
     * @param main the attack in which the attacking object or operator is potentially sought.
     * 
     * @return the element sought in the main attack
     * @throws Exception throws Exception
     */
    private AbstractElement elementAlreadyPresent(AbstractElement e, Attack main) throws Exception {
    	
    	
    	// on remonte jusqu'au root de attack
    	Attack root = main.getRoot();
    	
    	// on v�rifie que l'attaque root n'est pas nulle, sinon erreur.
    	if(root != null) {
    		
    		// Si l'�l�ment que l'on recherche est une Attack alors on commence par v�rifier si cette attaque n'est pas 
    		// l'attaque le root de l'attaque main directement si c'est le cas alors on renvoie le root 
    		if (e instanceof Attack)
	    		if(((Attack)root).getName().equals(((Attack)e).getName()))
					return root;

			// Sinon on parcourt l'ensemble des sous �l�ments de l'attaque root � la recherche de l'attaque ou de l'op�rateur 'e' que l'on cherche
			for(IElement element : ((Attack)root).getAllSubElements()) {
				
				// On regarde tout d'abord si 'e' et l'element dans notre iterateur sont tous les deux des attaques
				// si c'est le cas et que les noms des attaques sont identiques alors on a trouver l'�l�ment et on renvoi la r�ponse
				if(e instanceof Attack && element instanceof Attack)
					if(((Attack)element).getName().equals(((Attack)e).getName()))
						return (AbstractElement) element;
				
				// Sinon on regarde si 'e' et l'�lement dans notre it�rateur sont tous les deux des op�rateurs
				if(element instanceof Operator && e instanceof Operator) {
					
					// si les deux op�rateurs sont du m�me types (ex: OR = OR; AND = AND)
					if(((Operator) e).getOperatorType() == ((Operator) element).getOperatorType())
						
						// si les parents ne sont pas nulles et que les parents portent le m�me nom alors on a trouver l'�l�ment et on renvoi la 
						// r�ponse
						if(((Operator)element).getFather() != null && (((Operator)e).getFather() != null))
							if(((Operator)element).getFather().getName().equals(((Operator)e).getFather().getName()))
								return (AbstractElement) element;	
							
				}
					
			}
    	}

    	return null;
    		
    }
    
    /**
     * Allows you to add an attack to another attack.
     * 
     * @param main the attack in which one should include an attack
     * @param leaf the attack that should be included
     * @throws Exception throws Exception
     */
    private void addAttack(Attack main, Attack leaf) throws Exception {
    	
    	// cr�ation d'un nouvel objet attaque qui poss�de comme attribut uniqueme le nom de l'attaque
    	AbstractElement newAttack = new Attack(leaf.getName());
    	
    	System.out.println("\nOn souhaite ratacher ");
    	System.out.println(newAttack);
    	System.out.println("dans l'arbre :");
    	System.out.println(main);
    	
    	// on r�cup�re le parent de la feuille a ins�rer
    	AbstractElement father = leaf.getFather();
    	System.out.println("P�re de "+leaf.getName()+" = " + father);
    	
    	// on regarde si le parent est d�ja dans l'arbre principal
    	AbstractElement res = elementAlreadyPresent(father,main);
    	System.out.println("1. "+res);
    	
    	// tant que l'on ne retrouve pas de parent on remonte sur les parents de la feuille
    	// en veillant a ajout� � newAttack un p�re
    	while(res == null && father != null) {
    		
    		if (father instanceof Attack) {
    			if (newAttack instanceof Attack) {
    				((Attack) newAttack).setFather(new Attack(((Attack) father).getName()));
        			Attack f = (Attack) ((Attack) newAttack).getFather();
        			f.addChildAttack((Attack) newAttack);
        			newAttack = f;
        			father = ((Attack) father).getFather();
    			}
    			else if(newAttack instanceof Operator) {
    				((Operator) newAttack).setFather(new Attack(((Attack) father).getName()));
        			Attack f = (Attack) ((Operator) newAttack).getFather();
        			f.addChildOperator((Operator) newAttack);
        			newAttack = f;
        			father = ((Attack) father).getFather();
    			}
    			
    		}
    		else if (father instanceof Operator) {
    			
    			if (newAttack instanceof Attack) {
    				((Attack) newAttack).setFather(new Operator(((Operator) father).getOperatorType()));
        			Operator o = (Operator) ((Attack) newAttack).getFather();
        			o.addChildAttack((Attack) newAttack);
        			newAttack = o;
        			father = ((Operator) father).getFather();
    			}

    		}
    		
    		if(father != null)
    			res = elementAlreadyPresent(father,main);
    	}
    	

    	System.out.println("Noeud a rajouter : "+newAttack);

    	// a ce moment la on connait l'endroit dans l'attaque on l'on doit ajouter 'newAttack' (l'attaque)
    	// res = endroit on ajouter l'attaque 
    	// on doit donc d�finir 
    	// 1. pour newAttack son parent, ici res 
    	// 2. pour res un nouvel enfant, ici newAttack
    	// ATTENTION, il faut v�rifier si c'est des Operator ou des Attacks qui sont trait�s
    	if (father != null) {
    		
    		// Si on doit ajouter une attaque 
    		if (newAttack instanceof Attack) {
    			// dans un enfant d'attaque
    			if(res instanceof Attack) {
    				((Attack)res).addChildAttack((Attack) newAttack);
    				((Attack) newAttack).setFather(res);
    			}
    			// dans un enfant d'operator
    			else if (res instanceof Operator) {
    				((Operator)res).addChildAttack((Attack) newAttack);
    				((Attack) newAttack).setFather(res);
    			}
    		}
    		// Si on doit ajouter un Operator
    		else if(newAttack instanceof Operator) {
    			// dans un enfant d'attaque
    			if(res instanceof Attack) {
    				((Attack)res).addChildOperator((Operator) newAttack);
    				((Operator) newAttack).setFather((Attack) res);
    			}
    			// erreur car un objet Operator a forcement un parent attaque
    			else if (res instanceof Operator) {
    				throw new Exception("Erreur dans l'int�gration d'un nouvel �lement dans l'attaque principal");
    			}
    		}
    		
    		
    	}
    	else
    		throw new Exception("Impossible de cr�er un arbre avec ces donn�es");
    	
    }
    
}