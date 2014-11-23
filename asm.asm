
main:  #main function
    add $zero,$zero,$zero
    add $zero,$zero,$zero

    addi $sp,$zero,0x700   # 4000
    lui $s7,0x000c   #$s1 vram write addr 

    jal init

mainLoop:
    addi $a0,$zero,0x61
    addi $a1,$zero,0x69
    jal getValidInput
    add $s0,$zero,$v0
    addi $s0,$s0,-32
#
    addi $a0,$zero,37
    addi $a1,$zero,38
    addi $a2,$zero,80
    jal getMatrixPos
    add $v0,$v0,$s7
    sw $s0,0($v0)
    addi $s0,$s0,-65


    addi $a0,$zero,0x61
    addi $a1,$zero,0x69
    jal getValidInput
    add $s1,$zero,$v0
    addi $s1,$s1,-32
#
    addi $a0,$zero,39
    addi $a1,$zero,38
    addi $a2,$zero,80
    jal getMatrixPos
    add $v0,$v0,$s7
    sw $s1,0($v0)
    addi $s1,$s1,-65


    addi $a0,$zero,0x30
    addi $a1,$zero,0x39
    jal getValidInput
    add $s2,$zero,$v0
#
    addi $a0,$zero,41
    addi $a1,$zero,38
    addi $a2,$zero,80
    jal getMatrixPos
    add $v0,$v0,$s7
    sw $s2,0($v0)

    jal delay

    sw $zero,0($v0)
    addi $v0,$v0,-8
    sw $zero,0($v0)
    addi $v0,$v0,-8
    sw $zero,0($v0)

#check and write digit
    add $a0,$zero,$s0
    add $a1,$zero,$s1
    addi $a2,$zero,9
    jal getMatrixPos
    la $t0,problem
    add $v0,$t0,$v0
    lw $t1,0($v0)
    addi $t0,$zero,0xf00
    and $t0,$t0,$t1
    bne $t0,$zero,mainLoopPrintCanNotChange
#write digit in the grid
    sw $s2,0($v0)
    jal init 

    j mainLoop

mainLoopPrintCanNotChange:
#print "can not change"
    addi $a0,$zero,24
    addi $a1,$zero,40
    la $a2,canNotChange
    jal printString

    jal delay
    jal delay
    jal delay
    addi $a1,$zero,40
    jal clearLine

    j mainLoop








#a1: line y
clearLine:
    addi $sp,$sp,-4
    sw $ra,0($sp)

    add $a0,$zero,$zero
    addi $a2,$zero,80
    jal getMatrixPos
    add $v0,$v0,$s7
clearLineLoop:
    beq $a2,$zero,clearLineLoopRet
    sw $zero,0($v0)
    addi $a2,$a2,-1
    addi $v0,$v0,4
    j clearLineLoop
clearLineLoopRet:
    lw $ra,0($sp)
    addi $sp,$sp,4
    jr $ra


#a0: print position x
#a1: print position y
#a2: string address
printString:
    addi $sp,$sp,-8
    sw $s0,0($sp)
    sw $ra,4($sp)

    add $s0,$zero,$a2
    addi $a2,$zero,80
    jal getMatrixPos
    add $v0,$v0,$s7
printStringLoop:
    lw $t0,0($s0)
    beq $t0,$zero,printStringRet
    sw $t0,0($v0)
    addi $s0,$s0,4
    addi $v0,$v0,4
    j printStringLoop

printStringRet:
    lw $s0,0($sp)
    lw $ra,4($sp)
    addi $sp,$sp,8
    jr $ra





#a0: lower bound (include)
#a1: upper bound (include)
#v0: valid input ascii
#this function is blocking
getValidInput:
    addi $sp,$sp,-4
    sw $ra,0($sp)

getValidInputLoop:
    jal getKey
    addi $t0,$zero,-1
    beq $v0,$t0,getValidInputLoop

    addi $t0,$zero,1
    and $t1,$v1,$t0
    bne $t1,$zero,getValidInputLoop

    slt $t0,$v0,$a0
    bne $t0,$zero,getValidInputLoop

    slt $t0,$a1,$v0
    bne $t0,$zero,getValidInputLoop

getValidInputRet:
    lw $ra,0($sp) 
    addi $sp,$sp,4
    jr $ra





#no key: v0=-1
#v0: ascii
#F0: v1[0]=1
#E0: v1[1]=1
getKey:
    addi $sp,$sp,-8
    sw $s0,4($sp)
    sw $ra,0($sp)
    
    add $s0,$zero,$zero
    add $v0,$zero,$zero
    add $v1,$zero,$zero
