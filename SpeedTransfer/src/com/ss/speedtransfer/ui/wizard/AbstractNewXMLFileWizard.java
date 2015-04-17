package com.ss.speedtransfer.ui.wizard;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.wizards.newresource.BasicNewResourceWizard;
import org.eclipse.wst.common.uriresolver.internal.util.URIHelper;
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
import org.eclipse.wst.xml.ui.internal.wizards.NewXMLWizard;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

@SuppressWarnings("restriction")
public abstract class AbstractNewXMLFileWizard extends NewXMLWizard {

	protected NewFilePage newFilePage;
	protected String cmDocumentErrorMessage;
	protected NewXMLGenerator generator;

	public AbstractNewXMLFileWizard() {
		setWindowTitle("Create new file");
		setDefaultPageImageDescriptor(getImageDescriptor());
		generator = new NewXMLGenerator();
	}

	public AbstractNewXMLFileWizard(IFile file, CMDocument cmDocument) {
		this();
		generator.setGrammarURI(URIHelper.getPlatformURI(file));
		generator.setCMDocument(cmDocument);
	}

	public abstract String getCatalogKey();

	public abstract ImageDescriptor getImageDescriptor();

	public static void showDialog(Shell shell, IFile file, IStructuredSelection structuredSelection) {
		String[] errorInfo = new String[2];

		CMDocument cmDocument = NewXMLGenerator.createCMDocument(URIHelper.getPlatformURI(file), errorInfo);
		if (errorInfo[0] == null) {
			NewXMLWizard wizard = new NewXMLWizard(file, cmDocument);
			wizard.init(PlatformUI.getWorkbench(), structuredSelection);
			wizard.setNeedsProgressMonitor(true);
			WizardDialog dialog = new WizardDialog(shell, wizard);
			dialog.create();
			dialog.getShell().setText("");
			dialog.setBlockOnOpen(true);
			dialog.open();
		} else {
			MessageDialog.openInformation(shell, errorInfo[0], errorInfo[1]);
		}
	}

	public abstract void addPages();

	public IWizardPage getStartingPage() {
		WizardPage result = null;
		result = newFilePage;
		return result;
	}

	public boolean canFinish() {
		IWizardPage currentPage = getContainer().getCurrentPage();
		return currentPage.isPageComplete();

	}

	public boolean performFinish() {

		boolean result = true;

		final ICatalogEntry catalogEntry = getCatalogEntry();

		if (generator.getGrammarURI() == null) {
			generator.setGrammarURI(catalogEntry.getURI());
			generator.setXMLCatalogEntry(catalogEntry);
		}
		org.eclipse.core.runtime.Assert.isNotNull(generator.getGrammarURI());

		int buildPolicy = 0;
		buildPolicy = buildPolicy | DOMContentBuilder.BUILD_OPTIONAL_ATTRIBUTES;
		// buildPolicy = buildPolicy | DOMContentBuilder.BUILD_OPTIONAL_ELEMENTS;
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
			((NamespaceInfo) generator.namespaceInfoList.get(0)).prefix = ""; //$NON-NLS-1$
			cmDocumentErrorMessage = errorInfo[1];
		}

		String fileName = null;
		try {

			String[] namespaceErrors = generator.getNamespaceInfoErrors();
			if (namespaceErrors != null) {
				String title = namespaceErrors[0];
				String message = namespaceErrors[1];
				result = MessageDialog.openQuestion(getShell(), title, message);
			}

			if (result) {
				fileName = newFilePage.getFileName();
				if ((new Path(fileName)).getFileExtension() == null) {
					newFilePage.setFileName(fileName.concat(newFilePage.defaultFileExtension));
				}

				IFile newFile = newFilePage.createNewFile();
				String xmlFileName = newFile.getLocation().toOSString();

				if (getContainer().getCurrentPage() != selectRootElementPage) {
					generator.createXMLDocument(newFile, xmlFileName);
				}

				setAttributes(newFile, fileName);

				newFile.refreshLocal(IResource.DEPTH_ONE, null);

				IWorkbenchWindow workbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
				BasicNewResourceWizard.selectAndReveal(newFile, workbenchWindow);
				openEditor(newFile);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	protected void setAttributes(IFile file, String fileName) {
		try {
			IDOMModel xmlmodel = getModelForResource(file);
			IDOMDocument document = xmlmodel.getDocument();
			beforeSaveHook(document);
			xmlmodel.save(file);
		} catch (Exception e) {
		}
	}

	protected void beforeSaveHook(IDOMDocument document) {

	}

	public void openEditor(IFile file) {
		// Open editor on new file.
		String editorId = null;
		IWorkbenchWindow dw = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		try {
			if (dw != null) {
				IWorkbenchPage page = dw.getActivePage();

				IEditorDescriptor editor = PlatformUI.getWorkbench().getEditorRegistry().getDefaultEditor(file.getFullPath().toString(), file.getContentDescription().getContentType());
				if (editor != null) {
					editorId = editor.getId();

					if (page != null) {
						file.refreshLocal(IResource.DEPTH_ONE, null);
						page.openEditor(new FileEditorInput(file), editorId, true);
					}
				}
			}
		} catch (PartInitException e) {
			// editor can not open for some reason
			return;
		} catch (CoreException e1) {
			// editor id could not be retrieved, so we can not open editor
			return;
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

	public static GridLayout createOptionsPanelLayout() {
		GridLayout gridLayout = new GridLayout();
		gridLayout.marginWidth = 0;
		gridLayout.horizontalSpacing = 0;
		return gridLayout;
	}

	public Element getElement(Node node) {
		Element result = null;
		if (node.getNodeType() == Node.ELEMENT_NODE) {
			result = (Element) node;
		} else if (node.getNodeType() == Node.DOCUMENT_NODE) {
			result = getRootElement((Document) node);
		}
		return result;
	}

	public Element getRootElement(Document document) {
		Element rootElement = null;
		NodeList nodeList = document.getChildNodes();
		int nodeListLength = nodeList.getLength();
		for (int i = 0; i < nodeListLength; i++) {
			Node childNode = nodeList.item(i);
			if (childNode.getNodeType() == Node.ELEMENT_NODE) {
				rootElement = (Element) childNode;
				break;
			}
		}
		return rootElement;
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

	protected abstract String getRootElement();

}
