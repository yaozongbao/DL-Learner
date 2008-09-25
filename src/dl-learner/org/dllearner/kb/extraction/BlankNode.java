package org.dllearner.kb.extraction;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.dllearner.kb.aquisitors.RDFBlankNode;
import org.dllearner.kb.aquisitors.TupleAquisitor;
import org.dllearner.kb.manipulator.Manipulator;
import org.dllearner.utilities.datastructures.RDFNodeTuple;
import org.dllearner.utilities.datastructures.StringTuple;
import org.dllearner.utilities.owl.OWLVocabulary;
import org.semanticweb.owl.model.OWLDataFactory;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLObjectProperty;

public class BlankNode extends Node {
	private static Logger logger = Logger
	.getLogger(BlankNode.class);
	
	RDFBlankNode bNode;
	
	String inboundEdge;
	
	
	private List<BlankNode> blankNodes =new ArrayList<BlankNode>();
	private SortedSet<StringTuple> otherNodes = new TreeSet<StringTuple> ();
	private List<DatatypePropertyNode> datatypeProperties = new ArrayList<DatatypePropertyNode>();
	
	//private List<ObjectPropertyNode> objectProperties = new ArrayList<ObjectPropertyNode>();
	//private List<DatatypePropertyNode> datatypeProperties = new ArrayList<DatatypePropertyNode>();

	
	public BlankNode(RDFBlankNode bNode, String inboundEdge){
		super(""+bNode.getBNodeId());
		this.bNode=bNode;
		this.inboundEdge = inboundEdge;
	}

	
	
	@Override
	public List<Node> expand(TupleAquisitor tupleAquisitor,
			Manipulator manipulator) {
		List<Node> newNodes = new ArrayList<Node>();
		SortedSet<RDFNodeTuple> s = tupleAquisitor.getBlankNode(bNode.getBNodeId());
		//System.out.println("entering "+bNode.getBNodeId());
		
	
		for (RDFNodeTuple tuple : s) {
			if(tuple.b.isLiteral()) {
				//System.out.println("adding dtype: "+tuple);
				datatypeProperties.add(new DatatypePropertyNode(tuple.a.toString(), this, new LiteralNode(tuple.b) ));
				//connectedNodes.add(new DatatypePropertyNode(tuple.a.toString(), this, new LiteralNode(tuple.b) ));
			}else if(tuple.b.isAnon()){
				//System.out.println("adding bnode: "+tuple);
				BlankNode tmp = new BlankNode( (RDFBlankNode)tuple.b, tuple.a.toString());
				//objectProperties.add(new ObjectPropertyNode(tuple.a.toString(), this, tmp ));
				//connectedNodes.add(new BlankNode( (RDFBlankNode)tuple.b, tuple.a.toString()));
				blankNodes.add(tmp);
				newNodes.add(tmp);
			}else{
				//System.out.println("adding other: "+tuple);
				otherNodes.add(new StringTuple(tuple.a.toString(), tuple.b.toString()));
				//objectProperties.add(new ObjectPropertyNode(tuple.a.toString(), this, new ClassNode(tuple.b.toString()) ));
				//connectedNodes.add(new ObjectPropertyNode(tuple.a.toString(), this, new ClassNode(tuple.b.toString()) ));
			}
		}
		
		//System.out.println("finished");
	
		return newNodes;
	}

	@Override
	public List<BlankNode>  expandProperties(TupleAquisitor TupelAquisitor,
			Manipulator manipulator) {
		return new ArrayList<BlankNode>();
	}

	@Override
	public SortedSet<String> toNTriple() {
		SortedSet<String> returnSet = new TreeSet<String>();
		//String subject = getNTripleForm();
		/*for (ObjectPropertyNode one : objectProperties) {
			returnSet.add(subject + one.getNTripleForm() + one.getBPart().getNTripleForm()+" . ");
			returnSet.addAll(one.getBPart().toNTriple());
		}*/
		return returnSet;
	}
	
	@Override
	public String getNTripleForm(){
		return " "+uri+" ";
	}
	
	@Override
	public String toString(){
		return "id: "+bNode.getBNodeId()+" inbound: "+getInBoundEdge();
		
	}
	
	@Override
	public URI getURI(){
		return URI.create("http://www.empty.org/empty#empty");
	}
	
	@Override
	public void toOWLOntology( OWLAPIOntologyCollector owlAPIOntologyCollector){
		//FIXME
	}
	
	public String getInBoundEdge(){
		return inboundEdge;
	}
		
