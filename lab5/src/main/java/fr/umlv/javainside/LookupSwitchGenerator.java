package fr.umlv.javainside;

import org.objectweb.asm.*;
// import org.objectweb.asm.util.CheckClassAdapter;

import java.io.PrintWriter;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Arrays;
import java.util.Comparator;

import static java.lang.invoke.MethodHandles.lookup;
import static java.util.stream.IntStream.range;
import static org.objectweb.asm.ClassWriter.COMPUTE_FRAMES;
import static org.objectweb.asm.ClassWriter.COMPUTE_MAXS;
import static org.objectweb.asm.Opcodes.*;

public class LookupSwitchGenerator {
    private static void emitCallAndReturn(MethodVisitor mv, String invokeExactDesc, int mhIndex, Class<?> returnType, Class<?>[] parameterTypes) {
        mv.visitVarInsn(ALOAD, mhIndex);
        var slot = 0;
        for(var parameterType: parameterTypes) {
            var asmType = Type.getType(parameterType);
            mv.visitVarInsn(asmType.getOpcode(ILOAD), slot);
            slot += asmType.getSize();
        }
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/invoke/MethodHandle", "invokeExact", invokeExactDesc, false);
        var asmReturnType = Type.getType(returnType) ;
        mv.visitInsn(asmReturnType.getOpcode(IRETURN));
    }

    private static byte[] generate(int[] hashCodes, MethodType methodType, MethodType type) {
        var parameters = range(0, hashCodes.length).boxed().toArray(Integer[]::new);
        Arrays.sort(parameters, Comparator.comparingInt(i -> hashCodes[i]));
        var keys = Arrays.stream(parameters).mapToInt(i -> hashCodes[i]).toArray();
        var labels = range(0, keys.length).mapToObj(__ -> new Label()).toArray(Label[]::new);

        var writer = new ClassWriter(COMPUTE_MAXS | COMPUTE_FRAMES);
        writer.visit(V11, ACC_SUPER | ACC_PUBLIC, "fr/umlv/javainside/LookupSwitch", null, "java/lang/Object", null);
        var mv = writer.visitMethod(ACC_PUBLIC | ACC_STATIC, "switch", type.descriptorString(), null, null);
        mv.visitCode();

        var parameterTypes = methodType.parameterArray();
        var parameterInSlots = Arrays.stream(parameterTypes).mapToInt(t -> (t == long.class || t == double.class)? 2: 1).sum();
        var invokeExactDesc = methodType.descriptorString();

        var defaultLabel = new Label();
        mv.visitVarInsn(ILOAD, 0);
        mv.visitLookupSwitchInsn(defaultLabel, keys, labels);
        for(var i = 0; i < parameters.length; i++) {
            mv.visitLabel(labels[i]);
            emitCallAndReturn(mv, invokeExactDesc, parameterInSlots + parameters[i], methodType.returnType(), parameterTypes);
        }
        mv.visitLabel(defaultLabel);
        emitCallAndReturn(mv, invokeExactDesc, parameterInSlots + parameters.length, methodType.returnType(), parameterTypes);

        mv.visitMaxs(-1, -1);
        mv.visitEnd();
        writer.visitEnd();

        return writer.toByteArray();
    }

    public static MethodHandle lookupSwitch(int[] hashCodes, MethodHandle[] methodHandles) {
        if (hashCodes.length == 0) {
            throw new IllegalArgumentException("hashCodes is empty");
        }
        if (methodHandles.length != hashCodes.length + 1) {
            throw new IllegalArgumentException("wrong number of method handles, should be equals to hashCodes + 1");
        }
        var methodType = methodHandles[0].type();
        if (methodType.parameterCount() == 0 || methodType.parameterType(0) != int.class) {
            throw new IllegalArgumentException("the first parameter should be an int " + methodType);
        }
        if (Arrays.stream(hashCodes).distinct().count() != hashCodes.length) {
            throw new IllegalArgumentException("hashCodes are not unique");
        }
        for(var i = 1; i < methodHandles.length; i++) {
            if (!methodHandles[i].type().equals(methodType)) {
                throw new IllegalArgumentException("invalid method handle type at index " + i);
            }
        }

        var type = methodType.appendParameterTypes(range(0, 1 + hashCodes.length).mapToObj(__ -> MethodHandle.class).toArray(Class[]::new));
        var bytecode = generate(hashCodes, methodType, type);

        //CheckClassAdapter.verify(new ClassReader(bytecode), true, new PrintWriter(System.err));

        MethodHandle target;
        try {
            var hiddenClassLookup = lookup().defineHiddenClass(bytecode, true);
            target = hiddenClassLookup.findStatic(hiddenClassLookup.lookupClass(), "switch", type);
        } catch (IllegalAccessException | NoSuchMethodException e) {
            throw new AssertionError(e);
        }
        return MethodHandles.insertArguments(target, methodType.parameterCount(), (Object[])methodHandles);
    }
}
