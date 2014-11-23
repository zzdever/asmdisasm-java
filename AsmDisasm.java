import java.util.*;
import java.io.*;
import java.util.regex.*;



class MyException extends Exception
{
	Vector<String> errorMessages;
	
	public MyException()
	{
		errorMessages = new Vector<String>(10);
	}

	
	public void addErrorMessage(String message)
	{
		errorMessages.add(message);
	}
	
	public Vector<String> getErrorMessages()
	{
		return errorMessages;
	}
	
	public boolean isEmpty()
	{
		return errorMessages.size()<=0;
	}
	
}


public class AsmDisasm extends IR
{
	
	public static void main(String args[])
	{
		//AsmDisasm asm = new AsmDisasm();
		//asm.Assemble("/users/ying/AsmDisasm-java/asm");
		//asm.Assemble("/users/ying/AsmDisasm-java/a");
	}
	
	
	public static String Assemble(String filename) throws MyException
	{		
	    Parser(filename);
	    return Assem(filename);
	}
	
	
	private static int Parser(String filename)
	{	
		BufferedReader streamAsm = null;
		PrintWriter streamXml = null;
		try{
			File fasm = new File(filename+".asm");
			File fxml = new File(filename+".xml");
			streamAsm = new BufferedReader(new FileReader(fasm));
			streamXml = new PrintWriter(new FileOutputStream(fxml));
		}
		catch (FileNotFoundException except){
			System.out.println(except);
			return -1;
		}
		catch (IOException except){
			System.out.println(except);
			return -1;
		}
		
		
		Pattern instructionPattern = Pattern.compile("\\s*([a-zA-Z]+)\\s*", Pattern.CASE_INSENSITIVE);
	    Pattern registerPattern = Pattern.compile("[,\\s\\(]*(\\$\\w+)\\s*[,\\)]*\\s*", Pattern.CASE_INSENSITIVE);
	    Pattern parameterPattern = Pattern.compile("[,\\s]*([\\+\\-\\w]+)[,\\(]*\\s*", Pattern.CASE_INSENSITIVE);
	    Pattern labelPattern = Pattern.compile("\\s*(\\w+):\\s*", Pattern.CASE_INSENSITIVE);
		Pattern directivePattern = Pattern.compile("\\s*(.\\w+)\\s*", Pattern.CASE_INSENSITIVE);
	    Pattern blanklinePattern = Pattern.compile("^\\s*$", Pattern.CASE_INSENSITIVE);
		Matcher matcher;
		

	    String line;
	    int lineNumber=0;
	    int address=0;
	    
		LookUpTable labelLookUpTable = new LookUpTable();


		try{
			while ((line = streamAsm.readLine()) != null) {
		
		        streamXml.println("<linenumber> "+(++lineNumber)+" </linenumber>");
				
				matcher = blanklinePattern.matcher(line);
				if((matcher.find())){
		            streamXml.println("\t<blankline/>");
		            continue;
		        }



		        int matchPosition=-1;
		        int position=0;
		        boolean instructionFlag=false;
				
		        while(position<line.length())
		        {
		            if(line.charAt(position)=='#' || (line.charAt(position)=='/'&&line.charAt(position+1)=='/'))
		                break;
					
					
					matchPosition = -1;
					matcher = labelPattern.matcher(line);
					if(matcher.find(position)){
						matchPosition = matcher.start(0);
					}
	                if(matchPosition==0)
	                {
	                    streamXml.println("\t<label> "+matcher.group(1)+" </label>");
	                    labelLookUpTable.Push(matcher.group(1), address);
	                    position=matchPosition+matcher.group(0).length();             
	                    break;
	                }
					
					
					
					// TODO: implement directive
					/*
					matchPosition = -1;
					matcher = directivePattern.matcher(line);
					if(matcher.find(position)){
						matchPosition = matcher.start(0);
					}
					
	                if(matchPosition>=0)
	                {
	                    streamXml.println("\t<directive> "+matcher.group(1)+" </directive>");
	                    position=matchPosition+matcher.group(0).length();
						
						
						if(matcher.group(1).equalsIgnoreCase(".text")){
							;
						}
						else if(true){
							;
						}
						
	                    continue;
	                }
					*/
							
								
					matchPosition = -1;
		            if(instructionFlag==false)
		            {
						matcher = instructionPattern.matcher(line);
						if(matcher.find(position)){
							matchPosition = matcher.start(0);
						}
						
		                if(matchPosition>=0)
		                {
							//System.out.println("matched instruct: matchposition "+matchPosition+"^"+matcher.group(1)+"$"+" len "+matcher.group(0).length());
		                    streamXml.println("\t<instruction> "+matcher.group(1)+" </instruction>");
		                    position=matchPosition+matcher.group(0).length();

		                    instructionFlag=true;
		                    address+=4;  //address counting
		                    continue;
		                }
		            }

		            //consider to use pattern.pos() to judge
					matchPosition = -1;
		            if(line.charAt(position)=='$')
		            {
						matcher = registerPattern.matcher(line);
						if(matcher.find(position)){
							matchPosition = matcher.start(0);
						}
						

		                if(matchPosition>=0)
		                {
							//System.out.println("matched register"+matcher.group(1)+" len "+matcher.group(0).length());
		                    streamXml.println("\t\t<register> "+matcher.group(1)+" </register>");
		                    position=matchPosition+matcher.group(0).length();
		                    continue;
		                }
		            }

					matchPosition = -1;
					matcher = parameterPattern.matcher(line);
					if(matcher.find(position)){
						matchPosition = matcher.start(0);
					}
		            if(matchPosition>=0)
		            {
		                if((line.charAt(position)>='a'&&line.charAt(position)<='z')
		                        ||(line.charAt(position)>='A'&&line.charAt(position)<='Z'))
		                    streamXml.println("\t\t<reference> "+matcher.group(1)+" </reference>");
		                else
		                    streamXml.println("\t\t<parameter> "+matcher.group(1)+" </parameter>");
	                    position=matchPosition+matcher.group(0).length();
		                continue;
		            }
					
		        }

		        if(position<line.length())
		        {
		            streamXml.println("\t<comment> "+line.substring(position,line.length())
		                    +" </comment>");
		        }		
			}		
		}
		catch (IOException except){
			System.out.println(except);
		}


	    labelLookUpTable.Save(filename);
					
					
		try{
			streamAsm.close();
			streamXml.close();
		}
		catch (IOException except){
			System.out.println(except);
			return -1;
		}
					
	    return 0;
					
	}
	
	
	
	
	private static String Assem(String filename) throws MyException
	{	
		
		BufferedReader streamXmlBR = null;
		//PrintWriter streamObjBin = null;
		StringBuffer streamObjBin = new StringBuffer();
		MyException errors = new MyException();
		//PrintWriter streamObjHex = null;
		
		try{
			File fxml = new File(filename+".xml");
			//File fobjb = new File(filename+".obj");
			//File fobjh = new File(filename+".objh");
			
			streamXmlBR = new BufferedReader(new FileReader(fxml));
			//streamObjBin = new PrintWriter(new FileOutputStream(fobjb));
			//streamObjHex = new PrintWriter(new FileOutputStream(fobjh));
			
		}
		catch (FileNotFoundException except){
			System.out.println(except);
			return "";
		}
		catch (IOException except){
			System.out.println(except);
			return "";
		}
		



	    String line;
	    int lineNumber = 0;
	    int instructionAddress=0;
	    int instruction=0;
	    MatchTable matchTable = new MatchTable();
	    int matchId;
	    int rs=0, rt=0, rd=0, shamt=0;
	    int address;
	    int immediate;
		LookUpTable labelLookUpTable = new LookUpTable();
	    labelLookUpTable.Load(filename);

		Scanner streamXml = new Scanner(streamXmlBR);
		
		
		try{
		    while((line = streamXml.next()) != null)
			{
				try{

		        if(line.equals("<blankline/>"))
		            continue;
		        else if(line.equals("<linenumber>"))
		        {
		            line = streamXml.next();
		            lineNumber=Integer.parseInt(line);
		        }
		        else if(line.equals("<instruction>"))
		        {
		            line = streamXml.next();
		            matchId = matchTable.MatchInstruction(line);

		            if(matchId<0)
		            {
						errors.addErrorMessage(lineNumber+"|"+line+"|"+"Unknown/unsupported instruction name");
		                //System.out.println(line+": Unknown/unsupported instruction name");
		                continue;
		            }
		            instruction = coreInstructionSet[matchId].opcode<<26
		                      | coreInstructionSet[matchId].funct;

		            streamXml.nextLine();
		            switch (matchId) {
			            case 0: case 3: case 4: case 16: case 17: case 19: case 22: case 29: case 30:
			                //add,addu,and,nor,or,slt,sltu,sub,subu
			                rd=matchTable.instructionEncode(streamXml,"<register>",labelLookUpTable);
			                rs=matchTable.instructionEncode(streamXml,"<register>",labelLookUpTable);
			                rt=matchTable.instructionEncode(streamXml,"<register>",labelLookUpTable);
			                instruction=instruction | rs<<21 | rt<<16 | rd<<11;
			                break;

			            case 1: case 2: case 5: case 18: case 20: case 21:
			                //addi,addiu,andi,ori,slti,sltiu
			                rt=matchTable.instructionEncode(streamXml,"<register>",labelLookUpTable);
			                rs=matchTable.instructionEncode(streamXml,"<register>",labelLookUpTable);
			                immediate=matchTable.instructionEncode(streamXml,"<parameter>",labelLookUpTable);
			                instruction=instruction | rs<<21 | rt<<16 | immediate;
			                break;

			            case 6: case 7:
			                //beq,bne
			                rs=matchTable.instructionEncode(streamXml,"<register>",labelLookUpTable);
			                rt=matchTable.instructionEncode(streamXml,"<register>",labelLookUpTable);
			                address=matchTable.instructionEncode(streamXml,"<ref/param>",labelLookUpTable);
			                instruction=instruction | rs<<21 | rt<<16 | address;	// TODO: address/4 ?
			                break;

			            case 8: case 9:
			                //j,jal
			                address=matchTable.instructionEncode(streamXml,"<ref/param>",labelLookUpTable);
			                instruction=instruction | address;	// TODO: address/4 ?
			                break;

			            case 10:
			                //jr
			                rs=matchTable.instructionEncode(streamXml,"<register>",labelLookUpTable);
			                instruction=instruction | rs<<21;
			                break;

			            case 11: case 12: case 13: case 15: case 25: case 26: case 27: case 28:
			                //lbu,lhu,ll,lw,sb,sc,sh,sw
			                rt=matchTable.instructionEncode(streamXml,"<register>",labelLookUpTable);
			                immediate=matchTable.instructionEncode(streamXml,"<parameter>",labelLookUpTable);
			                rs=matchTable.instructionEncode(streamXml,"<register>",labelLookUpTable);
			                instruction=instruction | rs<<21 | rt<<16 | immediate;
			                break;

			            case 14:
			                //lui
			                rt=matchTable.instructionEncode(streamXml,"<register>",labelLookUpTable);
				                immediate=matchTable.instructionEncode(streamXml,"<parameter>",labelLookUpTable);
				                instruction=instruction | rt<<16 | immediate;
				                break;

				            case 23: case 24:
				                //sll,srl
				                rd=matchTable.instructionEncode(streamXml,"<register>",labelLookUpTable);
				                rt=matchTable.instructionEncode(streamXml,"<register>",labelLookUpTable);
				                shamt=matchTable.instructionEncode(streamXml,"<parameter>",labelLookUpTable);
				                instruction=instruction | rt<<16 | rd<<11 | shamt<<6;
				                break;

				            default:
					
				                break;
			            }
				

			            instructionAddress+=4;
								
				
						/*
						for(int i=0;i<8;i++){
							streamObjHex.print(Integer.toHexString((instruction >> (7-i)*4)&0xF));
							System.out.println(Integer.toHexString((instruction >> (7-i)*4)&0xF));
						}
						streamObjHex.println();
						*/
				
				
						for(int i=0;i<32;i++){
							streamObjBin.append((instruction >> (31-i))&1);
							//streamObjBin.print((instruction >> (31-i))&1);
						}
						//streamObjBin.println();
						streamObjBin.append("\n");
				
			        }

			        line=streamXml.nextLine();

			    }
				catch (MyException except){
					Vector<String> e = except.getErrorMessages();
					for(int i=0;i<e.size();i++){
						errors.addErrorMessage(lineNumber+"|"+e.get(i));
				}
			}
		
			}
			
		}
		catch (NoSuchElementException e){
			;
		}
		
	    
		if(!errors.isEmpty()){
			throw errors;
		}
		
	    try{
		    streamXmlBR.close();
		    //streamObjHex.close();
		    //streamObjBin.close();
	    }
		catch (IOException except){
			System.out.println(except);
		}


	    return streamObjBin.toString();
	}
	
	
	

