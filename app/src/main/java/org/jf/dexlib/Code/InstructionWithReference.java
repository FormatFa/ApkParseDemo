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
 * INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.jf.dexlib.Code;

import org.jf.dexlib.*;
import org.jf.dexlib.Util.NumberUtils;

public abstract class InstructionWithReference extends Instruction {
    private Item referencedItem;

    protected InstructionWithReference(Opcode opcode, Item referencedItem) {
        super(opcode);
        this.referencedItem = referencedItem;
        checkReferenceType();
    }

    protected InstructionWithReference(DexFile dexFile, Opcode opcode, byte[] buffer, int bufferIndex) {
        super(opcode);

        int itemIndex = getReferencedItemIndex(buffer, bufferIndex); 
        lookupReferencedItem(dexFile, opcode, itemIndex);
    }

    protected int getReferencedItemIndex(byte[] buffer, int bufferIndex) {
        return NumberUtils.decodeUnsignedShort(buffer, bufferIndex + 2);
    }

    public Item getReferencedItem() {
        return referencedItem;
    }


    public void setReferencedItem(Item referencedItem) {
        this.referencedItem=referencedItem;
    }


    private void lookupReferencedItem(DexFile dexFile, Opcode opcode, int itemIndex) {
        switch (opcode.referenceType) {
            case field:
                referencedItem = dexFile.FieldIdsSection.getItemByIndex(itemIndex);
                return;
            case method:
                referencedItem = dexFile.MethodIdsSection.getItemByIndex(itemIndex);
                return;
            case type:
                referencedItem = dexFile.TypeIdsSection.getItemByIndex(itemIndex);
                return;
            case string:
                referencedItem = dexFile.StringIdsSection.getItemByIndex(itemIndex);
        }
    }


    private void checkReferenceType() {
        switch (opcode.referenceType) {
            case field:
                if (!(referencedItem instanceof FieldIdItem)) {
                    throw new RuntimeException(referencedItem.getClass().getSimpleName() +
                            " is the wrong item type for opcode " + opcode.name + ". Expecting FieldIdItem.");
                }
                return;
            case method:
                if (!(referencedItem instanceof MethodIdItem)) {
                    throw new RuntimeException(referencedItem.getClass().getSimpleName() +
                            " is the wrong item type for opcode " + opcode.name + ". Expecting MethodIdItem.");
                }
                return;
            case type:
                if (!(referencedItem instanceof TypeIdItem)) {
                    throw new RuntimeException(referencedItem.getClass().getSimpleName() +
                            " is the wrong item type for opcode " + opcode.name + ". Expecting TypeIdItem.");
                }
                return;
            case string:
                if (!(referencedItem instanceof StringIdItem)) {
                    throw new RuntimeException(referencedItem.getClass().getSimpleName() +
                            " is the wrong item type for opcode " + opcode.name + ". Expecting StringIdItem.");
                }
                return;
            default:
                if (referencedItem != null) {
                    throw new RuntimeException(referencedItem.getClass().getSimpleName() +
                            " is invalid for opcode " + opcode.name + ". This opcode does not reference an item");
                }
        }
    }
}
