package de.paginagmbh.epubchecker;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.io.*;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javax.swing.*;
import org.xml.sax.SAXException;
import javax.xml.parsers.*;
import javax.xml.xpath.*;
import javax.swing.JFrame;
import com.adobe.epubcheck.api.EpubCheck;
import com.adobe.epubcheck.api.Report;
import com.apple.eawt.AboutHandler;
import com.apple.eawt.AppEvent.AboutEvent;
import com.apple.eawt.AppEvent.QuitEvent;
import com.apple.eawt.Application;
import com.apple.eawt.OpenFilesHandler;
import com.apple.eawt.AppEvent.OpenFilesEvent;
import com.apple.eawt.QuitHandler;
import com.apple.eawt.QuitResponse;


/**
  * checks and validates EPUB eBooks
  * 
  * @author		Tobias Fischer
  * @copyright	pagina GmbH, Tübingen
  * @version	1.1.1
  * @date 		2013-02-01
  * @lastEdit	Tobias Fischer
  */
public class paginaEPUBChecker {
	
	// +++++++++++++++++++++++++ DON'T FORGET TO UPDATE EVERYTIME ++++++++++++++++++ //
	
	public static final String PROGRAMVERSION = "1.1.1";
	public static final String VERSIONDATE = "01.02.2013";
	public static final String PROGRAMRELEASE = "";	// "" or "beta"
	
	// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ //

    public static final String[] availableLanguages = {
    	"German",
    	"English",
    	"French"
    };
	
	// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ //
    
	// global objects
    public static RegexSearchReplace regex = null;
    public static JFrame gui = null;
    public static Report epubcheck_Report;
    public static Localization l10n = null;
    public static Application macApp = null;
    
    // global variables
    public static File epubcheck_File;
    public static Boolean epubcheck_Result;
    public static String epubcheck_EpubVersion;
	public static Boolean AutoSave = false;
    public static long timestamp_begin, timestamp_end;
    public static String os_name;
    
    // predefined global variables
    public static Boolean guiReady = false;
    public static String programLanguage = "systemLanguage";
	public static Boolean epubcheck_translate = true;
    public static Point MainGuiPosition = null;
    public static Dimension MainGuiDimension = null;
    
    // paths and files
	public static String path_ConfigDir;
	public static String path_LastUpdateCheckFile;
	private static String cfgFile_LastUpdateCheck = "UpdateCheck.cfg";
	public static String path_AutoSaveFile;
	private static String cfgFile_AutoSave = "AutoSave.cfg";
	public static String path_LanguageFile;
	private static String cfgFile_Language = "Language.cfg";
	public static String path_TranslateFile;
	private static String cfgFile_Translate = "Translate.cfg";

	// icons
    public static final Icon loadingIcon = new ImageIcon(Toolkit.getDefaultToolkit().getImage(mainGUI.class.getResource("/resources/icons/loading.gif")));
    public static final Image logoImg16 = Toolkit.getDefaultToolkit().getImage(mainGUI.class.getResource("/resources/icons/paginaEPUBChecker_16.png"));
    public static final Image logoImg32 = Toolkit.getDefaultToolkit().getImage(mainGUI.class.getResource("/resources/icons/paginaEPUBChecker_32.png"));
    public static final Image logoImg64 = Toolkit.getDefaultToolkit().getImage(mainGUI.class.getResource("/resources/icons/paginaEPUBChecker_64.png"));
//    public static final Image logoImg128 = Toolkit.getDefaultToolkit().getImage(mainGUI.class.getResource("/resources/icons/paginaEPUBChecker_128.png"));
//    public static final Image logoImg256 = Toolkit.getDefaultToolkit().getImage(mainGUI.class.getResource("/resources/icons/paginaEPUBChecker_256.png"));
//    public static final Image logoImg512 = Toolkit.getDefaultToolkit().getImage(mainGUI.class.getResource("/resources/icons/paginaEPUBChecker_512.png"));
    
    
    
    /* ********************************************************************************************************** */
	
