package com.vikestep.anticacti;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import static org.objectweb.asm.Opcodes.*;

import java.util.Arrays;

public class AntiCactiClassTransformer implements IClassTransformer
{
    private static final String[] classesBeingTransformed =
            {
                    "net.minecraft.block.BlockCactus"
            };

    private static final boolean isBadMethod = false;

    @Override
    public byte[] transform(String name, String transformedName, byte[] classBeingTransformed)
    {
        boolean isObfuscated = !name.equals(transformedName);
        int index = Arrays.asList(classesBeingTransformed).indexOf(transformedName);
        return index != -1 ? transform(index, classBeingTransformed, isObfuscated) : classBeingTransformed;
    }

    private static byte[] transform(int index, byte[] classBeingTransformed, boolean isObfuscated)
    {
        System.out.println("Transforming: " + classesBeingTransformed[index]);
        try
        {
            ClassNode classNode = new ClassNode();
            ClassReader classReader = new ClassReader(classBeingTransformed);
            classReader.accept(classNode, 0);

            switch(index)
            {
                case 0:
                    transformBlockCactus(classNode, isObfuscated);
                    break;
            }

            ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
            classNode.accept(classWriter);
            return classWriter.toByteArray();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return classBeingTransformed;
    }

    private static void transformBlockCactus(ClassNode blockCactusClass, boolean isObfuscated)
    {
        final String ENTITY_COLLIDE = isObfuscated ? "a" : "onEntityCollidedWithBlock";
        final String ENTITY_COLLIDE_DESC = isObfuscated ? "(Lahb;IIILsa;)V" : "(Lnet/minecraft/world/World;IIILnet/minecraft/entity/Entity;)V";

        for (MethodNode method : blockCactusClass.methods)
        {
            if (method.name.equals(ENTITY_COLLIDE) && method.desc.equals(ENTITY_COLLIDE_DESC))
            {
                AbstractInsnNode targetNode = null;
                for (AbstractInsnNode instruction : method.instructions.toArray())
                {
                    if (instruction.getOpcode() == ALOAD)
                    {
                        if (((VarInsnNode) instruction).var == 5 && instruction.getNext().getOpcode() == GETSTATIC)
                        {
                            targetNode = instruction;
                            break;
                        }
                    }
                }
                if (targetNode != null && isBadMethod)
                {
                    /*
                    REMOVING:
                    mv.visitVarInsn(ALOAD, 5);
                    mv.visitFieldInsn(GETSTATIC, "net/minecraft/util/DamageSource", "cactus", "Lnet/minecraft/util/DamageSource;");
                    mv.visitInsn(FCONST_1);
                    mv.visitMethodInsn(INVOKEVIRTUAL, "net/minecraft/entity/Entity", "attackEntityFrom", "(Lnet/minecraft/util/DamageSource;F)Z", false);
                    mv.visitInsn(POP);
                     */

                    for (int i = 0; i < 5; i++)
                    {
                        targetNode = targetNode.getNext();
                        method.instructions.remove(targetNode.getPrevious());
                    }

                    //Wrapping this in a conditional
                }
                else if (targetNode != null)
                {
                    /*
                    Replacing:
                        p_149670_5_.attackEntityFrom(DamageSource.cactus, 1.0F);
                    With:
                        if (Hooks.cactusDoesDamage(p_149670_5_))
                        {
                            p_149670_5_.attackEntityFrom(DamageSource.cactus, 1.0F);
                        }

                    BYTECODE

                    Replacing:
                        mv.visitVarInsn(ALOAD, 5);
                        mv.visitFieldInsn(GETSTATIC, "net/minecraft/util/DamageSource", "cactus", "Lnet/minecraft/util/DamageSource;");
                        mv.visitInsn(FCONST_1);
                        mv.visitMethodInsn(INVOKEVIRTUAL, "net/minecraft/entity/Entity", "attackEntityFrom", "(Lnet/minecraft/util/DamageSource;F)Z", false);
                        mv.visitInsn(POP);
                    With:
                        ALOAD 5
                        INVOKEVIRTUAL Hooks.cactusDoesDamage(net/minecraft/entity/Entity)
                        IFEQ newLabelNode
                        mv.visitVarInsn(ALOAD, 5);
                        mv.visitFieldInsn(GETSTATIC, "net/minecraft/util/DamageSource", "cactus", "Lnet/minecraft/util/DamageSource;");
                        mv.visitInsn(FCONST_1);
                        mv.visitMethodInsn(INVOKEVIRTUAL, "net/minecraft/entity/Entity", "attackEntityFrom", "(Lnet/minecraft/util/DamageSource;F)Z", false);
                        mv.visitInsn(POP);
                        LABEL newLabelNode
                     */

                    AbstractInsnNode popNode = targetNode;
                    for (int i = 0; i < 4; i++)
                    {
                        popNode = popNode.getNext();
                    }

                    LabelNode newLabelNode = new LabelNode();

                    InsnList toInsert = new InsnList();
                    toInsert.add(new VarInsnNode(ALOAD, 5));
                    toInsert.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(Hooks.class), "cactusDoesDamage", isObfuscated ? "(Lsa;)Z" : "(Lnet/minecraft/entity/Entity;)Z", false));
                    toInsert.add(new JumpInsnNode(IFEQ, newLabelNode));

                    method.instructions.insertBefore(targetNode, toInsert);
                    method.instructions.insert(popNode, newLabelNode);
                }
                else
                {
                    System.out.println("Something went wrong transforming BlockCactus!");
                }
            }
        }
    }
}
