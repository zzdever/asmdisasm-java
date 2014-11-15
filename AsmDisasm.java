import java.util.*;
import java.io.*;
import java.util.regex.*;

public class AsmDisasm
{
	public static void main(String args[])
	{
		//AsmDisasm.Assemble("/users/ying/asm.asm");
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
	    
		//LookUpTable labelLookUpTable;

	    int count=0;
		try{
			while ((line = streamAsm.readLine()) != null) {
				System.out.println(line);
				// count++;
				// if(count>444)
				// 	break;
				//
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
	                if(matchPosition>=0)
	                {
	                    streamXml.println("\t<label> "+matcher.group(1)+" </label>");
	                    //labelLookUpTable.Push(labelPattern.capturedTexts()[1],address);
	                    position=matchPosition+matcher.group(0).length();             
	                    break;
	                }
					
					
					matchPosition = -1;
					matcher = directivePattern.matcher(line);
					if(matcher.find(position)){
						matchPosition = matcher.start(0);
					}
					
	                if(matchPosition>=0)
	                {
						System.out.println("matched directive: matchposition "+matchPosition+"^"+matcher.group(1)+"$"+" len "+matcher.group(0).length());
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
		
		
		
		
		

				/*
	 

	    labelLookUpTable.Save(filename);


		*/
					
					
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
	
	/*
	private static int Assem(String filename)
	{
	    QFile fileXml(filename+".xml");
	    if(!fileXml.open(QIODevice::ReadOnly | QIODevice::Text)){
	        qDebug()<<"Error in opening temporary file";
	        return -1;
	    }
	    QTextStream streamXml(&fileXml);

	    QFile fileObjHex(filename+".objh");
	    if(!fileObjHex.open(QIODevice::WriteOnly | QIODevice::Text)){
	        qDebug()<<"Error in writing to object file";
	        return -1;
	    }
	    QTextStream streamObj(&fileObjHex);

	    QFile fileObj(filename+".obj");
	    if(!fileObj.open(QIODevice::WriteOnly)){
	        qDebug()<<"Error in writing to object file";
	        return -1;
	    }
	    QDataStream streamObjB(&fileObj);


	    QString line;
	    unsigned int lineNumber;
	    unsigned int instructionAddress=0;
	    unsigned int instruction=0;
	    MatchTable matchTable;
	    int matchId;
	    unsigned short int rs, rt, rd, shamt;
	    unsigned int address;
	    int immediate;
	    LookUpTable labelLookUpTable;
	    labelLookUpTable.Load(filename);

	    while(!streamXml.atEnd()){
	        streamXml>>line;

	        if(line.compare("<blankline/>")==0)
	            continue;
	        else if(line.compare("<linenumber>")==0)
	        {
	            streamXml>>line;
	            lineNumber=line.toInt();
	        }
	        else if(line.compare("<instruction>")==0)
	        {
	            streamXml>>line;
	            matchId=matchTable.MatchInstruction(line);

	            if(matchId<0)
	            {
	                qDebug()<<line<<": Unknown/unsupported instruction name";
	                continue;
	            }
	            instruction=coreInstructionSet[matchId].opcode<<26
	                      | coreInstructionSet[matchId].funct;

	            streamXml.readLine();
	            switch (matchId) {
	            case 0: case 3: case 4: case 16: case 17: case 19: case 22: case 29: case 30:
	                //add,addu,and,nor,or,slt,sltu,sub,subu
	                rd=(unsigned short int)matchTable.instructionEncode(streamXml,"<register>",labelLookUpTable);
	                rs=(unsigned short int)matchTable.instructionEncode(streamXml,"<register>",labelLookUpTable);
	                rt=(unsigned short int)matchTable.instructionEncode(streamXml,"<register>",labelLookUpTable);
	                instruction=instruction | rs<<21 | rt<<16 | rd<<11;
	                break;

	            case 1: case 2: case 5: case 18: case 20: case 21:
	                //addi,addiu,andi,ori,slti,sltiu
	                rt=(unsigned short int)matchTable.instructionEncode(streamXml,"<register>",labelLookUpTable);
	                rs=(unsigned short int)matchTable.instructionEncode(streamXml,"<register>",labelLookUpTable);
	                immediate=(unsigned short int)matchTable.instructionEncode(streamXml,"<parameter>",labelLookUpTable);
	                instruction=instruction | rs<<21 | rt<<16 | immediate;
	                break;

	            case 6: case 7:
	                //beq,bne
	                rs=(unsigned short int)matchTable.instructionEncode(streamXml,"<register>",labelLookUpTable);
	                rt=(unsigned short int)matchTable.instructionEncode(streamXml,"<register>",labelLookUpTable);
	                address=(unsigned short int)matchTable.instructionEncode(streamXml,"<ref/param>",labelLookUpTable);
	                instruction=instruction | rs<<21 | rt<<16 | address;
	                break;

	            case 8: case 9:
	                //j,jal
	                address=(unsigned short int)matchTable.instructionEncode(streamXml,"<ref/param>",labelLookUpTable);
	                instruction=instruction | address;
	                break;

	            case 10:
	                //jr
	                rs=(unsigned short int)matchTable.instructionEncode(streamXml,"<register>",labelLookUpTable);
	                instruction=instruction | rs<<21;
	                break;

	            case 11: case 12: case 13: case 15: case 25: case 26: case 27: case 28:
	                //lbu,lhu,ll,lw,sb,sc,sh,sw
	                rt=(unsigned short int)matchTable.instructionEncode(streamXml,"<register>",labelLookUpTable);
	                immediate=(unsigned short int)matchTable.instructionEncode(streamXml,"<parameter>",labelLookUpTable);
	                rs=(unsigned short int)matchTable.instructionEncode(streamXml,"<register>",labelLookUpTable);
	                instruction=instruction | rs<<21 | rt<<16 | immediate;
	                break;

	            case 14:
	                //lui
	                rt=(unsigned short int)matchTable.instructionEncode(streamXml,"<register>",labelLookUpTable);
	                immediate=(unsigned short int)matchTable.instructionEncode(streamXml,"<parameter>",labelLookUpTable);
	                instruction=instruction | rt<<16 | immediate;
	                break;

	            case 23: case 24:
	                //sll,srl
	                rd=(unsigned short int)matchTable.instructionEncode(streamXml,"<register>",labelLookUpTable);
	                rt=(unsigned short int)matchTable.instructionEncode(streamXml,"<register>",labelLookUpTable);
	                shamt=(unsigned short int)matchTable.instructionEncode(streamXml,"<parameter>",labelLookUpTable);
	                instruction=instruction | rs<<21 | rt<<16 | rd<<11 | shamt<<6;
	                break;

	            default:
	                break;
	            }

	            instructionAddress+=4;
	            streamObj<<qSetFieldWidth(8)<<qSetPadChar('0')<<hex<<instruction;
	            streamObj<<reset;
	            streamObj<<endl;
	            streamObjB<<instruction;
	        }

	        line=streamXml.readLine();


	    }

	    fileXml.close();
	    fileObjHex.close();
	    fileObj.close();

	    return 0;
	}
	
	
	
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










