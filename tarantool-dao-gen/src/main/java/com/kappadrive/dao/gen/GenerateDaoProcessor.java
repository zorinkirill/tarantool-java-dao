package com.kappadrive.dao.gen;

import com.google.auto.service.AutoService;
import com.kappadrive.dao.api.Space;
import com.kappadrive.dao.api.TarantoolDao;
import com.kappadrive.dao.gen.util.AnnotationUtil;
import com.kappadrive.dao.gen.util.GenerateUtil;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import org.tarantool.TarantoolClient;

import javax.annotation.Nonnull;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.util.ElementKindVisitor9;
import javax.tools.Diagnostic;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.kappadrive.dao.gen.util.GenerateUtil.CLIENT;
import static com.kappadrive.dao.gen.util.GenerateUtil.SPACE;
import static com.kappadrive.dao.gen.util.GenerateUtil.verifyFields;

@SupportedAnnotationTypes("com.kappadrive.dao.api.TarantoolDao")
@SupportedSourceVersion(SourceVersion.RELEASE_11)
@AutoService(Processor.class)
public class GenerateDaoProcessor extends AbstractProcessor {

    private GenerateUtil generateUtil;

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        generateUtil = new GenerateUtil(processingEnv);
        roundEnv.getElementsAnnotatedWith(TarantoolDao.class).forEach(this::processDao);
        return true;
    }

    private void processDao(@Nonnull final Element rootElement) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "Starting DAO generation for " + rootElement);

        DaoImplData daoImplData = rootElement.accept(createDaoElementVisitor(), null);

        List<MethodSpec> daoMethods = daoImplData.getDaoMethods()
                .stream()
                .map(e -> createDaoMethod(e, daoImplData))
                .collect(Collectors.toList());

        TypeSpec.Builder impl = TypeSpec.classBuilder(daoImplData.getImplName())
                .addModifiers(Modifier.PUBLIC)
                .addSuperinterface(daoImplData.getInterfaceElement().asType())
                .addAnnotation(GenerateUtil.createGeneratedAnnotation())
                .addAnnotations(daoImplData.getAnnotations().stream()
                        .map(a -> AnnotationSpec.builder(ClassName.get(a)).build())
                        .collect(Collectors.toList()))
                .addField(FieldSpec.builder(String.class, SPACE, Modifier.PRIVATE, Modifier.FINAL)
                        .initializer("$S", daoImplData.getSpace())
                        .build())
                .addField(TarantoolClient.class, CLIENT, Modifier.PRIVATE, Modifier.FINAL)
                .addMethod(MethodSpec.constructorBuilder()
                        .addParameter(TarantoolClient.class, CLIENT)
                        .addStatement("this.$L = $L", CLIENT, CLIENT)
                        .build())
                .addMethods(daoMethods)
                .addMethod(daoImplData.getToTuple())
                .addMethod(daoImplData.getToKey())
                .addMethod(daoImplData.getToEntity());

        JavaFile javaFile = JavaFile.builder(daoImplData.getPackageName(), impl.build())
                .build();

        generateUtil.writeSafe(javaFile);
    }

    @Nonnull
    private MethodSpec createDaoMethod(@Nonnull final DaoMethodData method, @Nonnull final DaoImplData daoImplData) {
        for (DaoMethodType type : DaoMethodType.values()) {
            if (type.supportsByAnnotation(method.getMethod())) {
                return type.createMethodSpec(method, daoImplData, generateUtil);
            }
        }
        for (DaoMethodType type : DaoMethodType.values()) {
            if (type.supportsByName(method.getMethod())) {
                return type.createMethodSpec(method, daoImplData, generateUtil);
            }
        }
        throw new IllegalArgumentException("Unsupported method: " + method.getMethod());
    }

    @Nonnull
    private ElementKindVisitor9<DaoImplData, Object> createDaoElementVisitor() {
        // do not optimize generic - jdk11 has unfixed bug
        return new ElementKindVisitor9<DaoImplData, Object>() {
            @Override
            public DaoImplData visitType(TypeElement e, Object o) {
                PackageElement packageElement = (PackageElement) e.getEnclosingElement();
                DeclaredType interfaceType = (DeclaredType) e.asType();
                DeclaredType entityType = AnnotationUtil.getAnnotationValue(e, TarantoolDao.class, DeclaredType.class)
                        // should never happen
                        .orElseThrow(IllegalStateException::new);
                String space = AnnotationUtil.getAnnotationValue(e, Space.class, String.class)
                        .or(() -> AnnotationUtil.getAnnotationValue(entityType.asElement(), Space.class, String.class))
                        .orElseThrow(() -> new IllegalStateException("Either entity or DAO type should be annotated with @" + Space.class.getSimpleName()));
                List<TypeElement> annotations = generateUtil.lookupStyleValue(e,
                        (element, style) -> AnnotationUtil.getAnnotationArrayValue(element, style, "addAnnotations", AnnotationUtil.typeVisitor()))
                        .orElse(Collections.emptyList());
                List<FieldData> allFields = generateUtil.getAllFields(entityType);
                verifyFields(allFields, entityType);
                EntityData entityData = EntityData.builder()
                        .type(entityType)
                        .fields(allFields)
                        .build();
                return DaoImplData.builder()
                        .packageName(packageElement.getQualifiedName().toString())
                        .interfaceElement(e)
                        .interfaceType(interfaceType)
                        .implName(e.getSimpleName() + "Impl")
                        .daoMethods(generateUtil.getAllDaoMethods(e))
                        .annotations(annotations)
                        .entity(entityData)
                        .space(space)
                        .toTuple(generateUtil.createDaoToTupleMethod("toTuple", entityData, f -> true))
                        .toKey(generateUtil.createDaoToTupleMethod("toKey", entityData, FieldData::isKey))
                        .toEntity(generateUtil.createDaoToEntityMethod(entityData))
                        .build();
            }
        };
    }
}
