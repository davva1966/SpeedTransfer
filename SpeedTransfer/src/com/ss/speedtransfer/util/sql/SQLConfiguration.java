package com.ss.speedtransfer.util.sql;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextDoubleClickStrategy;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.formatter.IContentFormatter;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;

public class SQLConfiguration extends SourceViewerConfiguration {

	private SQLDoubleClickStrategy doubleClickStrategy;

	private IContentFormatter formatter;

	private SQLScanner scanner;

	private SQLColorManager colorManager;

	public SQLConfiguration() {
		this(new SQLColorManager());
	}

	public SQLConfiguration(SQLColorManager colorManager) {
		this.colorManager = colorManager;

	}

	public ITextDoubleClickStrategy getDoubleClickStrategy(ISourceViewer sourceViewer, String contentType) {
		if (doubleClickStrategy == null)
			doubleClickStrategy = new SQLDoubleClickStrategy();
		return doubleClickStrategy;
	}

	protected SQLScanner getSQLScanner() {
		if (scanner == null) {
			scanner = new SQLScanner(colorManager);
			scanner.setDefaultReturnToken(new Token(new TextAttribute(colorManager.getColor(ISQLColorConstants.DEFAULT))));
		}
		return scanner;
	}

	public IPresentationReconciler getPresentationReconciler(ISourceViewer sourceViewer) {
		PresentationReconciler reconciler = new PresentationReconciler();

		DefaultDamagerRepairer dr = new DefaultDamagerRepairer(getSQLScanner());
		reconciler.setDamager(dr, IDocument.DEFAULT_CONTENT_TYPE);
		reconciler.setRepairer(dr, IDocument.DEFAULT_CONTENT_TYPE);

		return reconciler;
	}

	public IContentFormatter getContentFormatter(ISourceViewer sourceViewer) {
		if (formatter == null)
			formatter = new SQLFormatter();
		return formatter;

	}

}