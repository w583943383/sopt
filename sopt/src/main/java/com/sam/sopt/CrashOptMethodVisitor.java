package com.sam.sopt;

import org.objectweb.asm.MethodVisitor;

public class CrashOptMethodVisitor  extends MethodVisitor {

    public CrashOptMethodVisitor(int api) {
        super(api);
    }

    public CrashOptMethodVisitor(int api, MethodVisitor mv) {
        super(api, mv);
    }


}