	public OWLDescription getAnonymousClass(OWLAPIOntologyCollector owlAPIOntologyCollector){
		OWLDataFactory factory =  owlAPIOntologyCollector.getFactory();
		OWLDescription ret = factory.getOWLClass(URI.create("http://dummy.org/dummy"));
		
		//System.out.println(inboundEdge);
		
		if(
			(inboundEdge.equals(OWLVocabulary.OWL_intersectionOf))||
			(inboundEdge.equals(OWLVocabulary.OWL_complementOf))||
			(inboundEdge.equals(OWLVocabulary.OWL_unionOf))
		 ){
			Set<OWLDescription> target = new HashSet<OWLDescription>();
			List<BlankNode> tmp = new ArrayList<BlankNode>();
			tmp.add(this);
			while(!tmp.isEmpty()){
				BlankNode next = tmp.remove(0);
				//next.printAll();
				
				if(next.otherNodes.contains(new StringTuple(OWLVocabulary.RDF_REST, OWLVocabulary.RDF_NIL))){
					for(StringTuple t : next.otherNodes){
						if(t.a.equals(OWLVocabulary.RDF_FIRST)){
							target.add(factory.getOWLClass(URI.create(t.b)));
							//System.out.println("added "+t.b);
						}
					}
					//System.out.println("nil found");
					//do nothing
				}else{
					if(next.otherNodes.first().a.equals(OWLVocabulary.RDF_FIRST)){
						target.add(factory.getOWLClass(URI.create(next.otherNodes.first().b)));
						tmp.add(next.blankNodes.get(0));
						//System.out.println("bnode added");
					}else{
						System.out.println("double nesting not supported yet");
						System.exit(0);
					}
				}
			}//end while
			
			if(inboundEdge.equals(OWLVocabulary.OWL_intersectionOf)){
				return factory.getOWLObjectIntersectionOf(target);
			}else if(inboundEdge.equals(OWLVocabulary.OWL_unionOf)){
				return factory.getOWLObjectUnionOf(target);
			}else if(inboundEdge.equals(OWLVocabulary.OWL_complementOf)){
				if(target.size()>1) {
					logger.warn("more than one complement"+target);
					System.exit(0);
				}else{
					return factory.getOWLObjectComplementOf(new ArrayList<OWLDescription>(target).remove(0));
				}
			}else{
				printAll();
				tail("wrong type: " +inboundEdge+ this);
			}
		}
		
		// restriction
		if(otherNodes.contains(
				new StringTuple(OWLVocabulary.RDF_TYPE, OWLVocabulary.OWL_RESTRICTION))){
			return getRestriction( owlAPIOntologyCollector);
			
		}
		
		if(!blankNodes.isEmpty()){
			return blankNodes.get(0).getAnonymousClass(owlAPIOntologyCollector);
		}
		
		
		return ret;
		
	}
	
	public void printAll(){
		System.out.println(this);
		
		System.out.println("otherNodes");
		for (StringTuple t : otherNodes) {
			System.out.println(""+t);
		}
		System.out.println("***************");
		System.out.println("dtype ");
		for (DatatypePropertyNode d : datatypeProperties) {
			System.out.println(d.getURIString()+" "+d.getNTripleFormOfB());
		}
		System.out.println("***************");
		System.out.println("other bnodes");
		for (BlankNode b : blankNodes) {
			System.out.println(b);
		}
		System.out.println("***************");
		
	}
	
	private OWLDescription getRestriction(OWLAPIOntologyCollector owlAPIOntologyCollector){
		OWLDataFactory factory =  owlAPIOntologyCollector.getFactory();
		OWLObjectProperty property = null;
		OWLDescription concept = null;
		OWLDescription dummy = factory.getOWLClass(URI.create("http://dummy.org/dummy"));
		
		int total = otherNodes.size()+blankNodes.size()+datatypeProperties.size();
		if(total >=4 ){
			System.out.println("qualified p restrictions not supported currently");
		}
		
		// get Objectproperty
		for(StringTuple n : otherNodes) {
			if(n.a.equals(OWLVocabulary.OWL_ON_PROPERTY)){
				property = factory.getOWLObjectProperty(URI.create(n.b)); 
			}
		}
		
		// has an Integer value
		if(!datatypeProperties.isEmpty()){
			DatatypePropertyNode d = datatypeProperties.get(0);
			String p = d.getURIString();
			if( p.equals(OWLVocabulary.OWL_cardinality)){
				return factory.getOWLObjectExactCardinalityRestriction(property, d.getBPart().getLiteral().getInt());
			}else if(p.equals(OWLVocabulary.OWL_maxCardinality)){
				return factory.getOWLObjectMaxCardinalityRestriction(property, d.getBPart().getLiteral().getInt());
			}else if(p.equals(OWLVocabulary.OWL_minCardinality)){
				return factory.getOWLObjectMinCardinalityRestriction(property, d.getBPart().getLiteral().getInt());
			}else {
				tail(p+d+" in "+this);
			}
		}
		
		if(!blankNodes.isEmpty()){
			concept = blankNodes.get(0).getAnonymousClass(owlAPIOntologyCollector);
		}else{
			for(StringTuple n : otherNodes) {
				String p = n.a;
				String o = n.b;
				if(
					(p.equals(OWLVocabulary.OWL_ALL_VALUES_FROM)) ||
					(p.equals(OWLVocabulary.OWL_SOME_VALUES_FROM)) ||
					(p.equals(OWLVocabulary.OWL_HAS_VALUE))
				  ){
					concept = factory.getOWLClass(URI.create(o));
				}
			}
		}
		
		for(StringTuple n : otherNodes) {
			String p = n.a;
			if(p.equals(OWLVocabulary.OWL_ALL_VALUES_FROM)){
				return factory.getOWLObjectAllRestriction(property, concept);
			}else if(p.equals(OWLVocabulary.OWL_SOME_VALUES_FROM)){
				return factory.getOWLObjectSomeRestriction(property, concept);
			}else if(p.equals(OWLVocabulary.OWL_HAS_VALUE)){
				logger.warn("OWL_hasValue not implemented yet");
				return dummy;
			}
		}
		return dummy;
	}
	
	
	
	
	/*private boolean isOfType(String type){
		for (Node n : connectedNodes) {
			if((n  instanceof BlankNode )
					&& 
				((BlankNode)n).getInBoundEdge().equals(type)) {
				return true;
			}else if((n  instanceof ObjectPropertyNode )
					&& 
				((ObjectPropertyNode)n).getAPart().toString().equals(type)) {
				return true;
			}
		}
		return false;
	}
	
	private boolean containsDataTypeProperties(){
		for (Node n : connectedNodes) {
			if(n  instanceof DatatypePropertyNode) {
				return true;
			}
		}
		return false;
	}*/

}