getKeyLoop:
    jal getScanCode
    
    slt $t0,$v0,$zero
    bne $t0,$zero,noKey

    addi $t0,$zero,0xF0
    beq $v0,$t0,F0Key

    addi $t0,$zero,0xE0
    beq $v0,$t0,E0Key

    
    j getKeyRet

F0Key:
    addi $s0,$s0,1
F0KeyLoop:
    jal getScanCode
    addi $t0,$zero,-1
    beq $t0,$v0,F0KeyLoop
F0KeyLoopRet:
    j getKeyRet

E0Key:
    addi $s0,$s0,0x2
    j getKeyLoop

noKey:
    addi $v0,$zero,-1
getKeyRet:
    add $v1,$zero,$s0
    lw $ra,0($sp)
    lw $s0,4($sp)
    addi $sp,$sp,8
    jr $ra




#v0: scan code 
#actually scan code has been 
#converted into ascii in the hardware circuit
getScanCode:
    addi $t0,$zero,-12288    #ps2 read addr
    lui $t1,0x8000    #mask 

    lw $t2,0($t0)       #scan code in t2[7:0]
    and $t3,$t1,$t2     
    slt $t4,$zero,$t3 
    beq $zero,$t4,noScanCode    

    addi $t1,$zero,0xFF    #mask out [7:0]
    and $t2,$t2,$t1

    add $v0,$zero,$t2
    j getScanCodeRet
noScanCode:
    addi $v0,$zero,-1
getScanCodeRet:
    jr $ra






#initialize the display ui
init:
	addi $sp,$sp,-32
	sw $s0,0($sp)
	sw $s1,4($sp)
	sw $s2,8($sp)
	sw $s3,12($sp) 
	sw $s4,16($sp) 
	sw $s5,20($sp) 
	sw $s6,24($sp) 
	sw $ra,28($sp)
initHyphen:
	addi $s0,$zero,30	#start x
	addi $s1,$zero,17	#start y
	addi $s2,$zero,19	#x count
	addi $s3,$zero,10	#y count
initHyphenLoop:	
	add $a0,$zero,$s0
	add $a1,$zero,$s1
	addi $a2,$zero,80
	jal getMatrixPos
	
	add $v0,$v0,$s7
	addi $t0,$zero,0x2d	#'-'
	sw $t0,0($v0)
	addi $s0,$s0,1
	addi $s2,$s2,-1	#x count -1
	
	bne $s2,$zero,initHyphenLoop
	addi $s0,$zero,30
	addi $s1,$s1,2
	addi $s2,$zero,19
	addi $s3,$s3,-1
	beq $s3,$zero,initVBar
	j initHyphenLoop
	
initVBar:

	addi $s0,$zero,30	#start x
	addi $s1,$zero,18	#start y

	addi $s2,$zero,10 	#x count
	addi $s3,$zero,9	#y count
initVBarLoop:	
	add $a0,$zero,$s0
	add $a1,$zero,$s1
	addi $a2,$zero,80
	jal getMatrixPos
	
	add $v0,$v0,$s7
	addi $t0,$zero,0x7c	#'|'
	sw $t0,0($v0)
	addi $s0,$s0,2
	addi $s2,$s2,-1
	
	bne $s2,$zero,initVBarLoop
	addi $s0,$zero,30
	addi $s1,$s1,2
	addi $s2,$zero,10
	addi $s3,$s3,-1
	beq $s3,$zero,initDigit
	j initVBarLoop
	
initDigit:
	addi $s0,$zero,31	#start x
	addi $s1,$zero,18	#start y
	addi $s4,$zero,9	#x count
	addi $s5,$zero,9	#y count
	addi $s2,$zero,0	#num start x
	addi $s3,$zero,0	#num start y

initDigitLoop:	
	#get num
	add $a0,$zero,$s2
	add $a1,$zero,$s3
	addi $a2,$zero,9
	jal getMatrixPos
	la $t0,problem
	add $v0,$v0,$t0
	lw $s6,0($v0)
	addi $t0,$zero,0xff
	and $s6,$s6,$t0
	#get vga pos
	add $a0,$zero,$s0
	add $a1,$zero,$s1
	addi $a2,$zero,80
	jal getMatrixPos
	
	add $v0,$v0,$s7
	sw $s6,0($v0)
	addi $s0,$s0,2
	addi $s2,$s2,1
	addi $s4,$s4,-1
	
	bne $s4,$zero,initDigitLoop
	addi $s0,$zero,31
	addi $s2,$zero,0
	addi $s1,$s1,2
	addi $s3,$s3,1
	addi $s4,$zero,9
	addi $s5,$s5,-1
	beq $s5,$zero,printIndex
	j initDigitLoop
	