	public static String DisAssem(String filename) throws MyException
	{
		BufferedReader streamObjBinBR = null;
		MyException errors = new MyException();
		
		
		try{
			File fobjb = new File(filename+".obj");
			
			streamObjBinBR = new BufferedReader(new FileReader(fobjb));
			
		}
		catch (FileNotFoundException except){
			System.out.println(except);
			return "";
		}
		catch (IOException except){
			System.out.println(except);
			return "";
		}
		
		Scanner streamObjBin = new Scanner(streamObjBinBR);
		StringBuffer streamDisassem = new StringBuffer();


	    String instruction="";
	    MatchTable matchTable = new MatchTable();
	    int matchId;
		int lineNumber = 0;
		
		try{
			int instruction_31_26 = 0;
			int instruction_25_21 = 0;
			int instruction_20_16 = 0;
			int instruction_15_11 = 0;
			int instruction_10_6 = 0;
			int instruction_5_0 = 0;
			int instruction_6_0 = 0;
			int instruction_15_0 = 0;
			int instruction_26_0 = 0;
		    while(true)
		    {
				instruction = streamObjBin.nextLine().trim();
				lineNumber++;
				
				
				if(instruction.length()<=0) continue;
				
				try{
					instruction_31_26 = Integer.parseInt(instruction.substring(0,6), 2);
					instruction_25_21 = Integer.parseInt(instruction.substring(6,11), 2);
					instruction_20_16 = Integer.parseInt(instruction.substring(11,16), 2);
					instruction_15_11 = Integer.parseInt(instruction.substring(16,21), 2);
					instruction_10_6 = Integer.parseInt(instruction.substring(21,26), 2);
					instruction_5_0 = Integer.parseInt(instruction.substring(26,32), 2);
					instruction_6_0 = Integer.parseInt(instruction.substring(25,32), 2);
					instruction_15_0 = Integer.parseInt(instruction.substring(16,32), 2);
					instruction_26_0 = Integer.parseInt(instruction.substring(6,32), 2);
					
				}
				catch (NumberFormatException e){
					errors.addErrorMessage(lineNumber+"|"+"Unknown instruction: "+instruction);
				}

		        matchId=matchTable.DisassemMatchInstruction(instruction_31_26, instruction_6_0);
		        switch (matchId) {
		        case 0: case 3: case 4: case 16: case 17: case 19: case 22: case 29: case 30:
		            //add,addu,and,nor,or,slt,sltu,sub,subu
		            streamDisassem.append(coreInstructionSet[matchId].mnemonic+" $"
		                            +(registerSet[instruction_15_11].name)+", $"
		                           +(registerSet[instruction_25_21].name)+", $"
		                             +(registerSet[instruction_20_16].name)+"\n");
		            break;

		        case 1: case 2: case 5: case 18: case 20: case 21:
		            //addi,addiu,andi,ori,slti,sltiu
		            streamDisassem.append(coreInstructionSet[matchId].mnemonic+" $"
		                            +(registerSet[instruction_20_16].name)+", $"
		                           +(registerSet[instruction_25_21].name)+", 0x"
		                             +(Integer.toString(instruction_15_0,16))+"\n");
		            break;

		        case 6: case 7:
		            //beq,bne
		            streamDisassem.append(coreInstructionSet[matchId].mnemonic+" $"
		                            +(registerSet[instruction_25_21].name)+", $"
		                           +(registerSet[instruction_20_16].name)+", 0x"
		                             +(Integer.toString(instruction_15_0,16))+"\n");
		            break;

		        case 8: case 9:
		            //j,jal
		            streamDisassem.append(coreInstructionSet[matchId].mnemonic+" 0x"
		                          +(Integer.toString(instruction_26_0,16))+"\n");
		            break;

		        case 10:
		            //jr
		            streamDisassem.append(coreInstructionSet[matchId].mnemonic+" $"
		                          +(registerSet[instruction_25_21].name)+"\n");
		            break;

		        case 11: case 12: case 13: case 15: case 25: case 26: case 27: case 28:
		            //lbu,lhu,ll,lw,sb,sc,sh,sw
					streamDisassem.append(coreInstructionSet[matchId].mnemonic+" $"
					                            +(registerSet[instruction_20_16].name)+", "
					                           +(instruction_15_0)+"($"
					                             +(registerSet[instruction_25_21].name)+")"+"\n");
		            break;

		        case 14:
		            //lui
		            streamDisassem.append(coreInstructionSet[matchId].mnemonic+" $"
		                            +(registerSet[instruction_20_16].name)+", 0x"
		                           +(Integer.toString(instruction_15_0,16))+"\n");
		            break;

		        case 23: case 24:
		            //sll,srl
		            streamDisassem.append(coreInstructionSet[matchId].mnemonic+" $"
		                            +(registerSet[instruction_15_11].name)+", $"
		                           +(registerSet[instruction_20_16].name)+", 0x"
		                             +(Integer.toString(instruction_10_6,16))+"\n");
		            break;

		        default:
					errors.addErrorMessage(lineNumber+"|"+"Unknown instruction: "+instruction);
		            break;
		        }

		    }
		}
		catch (NoSuchElementException except){
			;
		}
		
		
		if(!errors.isEmpty()){
			throw errors;
		}
		
		try{
			streamObjBinBR.close();
		}
		catch (IOException except){
			System.out.println(except);
		}
	    
	    return streamDisassem.toString();
	}

}




