package com.ss.speedtransfer.actions;

import java.sql.Connection;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;

import com.ss.speedtransfer.model.QueryDefinition;
import com.ss.speedtransfer.ui.StyledEdit;
import com.ss.speedtransfer.ui.wizard.qb.SQLQueryBuilderWizard;
import com.ss.speedtransfer.ui.wizard.qb.SQLQueryData;
import com.ss.speedtransfer.util.ConnectionManager;
import com.ss.speedtransfer.util.SSUtil;
import com.ss.speedtransfer.util.UIHelper;
import com.ss.speedtransfer.util.sql.SQLFormatter;


public class OpenSQLBuilderWizardAction extends Action {

	StyledEdit sqlEdit;
	QueryDefinition queryDef;

	public OpenSQLBuilderWizardAction(StyledEdit sqlEdit, QueryDefinition queryDef) {
		super("Open SQL Builder wizard", IAction.AS_PUSH_BUTTON);
		this.sqlEdit = sqlEdit;
		this.queryDef = queryDef;
		setImageDescriptor(UIHelper.instance().getImageDescriptor("sql_wizard.gif"));

	}

	public void run() {
		try {
			BusyIndicator.showWhile(UIHelper.instance().getDisplay(), new Runnable() {
				public void run() {
					Connection con = null;
					try {
						con = ConnectionManager.getConnection(queryDef);

						SQLQueryData qd = new SQLQueryData(con, queryDef);
						if (qd.isCancelled())
							return;

						String msg = qd.loadQueryData(sqlEdit.getText());
						if (msg != null) {
							UIHelper.instance().showErrorMsg("Error", msg);
							return;
						}

						SQLQueryBuilderWizard wizard = new SQLQueryBuilderWizard(qd);
						IWorkbench wb = PlatformUI.getWorkbench();
						wizard.init(wb, null);

						// Instantiates the wizard container with the wizard and opens it
						final WizardDialog dialog = new WizardDialog(new Shell(), wizard);

						dialog.setPageSize(730, 500);
						BusyIndicator.showWhile(null, new Runnable() {

							public void run() {
								dialog.open();
							}
						});

						String sqlString = qd.createSQL();

						if (sqlString != null)
							sqlEdit.setText(SQLFormatter.formatText(sqlString));

					} catch (Exception e) {
						UIHelper.instance().showErrorMsg("Error", SSUtil.getMessage(e));
					} finally {
						try {
							if (con != null && con.isClosed() == false)
								con.close();
						} catch (Exception e2) {
						}
					}
				}
			});

		} catch (Exception e) {
			UIHelper.instance().showErrorMsg("Error", SSUtil.getMessage(e));
		}
	}

}
