package edu.illinois.cs.testrunner.agent;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

import static org.objectweb.asm.Type.getInternalName;

public class MethodTracer extends MethodVisitor {
    private String methodName;
    private static Set<String> blackList;
    private static Set<String> immutableList;

    public MethodTracer(int api, MethodVisitor methodVisitor, String methodName) {
        super(api, methodVisitor);
        this.methodName = methodName;
    }

    private Set<String> getBlackList() {
        if (blackList == null) {
            blackList = new HashSet<>();

            blackList.add("java");
            blackList.add("sun");
            blackList.add("edu/illinois/cs/testrunner/agent");
            blackList.add("org/apache/maven");
            blackList.add("com/sun");
            blackList.add("jdk");
            blackList.add("org/junit");
        }
        return blackList;
    }

    private void getImmutableList() {
        if (immutableList == null) {
            immutableList = new HashSet<>();

            immutableList.add("java.lang.String");
            immutableList.add("java.lang.Enum");
            immutableList.add("java.lang.StackTraceElement");
            immutableList.add("java.math.BigInteger");
            immutableList.add("java.math.BigDecimal");
            immutableList.add("java.io.File");
            immutableList.add("java.awt.Font");
            immutableList.add("java.awt.BasicStroke");
            immutableList.add("java.awt.Color");
            immutableList.add("java.awt.GradientPaint");
            immutableList.add("java.awt.LinearGradientPaint");
            immutableList.add("java.awt.RadialGradientPaint");
            immutableList.add("java.awt.Cursor");
            immutableList.add("java.util.Locale");
            immutableList.add("java.util.UUID");
            immutableList.add("java.util.Collections");
            immutableList.add("java.net.URL");
            immutableList.add("java.net.URI");
            immutableList.add("java.net.Inet4Address");
            immutableList.add("java.net.Inet6Address");
            immutableList.add("java.net.InetSocketAddress");
            immutableList.add("java.awt.BasicStroke");
            immutableList.add("java.awt.Color");
            immutableList.add("java.awt.GradientPaint");
            immutableList.add("java.awt.LinearGradientPaint");
            immutableList.add("java.awt.RadialGradientPaint");
            immutableList.add("java.awt.Cursor");
            immutableList.add("java.util.regex.Pattern");
        }
    }


    private boolean isImmutable(Field field) {
        boolean isFinal = false;
        if (Modifier.isFinal(field.getModifiers())) {
            isFinal = true;
        }

        if ((field.getType().isPrimitive() || field.getDeclaringClass().isEnum()) && isFinal) {
            return true;
        }

        for (String immutableTypeName : immutableList) {
            if ((field.getType().getName().equals(immutableTypeName)) && isFinal) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void visitFieldInsn(int opcode, String owner, String name, String desc) {
        for (String blackListItem : getBlackList()) {
            if (owner.startsWith(blackListItem)) {
                mv.visitFieldInsn(opcode, owner, name, desc);
                return;
            }
        }
        switch (opcode) {
            case Opcodes.GETSTATIC:
                super.visitLdcInsn(owner.replace("/", ".") + "." + name);
                super.visitMethodInsn(Opcodes.INVOKESTATIC, getInternalName(Helper.class), "store", "(Ljava/lang/String;)V", false);
                mv.visitFieldInsn(opcode, owner, name, desc);
                break;
            case Opcodes.PUTSTATIC:
                mv.visitFieldInsn(opcode, owner, name, desc);
                super.visitLdcInsn(owner.replace("/", ".") + "." + name);
                super.visitMethodInsn(Opcodes.INVOKESTATIC, getInternalName(Helper.class), "store", "(Ljava/lang/String;)V", false);
                break;
            default:
                mv.visitFieldInsn(opcode, owner, name, desc);
                break;
        }
    }



}
