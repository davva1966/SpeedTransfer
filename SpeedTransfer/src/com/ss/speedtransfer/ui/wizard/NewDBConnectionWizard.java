package com.ss.speedtransfer.ui.wizard;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.wst.common.uriresolver.internal.util.URIHelper;
import org.eclipse.wst.xml.core.internal.contentmodel.CMDocument;

import com.ss.speedtransfer.model.DBConnection;
import com.ss.speedtransfer.util.UIHelper;


@SuppressWarnings("restriction")
public class NewDBConnectionWizard extends AbstractNewXMLFileWizard {

	protected String defaultName = null;

	public NewDBConnectionWizard() {
		super();
	}

	public NewDBConnectionWizard(String defaultName) {
		super();
		this.defaultName = defaultName;
	}

	public NewDBConnectionWizard(IFile file, CMDocument cmDocument) {
		super(file, cmDocument);
	}

	public String getCatalogKey() {
		return DBConnection.CATALOG_KEY;
	}

	public ImageDescriptor getImageDescriptor() {
		return UIHelper.instance().getImageDescriptor("db_connection.png");
	}

	public void addPages() {
		String grammarURI = generator.getGrammarURI();

		// new file page
		newFilePage = new NewFilePage(fSelection);
		newFilePage.setTitle("Database Connection File");
		newFilePage.setDescription("Create a new Database Connection file.");
		if (defaultName != null && defaultName.trim().length() > 0)
			newFilePage.defaultName = defaultName;
		else
			newFilePage.defaultName = (grammarURI != null) ? URIHelper.removeFileExtension(URIHelper.getLastSegment(grammarURI)) : "NewDBConnection";
		String ext = "xml"; //$NON-NLS-1$
		newFilePage.defaultFileExtension = "." + ext; //$NON-NLS-1$
		newFilePage.filterExtensions = filePageFilterExtensions;
		addPage(newFilePage);

	}

	protected String getRootElement() {
		return DBConnection.DB_CONNECTION_DEF;
	}

}