class CoreInstruction{
	String mnemonic;
    int opcode;
    int funct;

	public CoreInstruction(String mne, int op, int func){
		mnemonic = mne;
		opcode = op;
		funct = func;
	}
};

class Register{
	String name;
    int number;

	public Register(String n, int num){
		name = n;
		number = num;
	}
};


// CoreInstruction and Register
class IR{
	
	public static CoreInstruction[] coreInstructionSet = 
	{
	    new CoreInstruction("add", 0, 0x20),
	    new CoreInstruction("addi", 0x8, 0),
	    new CoreInstruction("addiu", 0x9, 0),
	    new CoreInstruction("addu", 0, 0x21),
	    new CoreInstruction("and", 0, 0x24),
	    new CoreInstruction("andi", 0xC, 0),
	    new CoreInstruction("beq", 0x4, 0),
	    new CoreInstruction("bne", 0x5, 0),
	    new CoreInstruction("j", 0x2, 0),
	    new CoreInstruction("jal", 0x3, 0),
	    new CoreInstruction("jr", 0, 0x8),
	    new CoreInstruction("lbu", 0x24, 0),
	    new CoreInstruction("lhu", 0x25, 0),
	    new CoreInstruction("ll", 0x30, 0),
	    new CoreInstruction("lui", 0xF, 0),
	    new CoreInstruction("lw", 0x23, 0),
	    new CoreInstruction("nor", 0, 0x27),
	    new CoreInstruction("or", 0, 0x25),
	    new CoreInstruction("ori", 0xD, 0),
	    new CoreInstruction("slt", 0, 0x2A),
	    new CoreInstruction("slti", 0xA, 0),
	    new CoreInstruction("sltiu", 0xB, 0),
	    new CoreInstruction("sltu", 0, 0x2B),
	    new CoreInstruction("sll", 0, 0),
	    new CoreInstruction("srl", 0, 0x2),
	    new CoreInstruction("sb", 0x28, 0),
	    new CoreInstruction("sc", 0x38, 0),
	    new CoreInstruction("sh", 0x29, 0),
	    new CoreInstruction("sw", 0x2B, 0),
	    new CoreInstruction("sub", 0, 0x22),
	    new CoreInstruction("subu", 0, 0x2)
	};

