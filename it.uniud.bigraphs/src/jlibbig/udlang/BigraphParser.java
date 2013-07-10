package jlibbig.udlang;

import java.util.ArrayList;
import java.io.*;
import beaver.*;
import java.util.*;
import jlibbig.core.*;

/**
 * This class is a LALR parser generated by
 * <a href="http://beaver.sourceforge.net">Beaver</a> v0.9.6.1
 * from the grammar specification "parser.grammar".
 */
public class BigraphParser extends Parser {
	static public class Terminals {
		static public final short EOF = 0;
		static public final short VARID = 1;
		static public final short ZERO = 2;
		static public final short ONE = 3;
		static public final short TAGOPEN = 4;
		static public final short PAROPEN = 5;
		static public final short SIGIL = 6;
		static public final short NIL = 7;
		static public final short POINT = 8;
		static public final short SEMICOLON = 9;
		static public final short PIPE = 10;
		static public final short PLUS = 11;
		static public final short DASH = 12;
		static public final short DPIPE = 13;
		static public final short SLASH = 14;
		static public final short NUM = 15;
		static public final short MODE = 16;
		static public final short COLON = 17;
		static public final short TAGCLOSE = 18;
		static public final short SQCLOSE = 19;
		static public final short SQOPEN = 20;
		static public final short REACT = 21;
		static public final short PARCLOSE = 22;
		static public final short COMMA = 23;
	}

	static final ParsingTables PARSING_TABLES = new ParsingTables(
		"U9ojaybhKr4KXtylQThKDcawsZPDqpQrTOXZ7UjGH131XIu4SIC8WgBWIbs8e5iLKT4wB1G" +
		"N8YWE2pV#CSxpFC$JzNitzuLdJDjDN$ZkEVUStpttvTnt9TqqtzIZ3cKqGPsQgExIkGR7L5" +
		"IeHTNgrm3qAQ4fYcYkkLMY6fUPCMhIB3MeaZrJXPtD3qLLXnSZLqKkRxu7EUKLLptUL4rNh" +
		"NcYDhLRFuysYRRVF4BRfrwbRRnGlOjyYimSLiwsQu9rCr6qecXI3QGjZQYQ8GlJbDCapJ2F" +
		"pLVpH$CqMVErG0krI8krH4krJ997VwAyNun$KVNVF3McejM#JPg#fEbA5lmkvKfKupCJ$Qk" +
		"c#$wU2RUpsDdtcbtE8nBaF9UhKItzJTfQlk#XRK2RPapOjwwpyKA55l89#bYeuQy9rgLH#A" +
		"SVvqpYhF7nLG4U9z38c9J$fFX6FCuxGzN0IaFxLNpYi1xMGXusGZriWcsm5RR03jWE0t0S1" +
		"k4eB8TXM0MdNDrTlYq7pnhO6PgVW3rmCXGx3GUTVp4Kl#JiMMY3Ax0Fhi8vk0xduGXSTjeR" +
		"S06EmRLGBRyFAqFp5Swk2yNs1bfdLpkxmTa3pXugNJF2VpzS9hQvJAncf1ekltkKt72Ps9c" +
		"GFwHH7lFHV63y0f$XatdF#CuyDy$CK$FMlB6Qr#QLUM6zb#QXUO2z3$VCNNFRt0dL#btqVX" +
		"MzdmmvVX5xExkXXrlLmztgi7ZsDhNphRV#GmmgmOE8ivxBUNmB5VRBpuJeeauNDelDOZijK" +
		"UvF1BoYxVqBqNAeTG==");

	static final Action RETURN2 = new Action() {
		public Symbol reduce(Symbol[] _symbols, int offset) {
			return _symbols[offset + 2];
		}
	};
	
		private BigraphSystem _sys;

		/**
		 * Generate a system (sets of bigraphs and reactions with the same signature) from a string.
		 * @param str the string that will be parsed.
		 * @return Return a system, carrying bigraphs and reactions with the same signature.
		 * @throws IOException
		 * @throws Parser.Exception
		 * @see BigraphSystem
		 */
		BigraphSystem parse( String str ) throws IOException, Parser.Exception{
			_sys = null;
			BigraphLexer input = new BigraphLexer( new StringReader( str ) );
			parse( input );

			return _sys;
		}

