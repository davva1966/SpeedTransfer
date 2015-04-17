// {{CopyrightNotice}}

package com.ss.speedtransfer.ui;

import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

public class StyledEdit extends SourceViewer{
	
	public StyledEdit(Composite parent, SourceViewerConfiguration config){
		this(parent, config, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL, true);
	}

	public StyledEdit(Composite parent, SourceViewerConfiguration config, int style, boolean editable){
		super(parent, null, null, false, style);
		configure(config);

		IDocument document = new Document("");
		setEditable(editable);
		setDocument(document);
	}

	/**
	 * Get document text
	 * 
	 * @return text
	 */
	public String getText(){
		return getDocument().get();
	}

	/**
	 * Set document text
	 * 
	 * @param text
	 */
	public void setText(String text){
		getDocument().set(text);
	}

}