	public static Register[] registerSet = 
	{
	    new Register("zero", 0),
	    new Register("at", 1),
	    new Register("v0", 2),
	    new Register("v1", 3),
	    new Register("a0", 4),
	    new Register("a1", 5),
	    new Register("a2", 6),
	    new Register("a3", 7),
	    new Register("t0", 8),
	    new Register("t1", 9),
	    new Register("t2", 10),
	    new Register("t3", 11),
	    new Register("t4", 12),
	    new Register("t5", 13),
	    new Register("t6", 14),
	    new Register("t7", 15),
	    new Register("s0", 16),
	    new Register("s1", 17),
	    new Register("s2", 18),
	    new Register("s3", 19),
	    new Register("s4", 20),
	    new Register("s5", 21),
	    new Register("s6", 22),
	    new Register("s7", 23),
	    new Register("t8", 24),
	    new Register("t9", 25),
	    new Register("k0", 26),
	    new Register("k1", 27),
	    new Register("gp", 28),
	    new Register("sp", 29),
	    new Register("fp", 30),
	    new Register("ra", 31),
	    new Register("pc", 32)
	};
}



class MatchTable extends IR 
{
	private final int INSTRUCTIONSETSIZE = 31;
	private final int REGISTERSETSIZE = 31;
	

	private Pattern registerPatternName = Pattern.compile("^\\$([a-zA-Z]+\\d*)$", Pattern.CASE_INSENSITIVE);
	private Pattern registerPatternNumber = Pattern.compile("^\\$(\\d+)$", Pattern.CASE_INSENSITIVE);
	Matcher matcher;
	