		/**
		 * This class stores inner and outer names
		 *
		 */
		private class NameId{
			private String name;
			private boolean outer;
			NameId( String name , boolean outer ){
				this.name = name;
				this.outer = outer;
			}	
		}

		/**
		 * Data Structure used to store the parsed bigraph
		 *
		 */
		private class ParsedBigraph{
			private boolean polymorphicSites;
			private BigraphBuilder bb;
			private List<Integer> siteNames;
			
			private ParsedBigraph( Signature sig ){
				polymorphicSites = false;
				bb = new BigraphBuilder( sig );
				siteNames = new ArrayList<>();
			}

			/**
			 * Add a Ion (also with inner link face) to the current bigraph
			 * @param c the name of the control
			 * @param li the list of outer and inner names
			 * @return the resulting bigraph
			 */
			private ParsedBigraph makeIon( String c , List<NameId> li ){
				polymorphicSites = true;	//this bigraph can change the number of its sites from 1 to 0.
				if( bb.getSignature().getByName( c ) == null )
					throw new IllegalArgumentException( "Control \"" + c +"\" should be in the signature." );
				
				//place graph
				Node node = bb.addNode( c , bb.addRoot() );
				bb.addSite( node );

				//link graph
				if( li == null ) return this;

				List<? extends Port> ports = node.getPorts();

				if( ports.size() < li.size() )
					throw new IllegalArgumentException( "Control \"" + c +"\" have " + ports.size() + " ports, " + li.size() + " ports found in one of its instances." );

				Map<String , OuterName> outerNames = new HashMap<>();
				Set<String> innerNames = new HashSet<>();

				Iterator<? extends Port> portIt = ports.iterator();
				for( NameId name : li ){
					if( name == null ){ 
						//case: "-" , skip this port (already has an edge attached to it)
						portIt.next();
						continue;
					}
					if( name.outer == true ){
						//case: "+p" or "p" , this name must appear in the outerface of this bigraph
						OuterName outer = outerNames.get( name.name );
						if( outer == null )
							outerNames.put( name.name , outer = bb.addOuterName( name.name ) );
						bb.relink( (Point) portIt.next() , outer );	
					}else{
						//case: "-p" , innerface
						if( innerNames.contains( name.name ) )
							throw new RuntimeException( "Innernames ( -" + name.name + " ) can't appear multiple time in a single bigraph." );
						Handle inner_edge = ((Port) portIt.next()).getHandle();
						bb.addInnerName( name.name , inner_edge );
						innerNames.add( name.name ); 
					}
				}
				
				return this;
			}

			/**
			 * Modify the link graph
			 * @param x the outer name that will be added. If null, an edge will be generated.
			 * @param li list of innernames that will be linked with x (the first parameter, edge or outername)
			 * @return the resulting bigraph
			 */
			private ParsedBigraph makeLinks( NameId x , List<NameId> li ){
				polymorphicSites = true;	//this bigraph can change the number of roots and sites 
				if( x == null && li.size() == 0 ) return this;	//case: empty , </> 

				Handle o = null;
				if( x != null && !x.outer )
					throw new IllegalArgumentException( "Innernames ( -" + x.name + " ) can't appear as the first argument of <x/xs> operator." ); 
				if( x != null )
					o = bb.addOuterName( x.name );
				
				Set<String> innerNames = new HashSet<>();	//set of innernames already added
				for( NameId name : li ){
					if( name != null ){
						if( innerNames.contains( name.name ) )
							throw new RuntimeException( "Innernames ( -" + name.name + " ) can't appear multiple time in a single bigraph." );
						if( o == null )
							o = bb.addInnerName( name.name ).getHandle();
						else
							bb.addInnerName( name.name , o );
						innerNames.add( name.name ); 
					}
				}
				return this;			
			}

			/**
			 * Close all sites of a bigraph
			 * @return the resulting bigraph
			 */
			private ParsedBigraph makeGroundPlaceGraph(){
				polymorphicSites = false;
				BigraphBuilder ground = new BigraphBuilder( bb.getSignature() );
				for( int i = 0; i < bb.getSites().size() ; ++i )
					ground.addRoot();
				for( InnerName in : bb.getInnerNames() )
					ground.addInnerName( in.getName() , ground.addOuterName( in.getName() ) );
				bb.innerCompose( ground.makeBigraph() );
				return this;
			}

