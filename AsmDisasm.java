import java.util.*;
import java.io.*;
import java.util.regex.*;



public class AsmDisasm
{
	
	public static void main(String args[])
	{
		AsmDisasm asm = new AsmDisasm();
		asm.Assemble("/users/ying/AsmDisasm-java/asm");
	}
	
	
	public static int Assemble(String filename)
	{		
	    Parser(filename);
	    //Assem(filename);

	    return 0;
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
							System.out.println("matched instruct: matchposition "+matchPosition+"^"+matcher.group(1)+"$"+" len "+matcher.group(0).length());
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
							System.out.println("matched register"+matcher.group(1)+" len "+matcher.group(0).length());
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
					
					System.out.println(position);

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
	
	
	
	private class CoreInstruction{
		String mnemonic;
	    int opcode;
	    int funct;
	
		public CoreInstruction(String mne, int op, int func){
			mnemonic = mne;
			opcode = op;
			funct = func;
		}
	};

	private class Register{
		String name;
	    int number;
	
		public Register(String n, int num){
			name = n;
			number = num;
		}
	};




	
	
	
	private static int Assem(String filename)
	{
		
		static CoreInstruction[] coreInstructionSet = 
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

		static Register[] registerSet = 
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
	
		
		
		BufferedReader streamXml = null;
		PrintWriter streamObjBin = null;
		PrintWriter streamObjHex = null;
		
		try{
			File fxml = new File(filename+".xml");
			File fobjb = new File(filename+".obj");
			File fobjh = new File(filename+".objh");
			
			streamXml = new BufferedReader(new FileReader(fxml));
			streamObjBin = new PrintWriter(new FileOutputStream(fobjb));
			streamObjHex = new PrintWriter(new FileOutputStream(fobjh));
			
		}
		catch (FileNotFoundException except){
			System.out.println(except);
			return -1;
		}
		catch (IOException except){
			System.out.println(except);
			return -1;
		}
		



	    String line;
	    int lineNumber;
	    int instructionAddress=0;
	    int instruction=0;
	    MatchTable matchTable = new MatchTable();
	    int matchId;
	    int rs, rt, rd, shamt;
	    int address;
	    int immediate;
		LookUpTable labelLookUpTable = new LookUpTable();
	    labelLookUpTable.Load(filename);


		try{
		    while((line = streamXml.readLine()) != null){

		        if(line.equals("<blankline/>"))
		            continue;
		        else if(line.equals("<linenumber>"))
		        {
					streamXml.readLine();
		            lineNumber=Integer.parseInt(line);
		        }
		        else if(line.equals("<instruction>"))
		        {
					streamXml.readLine();
		            matchId = matchTable.MatchInstruction(line);

		            if(matchId<0)
		            {
		                System.out.println(line+": Unknown/unsupported instruction name");
		                continue;
		            }
		            instruction = coreInstructionSet[matchId].opcode<<26
		                      | coreInstructionSet[matchId].funct;

		            streamXml.readLine();
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
		                instruction=instruction | rs<<21 | rt<<16 | rd<<11 | shamt<<6;
		                break;

		            default:
		                break;
		            }

		            instructionAddress+=4;
					
					
					
					for(int i=0;i<8;i++){
						streamObjHex.print(Integer.toHexString((instruction >> (7-i)*4)&0xF));
					}
					streamObjHex.println();
					
					
					for(int i=0;i<32;i++){
						streamObjBin.print((instruction >> (31-i))&1);
					}
					streamObjBin.println();
					
		        }

		        line=streamXml.nextLine();

		    }
		}
		catch (IOException except){
			System.out.println(except);
		}
		
		
		
	    try{
		    fileXml.close();
		    fileObjHex.close();
		    fileObj.close();
	    }
		catch (IOException except){
			System.out.println(except);
		}


	    return 0;
	}
	
	
	