    private String line;
	

	MatchTable()
	{}
	

	public int MatchInstruction(String instruct)
	{
	    for(int i=0;i<INSTRUCTIONSETSIZE;i++)
	    {
	        if(instruct.equalsIgnoreCase(coreInstructionSet[i].mnemonic)) return i;
	    }

	    return -1;
	}

	public int MatchRegister(String registerName)
	{
	    registerName = registerName.toLowerCase();
		
		
		int matchPosition = -1;
		matcher = registerPatternNumber.matcher(registerName);
		if(matcher.find()){
			return Integer.parseInt(matcher.group(1));
		}
		
		matchPosition = -1;
		matcher = registerPatternName.matcher(registerName);
		if(matcher.find()){
			registerName = matcher.group(1);
	        for(int i=0;i<REGISTERSETSIZE;i++)
	        {
	            if(registerName.equalsIgnoreCase(registerSet[i].name)) return i;
	        }
		}

	    return -1;
	}

	public int DisassemMatchInstruction(int opcode, int funct)
	{
	    for(int i=0;i<INSTRUCTIONSETSIZE;i++)
	    {
	        if(i==0 || i==3 || i==4 || i==10
	                || i==16 || i==17 || i==19
	                || i==22 || i==23 || i==24
	                || i==29 || i==30 )
	        {
	            if(opcode==coreInstructionSet[i].opcode && funct==coreInstructionSet[i].funct)
	                        return i;
	        }
	        else
	        {
	            if(opcode==coreInstructionSet[i].opcode)
	                        return i;
	        }
	    }
	    return -1;
	}

