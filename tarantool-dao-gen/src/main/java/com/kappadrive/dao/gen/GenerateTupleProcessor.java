package com.kappadrive.dao.gen;

import com.kappadrive.dao.api.Tuple;
import com.kappadrive.dao.gen.util.AnnotationUtil;
import com.kappadrive.dao.gen.util.GenerateUtil;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import javax.annotation.Nonnull;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.util.ElementKindVisitor9;
import javax.tools.Diagnostic;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.kappadrive.dao.gen.util.GenerateUtil.TUPLE;
import static com.kappadrive.dao.gen.util.NameUtil.removeGetIfPresent;

@SupportedAnnotationTypes("com.kappadrive.dao.api.Tuple")
@SupportedSourceVersion(SourceVersion.RELEASE_11)
public class GenerateTupleProcessor extends AbstractProcessor {

    private GenerateUtil generateUtil;

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        generateUtil = new GenerateUtil(processingEnv);
        roundEnv.getElementsAnnotatedWith(Tuple.class).forEach(this::processTuple);
        return true;
    }

    private void processTuple(@Nonnull final Element rootElement) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "Starting Tuple generation for " + rootElement);

        TupleImplData tupleImplData = rootElement.accept(createTupleElementVisitor(), null);

        FieldSpec tupleField = FieldSpec.builder(GenerateUtil.OBJECT_LIST_TYPE, TUPLE, Modifier.PRIVATE, Modifier.FINAL).build();

        MethodSpec listConstructor = MethodSpec.constructorBuilder()
                .addParameter(ParameterSpec.builder(GenerateUtil.OBJECT_LIST_TYPE, TUPLE, Modifier.FINAL)
                        .addAnnotation(Nonnull.class)
                        .build())
                .addStatement("this.$N=$N", TUPLE, TUPLE)
                .build();

        List<MethodSpec> builderMethods = generateUtil.createBuilderInterfaceMethods(tupleImplData);

        TypeSpec builderInterface = TypeSpec.interfaceBuilder(tupleImplData.getBuilderInterfaceName())
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(GenerateUtil.createGeneratedAnnotation())
                .addMethods(builderMethods)
                .addMethod(MethodSpec.methodBuilder("build")
                        .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                        .returns(TypeName.get(tupleImplData.getInterfaceType()))
                        .addAnnotation(Nonnull.class)
                        .build())
                .build();

        JavaFile builderFile = JavaFile.builder(tupleImplData.getPackageName(), builderInterface)
                .build();

        generateUtil.writeSafe(builderFile);

        MethodSpec emptyConstructor = MethodSpec.constructorBuilder()
                .addStatement("this.$N = $T.asList(new $T[$L])", TUPLE, Arrays.class, Object.class, tupleImplData.getMethods().size())
                .build();

        MethodSpec.Builder buildImpl = MethodSpec.methodBuilder("build")
                .addModifiers(Modifier.PUBLIC)
                .returns(TypeName.get(tupleImplData.getInterfaceType()))
                .addAnnotation(Nonnull.class)
                .addAnnotation(Override.class);

        tupleImplData.getMethods().stream()
                .filter(Predicate.not(TupleImplData.FieldData::isOptional))
                .forEach(f -> buildImpl.addStatement("$T.requireNonNull(this.$L.get($L), $S)", Objects.class, TUPLE, f.getOrder(), f.getName()));
        buildImpl.addStatement("return new $L(this.$L)", tupleImplData.getImplName(), TUPLE);

        TypeSpec builder = TypeSpec.classBuilder("Builder")
                .addModifiers(Modifier.STATIC)
                .addSuperinterface(ClassName.get(tupleImplData.getPackageName(), tupleImplData.getBuilderInterfaceName()))
                .addField(tupleField)
                .addMethod(emptyConstructor)
                .addMethod(listConstructor)
                .addMethods(generateUtil.createBuilderImplMethods(tupleImplData))
                .addMethod(buildImpl.build())
                .build();

        List<MethodSpec> methods = generateUtil.createOrderedMethods(tupleImplData);

        TypeSpec.Builder impl = TypeSpec.classBuilder(tupleImplData.getImplName())
                .addSuperinterface(tupleImplData.getInterfaceElement().asType())
                .addAnnotation(GenerateUtil.createGeneratedAnnotation())
                .addField(tupleField)
                .addMethod(listConstructor)
                .addMethods(methods);

        if (tupleImplData.getToBuilder() != null) {
            impl.addMethod(generateUtil.methodBuilder(tupleImplData.getToBuilder(), tupleImplData.getInterfaceType())
                    .addStatement("return new $L($L)", "Builder", TUPLE)
                    .build());
        }

        impl.addType(builder);

        JavaFile javaFile = JavaFile.builder(tupleImplData.getPackageName(), impl.build())
                .build();

        generateUtil.writeSafe(javaFile);
    }

    @Nonnull
    private ElementKindVisitor9<TupleImplData, Object> createTupleElementVisitor() {
        // do not optimize generic - jdk11 has unfixed bug
        return new ElementKindVisitor9<TupleImplData, Object>() {
            @Override
            public TupleImplData visitType(TypeElement e, Object o) {
                PackageElement packageElement = (PackageElement) e.getEnclosingElement();
                DeclaredType interfaceType = (DeclaredType) e.asType();
                Collection<ExecutableElement> methods = generateUtil.getAllAbstractMethods(e);
                List<TupleImplData.FieldData> fieldMethods = methods.stream()
                        .filter(method -> getOrderValue(method).isPresent())
                        .map(method -> TupleImplData.FieldData.builder()
                                .method(method)
                                .name(removeGetIfPresent(method.getSimpleName().toString()))
                                .order(getOrderValue(method).orElseThrow() - 1)
                                .type(generateUtil.getFinalType(method.getReturnType()))
                                .isOptional(generateUtil.isAssignableGeneric(method.getReturnType(), Optional.class))
                                .build())
                        .collect(Collectors.toList());
                Optional<ExecutableElement> toBuilder = methods.stream()
                        .filter(m -> m.getSimpleName().contentEquals("toBuilder") && m.getParameters().isEmpty())
                        .findAny();
                return TupleImplData.builder()
                        .packageName(packageElement.getQualifiedName().toString())
                        .interfaceElement(e)
                        .interfaceType(interfaceType)
                        .implName(e.getSimpleName() + "Impl")
                        .methods(fieldMethods)
                        .builderInterfaceName(e.getSimpleName() + "Builder")
                        .toBuilder(toBuilder.orElse(null))
                        .build();
            }
        };
    }

    @Nonnull
    private static Optional<Integer> getOrderValue(@Nonnull final ExecutableElement method) {
        return AnnotationUtil.getAnnotationValue(method, Tuple.Order.class, "value", Integer.class);
    }
}