			/**
			 * Add a site ($num) to a bigraph. Its parent will be a new root.
			 * @return the resulting bigraph
			 */
			private ParsedBigraph makeSite( int n ){
				if( siteNames.contains( n ) )
					throw new IllegalArgumentException( "The same site ($" + n + ") can't appear multiple time in a single bigraph." );
				bb.addSite( bb.addRoot() );
				siteNames.add( n );
				return this;
			}

			/**
			 * Modify the link graph, according to the parameters parsed from a <x/xs> operator.
			 * Inner names in the list (second parameter) will appear in the resulting bigraph's innerface.
			 * Other names in the list will be linked with the outerface of the current bigraph, if they belongs to its outerface, otherwise they will appear in the resulting bigraph's innerface.
			 * Outernames of the current bigraph not linked with names in the list will appear in the resulting bigraph's outerface.
			 * @param x outername , it will appear in the resulting bigraph's outerface.
			 * @param li list of inner and outer names.
			 */
			private void addLinks( NameId x , List<NameId> li ){
				if( x == null && li.size() == 0 ) return;
				
				BigraphBuilder outer = new BigraphBuilder( bb.getSignature() );
				Set<String> bb_inner = new HashSet<>();
				Set<String> bb_outer = new HashSet<>();
				Set<String> outer_done = new HashSet<>();

				for( InnerName inner : this.bb.getInnerNames() )
					bb_inner.add( inner.getName() );
				for( OuterName out : this.bb.getOuterNames() )
					bb_outer.add( out.getName() );
			
				for(int i = 0 ; i<this.bb.getRoots().size() ; ++i )
					outer.addSite( outer.addRoot() );

				Handle o = null;
				if( x != null && !x.outer )
					throw new IllegalArgumentException( "Innernames ( -" + x.name + " ) can't appear as the first argument of <x/xs> operator." ); 
				if( x != null )
					o = outer.addOuterName( x.name );
				
				for( NameId name : li ){
					if( name != null ){
						boolean asInner = false;
						if( name.outer ){
							if( bb_outer.contains( name.name ) ){	//name.name will be an outername
								if( outer_done.contains( name.name ) )
									throw new RuntimeException( "Found the same OuterName ( " + name.name + " ) multiple time in the second argument of <x/xs> operation." );
								if( o == null )
									o = outer.addInnerName( name.name ).getHandle();
								else
									outer.addInnerName( name.name , o );
								outer_done.add( name.name );
							}else asInner = true;						
						}
						if( !name.outer || asInner ){	//name.name will be an innername in the resulting bigraph
							if( bb_inner.contains( name.name ) )
								throw new RuntimeException( "Innernames ( -" + name.name + " ) can't appear multiple time in a single bigraph." );
							OuterName on = this.bb.addOuterName();
							this.bb.addInnerName( name.name , on );
							bb_inner.add( name.name );

							if( o == null )
								o = outer.addInnerName( on.getName() ).getHandle();
							else
								outer.addInnerName( on.getName() , o );
						}
					}
				}

				Set<String> l_inner = new HashSet<>();
				for( InnerName on : outer.getInnerNames() )
					l_inner.add( on.getName() );

				//bigraph's outernames that don't appear in xs will appear in the resulting bigraph's outerface
				for( String str : bb_outer ){
					if( !l_inner.contains( str ) ){
						if( str.equals( x.name ) ){
							outer.addInnerName( x.name , o );
							continue;
						}
						outer.addInnerName( str , outer.addOuterName( str ) );
					}			
				}

				this.bb.outerCompose( outer.makeBigraph() );
			}