	public int instructionEncode(Scanner streamXml, String type, LookUpTable labelTable) throws MyException
	{
	    int matchId;
		MyException errors = new MyException();
		//Scanner streamXml = new Scanner(streamXmlBR);
		
		line = streamXml.next();
	    if(type.equals("<ref/param>"))
	    {
	        if(!line.equals("<parameter>") && !line.equals("<reference>"))
	        {
				line = streamXml.next();
	            errors.addErrorMessage(line+"|Incompatible operand. A "+type+" is expected");
				streamXml.nextLine();
				throw(errors);
	        }
	    }
	    else if(!line.equals(type))
	    {
			line = streamXml.next();
			errors.addErrorMessage(line+"|Incompatible operand. A "+type+" is expected");
	        streamXml.nextLine();
	        throw(errors);
	    }

	    if(line.equals("<register>"))
	    {
			line = streamXml.next();
	        matchId = MatchRegister(line);

	        if(matchId<0)
	            errors.addErrorMessage(line+"|Unknown register number/name");
	        else
	        {
	            streamXml.nextLine();
	            return registerSet[matchId].number;
	        }
	    }
	    else if(line.equals("<parameter>"))
	    {
			line = streamXml.next();
			streamXml.nextLine();
			
			try{    
	            int num = Integer.parseInt(line);
				return num; 
			}
			catch (NumberFormatException e){
				try{
					int num = hexTextToInt(line);
				}
				catch (NumberFormatException ee){
					return -1;
				}
			}

	    }
	    else if(line.equals("<reference>"))
	    {
			line = streamXml.next();
	        int address=0;
	        address=labelTable.LookUp(line);
	        if(address>=0)  //address < 0xffffffff
	        {
	            streamXml.nextLine();
	            return address;
	        }
	        else
	        {
	            errors.addErrorMessage(line+"|Unrecognized label reference "+line);
	        }
	    }

        streamXml.nextLine();
	    throw(errors);
	}