printIndex:
    addi $s0,$zero,31	#horizontal x
	addi $s1,$zero,15	#horizontal y
    addi $s2,$zero,28	#vertical x
	addi $s3,$zero,18	#vertical y
    addi $s4,$zero,9
    addi $s5,$zero,0x41 # 'A'
printIndexLoop:
    beq $s4,$zero,initRet

	add $a0,$zero,$s0
	add $a1,$zero,$s1
	addi $a2,$zero,80
	jal getMatrixPos
	add $v0,$v0,$s7
	sw $s5,0($v0)

	add $a0,$zero,$s2
	add $a1,$zero,$s3
	addi $a2,$zero,80
	jal getMatrixPos
	add $v0,$v0,$s7
	sw $s5,0($v0)

    addi $s0,$s0,2
    addi $s3,$s3,2
    addi $s5,$s5,1
    addi $s4,$s4,-1
    j printIndexLoop
    

initRet:
	lw $s0,0($sp)
	lw $s1,4($sp)
	lw $s2,8($sp)
	lw $s3,12($sp) 
	lw $s4,16($sp) 
	lw $s5,20($sp) 
	lw $s6,24($sp) 
	lw $ra,28($sp)
	addi $sp,$sp,32
	jr $ra




#a0: x
#a1: y
#a2: number in a line
#v0: result = (a2*y + x)*4
getMatrixPos:
    add $t0,$zero,$zero
getMatrixPosLoop:
    beq $a1,$zero,getMatrixPosRet
    add $t0,$t0,$a2
    addi $a1,$a1,-1
    j getMatrixPosLoop
getMatrixPosRet:
    add $v0,$t0,$a0
    sll $v0,$v0,1
    sll $v0,$v0,1
    jr $ra



delay:
    lui $t0, 0x10
delayLoop:
    beq $t0,$zero,delayFinish
    subi $t0,$t0,0x1
    j delayLoop
delayFinish:
    jr $ra






.data 0x500
problem:
    .word 0x20
    .word 0x20
    .word 0xF38
    .word 0xF33
    .word 0x20
    .word 0xF39
    .word 0xF31
    .word 0x20
    .word 0x20
    .word 0xF39
    .word 0x20
    .word 0x20
    .word 0x20
    .word 0xF36
    .word 0x20
    .word 0x20
    .word 0x20
    .word 0xF34
    .word 0x20
    .word 0x20
    .word 0xF37
    .word 0xF35
    .word 0x20
    .word 0xF34
    .word 0xF38
    .word 0x20
    .word 0x20
    .word 0x20
    .word 0xF33
    .word 0xF36
    .word 0x20
    .word 0x20
    .word 0x20
    .word 0xF35
    .word 0xF34
    .word 0x20
    .word 0x20
    .word 0x20
    .word 0xF31
    .word 0x20
    .word 0x20
    .word 0x20
    .word 0xF36
    .word 0x20
    .word 0x20
    .word 0x20
    .word 0xF34
    .word 0xF32
    .word 0x20
    .word 0x20
    .word 0x20
    .word 0xF39
    .word 0xF37
    .word 0x20
    .word 0x20
    .word 0x20
    .word 0xF35
    .word 0xF39
    .word 0x20
    .word 0xF37
    .word 0xF33
    .word 0x20
    .word 0x20
    .word 0xF36
    .word 0x20
    .word 0x20
    .word 0x20
    .word 0xF31
    .word 0x20
    .word 0x20
    .word 0x20
    .word 0xF38
    .word 0x20
    .word 0x20
    .word 0xF34
    .word 0xF36
    .word 0x20
    .word 0xF38
    .word 0xF32
    .word 0x20
    .word 0x20
canNotChange:
    .word 0x59
    .word 0x6f
    .word 0x75
    .word 0x20
    .word 0x63
    .word 0x61
    .word 0x6e
    .word 0x20
    .word 0x4e
    .word 0x4f
    .word 0x54
    .word 0x20
    .word 0x63
    .word 0x68
    .word 0x61
    .word 0x6e
    .word 0x67
    .word 0x65
    .word 0x20
    .word 0x74
    .word 0x68
    .word 0x69
    .word 0x73
    .word 0x20
    .word 0x6e
    .word 0x75
    .word 0x6d
    .word 0x62
    .word 0x65
    .word 0x72
    .word 0x2e
    .word 0x0
.end




