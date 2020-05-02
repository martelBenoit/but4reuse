package org.but4reuse.adapters.attackTree;

import java.io.File;

import java.io.IOException;

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

import javax.xml.parsers.*;


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
			
			for(IElement e : elements) {
				System.out.println(e);
			}
			
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
/**
		// Create graph
		Graph graph = new TinkerGraph();

		// Add vertices
		for (IElement element : elements) {
			if (element instanceof VertexElement) {
				VertexElement ve = (VertexElement) element;
				Vertex v = graph.addVertex(ve.getVertex().getId());
				// adding all its properties
				for (String key : ve.getVertex().getPropertyKeys()) {
					v.setProperty(key, ve.getVertex().getProperty(key));
				}
			}
		}

		// Add edges
		// We create link vertices when they do not exist in the graph but the
		// edge reference to it
		int idEdge = 1;
		int linkVertex = 0;
		for (IElement element : elements) {
			if (element instanceof EdgeElement) {
				EdgeElement ee = (EdgeElement) element;
				Vertex targetVertex = ee.getEdge().getVertex(Direction.IN);
				Vertex sourceVertex = ee.getEdge().getVertex(Direction.OUT);
				// If they dont exist, create fake ones
				if (graph.getVertex(targetVertex.getId()) == null) {
					targetVertex = graph.addVertex("link vertex " + linkVertex);
					targetVertex.setProperty("label", targetVertex.getId());
					linkVertex++;
				} else {
					// get the current object in the graph
					targetVertex = graph.getVertex(targetVertex.getId());
				}
				if (graph.getVertex(sourceVertex.getId()) == null) {
					sourceVertex = graph.addVertex("link vertex " + linkVertex);
					sourceVertex.setProperty("label", sourceVertex.getId());
					linkVertex++;
				} else {
					// get the current object in the graph
					sourceVertex = graph.getVertex(sourceVertex.getId());
				}

				Edge edge = graph.addEdge(idEdge, sourceVertex, targetVertex, ee.getEdge().getLabel());
				idEdge++;
				// adding all its properties
				for (String key : ee.getEdge().getPropertyKeys()) {
					edge.setProperty(key, ee.getEdge().getProperty(key));
				}
			}
		}

		// Save
		GraphMLWriter writer = new GraphMLWriter(graph);
		writer.setNormalize(true);

		try {
			// Use the given file or use a default name if a folder was given
			if (uri.toString().endsWith("/")) {
				uri = new URI(uri.toString() + "attackTree.graphml");
			}
			// Create file if it does not exist
			File file = FileUtils.getFile(uri);
			FileUtils.createFile(file);

			writer.outputGraph(file.getAbsolutePath());
		} catch (Exception e) {
			e.printStackTrace();
		}
			*/
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