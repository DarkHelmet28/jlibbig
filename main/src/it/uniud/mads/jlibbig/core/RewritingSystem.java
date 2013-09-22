package it.uniud.mads.jlibbig.core;

import java.util.*;

import it.uniud.mads.jlibbig.core.std.Signature;

public interface RewritingSystem<A extends Bigraph<?>, B extends Bigraph<?>>
		extends ReactiveSystem<A> {

	@Override
	public abstract Signature getSignature();

	@Override
	public abstract Set<? extends RewritingRule<? extends A, ? extends B>> getRules();

	@Override
	public abstract Set<? extends A> getBigraphs();
}