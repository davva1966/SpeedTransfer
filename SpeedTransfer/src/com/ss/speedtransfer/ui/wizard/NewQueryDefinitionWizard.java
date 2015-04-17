package com.ss.speedtransfer.ui.wizard;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.wst.common.uriresolver.internal.util.URIHelper;
import org.eclipse.wst.xml.core.internal.contentmodel.CMDocument;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMDocument;
import org.w3c.dom.Element;

import com.ss.speedtransfer.model.QueryDefinition;
import com.ss.speedtransfer.util.DefaultDBManager;
import com.ss.speedtransfer.util.SettingsManager;
import com.ss.speedtransfer.util.UIHelper;


@SuppressWarnings("restriction")
public class NewQueryDefinitionWizard extends AbstractNewXMLFileWizard {

	public NewQueryDefinitionWizard() {
		super();
	}

	public NewQueryDefinitionWizard(IFile file, CMDocument cmDocument) {
		super(file, cmDocument);
	}

	public String getCatalogKey() {
		return QueryDefinition.CATALOG_KEY;
	}

	public ImageDescriptor getImageDescriptor() {
		return UIHelper.instance().getImageDescriptor("query_def.gif");
	}

	public void addPages() {
		String grammarURI = generator.getGrammarURI();

		// new file page
		newFilePage = new NewFilePage(fSelection);
		newFilePage.setTitle("Query Definition File");
		newFilePage.setDescription("Create a new Query Definition file.");
		newFilePage.defaultName = (grammarURI != null) ? URIHelper.removeFileExtension(URIHelper.getLastSegment(grammarURI)) : "NewQueryDefinition";
		String ext = "xml"; //$NON-NLS-1$
		newFilePage.defaultFileExtension = "." + ext; //$NON-NLS-1$
		newFilePage.filterExtensions = filePageFilterExtensions;
		addPage(newFilePage);

	}

	protected String getRootElement() {
		return QueryDefinition.QUERY_DEFINITION;
	}

	protected void beforeSaveHook(IDOMDocument document) {
		try {
			super.beforeSaveHook(document);
			Element sqlNode = (Element) ((Element) document.getDocumentElement()).getElementsByTagName(QueryDefinition.SQL).item(0);
			String defaultDB = SettingsManager.get(DefaultDBManager.DEFAULT_DB_SETTING);
			if (defaultDB != null && defaultDB.trim().length() > 0)
				sqlNode.setAttribute(QueryDefinition.DB_CONNECTION_FILE, defaultDB);
		} catch (Exception e) {

		}

	}

}
