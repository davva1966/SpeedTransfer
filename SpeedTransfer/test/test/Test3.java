package test;


import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class Test3 extends Dialog {
  Double value;

  /**
   * @param parent
   */
  public Test3(Shell parent) {
    super(parent);
  }

  /**
   * @param parent
   * @param style
   */
  public Test3(Shell parent, int style) {
    super(parent, style);
  }

  /**
   * Makes the dialog visible.
   * 
   * @return
   */
  public Double open() {
    Shell parent = getParent();
    final Shell shell =
      new Shell(parent, SWT.TITLE | SWT.BORDER | SWT.APPLICATION_MODAL);
    shell.setText("NumberInputDialog");

    shell.setLayout(new GridLayout(2, true));

    Label label = new Label(shell, SWT.NULL);
    label.setText("Please enter a valid number:");

    final Text text = new Text(shell, SWT.SINGLE | SWT.BORDER);

    final Button buttonOK = new Button(shell, SWT.PUSH);
    buttonOK.setText("OK");
    buttonOK.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
    Button buttonCancel = new Button(shell, SWT.PUSH);
    buttonCancel.setText("Cancel");

    text.addListener(SWT.Modify, new Listener() {
      public void handleEvent(Event event) {
        try {
          value = new Double(text.getText());
          buttonOK.setEnabled(true);
        } catch (Exception e) {
          buttonOK.setEnabled(false);
        }
      }
    });

    buttonOK.addListener(SWT.Selection, new Listener() {
      public void handleEvent(Event event) {
        shell.dispose();
      }
    });

    buttonCancel.addListener(SWT.Selection, new Listener() {
      public void handleEvent(Event event) {
        value = null;
        shell.dispose();
      }
    });
    shell.addListener(SWT.Traverse, new Listener() {
      public void handleEvent(Event event) {
        if(event.detail == SWT.TRAVERSE_ESCAPE)
          event.doit = false;
      }
    });

    text.setText("");
    shell.pack();
    shell.open();

    Display display = parent.getDisplay();
    while (!shell.isDisposed()) {
      if (!display.readAndDispatch())
        display.sleep();
    }

    return value;
  }

  public static void main(String[] args) {
    Shell shell = new Shell();
    Test3 dialog = new Test3(shell);
    System.out.println(dialog.open());
  }
}