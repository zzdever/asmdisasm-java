import java.util.Scanner;
import java.util.Vector;
import java.io.*;

import javax.swing.*;
import javax.swing.border.*;

import java.awt.*;
import java.awt.event.*;


public class SimpleMIPS
{
	public static void main(String args[])
	{
		MainWindow window = new MainWindow();
		window.setVisible(true);
		
		return;
	}
}

class MainWindow extends JFrame implements ActionListener, WindowListener
{
	public static final int Width = 800;
	public static final int Height = 600;
	
	private JTextArea asmTextArea, objTextArea, infoTextArea;
	private String asmText, objText, infoText;
	private String asmFilename="", objFilename="";
	
	public MainWindow()
	{
		super();
		
		setSize(Width,Height);
		setMinimumSize(new Dimension(Width/2,Height/2));
		setTitle("Simple MIPS Assembler and Disassembler");
		addWindowListener(this);
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

		setMenus();		
		
		setMainViews();
				
	}
	
	
	private void setMainViews()
	{
		Container content = getContentPane();
		
		Box mainPanel = Box.createVerticalBox();
		Box mainTextViewPanel = Box.createHorizontalBox();
		
		asmTextArea = new JTextArea(30,20);
		asmTextArea.setLineWrap(true);
		mainTextViewPanel.add(new JScrollPane(asmTextArea));
		objTextArea = new JTextArea(30,20);
		objTextArea.setLineWrap(true);
		mainTextViewPanel.add(new JScrollPane(objTextArea));
		
		mainPanel.add(mainTextViewPanel);
		infoTextArea = new JTextArea(3,20);
		asmTextArea.setLineWrap(true);
		mainPanel.add(new JScrollPane(infoTextArea));
		
		content.add(mainPanel, BorderLayout.CENTER);
		
	}
	
	private void setMenus()
	{
		JMenu fileMenu = new JMenu("File");
		fileMenu.setLayout(new FlowLayout());
		JMenuItem fileASMOpen = new JMenuItem("Open ASM file");
		fileASMOpen.addActionListener(this);
		fileMenu.add(fileASMOpen);
		JMenuItem fileObjOpen = new JMenuItem("Open OBJ file");
		fileObjOpen.addActionListener(this);
		fileMenu.add(fileObjOpen);
		fileMenu.addSeparator();
		JMenuItem fileASMSave = new JMenuItem("Save ASM file");
		fileASMSave.addActionListener(this);
		fileMenu.add(fileASMSave);
		JMenuItem fileObjSave = new JMenuItem("Save OBJ file");
		fileObjSave.addActionListener(this);
		fileMenu.add(fileObjSave);
		
		
		JMenu functionMenu = new JMenu("Function");
		functionMenu.addActionListener(this);
		JMenuItem assemble = new JMenuItem("Assemble");
		assemble.addActionListener(this);
		functionMenu.add(assemble);
		JMenuItem disassemble = new JMenuItem("Disassemble");
		disassemble.addActionListener(this);
		functionMenu.add(disassemble);
		functionMenu.addSeparator();
		JMenuItem clearTmpFile = new JMenuItem("Clear temp files");
		clearTmpFile.addActionListener(this);
		functionMenu.add(clearTmpFile);
		
		
		JMenu infoMenu = new JMenu("Info");
		infoMenu.addActionListener(this);
		JMenuItem about = new JMenuItem("About");
		about.addActionListener(this);
		infoMenu.add(about);
		JMenuItem help = new JMenuItem("Help");
		help.addActionListener(this);
		infoMenu.add(help);
		
		//================
		JMenuItem test = new JMenuItem("Test");
		test.addActionListener(this);
		infoMenu.add(test);
		//================
			
			
		JMenuBar menuBar = new JMenuBar();
		menuBar.add(fileMenu);
		menuBar.add(functionMenu);
		menuBar.add(infoMenu);
		setJMenuBar(menuBar);
	}
	