			/**
			 * Compose the bigraph in input with the current bigraph.
			 * @param p the bigraph that will be composed with the current one.
			 */
			private void compose( ParsedBigraph p ){
				//if p is an instance of <x/xs> -> identity place graph
				if( p.polymorphicSites && p.bb.getSites().size() == 0 ){
					for(int i = 0 ; i < this.bb.getRoots().size() ; ++i )
						p.bb.addSite( p.bb.addRoot() );
				}

				if(!p.polymorphicSites)
					p.adjustBigraph();
				
				//middle: auxiliary bigraph 
				BigraphBuilder middle = new BigraphBuilder( this.bb.getSignature() );
				for(int i = 0 ; i < this.bb.getRoots().size() ; ++i )
					middle.addSite( middle.addRoot() );
				
				Set<? extends InnerName> down_inner = new HashSet<>( this.bb.getInnerNames() );
				Set<? extends OuterName> down_outer = new HashSet<>( this.bb.getOuterNames() );
				Set<? extends InnerName> up_inner = new HashSet<>( p.bb.getInnerNames() );
				Set<? extends OuterName> up_outer = new HashSet<>( p.bb.getOuterNames() );

				//at the end of this cycle, all names in the inner bigraph are handled
				for( OuterName in : down_outer ){
					boolean exists = false;
					for( InnerName out : up_inner ){
						if(in.getName().equals( out.getName() ) ){
							exists = true;
							break;
						}
					}
					String middleOuter = in.getName();
					if( !exists ){
						OuterName o = null;
						for( OuterName out : up_outer ){
							if(in.getName().equals( out.getName() ) ){
								o = out;
								break;
							}
						}
						if( o != null ){
							middleOuter = p.bb.addInnerName( o ).getName();
						}else{
							p.bb.addInnerName( in.getName() , p.bb.addOuterName( in.getName() ) );
						}
					}
					
					middle.addInnerName( in.getName() , middle.addOuterName( middleOuter ) );
				}

				//handle outer bigraph's inner names that aren't in inner bigraph's outerface
				for( InnerName in : up_inner ){
					boolean exists = false;
					for( OuterName out : down_outer ){
						if(in.getName().equals( out.getName() ) ){
							exists = true;
							break;
						}
					}
					if( !exists ){
						for( InnerName ni : down_inner ){
							if(in.getName().equals( ni.getName() ) )
								throw new RuntimeException( "Innername ( -" + ni.getName() + " , contained in the first argument of a composition ) can't appear in the inner face of the resulting bigraph and can't be matched with an OuterName of the second argument." );
							
						}
						middle.addInnerName( in.getName() , middle.addOuterName( in.getName() ) );
						this.bb.addInnerName( in.getName() , this.bb.addOuterName( in.getName() ) );
					}
				}

				this.bb.outerCompose( middle.makeBigraph() );
				this.bb.outerCompose( p.bb.makeBigraph() );
			}

			/**
			 * Juxtapose the bigraph in input with the current bigraph.
			 * @param p the bigraph that will be juxtaposed with the current one.
			 */
			private void juxtapose( ParsedBigraph p ){
				if( p.polymorphicSites && p.bb.getSites().size() == 1 )
					p.makeGroundPlaceGraph();
				if( this.polymorphicSites && this.bb.getSites().size() == 1 )
					this.makeGroundPlaceGraph();
								
				Set<String> right_inner = new HashSet();
				for(InnerName in : p.bb.getInnerNames() )
					right_inner.add( in.getName() );

				for(InnerName in : this.bb.getInnerNames() ){
					if( right_inner.contains( in.getName() ) )
						throw new RuntimeException( "Innernames ( -" + in.getName() + " ) can't appear multiple time in a single bigraph." );
				}

				Set<String> right_outer = new HashSet();
				for(OuterName out : p.bb.getOuterNames() )
					right_outer.add( out.getName() );

				//left_middle: bigraph used for renaming the outernames of the left bigraph, it prevents name clashing
				//original names will be restored with the bigraph named "outer"
				BigraphBuilder left_middle = new BigraphBuilder( bb.getSignature() );
				BigraphBuilder outer = new BigraphBuilder( bb.getSignature() );

				for(int i = 0 ; i < this.bb.getRoots().size() ; ++i )
					left_middle.addSite( left_middle.addRoot() );

				for(OuterName on : this.bb.getOuterNames() ){
					if(right_outer.contains( on.getName() ) ){
						OuterName x = left_middle.addOuterName();
						left_middle.addInnerName( on.getName() , x );
						OuterName o = outer.addOuterName( on.getName() );
						outer.addInnerName( x.getName() , o );
						outer.addInnerName( on.getName() , o );
					}else{
						left_middle.addInnerName( on.getName() , left_middle.addOuterName( on.getName() ) );
						outer.addInnerName( on.getName() , outer.addOuterName( on.getName() ) );
					}
				}

				Set<String> left_outer = new HashSet();
				for(OuterName out : this.bb.getOuterNames() )
					left_outer.add( out.getName() );

				for(String str : right_outer ){
					if( !left_outer.contains( str ) ){
						outer.addInnerName( str , outer.addOuterName( str ) );
					}
				}
				
				this.bb.outerCompose( left_middle.makeBigraph() );
				this.bb.rightJuxtapose( p.bb.makeBigraph() );
				
				for(int i = 0 ; i < this.bb.getRoots().size() ; ++i )
					outer.addSite( outer.addRoot() );
				
				this.bb.outerCompose( outer.makeBigraph() );
				this.siteNames.addAll( p.siteNames );

			}

