package jlibbig.core.std;

import jlibbig.core.Owned;

interface EditableChild extends Child, ReplicableEx, Owned {
	void setParent(EditableParent parent);

	@Override
	public abstract EditableParent getParent();

	@Override
	public abstract EditableChild replicate();
}
