package org.unix4j.unix.xargs;

import org.unix4j.variable.VariableContext;

class DefaultItemStorage implements ItemStorage {
	
	private final XargsLineProcessor processor;
	private final VariableContext variables;
	
	private final long maxLines;
	private final int maxArgs;
	private final boolean runWithoutItems;

	private int lines;
	private int items;
	
	public DefaultItemStorage(XargsLineProcessor processor) {
		this.processor = processor;
		this.variables = processor.getVariableContext();
		final XargsArguments args = processor.getArguments();
		this.maxLines = args.isMaxLinesSet() ? args.getMaxLines() : 1; 
		this.maxArgs = args.isMaxArgsSet() ? args.getMaxArgs() : Integer.MAX_VALUE; 
		this.runWithoutItems = !args.isNoRunIfEmpty(); 
	}

	@Override
	public void storeItem(String item) {
		variables.setValue(Xarg.arg(items), item);
		items++;
		if (items >= maxArgs) {
			invokeCommandAndClearAllItems();
			lines = 0;
		}
	}

	@Override
	public void incrementLineCount() {
		lines++;
		if (lines >= maxLines) {
			if (runWithoutItems || items > 0) {
				invokeCommandAndClearAllItems();
			}
			lines = 0;
		}
	}
	
	protected void flush() {
		if (runWithoutItems || items > 0) {
			invokeCommandAndClearAllItems();
		}
		lines = 0;
	}
	
	private void invokeCommandAndClearAllItems() {
		processor.invoke();
		while (items > 0) {
			items--;
			variables.setValue(Xarg.arg(items), null);//set null clears the variable
		}
	}

}
