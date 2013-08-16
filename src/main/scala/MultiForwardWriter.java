package com.rayrobdod.util;

import java.io.Writer;
import java.io.StringWriter;
import java.io.IOException;
import java.util.LinkedList;

/**
 * A Writer that will forward messages to other writers.
 * <p>
 * Anything written to this will be written to any forwards added to this writer.
 * In addition, when added to this writer, any history this writer has will be
 * added to that writer.
 *
 * @author Raymond Dodge
 * @version 2013 Aug 16
 */
public class MultiForwardWriter extends Writer {
	private final StringWriter history; 
	private final LinkedList<Writer> forwardTo;
	
	public MultiForwardWriter() {
		super();
		history = new StringWriter();
		forwardTo = new LinkedList<Writer>();
		
		forwardTo.add(history);
	}
	
	@Override
	public void close() throws IOException {
		synchronized(this) {
			for (Writer w : forwardTo) { w.close(); }
		}
	}
	
	@Override
	public void flush() throws IOException {
		synchronized(this) {
			for (Writer w : forwardTo) { w.flush(); }
		}
	}
	
	@Override
	public void write(char[] str, int off, int len) throws IOException {
		synchronized(this) {
			for (Writer w : forwardTo) { w.write(str, off, len); }
		}
	}
	
	public void addForward(Writer w) throws IOException {
		synchronized(this) {
			w.write(history.toString());
			w.flush();
			forwardTo.add(w);
		}
	}
	
	public void removeForward(Writer w) {
		synchronized(this) {
			forwardTo.remove(w);
		}
	}
}
