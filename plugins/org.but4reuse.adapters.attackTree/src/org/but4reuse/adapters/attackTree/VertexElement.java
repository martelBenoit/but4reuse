package org.but4reuse.adapters.attackTree;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.but4reuse.adapters.IElement;
import org.but4reuse.adapters.attackTree.activator.Activator;
import org.but4reuse.adapters.attackTree.preferences.AttackTreeAdapterPreferencePage;
import org.but4reuse.adapters.impl.AbstractElement;

import com.tinkerpop.blueprints.Vertex;

/**
 * Vertex Element
 * 
 * @author MARTEL Benoit, NAVEAU Simon, TRAVAILLE Lo�c
 */
public class VertexElement extends AbstractElement {

	private Vertex vertex = null;

	@Override
	public double similarity(IElement anotherElement) {
		if (anotherElement instanceof VertexElement) {
			VertexElement vertexElement = (VertexElement) anotherElement;
			String id = Activator.getDefault().getPreferenceStore().getString(AttackTreeAdapterPreferencePage.NODE_ID);
			if (id == null || id.isEmpty()) {
				if (vertexElement.getVertex().getId().equals(vertex.getId())) {
					return 1;
				}
			} else {
				if (vertexElement.getVertex().getProperty(id) != null
						&& vertexElement.getVertex().getProperty(id).equals(vertex.getProperty(id))) {
					return 1;
				}
			}
		}
		return 0;
	}

	@Override
	public String getText() {
		String properties = "";
		Set<String> keys = vertex.getPropertyKeys();
		for (String key : keys) {
			properties = properties + ", " + key + "=" + vertex.getProperty(key);
		}
		return "V: id=" + vertex.getId() + properties;
	}

	public Vertex getVertex() {
		return vertex;
	}

	public void setVertex(Vertex vertex) {
		this.vertex = vertex;
	}

	@Override
	public List<String> getWords() {
		List<String> words = new ArrayList<String>();
		String id = Activator.getDefault().getPreferenceStore().getString(AttackTreeAdapterPreferencePage.NODE_ID);
		if (id == null || id.isEmpty()) {
			words.add(vertex.getId().toString());
		} else {
			words.add(vertex.getProperty(id).toString());
		}
		return words;
	}

}