	int hexTextToInt(String line)
	{
		// line = "^0x123$"
		return Integer.parseInt(line.substring(2), 16);
	}
	
}



class LookUpTable
{

	private final int MAXLABELAMOUNT = 512;
	private final int MAXLABELLENGTH = 64;
		
    private LabelTable[] head = null;
    private int labelAmount = 0;
		
	

	private class LabelTable{
	    String name;
	    int address;
	}


	public LookUpTable()
	{
	    head = new LabelTable[MAXLABELAMOUNT];
	    labelAmount=0;
	}

	public void Load(String filename)
	{			
		BufferedReader streamLabelLookUpTable = null;
		try{
			File fileLabelLookUpTable = new File(filename+".table");
			streamLabelLookUpTable = new BufferedReader(new FileReader(fileLabelLookUpTable));
		}
		catch (FileNotFoundException except){
			System.out.println(except);
			return;
		}
		catch (IOException except){
			System.out.println(except);
			return;
		}
		
	    int i=0;
		String line = null;
		StringTokenizer token = null;
		try{
		    while((line = streamLabelLookUpTable.readLine()) != null)
		    {
				token = new StringTokenizer(line);
				if(head[i]==null){
					head[i] = new LabelTable();
				}
		        head[i].name = token.nextToken();
		        head[i].address = Integer.parseInt(token.nextToken());
			
		        i++;
		        labelAmount++;
		    }

		}
		catch (IOException except){
			System.out.println(except);
		}
	    

		try{
		    streamLabelLookUpTable.close();
		}
		catch (IOException except){
			System.out.println(except);
			return;
		}

	    return;
	}

	public int LookUp(String name)
	{
	    for(int i=0; i<labelAmount;i++)
	    {
	        if(name.equals(head[i].name)){
	            return head[i].address;
			}
	    }
	    return -1;  //0xffffffff
	}

	public void Push(String name, int addr)
	{
	    if(labelAmount>=MAXLABELAMOUNT)
	    {
	        System.out.println("Label lookup table is full!");
	        return;
	    }
		
		if(head[labelAmount]==null){
			head[labelAmount] = new LabelTable();
		}
	    head[labelAmount].name=name;
	    head[labelAmount].address=addr;
	    labelAmount++;

	    return;
	}

	public void Save(String filename)
	{
		
		PrintWriter streamLabelLookUpTable = null;
		try{
			File fileLabelLookUpTable = new File(filename+".table");
			streamLabelLookUpTable = new PrintWriter(new FileOutputStream(fileLabelLookUpTable));
		}
		catch (FileNotFoundException except){
			System.out.println(except);
			return;
		}
		catch (IOException except){
			System.out.println(except);
			return;
		}
		
		
	    for(int i=0;i<labelAmount;i++)
	    {
	        streamLabelLookUpTable.println(head[i].name+" "+head[i].address);
	    }


	    streamLabelLookUpTable.close();
		
	    return;
	}
	
}




