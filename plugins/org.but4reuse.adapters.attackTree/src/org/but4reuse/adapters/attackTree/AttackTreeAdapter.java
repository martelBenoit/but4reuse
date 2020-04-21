package org.but4reuse.adapters.attackTree;

import java.io.BufferedInputStream;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.but4reuse.adapters.IAdapter;
import org.but4reuse.adapters.IElement;
import org.but4reuse.utils.files.FileUtils;
import org.eclipse.core.runtime.IProgressMonitor;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.tg.TinkerGraph;
import com.tinkerpop.blueprints.util.io.gml.GMLReader;
import com.tinkerpop.blueprints.util.io.graphml.GraphMLReader;
import com.tinkerpop.blueprints.util.io.graphml.GraphMLWriter;

import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.*;
import java.io.*;


/**
 * EXEMPLE XML D'UN ARBRE D'ATTAQUE
 * <attack name="Cause physical harm to human beings or property">
    <operator type="OR">
        <attack name="Circumvent safety features">
            <operator type="OR">
                <attack name="Spoof sensor data"></attack>
                <attack name="Exploit weakness in software"></attack>
                <attack name="Modify firmware"></attack>
            </operator>
        </attack>
        <attack name="Cause damage">
            <operator type="OR">
                <attack name="Induce collision"></attack>
                <attack name="Trigger action">     
                    <operator type="OR">
                        <attack name="Impersonate operator"></attack>
                        <attack name="Take advantage of behavior"></attack>
                    </operator>
                </attack>
            </operator>
        </attack>
    </operator>
</attack>
 */

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
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.parse(file);
			
			document.getDocumentElement().normalize();
			
			Element attack = document.getDocumentElement();
			AttackElement rootAttack = new AttackElement(attack.getAttribute("name"));
			
			NodeList attackNodes = attack.getChildNodes();
			int nbAttackNodes = attackNodes.getLength();
			

			
		} catch (SAXException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	

		return elements;
	}

	@Override
	public void construct(URI uri, List<IElement> elements, IProgressMonitor monitor) {

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
	}
	

}