	public void actionPerformed(ActionEvent e)
	{
			
		if(e.getActionCommand().equals("Test")){
			
			try{
				//int num = Integer.parseInt(textArea.getText());
				//textArea2.setText(Integer.toString(num));
			}
			catch (NumberFormatException except){
				//textArea2.setText(textArea.getText()+"\nNOT A VALID NUMBER");
			}
		}
		else if(e.getActionCommand().equals("Open ASM file")){			
			JFileChooser fileChooser = new JFileChooser();
			fileChooser.setDialogTitle("Open ASM File");
			
			File file = null;
			while(true){
				fileChooser.showOpenDialog(this);
				
				file = fileChooser.getSelectedFile();
				if(file==null)
					return;
				
				asmFilename = file.getPath();
			
				int suffixPosition = asmFilename.toLowerCase().indexOf(".asm");
				if(suffixPosition<=0){
					JOptionPane.showMessageDialog(fileChooser, "File format is not correct.\nPlease choose .asm file.", 
						"File Not Correct", JOptionPane.INFORMATION_MESSAGE); 
				}
				else{
					break;
				}
			}			
			
			BufferedReader inStream = null;
			try{
				inStream = new BufferedReader(new FileReader(file));
				String line = null;
				asmText = "";
				while((line = inStream.readLine()) != null){
					asmText = asmText + line + "\n";
				}
				inStream.close();
			}
			catch (FileNotFoundException except){
				System.out.println(except);
				return;
			}
			catch (IOException except){
				System.out.println(except);
				return;
			}
			
			asmTextArea.setText(asmText);
			asmTextArea.setCaretPosition(0);
		}
		else if(e.getActionCommand().equals("Open OBJ file")){
			JFileChooser fileChooser = new JFileChooser();
			fileChooser.setDialogTitle("Open OBJ File");
			
			File file = null;
			while(true){
				fileChooser.showOpenDialog(this);
				
				file = fileChooser.getSelectedFile();
				if(file==null)
					return;
				
				objFilename = file.getPath();
			
				int suffixPosition = objFilename.toLowerCase().indexOf(".obj");
				if(suffixPosition<=0){
					JOptionPane.showMessageDialog(fileChooser, "File format is not correct.\nPlease choose .obj file.", 
						"File Not Correct", JOptionPane.INFORMATION_MESSAGE); 
				}
				else{
					break;
				}
			}			
			
			BufferedReader inStream = null;
			try{
				inStream = new BufferedReader(new FileReader(file));
				String line = null;
				objText = "";
				while((line = inStream.readLine()) != null){
					objText = objText + line + "\n";
				}
				inStream.close();
			}
			catch (FileNotFoundException except){
				System.out.println(except);
				return;
			}
			catch (IOException except){
				System.out.println(except);
				return;
			}
			
			objTextArea.setText(objText);
			objTextArea.setCaretPosition(0);

		}
		else if(e.getActionCommand().equals("Save ASM file")){
			
			if(asmFilename==""){
				JFileChooser fileChooser = new JFileChooser();
				fileChooser.setDialogTitle("Save ASM File");
			
				File file = null;
				String filename;
				
				fileChooser.showSaveDialog(this);
			
				file = fileChooser.getSelectedFile();
				if(file==null)
					return;
			
				filename = file.getPath();
		
				int suffixPosition = filename.toLowerCase().indexOf(".asm");
				if(suffixPosition<=0){
					filename = filename + ".asm";
				}
				
				
				file = new File(filename);
				if(file.exists()){
					System.out.println("exist");
				}
				
				asmFilename = filename;
			}		
			
			saveFile(asmFilename, asmTextArea.getText());
		}
		else if(e.getActionCommand().equals("Save OBJ file")){
			if(objFilename==""){
				JFileChooser fileChooser = new JFileChooser();
				fileChooser.setDialogTitle("Save OBJ File");
			
				File file = null;
				String filename;
				
				fileChooser.showSaveDialog(this);
			
				file = fileChooser.getSelectedFile();
				if(file==null)
					return;
			
				filename = file.getPath();
		
				int suffixPosition = filename.toLowerCase().indexOf(".obj");
				if(suffixPosition<=0){
					filename = filename + ".obj";
				}
				
				
				file = new File(filename);
				if(file.exists()){
					System.out.println("exist");
				}
				
				objFilename = filename;
			}		
			
			saveFile(objFilename, objTextArea.getText());
		}
		else if(e.getActionCommand().equals("Assemble")){
			if(asmFilename == ""){
				infoTextArea.setText("Please open or save ASM file first.\nStopped.");
				return; 
			}
			saveFile(asmFilename, asmTextArea.getText());
			
			int suffixPosition = asmFilename.toLowerCase().indexOf(".asm");
			if(suffixPosition>0){
				String obj;
				try{
					obj = AsmDisasm.Assemble(asmFilename.substring(0, suffixPosition));
				}
				catch (MyException except){
					Vector<String> errors = except.getErrorMessages();
					String info = new String(errors.size() + " error(s):\n");
					
					for(int i=0;i<errors.size();i++){
						info = info + errors.get(i) + "\n";
					}
					info = info + "Aborted.";
					
					infoTextArea.setText(info);
					return;
				}
				objTextArea.setText(obj);
				infoTextArea.setText("Assemble done, ASM file saved");
				objTextArea.setCaretPosition(0);
				
			}
			else{
				// TODO
				System.out.println("file not correct");
			}			
		}
		else if(e.getActionCommand().equals("Disassemble")){
			if(objFilename == ""){
				infoTextArea.setText("Please open or save OBJ file first.\nStopped.");
				return; 
			}
			saveFile(objFilename, objTextArea.getText());
			
			int suffixPosition = objFilename.toLowerCase().indexOf(".obj");
			if(suffixPosition>0){
				String asm;
				try{
					asm = AsmDisasm.DisAssem(objFilename.substring(0, suffixPosition));
				}
				catch (MyException except){
					Vector<String> errors = except.getErrorMessages();
					String info = new String(errors.size() + " error(s):\n");
					
					for(int i=0;i<errors.size();i++){
						info = info + errors.get(i) + "\n";
					}
					info = info + "Aborted.";
					
					infoTextArea.setText(info);
					return;
				}
				asmTextArea.setText(asm);
				infoTextArea.setText("Disassemble done, OBJ file saved");
				asmTextArea.setCaretPosition(0);
			}
			else{
				// TODO
				System.out.println("file not correct");
			}
		}
		else if(e.getActionCommand().equals("Clear temp files")){
			String filename = "";
			int suffixPosition;
			if(!asmFilename.equals("")){
				suffixPosition = asmFilename.toLowerCase().indexOf(".asm");
				
			}
			else{
				return;
			}
			
			if(suffixPosition>0){	
				filename = asmFilename.substring(0, suffixPosition);
				File fileXml = new File(filename+".xml");
				File fileTable = new File(filename+".table");
				if(fileXml.exists()){
					fileXml.delete();
				}
				if(fileTable.exists()){
					fileTable.delete();
				}
				infoTextArea.setText("Temp files cleared.");			
			}
		}
		else if(e.getActionCommand().equals("About")){
			JOptionPane.showMessageDialog(null, "BY\n3120101966 应哲敏\nyingzhemin@gmail.com", 
				"About", JOptionPane.INFORMATION_MESSAGE); 
		}
		else if(e.getActionCommand().equals("Help")){
			JOptionPane.showMessageDialog(null, "No help\nHelp yourself please.", 
				"Help", JOptionPane.INFORMATION_MESSAGE); 
		}
		
	}
	
	private void saveFile(String filename, String content)
	{
		PrintWriter outStream = null;
		try{
			outStream = new PrintWriter(new FileOutputStream(filename));
		}
		catch (FileNotFoundException except){
			System.out.println(except);
		}
		outStream.println(content);
		outStream.close();
		
		return;
	}
	
	public void windowOpened(WindowEvent e)
	{}
		
	public void windowClosing(WindowEvent e)
	{
		System.out.println("closing");
		
		dispose();
		System.exit(0);
	}
	
	public void windowClosed(WindowEvent e)
	{}
		
	public void windowIconified(WindowEvent e)
	{}
		
	public void windowDeiconified(WindowEvent e)
	{}
		
	public void windowActivated(WindowEvent e)
	{}
	
	public void windowDeactivated(WindowEvent e)
	{}
	
	public void windowStateChanged(WindowEvent e)
	{}
}

