/*
 * [The "BSD licence"]
 * Copyright (c) 2010 Ben Gruver (JesusFreke)
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.jf.dexlib.Code.Format;

import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.OffsetInstruction;
import org.jf.dexlib.Code.Opcode;
import org.jf.dexlib.DexFile;
import org.jf.dexlib.Util.AnnotatedOutput;
import org.jf.dexlib.Util.NumberUtils;

public class Instruction20t extends Instruction implements OffsetInstruction {
    public static final InstructionFactory Factory = new Factory();
    private int targetAddressOffset;

    public Instruction20t(Opcode opcode, int offA) {
        super(opcode);
        this.targetAddressOffset = offA;

        if (targetAddressOffset == 0) {
            throw new RuntimeException("The address offset cannot be 0. Use goto/32 instead.");
        }

        //allow out of range address offsets here, so we have the option of replacing this instruction
        //with goto/16 or goto/32 later
    }

    private Instruction20t(Opcode opcode, byte[] buffer, int bufferIndex) {
        super(opcode);

        assert buffer[bufferIndex] == opcode.value;

        this.targetAddressOffset = NumberUtils.decodeShort(buffer, bufferIndex+2);
        assert targetAddressOffset != 0;
    }

    protected void writeInstruction(AnnotatedOutput out, int currentCodeAddress) {
        if (targetAddressOffset == 0) {
            throw new RuntimeException("The address offset cannot be 0. Use goto/32 instead");
        }

        if (targetAddressOffset < -32768 || targetAddressOffset > 32767) {
            throw new RuntimeException("The address offset is out of range. It must be in [-32768,-1] or [1, 32768]");
        }

        out.writeByte(opcode.value);
        out.writeByte(0x00);
        out.writeShort(targetAddressOffset);
    }

    public void updateTargetAddressOffset(int targetAddressOffset) {
        this.targetAddressOffset = targetAddressOffset;
    }

    public Format getFormat() {
        return Format.Format20t;
    }

    public int getTargetAddressOffset() {
        return targetAddressOffset;
    }

    private static class Factory implements InstructionFactory {
        public Instruction makeInstruction(DexFile dexFile, Opcode opcode, byte[] buffer, int bufferIndex) {
            return new Instruction20t(opcode, buffer, bufferIndex);
        }
    }
}
