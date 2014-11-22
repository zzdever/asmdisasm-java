main:  #main function
    add $zero,$zero,0x3
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


    addi $a0,$zero,37
    addi $a1,$zero,38
    addi $a2,$zero,80
    jal getMatrixPos
    add $v0,$v0,$s7
    sw $s0,0($v0)
    addi $s0,$s0,-65