    public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {

        // use system proxy
		System.setProperty("java.net.useSystemProxies", "true");
    	
		
    	
		// load and set system LookAndFeel
		try {
			JFrame.setDefaultLookAndFeelDecorated(true);
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
        
		
        
    	/* check operating system */
		
		// windows
		if(System.getProperty("os.name").toLowerCase().indexOf("windows") > -1) {
			os_name = "windows";
			path_ConfigDir = System.getProperty("user.home") + File.separator + "AppData" + File.separator + "Local" + File.separator + "paginaEpubChecker";
		
		// mac
		} else if(System.getProperty("os.name").toLowerCase().indexOf("mac") > -1) {
			os_name = "mac";
			path_ConfigDir = System.getProperty("user.home") + File.separator + "Library" + File.separator + "paginaEpubChecker";
		
		// linux
		} else if(System.getProperty("os.name").toLowerCase().indexOf("linux") > -1) {
			os_name = "linux";
			path_ConfigDir = System.getProperty("user.home") + File.separator + ".paginaEpubChecker";
			
		// any other OS isn't supported yet
		} else {
			messageGUI msg = new messageGUI();
			msg.setTitle("pagina EPUB-Checker");
			msg.showError("This operating system isn't supported yet!");
			System.exit(1);
		}
		
		// set paths to config files after having set the OS dependant config base path above
		path_LastUpdateCheckFile = path_ConfigDir + File.separator + cfgFile_LastUpdateCheck;
		path_AutoSaveFile = path_ConfigDir + File.separator + cfgFile_AutoSave;
		path_LanguageFile = path_ConfigDir + File.separator + cfgFile_Language;
		path_TranslateFile = path_ConfigDir + File.separator + cfgFile_Translate;
		
		// create directories to config base path if not existing
		if(!new File(path_ConfigDir).exists()) {
			(new File(path_ConfigDir)).mkdirs();
		}
		
		
		
		// load language string from language config file
		if(new File(path_LanguageFile).exists()) {
			String lang = updateCheck.readFileAsString(paginaEPUBChecker.path_LanguageFile);
			if(lang.length() != 0) {
				programLanguage = lang;
			}
		}
		// "else" isn't needed since the default is the "systemLanguage" if no language is specified
		
		
		
    	// load and init GUI and its dependencies (e.g. language object, regex object, etc.)
		loadAndInitGuiAndDependencies();
        
        
        
        // perform update check
        new updateCheck(true);
        
        
        
        // init mac specific event listeners; after GUI is loaded
        initMacOSEventListeners();
    	
    }
    
    
    
    
    /* ********************************************************************************************************** */
	
    public static void loadAndInitGuiAndDependencies() {
    	
    	// invalidate the old language object (needed after switching the program's language in the gui menu)
        if(l10n != null) {
        	l10n = null;
        }
        
    	// init language object
    	l10n = new Localization();
		
		
    	// invalidate and dispose the old GUI (needed after switching the program's language in the gui menu)
        if(gui != null) {
        	paginaEPUBChecker.gui.invalidate();
        	paginaEPUBChecker.gui.dispose();
        }
        
		// show main GUI
    	gui = new mainGUI();
		
		
    	
    	// invalidate the old regex patterns (needed after switching the program's language in the gui menu)
        if(regex != null) {
        	regex = null;
        }
        
		// init regex-patterns
        regex = new RegexSearchReplace();
    }
  	
  	
    
    
    /* ********************************************************************************************************** */
    	
    public static void initMacOSEventListeners() {
		// mac specific event listeners
		// have to be set after the GUI was loaded
		if(os_name.equals("mac")) {

			/*
			 * Help and tutorial:
			 * https://developer.apple.com/library/mac/documentation/Java/Reference/JavaSE6_AppleExtensionsRef/api/com/apple/eawt/Application.html#addApplicationListener%28com.apple.eawt.ApplicationListener%29
			 *
			 */
			
			
			// create an instance of the mac osx Application class
			macApp = Application.getApplication();
			
			// Exit handler
			macApp.setQuitHandler(new QuitHandler() {
				@Override
				public void handleQuitRequestWith(QuitEvent arg0, QuitResponse arg1) {
					System.exit(0);
				}
			});
			
			// AboutMenu handler
			macApp.setAboutHandler(new AboutHandler() {
				@Override
				public void handleAbout(AboutEvent arg0) {
					subGUI s = new subGUI(paginaEPUBChecker.gui);
					s.displayAboutBox();
				}
			});
			
			// Drop handler (for dropping files on the program or dock)
			macApp.setOpenFileHandler(new OpenFilesHandler() {

				@Override
				public void openFiles(OpenFilesEvent arg0) {
					List<File> files = arg0.getFiles();
					
					DragDropListener.handleDropedFiles(files);

				}
			});

		}
    }
	
	
	
    
    /* ********************************************************************************************************** */
	
    public static void validate() {
    	
    	// set "begin" timestamp
    	timestamp_begin = System.currentTimeMillis();
    	
    	// clear and reset TextArea
    	mainGUI.txtarea_results.setText("");
		
		// Print timestamp of current epubcheck
    	Calendar cal = Calendar.getInstance();
        cal.setTime( new Date() );
        DateFormat formater = DateFormat.getDateTimeInstance( DateFormat.LONG, DateFormat.LONG );
		mainGUI.txtarea_results.insert(formater.format(cal.getTime()) + "\n\n\n", 0);
		mainGUI.txtarea_results.append("---------------------------------------------------\n\n");
		
		// disable validation button
		mainGUI.btn_validateEpub.setEnabled(false);
		
		// set the loading icon and update the statusbar
		mainGUI.statusBar.update(loadingIcon, __("Checking file"));
		
		// disable "save" menuItem
		mainGUI.mnItem_Save.setEnabled(false);
		
		
		// init SwingWorker
		SwingWorker<Void, Void> validationWorker = new SwingWorker<Void, Void>() {

			@Override
			protected Void doInBackground() throws Exception {
				
				// run original epubcheck
		    	EpubCheck epubcheck = new EpubCheck(paginaEPUBChecker.epubcheck_File, paginaEPUBChecker.epubcheck_Report);
		    	epubcheck_Result = epubcheck.validate();
		    	
				return null;
			}


			@Override
			protected void done() {
				
				
				// validation finished with warnings or errors
				if(epubcheck_Result == false) {


					// if output is translated, then format it nicely
					if(epubcheck_translate) {
						mainGUI.txtarea_results.append("\n" + "---------------------------------------------------");
					}
					
					
					// warnings AND errors
					if(epubcheck_translate && paginaReport.errorCount > 0 && paginaReport.warningCount > 0) {
						
						mainGUI.txtarea_results.append("\n\n"
								+ String.format(__("Check finished with %1$1s warnings and %2$1s errors!"), paginaReport.warningCount, paginaReport.errorCount)
								+ "\n");
					
					// only errors
					} else if(epubcheck_translate && paginaReport.errorCount > 0) {
						mainGUI.txtarea_results.append("\n\n" + String.format(__("Check finished with %d errors!"), paginaReport.errorCount) + "\n");
					
					// only warnings
					} else if(epubcheck_translate && paginaReport.warningCount > 0) {
						mainGUI.txtarea_results.append("\n\n" + String.format(__("Check finished with %d warnings!"), paginaReport.warningCount) + "\n");
					
					// something went wrong
					} else if(epubcheck_translate) {
						mainGUI.txtarea_results.append("\n\n" + __("Check finished with warnings or errors!") + "\n");
						
					
					// epubcheck results shouldn't be translated
					} else {
						mainGUI.txtarea_results.append("\n\n" + "Check finished with warnings or errors!" + "\n");
					}
					
					
					// set error counter in mac dock badge
					if(os_name.equals("mac")) {
						if(paginaReport.warningCount + paginaReport.errorCount > 0) {
							macApp.setDockIconBadge(new Integer(paginaReport.warningCount + paginaReport.errorCount).toString());
						} else {
							macApp.setDockIconBadge("error");
						}
					}
					
					
					
					
				
				// validation finished without warnings or errors
				} else {
					
					// translate the output
					if(epubcheck_translate) {
						mainGUI.txtarea_results.append("\n\n" + __("No errors or warnings detected") + "\n");
						
					// epubcheck results shouldn't be translated
					} else {
						mainGUI.txtarea_results.append("\n\n" + "No errors or warnings detected" + "\n");
					}
					
					
					
					// set error counter in mac dock badge
					if(os_name.equals("mac")) {
						macApp.setDockIconBadge("✓");
					}
				}
				
				
				// scroll to the end
				mainGUI.txtarea_results.setCaretPosition(mainGUI.txtarea_results.getText().length());
				
				
				
		    	// set "end" timestamp
		    	timestamp_end = System.currentTimeMillis();
		    	
		    	// calculate the processing duration
		    	double timestamp_diff = timestamp_end-timestamp_begin;
		    	DecimalFormat df = new DecimalFormat("0.0#");
		    	String timestamp_result = df.format(timestamp_diff/1000);
				
				// remove the loading icon and update the status bar
				mainGUI.statusBar.update(null, __("Done") + ". " + String.format(__("Validated in %s seconds"), timestamp_result));
				
				// re-enable validation button
				mainGUI.btn_validateEpub.setEnabled(true);
				
				// re-enable "save" menuItem
				mainGUI.mnItem_Save.setEnabled(true);
				
				// Auto Save logfile if desired
				if(AutoSave) {
					mainGUI.saveLogfile(new File(paginaEPUBChecker.epubcheck_File.getAbsolutePath().toLowerCase().replaceAll("\\.epub", "_log.txt")));
				}
			}
		};
		
		// execute SwingWorker
		validationWorker.execute();
    }
	
	
	
	
	/* ********************************************************************************************************** */
	
    private static String __(String s) {
		return l10n.getString(s);
	}
}