			/**
			 * produce a bigraph from the current parsedbigraph
			 * @return the resulting bigraph
			 * @see Bigraph
			 */
			private Bigraph makeBigraph(){
				if( this.polymorphicSites && this.bb.getSites().size() == 1 )
					this.makeGroundPlaceGraph();
				else
					this.adjustBigraph();
				return bb.makeBigraph();
			}

			/**
			 * Sort the current bigraph's sites.
			 */
			private void adjustBigraph(){
				class SiteInt implements Comparable<SiteInt>{
					private Root root;
					private Integer pos;
					SiteInt( Root r , int i ){
						root = r;
						pos = i;
					}
					public int compareTo( SiteInt si ){
						return pos.compareTo( si.pos );
					}
				}

				
				if( bb.getSites().size() != siteNames.size() )
					throw new RuntimeException( "Error while checking the number of sites. Indices Lost." );

				List<SiteInt> siteints = new ArrayList<>();
				BigraphBuilder inner = new BigraphBuilder( bb.getSignature() );
				for( InnerName in : bb.getInnerNames() )
					inner.addInnerName( in.getName() , inner.addOuterName( in.getName() ) );

				for( Integer v : siteNames )
					siteints.add( new SiteInt( inner.addRoot() , v ) );
				
				Collections.sort( siteints );
				for( SiteInt sint : siteints )
					inner.addSite( sint.root );
					
				bb.innerCompose( inner.makeBigraph() );
			}
		}

	private final Action[] actions;

