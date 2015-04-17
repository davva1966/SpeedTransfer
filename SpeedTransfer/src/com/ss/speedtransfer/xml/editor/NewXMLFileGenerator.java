package com.ss.speedtransfer.xml.editor;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.wst.sse.core.StructuredModelManager;
import org.eclipse.wst.sse.core.internal.provisional.IModelManager;
import org.eclipse.wst.sse.core.internal.provisional.IStructuredModel;
import org.eclipse.wst.xml.core.internal.XMLCorePlugin;
import org.eclipse.wst.xml.core.internal.catalog.provisional.ICatalog;
import org.eclipse.wst.xml.core.internal.catalog.provisional.ICatalogEntry;
import org.eclipse.wst.xml.core.internal.catalog.provisional.INextCatalog;
import org.eclipse.wst.xml.core.internal.contentmodel.CMDocument;
import org.eclipse.wst.xml.core.internal.contentmodel.util.DOMContentBuilder;
import org.eclipse.wst.xml.core.internal.contentmodel.util.NamespaceInfo;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMDocument;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMModel;
import org.eclipse.wst.xml.ui.internal.wizards.NewXMLGenerator;

import com.ss.speedtransfer.ui.UIHelper;


@SuppressWarnings("restriction")
public class NewXMLFileGenerator {

	protected NewXMLGenerator generator;
	protected String catalogKey;
	protected String rootElement;
	protected IFile file;

	public NewXMLFileGenerator(String catalogKey, String rootElement, IFile file) {
		generator = new NewXMLGenerator();
		this.catalogKey = catalogKey;
		this.rootElement = rootElement;
		this.file = file;
	}

	protected String getCatalogKey() {
		return catalogKey;
	}

	protected String getRootElement() {
		return rootElement;
	}

	public boolean generate() {

		boolean result = true;

		final ICatalogEntry catalogEntry = getCatalogEntry();

		if (generator.getGrammarURI() == null) {
			generator.setGrammarURI(catalogEntry.getURI());
			generator.setXMLCatalogEntry(catalogEntry);
		}
		org.eclipse.core.runtime.Assert.isNotNull(generator.getGrammarURI());

		int buildPolicy = 0;
		buildPolicy = buildPolicy | DOMContentBuilder.BUILD_OPTIONAL_ATTRIBUTES;
		buildPolicy = buildPolicy | DOMContentBuilder.BUILD_OPTIONAL_ELEMENTS;
		buildPolicy = buildPolicy | DOMContentBuilder.BUILD_FIRST_CHOICE | DOMContentBuilder.BUILD_FIRST_SUBSTITUTION;
		// buildPolicy = buildPolicy | DOMContentBuilder.BUILD_TEXT_NODES;

		generator.setBuildPolicy(buildPolicy);

		if (generator.getCMDocument() == null) {
			final String[] errorInfo = new String[2];
			final CMDocument[] cmdocs = new CMDocument[1];
			Runnable r = new Runnable() {

				public void run() {
					cmdocs[0] = NewXMLGenerator.createCMDocument(catalogEntry.getURI(), errorInfo);
				}
			};
			org.eclipse.swt.custom.BusyIndicator.showWhile(Display.getCurrent(), r);

			generator.setCMDocument(cmdocs[0]);
			generator.setRootElementName(getRootElement());
			generator.createNamespaceInfoList();
			((NamespaceInfo) generator.namespaceInfoList.get(0)).prefix = "";
		}

		try {

			String[] namespaceErrors = generator.getNamespaceInfoErrors();
			if (namespaceErrors != null) {
				String title = namespaceErrors[0];
				String message = namespaceErrors[1];
				result = MessageDialog.openQuestion(UIHelper.instance().getActiveShell(), title, message);
			}

			if (result) {
				String xmlFileName = file.getLocation().toOSString();
				generator.createXMLDocument(file, xmlFileName);
				setAttributes(file);
				file.refreshLocal(IResource.DEPTH_ONE, null);

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	protected void setAttributes(IFile file) {
		try {
			IDOMModel xmlmodel = getModelForResource(file);
			IDOMDocument document = xmlmodel.getDocument();
			xmlmodel.save(file);
			file.refreshLocal(IResource.DEPTH_ONE, null);
		} catch (Exception e) {
		}
	}

	protected ICatalogEntry getCatalogEntry() {

		ICatalogEntry catalogEntry = null;
		ICatalog catalog = XMLCorePlugin.getDefault().getDefaultXMLCatalog();
		INextCatalog[] nextCatalogs = catalog.getNextCatalogs();
		for (int i = 0; i < nextCatalogs.length; i++) {
			INextCatalog nextCatalog = nextCatalogs[i];
			if (XMLCorePlugin.SYSTEM_CATALOG_ID.equals(nextCatalog.getId())) {
				ICatalog systemCatalog = nextCatalog.getReferencedCatalog();
				if (systemCatalog != null) {
					ICatalogEntry[] catalogEntries = systemCatalog.getCatalogEntries();
					for (int j = 0; j < catalogEntries.length; j++) {
						if (catalogEntries[j].getKey().equals(getCatalogKey())) {
							catalogEntry = catalogEntries[j];
						}
					}

				}
			}
		}
		return catalogEntry;
	}

	protected IDOMModel getModelForResource(IFile file) {
		IStructuredModel model = null;
		IModelManager manager = StructuredModelManager.getModelManager();

		try {
			model = manager.getModelForRead(file);
		} catch (Exception e) {
		}

		return model instanceof IDOMModel ? (IDOMModel) model : null;
	}

}
