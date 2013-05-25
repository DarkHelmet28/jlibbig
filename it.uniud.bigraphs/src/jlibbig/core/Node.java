package jlibbig.core;

import java.util.*;

public interface Node extends Parent, Child {
	public List<? extends Port> getPorts();

	public Port getPort(int index);

	public Control getControl();

}
