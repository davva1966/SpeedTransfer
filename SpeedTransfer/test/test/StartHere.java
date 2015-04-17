package test;

import java.io.File;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public class StartHere {
	
	private File workingDir_;
	private File tempDir_;
	
	public StartHere(File root, File temp){
		this.workingDir_ = root;
		this.tempDir_ = temp;
	}
	
	/**
	 * The working directory, where the application was
	 * started from.
	 * @return
	 */
	public File getWorkingDir(){
		return this.workingDir_;
	}
	
	/**
	 * The temp directory, where the launcher extracted
	 * your app and JRE to on the users' system.
	 * @return
	 */
	public File getTempDir(){
		return this.tempDir_;
	}

	public static void main(String[] args)
		throws Exception {
		
		File root;
		try {
			root = new File(args[0]);
		} catch ( Exception e ) {
			root = new File(".");
		}
		
		File temp;
		try {
			temp = new File(args[1]);
		} catch ( Exception e ) {
			temp = new File(".");
		}
		
		final StartHere sh = new StartHere(root, temp);		
		Runnable worker = new Runnable() {
		    public void run() {
		    	showMessageDialog(sh);
		    	System.exit(0);
		    }
		};
		SwingUtilities.invokeLater(worker);
		
	}
	
	private static void showMessageDialog(StartHere sh) {
		try {
			JOptionPane.showMessageDialog(new JFrame(),
				"A java app launched by 7zip SFX!\n\n" +
				"My working directory is:\n" +
				sh.getWorkingDir().getCanonicalPath() +
				"\n\nAnd I've been extracted to temp directory:\n" +
				sh.getTempDir().getCanonicalPath() );
		} catch (IOException e) {
			e.printStackTrace( System.err );
		}
	}
	
}
