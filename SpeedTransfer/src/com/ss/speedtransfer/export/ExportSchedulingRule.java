package com.ss.speedtransfer.export;

import org.eclipse.core.runtime.jobs.ISchedulingRule;

public class ExportSchedulingRule implements ISchedulingRule {

	protected String targetFile;

	public ExportSchedulingRule(String targetFile) {
		super();
		this.targetFile = targetFile;
	}

	@Override
	public boolean contains(ISchedulingRule rule) {
		return rule == this;
	}

	@Override
	public boolean isConflicting(ISchedulingRule rule) {
		if (rule == this)
			return true;

		if (rule instanceof ExportSchedulingRule)
			return ((ExportSchedulingRule) rule).getTargetFile().trim().equalsIgnoreCase(targetFile);

		return false;
	}

	public String getTargetFile() {
		return targetFile;
	}

}
