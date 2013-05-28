package jlibbig.core;

import java.util.*;

import jlibbig.core.EditableNode.EditablePort;

public class Bigraph implements AbstBigraph {

	final Signature signature;
	final List<EditableRoot> roots = new ArrayList<>();
	final List<EditableSite> sites = new ArrayList<>();
	final Set<EditableOuterName> outers = new HashSet<>();
	final Set<EditableInnerName> inners = new HashSet<>();
	
	private final List<? extends Root> ro_roots = Collections.unmodifiableList(this.roots);
	private final List<? extends Site> ro_sites = Collections.unmodifiableList(this.sites);
	private final Set<? extends OuterName> ro_outers = Collections.unmodifiableSet(this.outers);
	private final Set<? extends InnerName> ro_inners = Collections.unmodifiableSet(this.inners);
	
	Bigraph(Signature sig){
		this.signature = sig;
	}
	
	boolean isConsistent(){
		Set<Point> ps = new HashSet<>();
		Set<Handle> hs = new HashSet<>();
		Set<Site> unseen_sites = new HashSet<>();
		unseen_sites.addAll(this.sites);
		Set<Child> seen = new HashSet<>();
		Queue<Parent> q = new LinkedList<>();
		for(EditableRoot r : this.roots){
			if(r.getOwner() != this)
				return false;
			q.add(r);
		}
		while(!q.isEmpty()){
			Parent p = q.poll();
			for(Child c : p.getChildren()){
				if(!p.equals(c.getParent())){
					// faux parent/child
					return false;
				}
				if (!seen.add(c)){
					// c was already visited
					// we have found a cycle (or diamond) in the place structure
					return false;
				}else if(c instanceof EditableNode){
					EditableNode n = (EditableNode) c;
					if(n.getControl().getArity() != n.getPorts().size() || !signature.contains(n.getControl())){
						return false;
					}
					q.add(n);
					for(Point t : n.getPorts()){
						EditableHandle h = ((EditablePoint) t).getHandle();
						if(h == null || h.getOwner() != this)
							// foreign or broken handle
							return false;
						if(!h.getPoints().contains(t))
							// broken link chain
							return false;
						ps.add(t);
						hs.add(h);
					}
				}else if(c instanceof EditableSite){
					Site s = (Site) c;
					unseen_sites.remove(s);
					if(!this.sites.contains(s)){
						//unknown site
						return false;
					}
				}else{
					// c is neither a site nor a node
					return false;
				}
			}
		}
		for(EditableInnerName n : this.inners){
			if(n.getOwner() != this) // || n.getHandle() == null is implicit 
				return false;
			ps.add(n);
		}
		for(Handle h : hs){
			Set<? extends Point> ts = h.getPoints();
			// ts must be contained in ps
			for(Point t : ts){
				if(!ps.remove(t))
					// foreign point
					return false;
			}	
		}
		if(ps.size() > 0){
			// broken handle chain
			return false;
		}
		if(unseen_sites.size() > 0){
			// these sites are unreachable from roots
			return false;
		}
		return true;		
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
			p.setOwner(big);
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
			s.setOwner(big);
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
						h.setOwner(big);
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
	
	/* (non-Javadoc)
	 * @see jlibbig.core.AbstBigraph#getSignature()
	 */
	@Override
	public Signature getSignature(){
		return this.signature;
	}
	
	/* (non-Javadoc)
	 * @see jlibbig.core.AbstBigraph#getRoots()
	 */
	@Override
	public List<? extends Root> getRoots(){
		return this.ro_roots;
	}
	
	/* (non-Javadoc)
	 * @see jlibbig.core.AbstBigraph#getSites()
	 */
	@Override
	public List<? extends Site> getSites(){
		return this.ro_sites;
	}
	
	/* (non-Javadoc)
	 * @see jlibbig.core.AbstBigraph#getOuterNames()
	 */
	@Override
	public Set<? extends OuterName> getOuterNames(){
		return this.ro_outers;
	}
	
	/* (non-Javadoc)
	 * @see jlibbig.core.AbstBigraph#getInnerNames()
	 */
	@Override
	public Set<? extends InnerName> getInnerNames(){
		return this.ro_inners;
	}
	
	/* (non-Javadoc)
	 * @see jlibbig.core.AbstBigraph#getNodes()
	 */
	@Override
	public Set<? extends Node> getNodes(){
		Set<EditableNode> s = new HashSet<>();
		Queue<EditableNode> q = new LinkedList<>();
		for(Root r : this.roots){
			for(Child c : r.getChildren()){
				if(c instanceof EditableNode){
					EditableNode n = (EditableNode) c;
					q.add(n);
				}
			}
		}
		while(!q.isEmpty()){
			EditableNode p = q.poll();
			s.add(p);
			for(Child c : p.getChildren()){
				if(c instanceof EditableNode){
					EditableNode n = (EditableNode) c;
					q.add(n);
				}
			}
		}
		return s;
	}
	
	/* (non-Javadoc)
	 * @see jlibbig.core.AbstBigraph#getEdges()
	 */
	@Override
	public Set<? extends Edge> getEdges(){
		Set<Edge> s = new HashSet<>();
		for(Node n : this.getNodes()){
			for(Port p : n.getPorts()){
				Handle h = p.getHandle();
				if(h instanceof Edge){
					s.add((Edge) h);
				}
			}
		}
		for(InnerName n : this.inners){
			Handle h = n.getHandle();
			if(h instanceof Edge){
				s.add((Edge) h);
			}
		}
		return s;
	}
	
	public static Bigraph juxtapose(Bigraph left, Bigraph right){
		return juxtapose(left,right,false);
	}
	
	static Bigraph juxtapose(Bigraph left, Bigraph right,boolean reuse){
		// Arguments are assumed to be consistent (e.g. parent and links are well defined)
		if(left.signature != right.signature){
			throw new IncompatibleSignatureException(left.signature,right.signature);
		}
		if(!Collections.disjoint(left.inners,right.inners) || 
				!Collections.disjoint(left.outers,right.outers)){
			//TODO exceptions
			throw new IllegalArgumentException("Incompatible interfaces");
		}
		Bigraph l = (reuse) ? left : left.clone();
		Bigraph r = (reuse) ? right : right.clone();
		l.roots.addAll(r.roots);
		l.sites.addAll(r.sites);
		l.outers.addAll(r.outers);
		l.inners.addAll(r.inners);
		return l;
	}
	
	public static Bigraph compose(Bigraph out, Bigraph in){
		return compose(out,in,false);
	}
	

	public static Bigraph compose(Bigraph out, Bigraph in, boolean reuse){
		// Arguments are assumed to be consistent (e.g. parent and links are well defined)
		if(out.signature != in.signature){
			throw new IncompatibleSignatureException(out.signature,in.signature);
		}
		if(!out.inners.equals(in.outers) || out.sites.size() != in.roots.size()){
			//TODO exceptions
			throw new IllegalArgumentException("Incompatible interfaces");
		}
		Bigraph a = (reuse) ? out : out.clone();
		Bigraph b = (reuse) ? in : in.clone();
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
