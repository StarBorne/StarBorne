package net.starborne.server.core;

import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraftforge.fml.relauncher.FMLRelaunchLog;
import net.starborne.server.core.transformer.Transformer;
import net.starborne.server.transformer.EntityListTransformer;
import net.starborne.server.transformer.EntityTransformer;
import net.starborne.server.transformer.ParticleManagerTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class StarborneTransformer implements IClassTransformer {
    public static final String TRANSFORMER_PACKAGE = "net.starborne.server.transformer.";

    private List<Transformer> transformers = new ArrayList<>();
    private boolean initialized;

    @Override
    public byte[] transform(String name, String transformedName, byte[] bytes) {
        if (bytes != null) {
            if (name.startsWith(TRANSFORMER_PACKAGE) && name.endsWith("Transformer")) {
                ClassReader classReader = new ClassReader(bytes);
                ClassNode classNode = new ClassNode();
                classReader.accept(classNode, 0);
                for (MethodNode methodNode : classNode.methods) {
                    for (AbstractInsnNode node : methodNode.instructions.toArray()) {
                        if (node.getOpcode() == Opcodes.LDC) {
                            LdcInsnNode ldc = (LdcInsnNode) node;
                            if (ldc.cst instanceof Type) {
                                ldc.cst = ((Type) ldc.cst).getClassName();
                            }
                        }
                    }
                }
                ClassWriter classWriter = new ClassWriter(classReader, ClassWriter.COMPUTE_FRAMES);
                classNode.accept(classWriter);
                bytes = classWriter.toByteArray();
            } else {
                if (!this.initialized) {
                    this.init();
                }
                List<Transformer> apply = new ArrayList<>();
                for (Transformer transformer : this.transformers) {
                    if (transformer.applies(transformedName)) {
                        apply.add(transformer);
                    }
                }
                if (apply.size() > 0) {
                    FMLRelaunchLog.info(">> Transforming Class: " + transformedName);
                    ClassReader classReader = new ClassReader(bytes);
                    ClassNode classNode = new ClassNode();
                    classReader.accept(classNode, 0);
                    boolean transformed = false;
                    for (Transformer transformer : apply) {
                        transformed |= transformer.transform(classNode, transformedName);
                    }
                    if (transformed) {
                        ClassWriter classWriter = new ClassWriter(classReader, ClassWriter.COMPUTE_MAXS);
                        classNode.accept(classWriter);
                        this.saveBytecode(transformedName, classWriter);
                        bytes = classWriter.toByteArray();
                    }
                }
            }
        }
        return bytes;
    }

    private void init() {
        this.initialized = true;
        this.transformers.add(new ParticleManagerTransformer());
        this.transformers.add(new EntityTransformer());
        this.transformers.add(new EntityListTransformer());
    }

    private void saveBytecode(String name, ClassWriter classWriter) {
        try {
            File debugDir = new File("debug/starborne/");
            if (debugDir.exists()) {
                debugDir.delete();
            }
            debugDir.mkdirs();
            FileOutputStream out = new FileOutputStream(new File(debugDir, name + ".class"));
            out.write(classWriter.toByteArray());
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
