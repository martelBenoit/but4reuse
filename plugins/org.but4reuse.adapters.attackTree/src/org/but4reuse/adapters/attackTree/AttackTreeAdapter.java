package org.but4reuse.adapters.attackTree;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;

import java.util.List;


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
		
		for(IElement e : elements) {
			System.out.println(e);
		}
		
		Attack attack = null;
		

		try {
			
			// On recherche le root
			for(IElement e : elements) {
				if(e instanceof Attack) {
					if (((Attack) e).getFather() == null) {
						attack = (Attack)e;
						elements.remove(e);
						break;
					}
				}
			}
			
			List<IElement> allSub = attack.getAllSubElements();
			
		
			
			System.out.println("ROOT = "+attack);
			
			
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
		            for(Operator op : attack.getOperatorChildren()) {
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
          
        } else {
            Element operator = doc.createElement("operator");
            operator.setAttribute("type", "" + ((Operator) element).type);
            father.appendChild(operator);
            for(Attack a : ((Operator) element).getChildren()) {
            	addChildToXML(a,operator,doc);
            }
        }
    }
	
	private Attack convertTree(Element root) throws Exception {
		
		NodeList list = root.getChildNodes();
		ArrayList<ArrayList<AbstractElement>> array;
		ArrayList<Operator> operators = new ArrayList<>();
		ArrayList<Attack> attacks = new ArrayList<>();
		
		for (int temp = 0; temp < list.getLength(); temp++) {
		    Node node = list.item(temp);
		    if (node.getNodeType() == Node.ELEMENT_NODE) {
		        Element tmpE = (Element) node;
		        array = this.getChildren(node);
		        
		        
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
		return new Attack(root.getAttribute("name"), operators, attacks);
	}
	
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
    
    

}