	public BigraphParser() {
		super(PARSING_TABLES);
		actions = new Action[] {
			RETURN2,	// [0] start = definitions reactions; returns 'reactions' although none is marked
			Action.RETURN,	// [1] start = reactions
			new Action() {	// [2] definitions = controls.sb
				public Symbol reduce(Symbol[] _symbols, int offset) {
					final Symbol _symbol_sb = _symbols[offset + 1];
					final SignatureBuilder sb = (SignatureBuilder) _symbol_sb.value;
					 _sys = new BigraphSystem( sb.makeSignature() ); return new Symbol( null );
				}
			},
			new Action() {	// [3] controls = controls.sb MODE.b VARID.v COLON num.n SEMICOLON
				public Symbol reduce(Symbol[] _symbols, int offset) {
					final Symbol _symbol_sb = _symbols[offset + 1];
					final SignatureBuilder sb = (SignatureBuilder) _symbol_sb.value;
					final Symbol _symbol_b = _symbols[offset + 2];
					final boolean b = (boolean) _symbol_b.value;
					final Symbol _symbol_v = _symbols[offset + 3];
					final String v = (String) _symbol_v.value;
					final Symbol _symbol_n = _symbols[offset + 5];
					final int n = (int) _symbol_n.value;
					 
					if( sb.contains( v ) )
						throw new RuntimeException( "Line: " + Symbol.getLine( _symbol_b.getStart() )  
										+ " - Control already defined: " + v );
					sb.put( v , b , n ); 
					return new Symbol( sb );
				}
			},
			new Action() {	// [4] controls = MODE.b VARID.v COLON num.n SEMICOLON
				public Symbol reduce(Symbol[] _symbols, int offset) {
					final Symbol _symbol_b = _symbols[offset + 1];
					final boolean b = (boolean) _symbol_b.value;
					final Symbol _symbol_v = _symbols[offset + 2];
					final String v = (String) _symbol_v.value;
					final Symbol _symbol_n = _symbols[offset + 4];
					final int n = (int) _symbol_n.value;
					 
					SignatureBuilder sb = new SignatureBuilder(); 
					sb.put( v , b , n );
					return new Symbol( sb );
				}
			},
			new Action() {	// [5] reactions = big.r REACT big.s SEMICOLON reactions
				public Symbol reduce(Symbol[] _symbols, int offset) {
					final Symbol _symbol_r = _symbols[offset + 1];
					final ParsedBigraph r = (ParsedBigraph) _symbol_r.value;
					final Symbol _symbol_s = _symbols[offset + 3];
					final ParsedBigraph s = (ParsedBigraph) _symbol_s.value;
					 
					_sys.addReaction( r.makeBigraph() , s.makeBigraph() );
					return new Symbol( null );
				}
			},
			Action.RETURN,	// [6] reactions = bigraphs
			Action.NONE,  	// [7] bigraphs = 
			new Action() {	// [8] bigraphs = big.b SEMICOLON bigraphs
				public Symbol reduce(Symbol[] _symbols, int offset) {
					final Symbol _symbol_b = _symbols[offset + 1];
					final ParsedBigraph b = (ParsedBigraph) _symbol_b.value;
					 
					_sys.addBigraph( b.makeBigraph() );
					return new Symbol( null );
				}
			},
			new Action() {	// [9] big = big.b DPIPE big.p
				public Symbol reduce(Symbol[] _symbols, int offset) {
					final Symbol _symbol_b = _symbols[offset + 1];
					final ParsedBigraph b = (ParsedBigraph) _symbol_b.value;
					final Symbol _symbol_p = _symbols[offset + 3];
					final ParsedBigraph p = (ParsedBigraph) _symbol_p.value;
					 
					try{
						b.juxtapose( p );
					}catch( IllegalArgumentException e ){
						throw new IllegalArgumentException( "Line: " + Symbol.getLine( _symbol_p.getStart() )  
										+ " - " + e.getMessage() );
					}catch( RuntimeException e ){
						throw new RuntimeException( "Line: " + Symbol.getLine( _symbol_p.getStart() )  
										+ " - " + e.getMessage() );
					}
					return new Symbol( b );
				}
			},
			new Action() {	// [10] big = big.b PIPE big.p
				public Symbol reduce(Symbol[] _symbols, int offset) {
					final Symbol _symbol_b = _symbols[offset + 1];
					final ParsedBigraph b = (ParsedBigraph) _symbol_b.value;
					final Symbol _symbol_p = _symbols[offset + 3];
					final ParsedBigraph p = (ParsedBigraph) _symbol_p.value;
					
					try{
						b.juxtapose( p );
					}catch( IllegalArgumentException e ){
						throw new IllegalArgumentException( "Line: " + Symbol.getLine( _symbol_p.getStart() )  
										+ " - " + e.getMessage() );
					}catch( RuntimeException e ){
						throw new RuntimeException( "Line: " + Symbol.getLine( _symbol_p.getStart() )  
										+ " - " + e.getMessage() );
					}
					b.bb.merge();
					return new Symbol( b );
				}
			},
			new Action() {	// [11] big = big.p POINT big.b
				public Symbol reduce(Symbol[] _symbols, int offset) {
					final Symbol _symbol_p = _symbols[offset + 1];
					final ParsedBigraph p = (ParsedBigraph) _symbol_p.value;
					final Symbol _symbol_b = _symbols[offset + 3];
					final ParsedBigraph b = (ParsedBigraph) _symbol_b.value;
					
					try{
						b.compose( p );
					}catch( IllegalArgumentException e ){
						throw new IllegalArgumentException( "Line: " + Symbol.getLine( _symbol_p.getStart() )  
										+ " - " + e.getMessage() );
					}catch( RuntimeException e ){
						throw new RuntimeException( "Line: " + Symbol.getLine( _symbol_p.getStart() )  
										+ " - " + e.getMessage() );
					}
					
					return new Symbol( b );
				}
			},
			new Action() {	// [12] big = TAGOPEN voidorname.x SLASH names.l TAGCLOSE
				public Symbol reduce(Symbol[] _symbols, int offset) {
					final Symbol _symbol_x = _symbols[offset + 2];
					final NameId x = (NameId) _symbol_x.value;
					final Symbol _symbol_l = _symbols[offset + 4];
					final LinkedList<NameId> l = (LinkedList<NameId>) _symbol_l.value;
					
					ParsedBigraph b;
					try{
						b = ( new ParsedBigraph( _sys.getSignature() ) ).makeLinks( x , l );
					}catch( IllegalArgumentException e ){
						throw new IllegalArgumentException( "Line: " + Symbol.getLine( _symbol_x.getStart() )  
										+ " - " + e.getMessage() );
					}catch( RuntimeException e ){
						throw new RuntimeException( "Line: " + Symbol.getLine( _symbol_l.getStart() )  
										+ " - " + e.getMessage() );
					}
					
					return new Symbol( b );
				}
			},
			new Action() {	// [13] big = TAGOPEN voidorname.x SLASH names.l TAGCLOSE big.b
				public Symbol reduce(Symbol[] _symbols, int offset) {
					final Symbol _symbol_x = _symbols[offset + 2];
					final NameId x = (NameId) _symbol_x.value;
					final Symbol _symbol_l = _symbols[offset + 4];
					final LinkedList<NameId> l = (LinkedList<NameId>) _symbol_l.value;
					final Symbol _symbol_b = _symbols[offset + 6];
					final ParsedBigraph b = (ParsedBigraph) _symbol_b.value;
					
					try{
						b.addLinks( x , l );
					}catch( IllegalArgumentException e ){
						throw new IllegalArgumentException( "Line: " + Symbol.getLine( _symbol_x.getStart() )  
										+ " - " + e.getMessage() );
					}catch( RuntimeException e ){
						throw new RuntimeException( "Line: " + Symbol.getLine( _symbol_l.getStart() )  
										+ " - " + e.getMessage() );
					}
					
					return new Symbol( b );
				}
			},
			new Action() {	// [14] big = place.pb
				public Symbol reduce(Symbol[] _symbols, int offset) {
					final Symbol _symbol_pb = _symbols[offset + 1];
					final ParsedBigraph pb = (ParsedBigraph) _symbol_pb.value;
					 return new Symbol( pb );
				}
			},
			new Action() {	// [15] big = SIGIL num.n
				public Symbol reduce(Symbol[] _symbols, int offset) {
					final Symbol _symbol_n = _symbols[offset + 2];
					final int n = (int) _symbol_n.value;
					 return new Symbol( ( new ParsedBigraph( _sys.getSignature() ) ).makeSite( n ) );
				}
			},
			new Action() {	// [16] big = ZERO
				public Symbol reduce(Symbol[] _symbols, int offset) {
					 ParsedBigraph b = new ParsedBigraph( _sys.getSignature() ); return new Symbol( b );
				}
			},
			new Action() {	// [17] big = ONE
				public Symbol reduce(Symbol[] _symbols, int offset) {
					 ParsedBigraph b = new ParsedBigraph( _sys.getSignature() ); b.bb.addRoot(); return new Symbol( b );
				}
			},
			new Action() {	// [18] big = PAROPEN big.pb PARCLOSE
				public Symbol reduce(Symbol[] _symbols, int offset) {
					final Symbol _symbol_pb = _symbols[offset + 2];
					final ParsedBigraph pb = (ParsedBigraph) _symbol_pb.value;
					 
				return new Symbol( pb );
				}
			},
			new Action() {	// [19] big = NIL
				public Symbol reduce(Symbol[] _symbols, int offset) {
					 ParsedBigraph b = new ParsedBigraph( _sys.getSignature() ); b.bb.addRoot(); return new Symbol( b );
				}
			},
			new Action() {	// [20] place = VARID.c SQOPEN names.l SQCLOSE
				public Symbol reduce(Symbol[] _symbols, int offset) {
					final Symbol _symbol_c = _symbols[offset + 1];
					final String c = (String) _symbol_c.value;
					final Symbol _symbol_l = _symbols[offset + 3];
					final LinkedList<NameId> l = (LinkedList<NameId>) _symbol_l.value;
					 
					if( _sys.getSignature() == null )
						throw new RuntimeException( "Signature not found\n" );
					ParsedBigraph b; 
					try{
						b = ( new ParsedBigraph( _sys.getSignature() ) ).makeIon( c , l );
					}catch( IllegalArgumentException e ){
						throw new IllegalArgumentException( "Line: " + Symbol.getLine( _symbol_c.getStart() )  
										+ " - " + e.getMessage() );
					}catch( RuntimeException e ){
						throw new RuntimeException( "Line: " + Symbol.getLine( _symbol_l.getStart() )  
										+ " - " + e.getMessage() );
					}
					
					return new Symbol( b );
				}
			},
			new Action() {	// [21] place = VARID.c
				public Symbol reduce(Symbol[] _symbols, int offset) {
					final Symbol _symbol_c = _symbols[offset + 1];
					final String c = (String) _symbol_c.value;
					 
					ParsedBigraph b;
					try{
						b = ( new ParsedBigraph( _sys.getSignature() ) ).makeIon( c , new LinkedList<NameId>() );
					}catch( IllegalArgumentException e ){
						throw new IllegalArgumentException( "Line: " + Symbol.getLine( _symbol_c.getStart() )  
										+ " - " + e.getMessage() );
					}catch( RuntimeException e ){
						throw new RuntimeException( "Line: " + Symbol.getLine( _symbol_c.getStart() )  
										+ " - " + e.getMessage() );
					}
					return new Symbol( b );
				}
			},
			new Action() {	// [22] num = ZERO
				public Symbol reduce(Symbol[] _symbols, int offset) {
					 return new Symbol( 0 );
				}
			},
			new Action() {	// [23] num = ONE
				public Symbol reduce(Symbol[] _symbols, int offset) {
					 return new Symbol( 1 );
				}
			},
			new Action() {	// [24] num = NUM.n
				public Symbol reduce(Symbol[] _symbols, int offset) {
					final Symbol _symbol_n = _symbols[offset + 1];
					final int n = (int) _symbol_n.value;
					 return new Symbol( n );
				}
			},
			new Action() {	// [25] voidorname = 
				public Symbol reduce(Symbol[] _symbols, int offset) {
					 return new Symbol( null );
				}
			},
			new Action() {	// [26] voidorname = name.n
				public Symbol reduce(Symbol[] _symbols, int offset) {
					final Symbol _symbol_n = _symbols[offset + 1];
					final NameId n = (NameId) _symbol_n.value;
					 return new Symbol( n );
				}
			},
			new Action() {	// [27] names = 
				public Symbol reduce(Symbol[] _symbols, int offset) {
					 return new Symbol( new LinkedList<>() );
				}
			},
			new Action() {	// [28] names = ns.l
				public Symbol reduce(Symbol[] _symbols, int offset) {
					final Symbol _symbol_l = _symbols[offset + 1];
					final LinkedList<NameId> l = (LinkedList<NameId>) _symbol_l.value;
					 return new Symbol( l );
				}
			},
			new Action() {	// [29] ns = name.n
				public Symbol reduce(Symbol[] _symbols, int offset) {
					final Symbol _symbol_n = _symbols[offset + 1];
					final NameId n = (NameId) _symbol_n.value;
					 List<NameId> l = new LinkedList<>(); l.add( n ); return new Symbol( l );
				}
			},
			new Action() {	// [30] ns = name.n COMMA ns.l
				public Symbol reduce(Symbol[] _symbols, int offset) {
					final Symbol _symbol_n = _symbols[offset + 1];
					final NameId n = (NameId) _symbol_n.value;
					final Symbol _symbol_l = _symbols[offset + 3];
					final LinkedList<NameId> l = (LinkedList<NameId>) _symbol_l.value;
					 l.addFirst( n ); return new Symbol( l );
				}
			},
			new Action() {	// [31] name = PLUS VARID.n
				public Symbol reduce(Symbol[] _symbols, int offset) {
					final Symbol _symbol_n = _symbols[offset + 2];
					final String n = (String) _symbol_n.value;
					 return new Symbol( new NameId( n , true ) );
				}
			},
			new Action() {	// [32] name = VARID.n
				public Symbol reduce(Symbol[] _symbols, int offset) {
					final Symbol _symbol_n = _symbols[offset + 1];
					final String n = (String) _symbol_n.value;
					 return new Symbol( new NameId( n , true ) );
				}
			},
			new Action() {	// [33] name = DASH VARID.n
				public Symbol reduce(Symbol[] _symbols, int offset) {
					final Symbol _symbol_n = _symbols[offset + 2];
					final String n = (String) _symbol_n.value;
					 return new Symbol( new NameId( n , false ) );
				}
			},
			new Action() {	// [34] name = DASH
				public Symbol reduce(Symbol[] _symbols, int offset) {
					 return new Symbol( null );
				}
			}
		};
	}

	protected Symbol invokeReduceAction(int rule_num, int offset) {
		return actions[rule_num].reduce(_symbols, offset);
	}
}
