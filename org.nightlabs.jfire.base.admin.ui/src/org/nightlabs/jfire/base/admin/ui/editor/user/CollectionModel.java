package org.nightlabs.jfire.base.admin.ui.editor.user;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

public class CollectionModel<ElementType> extends BaseModel {
	private Collection<ElementType> elements = Collections.emptySet();
	
	public Collection<ElementType> getElements() {
		return Collections.unmodifiableCollection(elements);
	}
	
	public void setElements(Collection<ElementType> elements) {
		this.elements = new HashSet<ElementType>(elements); // defensive copying
		modelChanged();
	}
	
	public void addElement(ElementType element) {
		elements.add(element);
		modelChanged();
	}
	
	public void removeElement(ElementType element) {
		elements.remove(element);
		modelChanged();
	}
	
	public boolean contains(ElementType element) {
		return elements.contains(element);
	}
}
