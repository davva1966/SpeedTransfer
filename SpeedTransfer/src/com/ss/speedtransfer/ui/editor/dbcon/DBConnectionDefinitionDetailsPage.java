package com.ss.speedtransfer.ui.editor.dbcon;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;

import com.ss.speedtransfer.model.DBConnection;
import com.ss.speedtransfer.model.QueryDefinition;


public class DBConnectionDefinitionDetailsPage extends AbstractDBConnectionDetailsPage {

	protected Text description;

	public DBConnectionDefinitionDetailsPage(DBConnection model) {
		super(model);
	}

	public void createContents(Composite parent) {
		TableWrapLayout layout = new TableWrapLayout();
		layout.topMargin = 10;
		layout.leftMargin = 5;
		layout.rightMargin = 2;
		layout.bottomMargin = 2;
		parent.setLayout(layout);

		FormToolkit toolkit = mform.getToolkit();
		Section s1 = toolkit.createSection(parent, Section.DESCRIPTION | Section.TITLE_BAR);
		s1.marginWidth = 10;

		s1.setText("Database Connection Definition");
		TableWrapData td = new TableWrapData(TableWrapData.FILL, TableWrapData.TOP);
		td.grabHorizontal = true;
		s1.setLayoutData(td);

		Composite client = toolkit.createComposite(s1);
		GridLayout glayout = new GridLayout();
		glayout.marginWidth = toolkit.getBorderStyle() == SWT.BORDER ? 0 : 2;
		glayout.numColumns = 2;
		glayout.marginBottom = 10;
		client.setLayout(glayout);

		createControls(parent, client);

		toolkit.paintBordersFor(client);
		s1.setClient(client);
	}

	protected void createControls(Composite parent, Composite baseSection) {

		FormToolkit toolkit = mform.getToolkit();

		GridData gd = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING);
		gd.widthHint = 10;

		toolkit.createLabel(baseSection, "Description");
		description = toolkit.createText(baseSection, "", SWT.SINGLE); //$NON-NLS-1$
		description.setLayoutData(gd);
		description.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				String value = ((Text) e.getSource()).getText();
				setAttributeValue(DBConnection.DESCRIPTION, value);
			}
		});

	}

	protected void internalUpdate() {
		updateText(description, getAttributeValue(QueryDefinition.DESCRIPTION));
	}

	public void setFocus() {
		description.setFocus();
	}

}
