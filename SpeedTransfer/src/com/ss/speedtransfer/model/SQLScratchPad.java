package com.ss.speedtransfer.model;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.w3c.dom.Document;

public class SQLScratchPad extends QueryDefinition {

	public static final String CATALOG_KEY = "http://com.ss.speedtransfer.schema/SQLScratchPad.xsd";

	// XML Node names
	public static final String SQL_SCRATCH_PAD = "sqlScratchPad";//$NON-NLS-1$

	public SQLScratchPad() {
		super();
	}

	public SQLScratchPad(String file) throws Exception {
		this(new FileInputStream(file));
		setName(file);

	}

	public SQLScratchPad(IFile file) throws Exception {
		this(file.getContents());
		setName(file.getName());
		this.file = file;
	}

	public SQLScratchPad(InputStream stream) throws Exception {
		super(stream, SQL_SCRATCH_PAD);

	}

	public SQLScratchPad(Document document) {
		super(document, SQL_SCRATCH_PAD);
	}

	public String getColumnHeading() {
		return null;
	}

	public Map<String, Object> getProperties() {
		return null;
	}

	public boolean hasReplacementVariables() {
		return false;

	}

	public static String getCatalogKey() {
		return CATALOG_KEY;
	}
	

}