	/*
	public static int DisAssem(String filename)
	{
	//QString filename("/Users/ying/assemble");
	    QFile fileObj(filename+".obj");
	    if(!fileObj.open(QIODevice::ReadOnly )){
	        qDebug()<<"Error in opening object file";
	        return -1;
	    }
	    //QTextStream streamObj(&fileObj);
	    QDataStream streamObj(&fileObj);
	    streamObj.setVersion(QDataStream::Qt_5_2);

	    QFile fileDisassem(filename+".asm");
	    if(!fileDisassem.open(QIODevice::WriteOnly | QIODevice::Text)){
	        qDebug()<<"Error in writing to disassemble file";
	        return -1;
	    }
	    QTextStream streamDisassem(&fileDisassem);


	    quint32 instruction;
	    MatchTable matchTable;
	    int matchId;
	    while(!streamObj.atEnd())
	    {
	        streamObj>>instruction;

	        matchId=matchTable.DisassemMatchInstruction(instruction>>26, instruction & 0x3f);
	        switch (matchId) {
	        case 0: case 3: case 4: case 16: case 17: case 19: case 22: case 29: case 30:
	            //add,addu,and,nor,or,slt,sltu,sub,subu
	            streamDisassem<<coreInstructionSet[matchId].mnemonic<<" $"
	                            <<((instruction>>11)&0x1f)<<", $"
	                           <<((instruction>>21)&0x1f)<<", $"
	                             <<((instruction>>16)&0x1f)<<endl;
	            break;

	        case 1: case 2: case 5: case 18: case 20: case 21:
	            //addi,addiu,andi,ori,slti,sltiu
	            streamDisassem<<coreInstructionSet[matchId].mnemonic<<" $"
	                            <<((instruction>>16)&0x1f)<<", $"
	                           <<((instruction>>21)&0x1f)<<", "
	                             <<(instruction&0xffff)<<endl;
	            break;

	        case 6: case 7:
	            //beq,bne
	            streamDisassem<<coreInstructionSet[matchId].mnemonic<<" $"
	                            <<((instruction>>21)&0x1f)<<", $"
	                           <<((instruction>>16)&0x1f)<<", "
	                             <<(instruction&0xffff)<<endl;
	            break;

	        case 8: case 9:
	            //j,jal
	            streamDisassem<<coreInstructionSet[matchId].mnemonic<<" "
	                          <<((instruction&0x3ffffff))<<endl;
	            break;

	        case 10:
	            //jr
	            streamDisassem<<coreInstructionSet[matchId].mnemonic<<" $"
	                          <<((instruction>>21)&0x1f)<<endl;
	            break;

	        case 11: case 12: case 13: case 15: case 25: case 26: case 27: case 28:
	            //lbu,lhu,ll,lw,sb,sc,sh,sw
	            streamDisassem<<coreInstructionSet[matchId].mnemonic<<" $"
	                            <<((instruction>>16)&0x1f)<<", "
	                           <<(instruction&0xffff)<<"($"
	                             <<((instruction>>21)&0x1f)<<")"<<endl;
	            break;

	        case 14:
	            //lui
	            streamDisassem<<coreInstructionSet[matchId].mnemonic<<" $"
	                            <<((instruction>>16)&0x1f)<<", "
	                           <<(instruction&0xffff)<<endl;
	            break;

	        case 23: case 24:
	            //sll,srl
	            streamDisassem<<coreInstructionSet[matchId].mnemonic<<" $"
	                            <<((instruction>>11)&0x1f)<<", $"
	                           <<((instruction>>16)&0x1f)<<", "
	                             <<((instruction>>6)&0x1f)<<endl;
	            break;

	        default:
	            qDebug()<<"Unknown instruction"<<hex<<instruction<<reset;
	            break;
	        }

	    }

	    fileObj.close();
	    fileDisassem.close();

	    return 0;
	}
		
		*/
}









class MatchTable
{
	private final int INSTRUCTIONSETSIZE = 31;
	private final int REGISTERSETSIZE = 31;
	
	
	private class CoreInstruction{
		String mnemonic;
	    int opcode;
	    int funct;
	
		public CoreInstruction(String mne, int op, int func){
			mnemonic = mne;
			opcode = op;
			funct = func;
		}
	};

	private class Register{
		String name;
	    int number;
	
		public Register(String n, int num){
			name = n;
			number = num;
		}
	};



	CoreInstruction[] coreInstructionSet = 
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

	Register[] registerSet = 
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
	
	

	private Pattern registerPatternName = Pattern.compile("^\\$([a-zA-Z]+\\d*)$", Pattern.CASE_INSENSITIVE);
	private Pattern registerPatternNumber = Pattern.compile("^\\$(\\d+)$", Pattern.CASE_INSENSITIVE);

    private String line;
	

	MatchTable()
	{}
	

	int MatchInstruction(String instruct)
	{
	    for(int i=0;i<INSTRUCTIONSETSIZE;i++)
	    {
	        if(instruct.equalsIgnoreCase(coreInstructionSet[i].mnemonic)) return i;
	    }

	    return -1;
	}

	int MatchRegister(String registerName)
	{
	    registerName = registerName.toLowerCase();
		
		
		matchPosition = -1;
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

	int DisassemMatchInstruction(int opcode, int funct)
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

	int instructionEncode(BufferedReader streamXmlBR, String type, LookUpTable labelTable)
	{
	    int matchId;

		Scanner streamXml = new Scanner(streamXmlBR);
		
		line = streamXml.next();
	    if(type.equals("<ref/param>"))
	    {
	        if(!line.equals("<parameter>") && !line.equals("<reference>"))
	        {
				line = streamXml.next();
	            System.out.println(line+"Incompatible operand. A "+type+" is expected");
	            streamXml.nextLine();
	            return -1;
	        }
	    }
	    else if(!line.equals(type))
	    {
			line = streamXml.next();
	        System.out.println(line+"Incompatible operand. A "+type+" is expected");
	        streamXml.nextLine();
	        return -1;
	    }

	    if(line.equals("<register>"))
	    {
			line = streamXml.next();
	        matchId = MatchRegister(line);

	        if(matchId<0)
	            System.out.println(line+"Unknown register number/name");
	        else
	        {
	            streamXml.nextLine();
	            return registerSet[matchId].number;
	        }
	    }
	    else if(line.equals("<parameter>"))
	    {
			line = streamXml.next();
	        if((line[0]=='0'&&line[1]=='x') || (line[0]=='0'&&line[1]=='X'))
	        {
	            streamXml.nextLine();
	            int num = hexTextToInt(line);
	//qDebug()<<"parameter match:"<<num;
	            return num;
	        }
	        else
	        {
	//qDebug()<<"parameter match:"<<line.toInt();
	            streamXml.nextLine();
	            return Integer.parseInt(line);
	        }
	    }
	    else if(line.equals("<reference>"))
	    {
			line = streamXml.next();
	        int address=0;
	        address=labelTable.LookUp(line);
	        if(address<0xffffffff)  //address < 0xffffffff
	        {
	            streamXml.nextLine();
	            return address;
	        }
	        else
	        {
	            System.out.println("Unrecognized label reference "+line);
	        }
	    }

        streamXml.nextLine();
	    return -1;
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
			
				System.out.println(head[i].name+head[i].address);
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
	        if(name.equals(head[i].name))
	            return head[i].address;
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




