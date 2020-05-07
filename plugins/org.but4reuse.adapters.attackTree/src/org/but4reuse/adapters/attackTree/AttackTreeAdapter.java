package org.but4reuse.adapters.attackTree;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;

import java.util.List;
import java.util.UUID;

import org.but4reuse.adapters.IAdapter;
import org.but4reuse.adapters.IElement;
import org.but4reuse.adapters.impl.AbstractElement;
import org.but4reuse.utils.files.FileUtils;
import org.eclipse.core.runtime.IProgressMonitor;

import org.w3c.dom.*;
import org.xml.sax.SAXException;

import com.tinkerpop.blueprints.util.io.graphml.GraphMLReader;

import javax.xml.parsers.*;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;


/**
 * Attack Tree Adapter
 * 
 * @author MARTEL Benoit, NAVEAU Simon, TRAVAILLE Loïc
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
	 * Read the graph file
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
			
			Attack attack = convertTree(root);
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
		
		
		ArrayList<Attack> attacks = new ArrayList<>();
		ArrayList<Operator> operators = new ArrayList<>();
		
		
		
		for(IElement element : elements)
			if (element instanceof Attack)
				attacks.add((Attack) element);
			else if(element instanceof Operator)
				operators.add((Operator)element);
		
		System.out.println("======== Liste des attaques à ajouter =============");
		for(Attack a : attacks) {
			System.out.println(a);
		}
		System.out.println("======== FIN =============\n");
		
		
		
		try {
			
			Attack attack = null;
			
			for(Attack a : attacks) {
				if(a.getFather() == null) {
					attack = new Attack(a.getName());
					attacks.remove(a);
					break;
				}
					
				
			}
			if (attack == null) {
				attack = new Attack(attacks.get(0).getName());
				attacks.remove(0);
			}
				
	
			
			int cpt = 0;
			for(Attack a : attacks) {
				cpt++;
				System.out.println("AJOUT N°"+cpt);
				addAttack(attack, a);
				System.out.println("\n\nFIN AJOUT N°"+cpt);
				

			}
			
			System.out.println("\n======== On est vers la fin =============");
			System.out.println("AVANT DE PRENDRE ROOT ATTACK : "+attack);
			attack = attack.getRoot();
			System.out.println("ROOT ATTACK : "+attack);
			
		/*	
			Attack attack = attacks.get(0);
			cutAttack(attack, attacks);
			attacks.remove(0);
			
			
			ArrayList<Attack> listAttackAlreadyPresent = new ArrayList<>();
			for(Attack a : attacks) {
				if (isÄlreadyPresent(a,attack))
						listAttackAlreadyPresent.add(a);
			}
			
			System.out.println(attacks.size());
			for(Attack a : listAttackAlreadyPresent)
				attacks.remove(a);
			System.out.println(attacks.size());
			
			for(Attack a : attacks) {
				addAttack(attack,a);
			}
			
			attack = attack.getRoot();
			System.out.println("ROOT ATTACK : "+attack);
			*/
			
		
			

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
		            for(Attack op : o.getChildren()) {
		            	addChildToXML(op,operator,document);
		            }
	            	
		         
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
	
	private Attack convertTree(Element root) throws Exception {
		String id_tree = UUID.randomUUID().toString();
		
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
		        	 array = this.getChildren(node,id_tree);
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
		        	array = this.getChildren(node, id_tree);
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
	
    private ArrayList<ArrayList<AbstractElement>> getChildren(Node n, String id_tree) throws Exception {
    	
        NodeList list = n.getChildNodes();
        ArrayList<ArrayList<AbstractElement>> array;
        ArrayList<AbstractElement> operators = new ArrayList<>();
        ArrayList<AbstractElement> attacks = new ArrayList<>();
        
        for (int temp = 0; temp < list.getLength(); temp++) {
            Node node = list.item(temp);
            array = this.getChildren(node, id_tree);
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
    
    private boolean isÄlreadyPresent(Attack attack, Attack main) throws Exception {
    
    	
    	// on remonte jusqu'au root de attack
    	Attack root = main.getRoot();
    	if(root != null) {
    		
    		if(((Attack)root).getName().equals(attack.getName()))
				return true;
			
			for(IElement element : ((Attack)root).getAllSubElements()) {
				if(element instanceof Attack) {
					if(((Attack)element).getName().equals(attack.getName()))
						return true;
				}
				
			}
    	}

    	return false;
    	
    	
    }
    
    private AbstractElement elementAlreadyPresent(AbstractElement e, Attack main) throws Exception {
    	
    	
    	// on remonte jusqu'au root de attack
    	Attack root = main.getRoot();
    	System.out.println("\n======== ELEMENT ALREADY PRESENT ==========");
    	System.out.println(e);
    	System.out.println("IN");
    	System.out.println(root);
    	if(root != null) {
    		
    		if (e instanceof Attack)
	    		if(((Attack)root).getName().equals(((Attack)e).getName()))
					return root;

			
			for(IElement element : ((Attack)root).getAllSubElements()) {
				
				System.out.println("Iterateur sur element : "+ element);
				
				
				if(e instanceof Attack && element instanceof Attack)
					if(((Attack)element).getName().equals(((Attack)e).getName()))
						return (AbstractElement) element;
					
				if(element instanceof Operator && e instanceof Operator) {
					// si les deux opérateurs sont du même types 
					if(((Operator) e).getOperatorType() == ((Operator) element).getOperatorType())
						System.out.println("type operator OK");
						System.out.println(((Operator) element).getFather());
						System.out.println(((Operator) e).getFather());
						if(((Operator)element).getFather() != null && (((Operator)e).getFather() != null)) {
							if(((Operator)element).getFather().getName().equals(((Operator)e).getFather().getName())) {
								System.out.println("operator identique");
								return (AbstractElement) element;
							}
								
							else
								System.out.println("type operator OK");
						}
						else
							System.out.println("erreur pere operator");
							
				}
					
				
				
			}
    	}

    	return null;
    	
    	
    }
    
    
    private void addAttack(Attack main, Attack leaf) throws Exception {
    	
    	
    	AbstractElement newAttack = new Attack(leaf.getName());
    	
    	System.out.println("\nOn souhaite ratacher ");
    	System.out.println(newAttack);
    	System.out.println("dans l'abre :");
    	System.out.println(main);
    	
    	// on récupère le parent de la feuille a insérer
    	AbstractElement father = leaf.getFather();
    	System.out.println("Père de "+leaf.getName()+" = " + father);
    	// on regarde si le parent est déja dans l'arbre principal
    	AbstractElement res = elementAlreadyPresent(father,main);
    	System.out.println("1. "+res);
    	
    	// tant que l'on ne retrouve pas de parent on remonte sur les parents de la feuille
    	// en veillant a ajouté à newAttack un père
    	int compteur = 1;
    	while(res == null && father != null) {
    		
    		if (father instanceof Attack) {
    			if (newAttack instanceof Attack) {
    				((Attack) newAttack).setFather(new Attack(((Attack) father).getName()));
        			Attack f = (Attack) ((Attack) newAttack).getFather();
        			f.addAttackChildren((Attack) newAttack);
        			newAttack = f;
        			father = ((Attack) father).getFather();
    			}
    			else if(newAttack instanceof Operator) {
    				((Operator) newAttack).setFather(new Attack(((Attack) father).getName()));
        			Attack f = (Attack) ((Operator) newAttack).getFather();
        			f.addOperatorChildren((Operator) newAttack);
        			newAttack = f;
        			father = ((Attack) father).getFather();
    			}
    			
    		}
    		else if (father instanceof Operator) {
    			
    			if (newAttack instanceof Attack) {
    				((Attack) newAttack).setFather(new Operator(((Operator) father).getOperatorType()));
        			Operator o = (Operator) ((Attack) newAttack).getFather();
        			o.addAttackChildren((Attack) newAttack);
        			newAttack = o;
        			father = ((Operator) father).getFather();
    			}

    		}
    		
    		System.out.println("Compteur "+compteur+" : "+father);
    		
    		if(father != null)
    			res = elementAlreadyPresent(father,main);
    	}
    	
    	System.out.println(res);
    	System.out.println("Noeud a rajouter : "+newAttack);
    	if(newAttack instanceof Operator)
    		System.out.println(((Operator) newAttack).getChildren());
    	
    	if (father != null) {

    		if (newAttack instanceof Attack) {
    			if(res instanceof Attack) {
    				((Attack)res).addAttackChildren((Attack) newAttack);
    				((Attack) newAttack).setFather(res);
    			}
    			else if (res instanceof Operator) {
    				((Operator)res).addAttackChildren((Attack) newAttack);
    				((Attack) newAttack).setFather(res);
    			}
    		}
    		else if(newAttack instanceof Operator) {
    			if(res instanceof Attack) {
    				((Attack)res).addOperatorChildren((Operator) newAttack);
    				((Operator) newAttack).setFather((Attack) res);
    			}
    			else if (res instanceof Operator) {
    				System.out.println("erreur");
    			}
    		}
    		
    		
    	}
    	else
    		throw new Exception("Impossible de créer un arbre avec ces données");
    	/*
    	
    	AbstractElement father = leaf.getFather();
    	
    	
    	
    	
    	int cpt = 1;
    	
    	AbstractElement node = elementÄlreadyPresent(father,main);
    	while(node == null && father != null) {
    		
    		
    		cpt++;
    		if (father instanceof Attack) {
    			if (newAttack instanceof Attack) {
    				((Attack) newAttack).setFather(new Attack(((Attack) father).getName()));
        			Attack f = (Attack) ((Attack) newAttack).getFather();
        			f.addAttackChildren((Attack) newAttack);
        			newAttack = f;
        			father = ((Attack) father).getFather();
    			}
    			else if(newAttack instanceof Operator) {
    				((Operator) newAttack).setFather(new Attack(((Attack) father).getName()));
        			Attack f = (Attack) ((Operator) newAttack).getFather();
        			f.addOperatorChildren((Operator) newAttack);
        			newAttack = f;
        			father = ((Attack) father).getFather();
    			}
    			
    		}
    		else if (father instanceof Operator) {
    			
    			if (newAttack instanceof Attack) {
    				((Attack) newAttack).setFather(new Operator(((Operator) father).getOperatorType()));
        			Operator o = (Operator) ((Attack) newAttack).getFather();
        			o.addAttackChildren((Attack) newAttack);
        			newAttack = o;
        			father = ((Operator) father).getFather();
    			}

    		}
    		
    		if(father != null)
    			node = elementÄlreadyPresent(father,main);

    			
    	}
    	
    	
    	
    	if (father != null) {
    		System.out.println("nb de parent avant repique sur arbre : "+cpt);
    		System.out.println(newAttack);
    		System.out.println("NODE "+node);

    		if (newAttack instanceof Attack) {
    			if(node instanceof Attack) {
    				((Attack)node).addAttackChildren((Attack) newAttack);
    			}
    			else if (node instanceof Operator) {
    				((Operator)node).addAttackChildren((Attack) newAttack);
    			}
    		}
    		else if(newAttack instanceof Operator) {
    			if(node instanceof Attack) {
    				((Attack)node).addOperatorChildren((Operator) newAttack);
    			}
    			else if (node instanceof Operator) {
    				System.out.println("erreur");
    			}
    		}
    		
    		
    	}
    	else
    		System.out.println("erreur");
    		
    		*/
    	
    }
    

    
    
    private void cutAttack(Attack attack, ArrayList<Attack> attacks) throws Exception {
    	
    	Attack root = attack.getRoot();
    	if(root != null) {
    		
    		ArrayList<IElement> notFoundList = new ArrayList<>();
    		

	    	for(IElement element : ((Attack)root).getAllSubElements()) {
	    		boolean found = false;
	    		if(element instanceof Attack) {
	    			for(Attack a : attacks) {
	    				if(((Attack)element).getName().equals(a.getName())) {
	    					found = true;
	    					break;
	    				}
	        		}
					
				}
	    		if(!found && element instanceof Attack) {
	    			System.out.println(element);
	    			notFoundList.add(element);
	    		}
	    				
				
			}
	    	
	    /*	
	    	for(Attack a : attacks) {
				if(((Attack)root).getName().equals(a.getName())) {
					notFoundList.add(root);
					break;
				}
    		}
	    	*/
	    	
	    	
	    	for(IElement a : notFoundList) {
	    		
	    		if(a instanceof Attack) {
	    			AbstractElement father = ((Attack)a).getFather();
	    			if(father != null && father instanceof Attack) {
	    				((Attack) father).getAttackChildren().remove(a);
	    				((Attack) a).setFather(null);
	    			}
	    			else if (father != null && father instanceof Operator) {
	    				((Operator) father).getChildren().remove(a);
	    				((Attack) a).setFather(null);
	    			}
	    			else if (father == null) {
	    				for(Attack attackChild : ((Attack) a).getAttackChildren())
	    					attackChild.setFather(null);
	    				for(Operator operatorChild : ((Attack) a).getOperatorChildren())
	    					operatorChild.setFather(null);
	    				
	    			}
	    		}
	    		else if(a instanceof Operator) {
	    			Attack father = ((Operator) a).getFather();
	    			if (father != null) {
		    			father.getOperatorChildren().remove(a);
		    			((Operator) a).setFather(null);
	    			}
	    			else {
	    				for(Attack attackChild : ((Operator) a).getChildren())
	    					attackChild.setFather(null);
	    				
	    			}
	    		}
	    		
	    	}
	    	
	    	System.out.println("Number elements to delete : "+notFoundList.size());
    	}
    	
    }
    

    
    
    

}