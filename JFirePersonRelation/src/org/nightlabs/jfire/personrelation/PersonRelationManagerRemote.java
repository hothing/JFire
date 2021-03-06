package org.nightlabs.jfire.personrelation;

import java.util.Collection;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ejb.Remote;

import org.nightlabs.jdo.ObjectID;
import org.nightlabs.jfire.personrelation.id.PersonRelationID;
import org.nightlabs.jfire.personrelation.id.PersonRelationTypeID;
import org.nightlabs.jfire.prop.id.PropertySetID;

@Remote
public interface PersonRelationManagerRemote {

	void initialise() throws Exception;

	Collection<PersonRelationTypeID> getPersonRelationTypeIDs();

	Collection<PersonRelationType> getPersonRelationTypes(
			Collection<PersonRelationTypeID> personRelationTypeIDs,
			String[] fetchGroups, int maxFetchDepth
	);

	long getPersonRelationCount(
			PersonRelationTypeID personRelationTypeID,
			PropertySetID fromPersonID, PropertySetID toPersonID
	);

	Collection<PersonRelationID> getPersonRelationIDs(
			PersonRelationTypeID personRelationTypeID,
			PropertySetID fromPersonID, PropertySetID toPersonID
	);

	Collection<PersonRelation> getPersonRelations(
			Collection<PersonRelationID> personRelationIDs,
			String[] fetchGroups, int maxFetchDepth
	);

	void createPersonRelation(
			PersonRelationTypeID personRelationTypeID,
			PropertySetID fromPersonID, PropertySetID toPersonID
	);

	void deletePersonRelation(
			PersonRelationTypeID personRelationTypeID,
			PropertySetID fromPersonID, PropertySetID toPersonID
	);

	Map<Class<? extends ObjectID>, List<Deque<ObjectID>>> getRootNodes(
			Set<PersonRelationTypeID> relationTypeIDs,
			PropertySetID startPoint, int maxDepth);

//	Map<PropertySetID, Map<Class<? extends ObjectID>, List<Deque<ObjectID>>>> getRootNodes(
//			Set<PersonRelationTypeID> relationTypeIDs,
//			Set<PropertySetID> personIDs, int maxDepth);

	List<PropertySetID> getRootNodes(Set<PersonRelationTypeID> relationTypeIDs,
			Set<PropertySetID> personIDs, int maxDepth);
}
