package jlibbig;

import java.util.*;

import jlibbig.EditableNode.EditablePort;

public class Bigraph {

	final Signature signature;
	final List<EditableRoot> roots = new ArrayList<>();
	final List<EditableSite> sites = new ArrayList<>();
	final Set<EditableOuterName> outers = new HashSet<>();
	final Set<EditableInnerName> inners = new HashSet<>();
	
	@SuppressWarnings("unchecked")
	private final List<Root> ro_roots = (List<Root>) (List<? extends Root>)  Collections.unmodifiableList(this.roots);
	@SuppressWarnings("unchecked")
	private final List<Site> ro_sites = (List<Site>) (List<? extends Site>)  Collections.unmodifiableList(this.sites);
	@SuppressWarnings("unchecked")
	private final Set<OuterName> ro_outers = (Set<OuterName>) (Set<? extends OuterName>)  Collections.unmodifiableSet(this.outers);
	@SuppressWarnings("unchecked")
	private final Set<InnerName> ro_inners = (Set<InnerName>) (Set<? extends InnerName>)  Collections.unmodifiableSet(this.inners);
	
	Bigraph(Signature sig){
		this.signature = sig;
	}
	
	boolean isConsistent(){
		//TODO implement consistency check
		throw new UnsupportedOperationException("Not implemented yet");
	}
	
	@Override
	public Bigraph clone(){
		/* firstly clone inner and outer names and store handles into a 
		 * translation map since ports are not yet cloned.
		 * then clones the place graph structure following the parent map 
		 * from roots to sites. during the visit follows outgoing links from 
		 * ports and clones edges and outer names if these are not already 
		 * present into the translation map. Idle edges are lost during 
		 * the process since these are not reachable. 
		 * The procedure may not terminate or raise exceptions if the bigraph
		 * is inconsistent (e.g. loops into the parent map or foreign sites/names)
		 */
		Bigraph big = new Bigraph(this.signature);
		BidMap<Handle,EditableHandle> trs = new BidMap<>();
		// replicate outer names
		for(EditableOuterName o : this.outers){
			EditableOuterName p = o.replicate();
			big.outers.add(p);
			trs.put(o, p);
		}
		// replicate inner names
		for(EditableInnerName i : this.inners){
			EditableInnerName j = i.replicate();
			// set replicated handle for j
			EditableHandle g = i.getHandle();
			// the bigraph is inconsistent if g is null
			EditableHandle h = g.replicate();
			j.setHandle(h);
			big.inners.add(j);
			trs.put(h, g);
		}
		// replicate place structure
		// the queue is used for a breadth first visit
		class Pair{
			final EditableChild c;
			final EditableParent p;
			Pair(EditableParent p, EditableChild c){
				this.c = c;
				this.p = p;
			}
		}
		Queue<Pair> q = new LinkedList<>();
		for(EditableRoot r : this.roots){
			EditableRoot s = r.replicate();
			big.roots.add(s);
			for(EditableChild c : r.getEditableChildren()){
				q.add(new Pair(s,c));
			}
		}
		EditableSite[] sites = new EditableSite[this.sites.size()];
		while(!q.isEmpty()){
			Pair p = q.poll();
			if(p.c instanceof EditableNode){
				EditableNode n = (EditableNode) p.c;
				EditableNode m = n.replicate();
				// set m's parent (which added adds m as its child)
				m.setParent(p.p);
				for(int i = n.getControl().getArity()-1;0 <= i;i--){
					EditablePort o = n.getPort(i);
					EditableHandle g = o.getHandle();
					// looks for an existing replica
					EditableHandle h = trs.get(g);
					if(h == null){
						// the bigraph is inconsistent if g is null
						h = g.replicate();
						trs.put(g, h);
					}
					m.getPort(i).setHandle(h);
				}
				// enqueue children for visit
				for(EditableChild c : n.getEditableChildren()){
					q.add(new Pair(m,c));
				}
			}else{
				// c instanceof EditableSit
				EditableSite s = (EditableSite) p.c;
				EditableSite t = s.replicate();
				t.setParent(p.p);
				// the order may be wrong
				sites[this.sites.indexOf(s)] = t;
			}
		}
		for(int i = 0; i< sites.length;i++){
			big.sites.add(sites[i]);
		}
		return big;
	}
	
	public Signature getSignature(){
		return this.signature;
	}
	
	public List<? extends Root> getRoots(){
		return this.ro_roots;
	}
	
	public List<? extends Site> getSites(){
		return this.ro_sites;
	}
	
	public Set<? extends OuterName> getOuterNames(){
		return this.ro_outers;
	}
	
	public Set<? extends InnerName> getInnerNames(){
		return this.ro_inners;
	}
	
	public static Bigraph juxtapose(Bigraph left, Bigraph right){
		// Arguments are assumed to be consistent (e.g. parent and links are well defined)
		if(left.signature != right.signature){
			throw new IncompatibleSignatureException(left.signature,right.signature);
		}
		if(!Collections.disjoint(left.inners,right.inners) || 
				!Collections.disjoint(left.outers,right.outers)){
			//TODO exceptions
			throw new IllegalArgumentException("Incompatible interfaces");
		}
		Bigraph l = left.clone();
		Bigraph r = right.clone();
		l.roots.addAll(r.roots);
		l.sites.addAll(r.sites);
		l.outers.addAll(r.outers);
		l.inners.addAll(r.inners);
		return l;
	}
	

	public static Bigraph compose(Bigraph out, Bigraph in){
		// Arguments are assumed to be consistent (e.g. parent and links are well defined)
		if(out.signature != in.signature){
			throw new IncompatibleSignatureException(out.signature,in.signature);
		}
		if(!out.inners.equals(in.outers) || out.sites.size() != in.roots.size()){
			//TODO exceptions
			throw new IllegalArgumentException("Incompatible interfaces");
		}
		Bigraph a = out.clone();
		Bigraph b = in.clone();
		// iterate over sites and roots of a and b respectively and glue them
		Iterator<EditableRoot> ir = b.roots.iterator();
		Iterator<EditableSite> is = a.sites.iterator();
		while (ir.hasNext()) { // |ir| == |is|
			EditableParent p = is.next().getParent();
			for(EditableChild c : ir.next().getEditableChildren()){
				c.setParent(p);
			}
		}
		// iterate over inner and outer names of a and b respectively and glue them
		for(EditableOuterName o : b.outers){
			for(EditableInnerName i : a.inners){
				if(!i.equals(o))
					continue;
				EditableHandle h = i.getHandle();
				for(EditablePoint p : o.getEditablePoints()){
					p.setHandle(h);
				}
				a.inners.remove(i);				
				break;
			}
		}
		// update inner interfaces
		a.inners.clear();
		a.sites.clear();
		a.inners.addAll(b.inners);
		a.sites.addAll(b.sites);
		return a;
	}
	
	public static Bigraph makeEmpty(Signature signature){
		return new Bigraph(signature);
	}
	
	//TODO factory methods
	
}
